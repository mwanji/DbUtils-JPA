package com.moandjiezana.dbutilsjpa;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import org.apache.commons.dbutils.BeanProcessor;

import com.moandjiezana.dbutilsjpa.internal.PropertyDescriptorWrapper;

public class JpaBeanProcessor extends BeanProcessor {

  private static final PropertyDescriptorWrapper[] EMPTY_PROPERTY_DESCRIPTOR_ARRAY = new PropertyDescriptorWrapper[0];

  /**
   * Special array value used by <code>mapColumnsToProperties</code> that
   * indicates there is no bean property that matches a column from a
   * <code>ResultSet</code>.
   */
  protected static final int PROPERTY_NOT_FOUND = -1;

  /*
   * Set a bean's primitive properties to these defaults when SQL NULL is
   * returned. These are the same as the defaults that ResultSet get* methods
   * return in the event of a NULL column.
   */
  private static final Map<Class<?>, Object> primitiveDefaults = new HashMap<Class<?>, Object>();

  static {
    primitiveDefaults.put(Integer.TYPE, Integer.valueOf(0));
    primitiveDefaults.put(Short.TYPE, Short.valueOf((short) 0));
    primitiveDefaults.put(Byte.TYPE, Byte.valueOf((byte) 0));
    primitiveDefaults.put(Float.TYPE, Float.valueOf(0f));
    primitiveDefaults.put(Double.TYPE, Double.valueOf(0d));
    primitiveDefaults.put(Long.TYPE, Long.valueOf(0L));
    primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
    primitiveDefaults.put(Character.TYPE, Character.valueOf((char) 0));
  }

  @Override
  public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
    checkIsEntity(type);
    PropertyDescriptorWrapper[] props = propertyDescriptors(type);

    ResultSetMetaData rsmd = rs.getMetaData();
    int[] columnToProperty = mapColumnsToProperties(rsmd, props);

