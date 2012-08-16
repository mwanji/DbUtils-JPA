package com.moandjiezana.dbutilsjpa;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.moandjiezana.dbutilsjpa.testutils.EntityWithJoin;

public class JpaBeanProcessor_JoinsTest {
  private Connection conn;

  @Before
  public void before() throws SQLException {
    conn = DriverManager.getConnection("jdbc:h2:mem:");
  }
  
  @After
  public void after() throws SQLException {
    conn.close();
  }
  
  @Test
  public void should_query_many_to_one() throws Exception {
    QueryRunner queryRunner = new QueryRunner();
    queryRunner.update(conn, "CREATE TABLE SimpleEntity(id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR)");
    queryRunner.update(conn, "CREATE TABLE EntityWithJoin(id INT AUTO_INCREMENT PRIMARY KEY, simple_id INT)");
    
    Integer simpleEntityId = queryRunner.insert(conn, "INSERT INTO SimpleEntity(name) VALUES(?)", new ScalarHandler<Integer>(), "my name");
    Integer entityWithJoinId = queryRunner.insert(conn, "INSERT INTO EntityWithJoin(simple_id) VALUES(?)", new ScalarHandler<Integer>(), simpleEntityId);
    
    BeanHandler<EntityWithJoin> beanHandler = new BeanHandler<EntityWithJoin>(EntityWithJoin.class, new BasicRowProcessor(new JpaBeanProcessor()));
    EntityWithJoin entityWithJoin = queryRunner.query(conn, "SELECT EntityWithJoin.*, SimpleEntity.* FROM EntityWithJoin, SimpleEntity WHERE EntityWithJoin.simple_id = SimpleEntity.id AND EntityWithJoin.id = ?", beanHandler, entityWithJoinId);
    
    assertEquals(Long.valueOf(simpleEntityId), entityWithJoin.simple.getId());
    assertEquals("my name", entityWithJoin.simple.getName());
  }

}
