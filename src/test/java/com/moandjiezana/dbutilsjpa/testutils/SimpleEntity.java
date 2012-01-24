package com.moandjiezana.dbutilsjpa.testutils;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SimpleEntity {

  @Id
  private Long id;
  private String name;

  public Long getId() {
    return id;
  }
}
