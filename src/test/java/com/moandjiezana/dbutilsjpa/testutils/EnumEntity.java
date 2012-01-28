package com.moandjiezana.dbutilsjpa.testutils;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class EnumEntity {

  public static enum SomeEnum {
    VALUE_1, VALUE_2;
  }
  
  @Id
  public Long id;
  
  public SomeEnum anEnum;
}
