package com.moandjiezana.dbutilsjpa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SqlWriterOrderByTest {

  @Test
  public void should_order_one_column_asc() {
    String orderBy = new SqlWriter().asc("myCol");
    
    assertEquals(" ORDER BY myCol ASC", orderBy);
  }
  
  @Test
  public void should_order_multiple_columns_asc() {
    String orderBy = new SqlWriter().asc("myCol", "myCol2", "myCol3", "myCol4");
    
    assertEquals(" ORDER BY myCol, myCol2, myCol3, myCol4 ASC", orderBy);
  }
  
  @Test
  public void should_order_one_column_desc() {
    String orderBy = new SqlWriter().desc("myCol");
    
    assertEquals(" ORDER BY myCol DESC", orderBy);
  }
  
  @Test
  public void should_order_multiple_columns_desc() {
    String orderBy = new SqlWriter().desc("myCol", "myCol2", "myCol3", "myCol4");
    
    assertEquals(" ORDER BY myCol, myCol2, myCol3, myCol4 DESC", orderBy);
  }
}
