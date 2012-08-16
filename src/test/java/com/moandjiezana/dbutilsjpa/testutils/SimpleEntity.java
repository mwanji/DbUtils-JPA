package com.moandjiezana.dbutilsjpa.testutils;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SimpleEntity {

  private static final Object CONSTANT = new Object(); 
  
  @Id
  private Long id;
  private String name;

  public Long getId() {
    return id;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
