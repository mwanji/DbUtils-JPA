package com.moandjiezana.dbutilsjpa.testutils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class EntityWithJoin {
  
  @Id
  public Long id;

  @ManyToOne
  public SimpleEntity simple;
}
