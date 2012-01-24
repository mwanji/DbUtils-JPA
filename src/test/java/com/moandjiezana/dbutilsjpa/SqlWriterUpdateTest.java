package com.moandjiezana.dbutilsjpa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.moandjiezana.dbutilsjpa.testutils.CustomNamePropertyEntity;
import com.moandjiezana.dbutilsjpa.testutils.EntityWithRelations;
import com.moandjiezana.dbutilsjpa.testutils.SimpleEntity;

public class SqlWriterUpdateTest {

  private final SqlWriter sqlWriter = new SqlWriter();
  
  @Test
  public void should_ignore_id() {
    String sql = sqlWriter.updateById(SimpleEntity.class);
    
    assertEquals("UPDATE SimpleEntity SET name=? WHERE id=?", sql);
  }
  
  @Test
  public void should_ignore_relations() {
    String sql = sqlWriter.updateById(EntityWithRelations.class);
    
    assertEquals("UPDATE EntityWithRelations SET name=? WHERE pk=?", sql);
  }
  
  @Test
  public void should_use_property_annotations() {
    String sql = sqlWriter.updateById(CustomNamePropertyEntity.class);
    
    assertEquals("UPDATE myTableName SET customDateColumn=? WHERE customNameId=?", sql);
  }
}
