package com.moandjiezana.dbutilsjpa;

import static org.junit.Assert.assertEquals;

import com.moandjiezana.dbutilsjpa.testutils.CustomNamePropertyEntity;
import com.moandjiezana.dbutilsjpa.testutils.SimpleEntity;
import com.moandjiezana.dbutilsjpa.testutils.Utils;

import org.junit.Test;

public class SqlWriterInsertTest {

  private final SqlWriter sqlWriter = new SqlWriter();
  
  @Test
  public void should_use_field() {
    String sql = sqlWriter.insert(SimpleEntity.class);
    
    assertEquals("INSERT INTO SimpleEntity (name) VALUES(?)", Utils.singleLine(sql));
  }
  
  @Test
  public void should_use_custom_names_from_property() {
    String sql = sqlWriter.insert(CustomNamePropertyEntity.class);
    
    assertEquals("INSERT INTO myTableName (customDateColumn) VALUES(?)", Utils.singleLine(sql));
  }
}
