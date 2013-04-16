package com.moandjiezana.dbutilsjpa;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

public class Entities {

  /**
   * @return The field or method annotated with {@link Id}. {@link AccessibleObject#setAccessible(boolean)} is called if necessary.
   * @throws Exception if there is no {@link Id} annotation.
   */
  public static AccessibleObject getIdAccessor(Class<?> type) {
    for (Method method : type.getMethods()) {
      if (isIdAccessor(method)) {
        if (!method.isAccessible()) {
          method.setAccessible(true);
        }
        return method;
      }
    }

    for (Field field : type.getDeclaredFields()) {
      if (isIdAccessor(field)) {
        if (!field.isAccessible()) {
          field.setAccessible(true);
        }
        return field;
      }
    }

    throw new IllegalArgumentException(type.getName() + " does not have a field or property annotated with @Id");
  }

  /**
   * @return Name of corresponding table. Uses Table annotation if present, defaults to class's simple name.
   */
  public static String getName(Class<?> entityClass) {
    if (entityClass.isAnnotationPresent(Table.class)) {
      String name = entityClass.getAnnotation(Table.class).name();
      if (!name.isEmpty()) {
        return name;
      }
    }

    return entityClass.getSimpleName();
  }

  /**
   * @return Name of corresponding field. Uses Column annotation if present, defaults to field if accessibleObject is a field or JavaBean-style property name if accessibleObject is a method.
   */
  public static String getName(AccessibleObject accessibleObject) {
    if (accessibleObject.isAnnotationPresent(Column.class)) {
      String name = accessibleObject.getAnnotation(Column.class).name();
      if (!name.isEmpty()) {
        return name;
      }
    }

    if (accessibleObject instanceof Field) {
      return ((Field) accessibleObject).getName();
    }

    Method method = (Method) accessibleObject;

    try {
      PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(method.getDeclaringClass()).getPropertyDescriptors();
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
        if (method.equals(propertyDescriptor.getReadMethod())) {
          return propertyDescriptor.getName();
        }
      }
    } catch (IntrospectionException e) {
      throw new RuntimeException(e);
    }

    throw new IllegalArgumentException("No column name can be derived from " + method + ". Make sure it is a getter.");

  }

  public static boolean isTransient(AccessibleObject accessibleObject) {
    return Modifier.isTransient(((Member) accessibleObject).getModifiers()) || accessibleObject.isAnnotationPresent(Transient.class);
  }

  public static boolean isRelation(AccessibleObject accessibleObject) {
    return accessibleObject.isAnnotationPresent(OneToMany.class) || isToOneRelation(accessibleObject);
  }

  public static boolean isToOneRelation(AccessibleObject accessibleObject) {
    return accessibleObject.isAnnotationPresent(ManyToOne.class) || accessibleObject.isAnnotationPresent(OneToOne.class);
  }

  public static boolean isIdAccessor(AnnotatedElement annotatedElement) {
    return annotatedElement.isAnnotationPresent(Id.class);
  }

  public static boolean isMapped(Class<?> objectClass) {
    return objectClass.isAnnotationPresent(Entity.class) || objectClass.isAnnotationPresent(MappedSuperclass.class);
  }

  public static boolean isStatic(Member member) {
    return Modifier.isStatic(member.getModifiers());
  }

  private Entities() {}
}
