package com.moandjiezana.dbutilsjpa;

import com.moandjiezana.dbutilsjpa.testutils.CustomNameEntity;
import com.moandjiezana.dbutilsjpa.testutils.CustomNamePropertyEntity;
import com.moandjiezana.dbutilsjpa.testutils.EmptyNameEntity;
import com.moandjiezana.dbutilsjpa.testutils.EmptyNamePropertyEntity;
import com.moandjiezana.dbutilsjpa.testutils.SimpleEntity;
import com.moandjiezana.dbutilsjpa.testutils.SimplePropertyEntity;
import com.moandjiezana.dbutilsjpa.testutils.Utils;

import junit.framework.Assert;

import org.junit.Test;

public class SqlWriterSelectTest {
  private SqlWriter sqlWriter = new SqlWriter();
  
  @Test
  public void should_select_all() {
    assertEquals("SELECT ALL * FROM myTableName", sqlWriter.select(CustomNameEntity.class));
  }
  
  @Test
  public void should_use_field_annotations() {
    String sql = sqlWriter.selectById(CustomNameEntity.class);
    
    assertEquals("SELECT ALL * FROM myTableName WHERE customNameId = ?", sql);
  }
  
  @Test
  public void should_use_property_annotations() {
    String sql = sqlWriter.selectById(CustomNamePropertyEntity.class);
    
    assertEquals("SELECT ALL * FROM myTableName WHERE customNameId = ?", sql);
  }

  @Test
  public void should_use_object_names_when_no_field_annotations() {
    String sql = sqlWriter.selectById(SimpleEntity.class);
    
    assertEquals("SELECT ALL * FROM SimpleEntity WHERE id = ?", sql);
  }

  @Test
  public void should_use_object_names_when_no_property_annotations() {
    String sql = sqlWriter.selectById(SimplePropertyEntity.class);
    
    assertEquals("SELECT ALL * FROM SimplePropertyEntity WHERE id = ?", sql);
  }

  @Test
  public void should_use_object_names_when_field_annotations_contain_empty_names() {
    String sql = sqlWriter.selectById(EmptyNamePropertyEntity.class);
    
    assertEquals("SELECT ALL * FROM EmptyNamePropertyEntity WHERE id = ?", sql);
  }

  @Test
  public void should_use_object_names_when_property_annotations_contain_empty_names() {
    String sql = sqlWriter.selectById(EmptyNameEntity.class);
    
    assertEquals("SELECT ALL * FROM EmptyNameEntity WHERE id = ?", sql);
  }
  
  private void assertEquals(String expected, String actual) {
    Assert.assertEquals(expected, Utils.singleLine(actual));
  }
}
