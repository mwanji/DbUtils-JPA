package com.moandjiezana.dbutilsjpa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.moandjiezana.dbutilsjpa.testutils.CustomNamePropertyEntity;
import com.moandjiezana.dbutilsjpa.testutils.SimpleEntity;

public class SqlWriterDeleteTest {
  private SqlWriter sqlWriter = new SqlWriter();

  @Test
  public void should_delete() {
    String sql = sqlWriter.deleteById(SimpleEntity.class);
    
    assertEquals("DELETE FROM SimpleEntity WHERE id=?", sql);
  }

  @Test
  public void should_use_annotations_on_property() {
    String sql = sqlWriter.deleteById(CustomNamePropertyEntity.class);
    
    assertEquals("DELETE FROM myTableName WHERE customNameId=?", sql);
  }

}
