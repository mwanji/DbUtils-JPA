package com.moandjiezana.dbutilsjpa;

import static org.junit.Assert.assertEquals;

import com.moandjiezana.dbutilsjpa.testutils.CustomNamePropertyEntity;
import com.moandjiezana.dbutilsjpa.testutils.EntityWithRelations;
import com.moandjiezana.dbutilsjpa.testutils.MultiplePropertyEntity;
import com.moandjiezana.dbutilsjpa.testutils.SimpleEntity;
import com.moandjiezana.dbutilsjpa.testutils.Utils;

import org.junit.Test;

public class SqlWriterUpdateTest {

  private final SqlWriter sqlWriter = new SqlWriter();
  
  @Test
  public void should_ignore_id() {
    String sql = sqlWriter.updateById(SimpleEntity.class);
    
    assertEquals("UPDATE SimpleEntity SET name = ? WHERE id = ?", Utils.singleLine(sql));
  }
  
  @Test
  public void should_ignore_relations() {
    String sql = sqlWriter.updateById(EntityWithRelations.class);
    
    assertEquals("UPDATE EntityWithRelations SET name = ? WHERE pk = ?", Utils.singleLine(sql));
  }
  
  @Test
  public void should_use_property_annotations() {
    String sql = sqlWriter.updateById(CustomNamePropertyEntity.class);
    
    assertEquals("UPDATE myTableName SET customDateColumn = ? WHERE customNameId = ?", Utils.singleLine(sql));
  }
  
  @Test
  public void should_update_specified_columns() {
    String sql = sqlWriter.updateById(MultiplePropertyEntity.class, "age");
    
    assertEquals("UPDATE MultiplePropertyEntity SET age = ? WHERE id = ?", Utils.singleLine(sql));
  }
}
