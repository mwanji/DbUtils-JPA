package com.moandjiezana.dbutilsjpa.internal;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class PropertyDescriptorWrapper extends PropertyDescriptor {
  
  public final Field field;
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
}