package com.moandjiezana.dbutilsjpa;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;

/**
 * Provides a JPA-friendly interface to the underlying QueryRunner.
 *
 * Immutable and thread-safe.
*/
public class JpaQueryRunner {

  private static final SqlWriter DEFAULT_SQL_WRITER = new SqlWriter();
  private static final BasicRowProcessor DEFAULT_ROW_PROCESSOR = new BasicRowProcessor(new JpaBeanProcessor());
  private static final NewEntityTester DEFAULT_ENTITY_TESTER = new NewEntityTester() {
    @Override
    public boolean isNew(Object entity) {
      AccessibleObject idAccessor = Entities.getIdAccessor(entity.getClass());
      try {
        if (idAccessor instanceof Field) {
          Field field = (Field) idAccessor;
          field.setAccessible(true);

          return field.get(entity) == null;
        } else {
          return ((Method) idAccessor).invoke(entity) == null;
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e.getCause());
      }
    }
  };

  private final QueryRunner queryRunner;
  private final SqlWriter sqlWriter;
  private final NewEntityTester entityTester;
  private final RowProcessor rowProcessor;

  public JpaQueryRunner(QueryRunner queryRunner) {
    this(queryRunner, DEFAULT_SQL_WRITER, DEFAULT_ENTITY_TESTER, DEFAULT_ROW_PROCESSOR);
  }

  public JpaQueryRunner(QueryRunner queryRunner, SqlWriter sqlWriter, NewEntityTester entityTester,
      RowProcessor rowProcessor) {
    this.queryRunner = queryRunner;
    this.sqlWriter = sqlWriter;
    this.entityTester = entityTester;
    this.rowProcessor = rowProcessor;
  }

  /**
   * Find by primary key. Search for an entity of the specified class and
   * primary key. If the entity instance is contained in the persistence
   * context, it is returned from there.
   * 
   * @param entityClass
   *          entity class
   * @param primaryKey
   *          primary key
   * @return the found entity instance or null if the entity does not exist
   * @throws IllegalArgumentException
   *           if the first argument does not denote an entity type or the
   *           second argument is is not a valid type for that entity&apos;s
   *           primary key or is null
   */
  public <T> T query(Class<T> entityClass, Object primaryKey) {
    try {
      return entityClass.cast(queryRunner.query(sqlWriter.selectById(entityClass, primaryKey), new BeanHandler<T>(
          entityClass, rowProcessor), primaryKey));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Insert if new, update if already exists.
   * 
   * @param entity
   * @return The number of rows updated
   */
  public int save(Object entity) {
    try {
      Class<? extends Object> entityClass = entity.getClass();
      boolean isNew = entityTester.isNew(entity);

      AccessibleObject idAccessor = Entities.getIdAccessor(entityClass);
      List<PropertyDescriptor> pD = new ArrayList<PropertyDescriptor>();
      if (idAccessor instanceof Method) {
        try {
          PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(entityClass).getPropertyDescriptors();
          for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            Method readMethod = propertyDescriptor.getReadMethod();
            if (readMethod.getDeclaringClass() != entityClass || Entities.isTransient(readMethod)
                || Entities.isRelation(readMethod) || Entities.isIdAccessor(readMethod)) {
              continue;
            }
            if (isNew && readMethod.isAnnotationPresent(Column.class)
                && !readMethod.getAnnotation(Column.class).insertable()) {
              // continue;
            } else if (readMethod.isAnnotationPresent(Column.class)
                && !readMethod.getAnnotation(Column.class).updatable()) {
              continue;
            }

            pD.add(propertyDescriptor);
          }
          if (!isNew) {
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
              if (Entities.isIdAccessor(propertyDescriptor.getReadMethod())) {
                pD.add(propertyDescriptor);
              }
            }
          }
        } catch (IntrospectionException e) {
          throw new RuntimeException(e);
        }
      } else {
        for (Field field : entityClass.getDeclaredFields()) {
          if (field.getDeclaringClass() != entityClass || Entities.isTransient(field) || Entities.isRelation(field)
              || Entities.isIdAccessor(field)) {
            continue;
          }
          if (isNew && field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).insertable()) {
            // continue;
          } else if (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).updatable()) {
            continue;
          }
          pD.add(new FieldBasedPropertyDescriptor(Entities.getName(field), field));
        }
        if (!isNew) {
          for (Field field : entityClass.getDeclaredFields()) {
            if (Entities.isIdAccessor(field)) {
              pD.add(new FieldBasedPropertyDescriptor(Entities.getName(field), field));
            }
          }
        }
      }

      Object[] args = new Object[pD.size()];
      for (int i = 0; i < args.length; i++) {
        PropertyDescriptor propertyDescriptor = pD.get(i);
        if (propertyDescriptor instanceof FieldBasedPropertyDescriptor) {
          args[i] = ((FieldBasedPropertyDescriptor) propertyDescriptor).field.get(entity);
        } else {
          args[i] = propertyDescriptor.getReadMethod().invoke(entity);
        }
      }

      if (isNew) {
        return queryRunner.update(sqlWriter.insert(entityClass), args);
      } else {
        return queryRunner.update(sqlWriter.updateById(entityClass), args);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    } catch (IntrospectionException e) {
      throw new RuntimeException(e);
    }
  }

  public int delete(Class<?> entityClass, Object primaryKey) {
    try {
      return queryRunner.update(sqlWriter.deleteById(entityClass), primaryKey);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
