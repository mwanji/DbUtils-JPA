package com.moandjiezana.dbutilsjpa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.moandjiezana.dbutilsjpa.testutils.CustomNameEntity;
import com.moandjiezana.dbutilsjpa.testutils.CustomNamePropertyEntity;
import com.moandjiezana.dbutilsjpa.testutils.EmptyNameEntity;
import com.moandjiezana.dbutilsjpa.testutils.EmptyNamePropertyEntity;
import com.moandjiezana.dbutilsjpa.testutils.SimpleEntity;
import com.moandjiezana.dbutilsjpa.testutils.SimplePropertyEntity;

public class SqlWriterSelectTest {
  private SqlWriter sqlWriter = new SqlWriter();
  
  @Test
  public void should_use_field_annotations() {
    String sql = sqlWriter.selectById(CustomNameEntity.class, null);
    
    assertEquals("SELECT * FROM myTableName WHERE customNameId=?", sql);
  }
  
  @Test
  public void should_use_property_annotations() {
    String sql = sqlWriter.selectById(CustomNamePropertyEntity.class, null);
    
    assertEquals("SELECT * FROM myTableName WHERE customNameId=?", sql);
  }

  @Test
  public void should_use_object_names_when_no_field_annotations() {
    String sql = sqlWriter.selectById(SimpleEntity.class, null);
    
    assertEquals("SELECT * FROM SimpleEntity WHERE id=?", sql);
  }

  @Test
  public void should_use_object_names_when_no_property_annotations() {
    String sql = sqlWriter.selectById(SimplePropertyEntity.class, null);
    
    assertEquals("SELECT * FROM SimplePropertyEntity WHERE id=?", sql);
  }

  @Test
  public void should_use_object_names_when_field_annotations_contain_empty_names() {
    String sql = sqlWriter.selectById(EmptyNamePropertyEntity.class, null);
    
    assertEquals("SELECT * FROM EmptyNamePropertyEntity WHERE id=?", sql);
  }

  @Test
  public void should_use_object_names_when_property_annotations_contain_empty_names() {
    String sql = sqlWriter.selectById(EmptyNameEntity.class, null);
    
    assertEquals("SELECT * FROM EmptyNameEntity WHERE id=?", sql);
  }
}
