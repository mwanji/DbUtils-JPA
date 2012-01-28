package com.moandjiezana.dbutilsjpa.internal;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

public class FieldBasedPropertyDescriptor extends PropertyDescriptor {
  
  public final Field field;
  
  public FieldBasedPropertyDescriptor(String propertyName, Field field) throws IntrospectionException {
    super(propertyName, null, null);
    this.field = field;
    field.setAccessible(true);
  }

  @Override
  public synchronized Class<?> getPropertyType() {
    return field.getType();
  }
}