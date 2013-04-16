package com.moandjiezana.dbutilsjpa.internal;

import com.moandjiezana.dbutilsjpa.Entities;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.JoinColumn;

public class PropertyDescriptorWrapper extends PropertyDescriptor {

  private static final PropertyDescriptorWrapper[] EMPTY_PROPERTY_DESCRIPTOR_ARRAY = new PropertyDescriptorWrapper[0];

  public static PropertyDescriptorWrapper[] getPropertyDescriptorsFromMethods(Class<?> c) {
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

  public static PropertyDescriptorWrapper[] getPropertyDescriptorsFromFields(Class<?> c) {
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


  private final Field field;
  private final PropertyDescriptor propertyDescriptor;

  public PropertyDescriptorWrapper(String propertyName, Field field) throws IntrospectionException {
    super(propertyName, null, null);
    this.field = field;
    field.setAccessible(true);
    this.propertyDescriptor = null;
  }

  public PropertyDescriptorWrapper(PropertyDescriptor propertyDescriptor) throws IntrospectionException {
    super(propertyDescriptor.getName(), propertyDescriptor.getReadMethod(), propertyDescriptor.getWriteMethod());
    this.propertyDescriptor = propertyDescriptor;
    this.field = null;
  }

  @Override
  public synchronized Class<?> getPropertyType() {
    return field != null ? field.getType() : propertyDescriptor.getPropertyType();
  }

  public Object get(Object target) {
    try {
      if (field != null) {
        return field.get(target);
      } else {
        return propertyDescriptor.getReadMethod().invoke(target);
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
  }

  public void set(Object target, Object value) {
    try {
      if (field != null) {
        field.set(target, value);
      } else {
        propertyDescriptor.getWriteMethod().invoke(target, value);
      }
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public AccessibleObject getAccessibleObject() {
    return field != null ? field : propertyDescriptor.getReadMethod();
  }

  public Member getMember() {
    return field != null ? field : propertyDescriptor.getReadMethod();
  }

  public String getColumnName(String defaultForeignKeySuffix) {
    AccessibleObject accessibleObject = getAccessibleObject();
    String columnName = getReadMethod() != null || field != null ? Entities.getName(accessibleObject) : getName();

    if (Entities.isToOneRelation(accessibleObject)) {
      if (accessibleObject.isAnnotationPresent(JoinColumn.class)) {
        return accessibleObject.getAnnotation(JoinColumn.class).name();
      }
      return columnName + defaultForeignKeySuffix;
    }

    return columnName;
  }
}