    return createBean(rs, type, props, columnToProperty);
  }

  @Override
  public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
    checkIsEntity(type);

    if (!rs.next()) {
      return Collections.emptyList();
    }
    
    List<T> results = new ArrayList<T>();

    PropertyDescriptorWrapper[] props = propertyDescriptors(type);
    ResultSetMetaData rsmd = rs.getMetaData();
    int[] columnsToProperties = mapColumnsToProperties(rsmd, props);

    do {
      results.add(this.createBean(rs, type, props, columnsToProperties));
    } while (rs.next());

    return results;
  }

  @Override
  protected int[] mapColumnsToProperties(ResultSetMetaData rsmd, PropertyDescriptor[] props) throws SQLException {

    int cols = rsmd.getColumnCount();
    int[] columnToProperty = new int[cols + 1];
    Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

    for (int col = 1; col <= cols; col++) {
      String columnName = rsmd.getColumnLabel(col);
      if (columnName == null || columnName.length() == 0) {
        columnName = rsmd.getColumnName(col);
      }
      for (int i = 0; i < props.length; i++) {
        PropertyDescriptor propertyDescriptor = props[i];
        String propertyName = propertyDescriptor.getName();

        if (propertyDescriptor.getReadMethod() != null) {
          propertyName = Entities.getName(propertyDescriptor.getReadMethod());
        }

        if (columnName.equalsIgnoreCase(propertyName)) {
          columnToProperty[col] = i;
          break;
        }
      }
    }

    return columnToProperty;
  }

  @Override
  protected <T> T newInstance(Class<T> c) throws SQLException {
    try {
      Constructor<T> constructor = c.getConstructor();
      if (Modifier.isProtected(constructor.getModifiers())) {
        constructor.setAccessible(true);
      }

      return constructor.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private <T> PropertyDescriptorWrapper[] propertyDescriptors(Class<T> type) {
    AccessibleObject idAccessor = getIdAccessor(type);

    PropertyDescriptorWrapper[] propertyDescriptors;
    if (idAccessor instanceof Method) {
      propertyDescriptors = getPropertyDescriptorsFromMethods(type);
    } else {
      propertyDescriptors = getPropertyDescriptorsFromFields(type);
    }
    return propertyDescriptors;
  }

  private <T> AccessibleObject getIdAccessor(Class<T> type) {
    return Entities.getIdAccessor(type);
  }

  private <T> void checkIsEntity(Class<T> type) {
    if (!type.isAnnotationPresent(Entity.class)) {
      throw new IllegalArgumentException(type.getName() + " is not a JPA @Entity");
    }
  }

  /**
   * Creates a new object and initializes its fields from the ResultSet.
   * 
   * @param <T>
   *          The type of bean to create
   * @param rs
   *          The result set.
   * @param type
   *          The bean type (the return type of the object).
   * @param props
   *          The property descriptors.
   * @param columnToProperty
   *          The column indices in the result set.
   * @return An initialized object.
   * @throws SQLException
   *           if a database error occurs.
   */
  private <T> T createBean(ResultSet rs, Class<T> type, PropertyDescriptorWrapper[] props, int[] columnToProperty)
      throws SQLException {

    T bean = this.newInstance(type);

    for (int i = 1; i < columnToProperty.length; i++) {
      if (columnToProperty[i] == PROPERTY_NOT_FOUND) {
        continue;
      }

      PropertyDescriptorWrapper prop = props[columnToProperty[i]];
      Class<?> propType = prop.getPropertyType();

      Object value = this.processColumn(rs, i, propType);

      if (propType != null && value == null && propType.isPrimitive()) {
        value = primitiveDefaults.get(propType);
      }

      this.callSetter(bean, prop, value);
    }

    return bean;
  }

  /**
   * Calls the setter method on the target object for the given property. If no
   * setter method exists for the property, this method does nothing.
   * 
   * @param target
   *          The object to set the property on.
   * @param prop
   *          The property to set.
   * @param value
   *          The value to pass into the setter.
   * @throws SQLException
   *           if an error occurs setting the property.
   */
  private void callSetter(Object target, PropertyDescriptorWrapper prop, Object value) throws SQLException {
    
    Class<?> parameterType = prop.getPropertyType();
    try {
      // convert types for some popular ones
      if (value != null) {
        value = convert(value, parameterType);
      }

      // Don't call setter if the value object isn't the right type
      if (this.isCompatibleType(value, parameterType)) {
        prop.set(target, value);
      } else {
        throw new SQLException("Cannot set " + prop.getName() + ": incompatible types.");
      }

    } catch (IllegalArgumentException e) {
      throw new SQLException("Cannot set " + prop.getName() + ": " + e.getMessage());
    }
  }

  private Object convert(Object value, Class<?> parameterType) {
    if (value instanceof java.util.Date) {
      if (parameterType.getName().equals("java.sql.Date")) {
        value = new java.sql.Date(((java.util.Date) value).getTime());
      } else if (parameterType.getName().equals("java.sql.Time")) {
        value = new java.sql.Time(((java.util.Date) value).getTime());
      } else if (parameterType.getName().equals("java.sql.Timestamp")) {
        value = new java.sql.Timestamp(((java.util.Date) value).getTime());
      }
    } else if (Enum.class.isAssignableFrom(parameterType)) {
      value = Enum.valueOf((Class<Enum>)parameterType, (String) value);
    }
    return value;
  }

  /**
   * ResultSet.getObject() returns an Integer object for an INT column. The
   * setter method for the property might take an Integer or a primitive int.
   * This method returns true if the value can be successfully passed into the
   * setter method. Remember, Method.invoke() handles the unwrapping of Integer
   * into an int.
   * 
   * @param value
   *          The value to be passed into the setter method.
   * @param type
   *          The setter's parameter type.
   * @return boolean True if the value is compatible.
   */
  private boolean isCompatibleType(Object value, Class<?> type) {
    // Do object check first, then primitives
    if (value == null || type.isInstance(value)) {
      return true;

    } else if (type.equals(Integer.TYPE) && Integer.class.isInstance(value)) {
      return true;

    } else if (type.equals(Long.TYPE) && Long.class.isInstance(value)) {
      return true;

    } else if (type.equals(Double.TYPE) && Double.class.isInstance(value)) {
      return true;

    } else if (type.equals(Float.TYPE) && Float.class.isInstance(value)) {
      return true;

    } else if (type.equals(Short.TYPE) && Short.class.isInstance(value)) {
      return true;

    } else if (type.equals(Byte.TYPE) && Byte.class.isInstance(value)) {
      return true;

    } else if (type.equals(Character.TYPE) && Character.class.isInstance(value)) {
      return true;

    } else if (type.equals(Boolean.TYPE) && Boolean.class.isInstance(value)) {
      return true;
    }
    return false;
  }

  private PropertyDescriptorWrapper[] getPropertyDescriptorsFromMethods(Class<?> c) {
    BeanInfo beanInfo = null;
    try {
      beanInfo = Introspector.getBeanInfo(c);
      
      List<PropertyDescriptorWrapper> propertyDescriptors = new ArrayList<PropertyDescriptorWrapper>();
      
      for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
        Method readMethod = propertyDescriptor.getReadMethod();
        if (Entities.isTransient(readMethod) || Entities.isStatic(readMethod)) {
          continue;
        }
        
        propertyDescriptors.add(new PropertyDescriptorWrapper(propertyDescriptor));
      }
      
      return propertyDescriptors.toArray(EMPTY_PROPERTY_DESCRIPTOR_ARRAY);
    } catch (IntrospectionException e) {
      throw new RuntimeException(e);
    }
  }

  private PropertyDescriptorWrapper[] getPropertyDescriptorsFromFields(Class<?> c) {
    List<PropertyDescriptorWrapper> propertyDescriptors = new ArrayList<PropertyDescriptorWrapper>();

    for (Field field : c.getDeclaredFields()) {
      if (Entities.isTransient(field) || Entities.isStatic(field)) {
        continue;
      }

      String propertyName = Entities.getName(field);

      try {
        propertyDescriptors.add(new PropertyDescriptorWrapper(propertyName, field));
      } catch (IntrospectionException e) {
        throw new RuntimeException(e);
      }
    }

    return propertyDescriptors.toArray(EMPTY_PROPERTY_DESCRIPTOR_ARRAY);
  }
}
