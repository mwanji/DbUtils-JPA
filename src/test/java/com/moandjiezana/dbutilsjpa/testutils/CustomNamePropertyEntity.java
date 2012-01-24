package com.moandjiezana.dbutilsjpa.testutils;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="myTableName")
public class CustomNamePropertyEntity {

  private Long id;
  private Date date;
  
  @Id
  @Column(name="customNameId")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(name="customDateColumn")
  @Temporal(TemporalType.DATE)
  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}