package com.moandjiezana.dbutilsjpa;

import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

import com.moandjiezana.dbutilsjpa.testutils.CustomNamePropertyEntity;
import com.moandjiezana.dbutilsjpa.testutils.NonUpdatableEntity;
import com.moandjiezana.dbutilsjpa.testutils.SimpleEntity;

import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.fest.reflect.core.Reflection;
import org.fest.reflect.field.Invoker;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class JpaQueryRunnerTest {

  private QueryRunner queryRunner = Mockito.mock(QueryRunner.class);
  private JpaQueryRunner runner = new JpaQueryRunner(queryRunner);
  
  @Test
  public void should_update_if_entity_has_id() throws SQLException {
    SimpleEntity entity = new SimpleEntity();
    Reflection.field("id").ofType(Long.class).in(entity).set(Long.valueOf(1));
    Invoker<String> name = Reflection.field("name").ofType(String.class).in(entity);
    name.set("my name");
    
    runner.save(entity);
    
    verify(queryRunner).update("UPDATE SimpleEntity\nSET name = ?\nWHERE id = ?", name.get(), entity.getId());
  }
  
  @Test
  public void should_update_from_annotated_properties_if_entity_has_id() throws SQLException {
    CustomNamePropertyEntity entity = new CustomNamePropertyEntity();
    entity.setId(Long.valueOf(1));
    entity.setDate(new Date());
    
    runner.save(entity);
    
    verify(queryRunner).update("UPDATE myTableName\nSET customDateColumn = ?\nWHERE customNameId = ?", entity.getDate(), entity.getId());
  }
  
  @Test
  public void should_ignore_non_updatable_column() throws SQLException {
    NonUpdatableEntity entity = new NonUpdatableEntity();
    entity.setId(1L);
    entity.setNotInserted("inserted");
    
    runner.save(entity);
    
    verify(queryRunner).update("UPDATE NonUpdatableEntity\nSET name = ?, notInserted = ?\nWHERE id = ?", entity.getName(), entity.getNotInserted(), entity.getId());
  }
  
  @Test
  public void should_include_non_updatable_column_on_insert() throws SQLException {
    NonUpdatableEntity entity = new NonUpdatableEntity();
    entity.setNotUpdated("included");
    entity.setName("a name");
    
    runner.save(entity);
    
    verify(queryRunner).insert(Mockito.eq("INSERT INTO NonUpdatableEntity (name, notUpdated)\nVALUES(?, ?)"), Mockito.any(ScalarHandler.class), Mockito.eq(entity.getName()), Mockito.eq(entity.getNotUpdated()));
  }
  
  @Test
  public void should_insert_new_entity() throws SQLException {
    
    SimpleEntity entity = new SimpleEntity();
    Invoker<String> name = field("name").ofType(String.class).in(entity);
    name.set("a name");
    stub(queryRunner.insert(Mockito.eq("INSERT INTO SimpleEntity (name)\nVALUES(?)"), Mockito.any(ResultSetHandler.class), Mockito.eq(name.get()))).toReturn(5L);
    
    runner.save(entity);
    
    Assert.assertEquals(5, field("id").ofType(Long.class).in(entity).get().longValue());
  }
  
  @Test
  public void should_ignore_non_insertable_column() throws SQLException {
    NonUpdatableEntity entity = new NonUpdatableEntity();
    
    runner.save(entity);
    
    verify(queryRunner).insert(Mockito.eq("INSERT INTO NonUpdatableEntity (name, notUpdated)\nVALUES(?, ?)"), Mockito.any(ResultSetHandler.class), Mockito.eq(entity.getName()), Mockito.eq(entity.getNotUpdated()));
  }
}
