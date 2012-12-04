package com.moandjiezana.dbutilsjpa;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

import org.sql.generation.api.grammar.builders.modification.DeleteBySearchBuilder;
import org.sql.generation.api.grammar.builders.modification.UpdateBySearchBuilder;
import org.sql.generation.api.grammar.builders.query.SimpleQueryBuilder;
import org.sql.generation.api.grammar.factories.BooleanFactory;
import org.sql.generation.api.grammar.factories.ColumnsFactory;
import org.sql.generation.api.grammar.factories.LiteralFactory;
import org.sql.generation.api.grammar.factories.ModificationFactory;
import org.sql.generation.api.grammar.factories.TableReferenceFactory;
import org.sql.generation.api.grammar.literals.DirectLiteral;
import org.sql.generation.api.grammar.modification.SetClause;
import org.sql.generation.api.vendor.MySQLVendor;
import org.sql.generation.api.vendor.SQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;

public class SqlWriter {
  
  private final SQLVendor sqlVendor;
  private final ColumnsFactory columns;
  private final LiteralFactory literals;
  private final TableReferenceFactory tables;
  private final BooleanFactory bool;
  private ModificationFactory dml;
  
  public SqlWriter() {
    this(MySQLVendor.class);
  }
  
  public SqlWriter(Class<? extends SQLVendor> vendorClass) {
    try {
      sqlVendor = SQLVendorProvider.createVendor(vendorClass);
      columns = sqlVendor.getColumnsFactory();
      literals = sqlVendor.getLiteralFactory();
      tables = sqlVendor.getTableReferenceFactory();
      bool = sqlVendor.getBooleanFactory();
      dml = sqlVendor.getModificationFactory();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String selectById(Class<?> entityClass) {
    AccessibleObject idAccessor = Entities.getIdAccessor(entityClass);
    if (!idAccessor.isAccessible()) {
      idAccessor.setAccessible(true);
    }
    String idColumnName = Entities.getName(idAccessor);
    
    SimpleQueryBuilder sql = sqlVendor.getQueryFactory().simpleQueryBuilder();
    return sql.selectAllColumns()
        .from(tables.tableName(Entities.getName(entityClass)))
        .where(bool.eq(columns.colName(idColumnName), literals.param()))
        .createExpression().toString();
  }

  public String select(Class<?> entityClass) {
    return sqlVendor.getQueryFactory().simpleQueryBuilder().selectAllColumns().from(tables.tableName(Entities.getName(entityClass))).createExpression().toString();
  }

  public String insert(Class<?> entityClass) {
    String[] columnNames = getColumnNames(entityClass, Entities.getIdAccessor(entityClass), NOT_INSERTABLE);
    DirectLiteral[] values = new DirectLiteral[columnNames.length];
    for (int i = 0; i < columnNames.length; i++) {
      values[i] = literals.param();
    }
    
    return dml.insert().setTableName(tables.tableName(Entities.getName(entityClass)))
      .setColumnSource(dml.columnSourceByValues()
        .addColumnNames(columnNames)
        .addValues(values).createExpression())
      .createExpression().toString();
  }

  public String deleteById(Class<?> entityClass) {
    DeleteBySearchBuilder builder = dml.deleteBySearch().setTargetTable(dml.createTargetTable(tables.tableName(Entities.getName(entityClass))));
    builder.getWhere().and(bool.eq(columns.colName(Entities.getName(Entities.getIdAccessor(entityClass))), literals.param()));
    
    return builder.createExpression().toString();
  }

  /**
   * @param columns Optional. If omitted, all columns are updated, except the ones marked with @Column(updatable=false)
   */
  public String updateById(Class<?> entityClass, String... columns) {
    AccessibleObject idAccessor = Entities.getIdAccessor(entityClass);
    String[] columnNames = columns.length == 0 ? getColumnNames(entityClass, idAccessor, NOT_UPDATABLE) : columns;
    SetClause[] setClauses = new SetClause[columnNames.length];
    for (int i = 0; i < columnNames.length; i++) {
      setClauses[i] = dml.setClause(columnNames[i], dml.updateSourceByExp(literals.param()));
    }
    
    UpdateBySearchBuilder builder = dml.updateBySearch()
      .setTargetTable(dml.createTargetTable(tables.tableName(Entities.getName(entityClass))))
      .addSetClauses(setClauses);
    builder.getWhereBuilder().and(bool.eq(this.columns.colName(Entities.getName(idAccessor)), literals.param()));
    
    return builder.createExpression().toString();
  }

  public String where(String column, String... columns) {
    StringBuilder builder = new StringBuilder(" WHERE ").append(quote(column)).append("=?");
    
    for (int i = 0; i < columns.length; i++) {
      builder.append(", ").append(quote(columns[i])).append("=?");
    }
    
    return builder.toString();
  }
  
  public String asc(String column, String... columns) {
    return orderBy(column, columns).append(" ASC").toString();
  }

  public String desc(String column, String... columns) {
    return orderBy(column, columns).append(" DESC").toString();
  }

  private StringBuilder orderBy(String column, String... columns) {
    StringBuilder builder = new StringBuilder(" ORDER BY ").append(column);
    for (String columnName : columns) {
      builder.append(", ").append(columnName);
    }
    
    return builder;
  }
  
  protected String quote(String identifier) {
    return identifier;
  }

  private String[] getColumnNames(Class<?> entityClass, AccessibleObject idAccessor, ColumnIgnorer columnIgnorer) {
    List<String> columnNames = new ArrayList<String>();
    try {
      if (idAccessor instanceof Field) {
        for (Field field : entityClass.getDeclaredFields()) {
          if (columnIgnorer.ignore(field) || isIgnorable(field) || isMultiValued(field.getType())) {
            continue;
          }
          columnNames.add(Entities.getName(field));
        }
      } else {
        for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(entityClass).getPropertyDescriptors()) {
          Method readMethod = propertyDescriptor.getReadMethod();
          if (columnIgnorer.ignore(readMethod) || isIgnorable(readMethod) || isMultiValued(propertyDescriptor.getPropertyType())) {
            continue;
          }
          columnNames.add(Entities.getName(readMethod));
        }
      }
    } catch (IntrospectionException e) {
      throw new RuntimeException(e);
    }
    return columnNames.toArray(new String[0]);
  }
  
  private boolean isIgnorable(AccessibleObject accessibleObject) {
    return Entities.isStatic(((Member) accessibleObject)) || !Entities.isMapped(((Member) accessibleObject).getDeclaringClass()) || Entities.isTransient(accessibleObject) || Entities.isIdAccessor(accessibleObject) || Entities.isRelation(accessibleObject);
  }
  
  private boolean isMultiValued(Class<?> type) {
    return Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
  }
  
  private static interface ColumnIgnorer {
    boolean ignore(AccessibleObject object);
  }
  
  private static final ColumnIgnorer NOT_UPDATABLE = new ColumnIgnorer() {
    @Override
    public boolean ignore(AccessibleObject object) {
      return object.isAnnotationPresent(Column.class) && !object.getAnnotation(Column.class).updatable();
    }
  };
  
  private static final ColumnIgnorer NOT_INSERTABLE = new ColumnIgnorer() {
    @Override
    public boolean ignore(AccessibleObject object) {
      return object.isAnnotationPresent(Column.class) && !object.getAnnotation(Column.class).insertable();
    }
  };
}
