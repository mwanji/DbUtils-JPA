package com.moandjiezana.dbutilsjpa;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.Test;

import com.moandjiezana.dbutilsjpa.testutils.CustomNamePropertyEntity;
import com.moandjiezana.dbutilsjpa.testutils.SimpleEntity;
import com.moandjiezana.dbutilsjpa.testutils.SimplePropertyEntity;

public class JpaBeanProcessorTest {

  private JpaBeanProcessor processor = new JpaBeanProcessor();
  
  @Test
  public void should_convert_with_annotated_field() throws SQLException {
    ResultSetMetaData metaData = mock(ResultSetMetaData.class);
    when(metaData.getColumnCount()).thenReturn(1);
    int idColumnIndex = 1;
    setColumnName(metaData, "id", idColumnIndex);

    ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.getMetaData()).thenReturn(metaData);
    setLongValue(resultSet, idColumnIndex, Long.valueOf(1));
    
    SimpleEntity entity = processor.toBean(resultSet, SimpleEntity.class);
    
    assertEquals(Long.valueOf(1), entity.getId());
  }
  
  @Test
  public void should_convert_with_annotated_property() throws SQLException {
    ResultSetMetaData metaData = mock(ResultSetMetaData.class);
    int columnIndex = 1;
    when(metaData.getColumnCount()).thenReturn(columnIndex);
    setColumnName(metaData, "id", columnIndex);

    ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.getMetaData()).thenReturn(metaData);
    setLongValue(resultSet, columnIndex, Long.valueOf(1));
    
    SimplePropertyEntity entity = processor.toBean(resultSet, SimplePropertyEntity.class);
    
    assertEquals(Long.valueOf(1), entity.getId());
  }
  
  @Test
  public void should_convert_with_annotated_property_with_custom_names() throws SQLException {
    ResultSetMetaData metaData = mock(ResultSetMetaData.class);
    int idColumnIndex = 1;
    when(metaData.getColumnCount()).thenReturn(1);
    setColumnName(metaData, "customNameId", idColumnIndex);

    ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.getMetaData()).thenReturn(metaData);
    setLongValue(resultSet, idColumnIndex, Long.valueOf(3));
    
    CustomNamePropertyEntity entity = processor.toBean(resultSet, CustomNamePropertyEntity.class);
    
    assertEquals(Long.valueOf(3), entity.getId());
  }

  private void setLongValue(ResultSet resultSet, int columnIndex, Long columnValue) throws SQLException {
    when(resultSet.getObject(columnIndex)).thenReturn(columnValue);
    when(resultSet.getLong(columnIndex)).thenReturn(columnValue);
  }

  private void setColumnName(ResultSetMetaData metaData, String columnName, int columnIndex) throws SQLException {
    when(metaData.getColumnName(columnIndex)).thenReturn(columnName);
  }
}
