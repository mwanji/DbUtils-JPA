package com.moandjiezana.dbutilsjpa.testutils;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class MultiplePropertyEntity {

  @Id
  public Long id;
  
  public String name;
  public int age;
  @Temporal(TemporalType.DATE)
  public Date birthDate;
}
