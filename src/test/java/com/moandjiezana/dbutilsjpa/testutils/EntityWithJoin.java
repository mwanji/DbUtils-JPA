package com.moandjiezana.dbutilsjpa.testutils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class EntityWithJoin {
  
  @Id
  public Long id;

  @ManyToOne
  public SimpleEntity simple;
  
  @ManyToOne @JoinColumn(name = "custom_enum_fk")
  public EnumEntity enumEntity;
  
  @OneToOne
  public MultiplePropertyEntity oneToOne;
  
  @ManyToOne
  public NonUpdatableEntity customSuffix;
}
