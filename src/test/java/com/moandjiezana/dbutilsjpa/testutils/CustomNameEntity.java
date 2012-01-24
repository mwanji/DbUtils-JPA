package com.moandjiezana.dbutilsjpa.testutils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="myTableName")
public class CustomNameEntity {

  @Id
  @Column(name="customNameId")
  private Long id;
}
