package com.moandjiezana.dbutilsjpa.testutils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class NonUpdatableEntity {

  @Id
  private Long id;
  private String name;
  @Column(updatable=false)
  private String notUpdated;
  @Column(insertable=false)
  private String notInserted;

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }
  
  public String getName() {
    return name;
  }

  public String getNotUpdated() {
    return notUpdated;
  }

  public String getNotInserted() {
    return notInserted;
  }

  public void setNotInserted(String notInserted) {
    this.notInserted = notInserted;
  }
}
