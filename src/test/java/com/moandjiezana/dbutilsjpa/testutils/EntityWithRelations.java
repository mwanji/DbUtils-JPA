package com.moandjiezana.dbutilsjpa.testutils;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class EntityWithRelations {

  private Long pk;
  private String name;
  private List<SimpleEntity> simpleEntities;
  private EmptyNamePropertyEntity emptyNameEntity;
  private SimplePropertyEntity simplePropertyEntity;
  
  @Id
  public Long getPk() {
    return pk;
  }
  
  public void setPk(Long pk) {
    this.pk = pk;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @OneToMany
  public List<SimpleEntity> getSimpleEntities() {
    return simpleEntities;
  }
  
  public void setSimpleEntities(List<SimpleEntity> simpleEntities) {
    this.simpleEntities = simpleEntities;
  }
  
  @ManyToOne
  public EmptyNamePropertyEntity getEmptyNameEntity() {
    return emptyNameEntity;
  }
  
  public void setEmptyNameEntity(EmptyNamePropertyEntity emptyNameEntity) {
    this.emptyNameEntity = emptyNameEntity;
  }

  @OneToOne
  public SimplePropertyEntity getSimplePropertyEntity() {
    return simplePropertyEntity;
  }

  public void setSimplePropertyEntity(SimplePropertyEntity simplePropertyEntity) {
    this.simplePropertyEntity = simplePropertyEntity;
  }
}
