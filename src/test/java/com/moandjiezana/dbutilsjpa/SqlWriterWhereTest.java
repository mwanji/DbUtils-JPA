package com.moandjiezana.dbutilsjpa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SqlWriterWhereTest {

  @Test
  public void should_add_single_column() {
    String where = new SqlWriter().where("myCol");
    
    assertEquals(" WHERE myCol = ?", where);
  }
  
  @Test
  public void should_add_multiple_columns() {
    String where = new SqlWriter().where("col1", "col2", "col3", "col4");
    
    assertEquals(" WHERE (((col1 = ? AND col2 = ?) AND col3 = ?) AND col4 = ?)", where);
  }
}
