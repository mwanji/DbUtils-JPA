package com.moandjiezana.dbutilsjpa.testutils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(catalog="catalog")
public class EmptyNameEntity {

  @Id
  @Column(nullable=false)
  private Long id;
}
