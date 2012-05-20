package com.moandjiezana.dbutilsjpa;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

public class SqlWriter {

  public String selectById(Class<?> entityClass, Object primaryKey) {
    AccessibleObject idAccessor = Entities.getIdAccessor(entityClass);
    if (!idAccessor.isAccessible()) {
      idAccessor.setAccessible(true);
    }
    String idColumnName = Entities.getName(idAccessor);
    
    return select(entityClass) + where(idColumnName);
  }

  public String select(Class<?> entityClass) {
    return "SELECT * FROM " + quote(Entities.getName(entityClass));
  }

  public String insert(Class<?> entityClass) {
    List<String> columnNames = getColumnNames(entityClass, Entities.getIdAccessor(entityClass), NOT_INSERTABLE);
    StringBuilder sb = new StringBuilder("INSERT INTO ").append(quote(Entities.getName(entityClass))).append("(");
    
    for (int i = 0; i < columnNames.size(); i++) {
      sb.append(quote(columnNames.get(i)));
      if (i + 1 < columnNames.size()) {
        sb.append(",");
      }
    }
    sb.append(") VALUES(");
    
    for (int i = 0; i < columnNames.size(); i++) {
      sb.append("?");
      if (i + 1 < columnNames.size()) {
        sb.append(",");
      }
    }
    sb.append(")");
    return sb.toString();
  }

  public String deleteById(Class<?> entityClass) {
    return "DELETE FROM " + quote(Entities.getName(entityClass)) + " WHERE " + quote(Entities.getName(Entities.getIdAccessor(entityClass))) + "=?";
  }

  public String updateById(Class<?> entityClass, String... columns) {
    StringBuilder sb = new StringBuilder("UPDATE ").append(quote(Entities.getName(entityClass))).append(" SET");
    
    AccessibleObject idAccessor = Entities.getIdAccessor(entityClass);
    List<String> columnNames = columns.length == 0 ? getColumnNames(entityClass, idAccessor, NOT_UPDATABLE) : Arrays.asList(columns);
    
    for (int i = 0; i < columnNames.size(); i++) {
      sb.append(" ").append(quote(columnNames.get(i))).append("=?");
      if (i + 1 < columnNames.size()) {
        sb.append(",");
      }
    }
    
    sb.append(where(Entities.getName(idAccessor)));
    
    return sb.toString();
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

  private List<String> getColumnNames(Class<?> entityClass, AccessibleObject idAccessor, ColumnIgnorer columnIgnorer) {
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
    return columnNames;
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
