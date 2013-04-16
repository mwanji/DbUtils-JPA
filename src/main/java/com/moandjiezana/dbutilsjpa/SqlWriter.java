package com.moandjiezana.dbutilsjpa;

import co.mewf.sqlwriter.Queries;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

public class SqlWriter {

  public String selectById(Class<?> entityClass) {
    AccessibleObject idAccessor = Entities.getIdAccessor(entityClass);

    return Queries.select().from(entityClass).where().eq(Entities.getName(idAccessor)).toString();
  }

  public String select(Class<?> entityClass) {
    return Queries.select().from(entityClass).toString();
  }

  public String insert(Class<?> entityClass) {
    String[] columnNames = getColumnNames(entityClass, Entities.getIdAccessor(entityClass), NOT_INSERTABLE);

    return Queries.insert(entityClass).columns(columnNames).toString();
  }

  public String deleteById(Class<?> entityClass) {
    return Queries.delete(entityClass).where().eq(Entities.getName(Entities.getIdAccessor(entityClass))).toString();
  }

  /**
   * @param columns Optional. If omitted, all columns are updated, except the ones marked with @Column(updatable=false)
   */
  public String updateById(Class<?> entityClass, String... columns) {
    AccessibleObject idAccessor = Entities.getIdAccessor(entityClass);
    String[] columnNames = columns.length == 0 ? getColumnNames(entityClass, idAccessor, NOT_UPDATABLE) : columns;

    return Queries.update(entityClass).set(columnNames).where().eq(Entities.getName(idAccessor)).toString();
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
