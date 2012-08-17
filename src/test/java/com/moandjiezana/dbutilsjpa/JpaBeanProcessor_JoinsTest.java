package com.moandjiezana.dbutilsjpa;

import static org.junit.Assert.*;

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
import com.moandjiezana.dbutilsjpa.testutils.EnumEntity;

public class JpaBeanProcessor_JoinsTest {
  private Connection conn;
  private final QueryRunner queryRunner = new QueryRunner();
  private final BeanHandler<EntityWithJoin> beanHandler = new BeanHandler<EntityWithJoin>(EntityWithJoin.class, new BasicRowProcessor(new JpaBeanProcessor()));
  private final ScalarHandler<Integer> idHandler = new ScalarHandler<Integer>();

  @Before
  public void before() throws SQLException {
    conn = DriverManager.getConnection("jdbc:h2:mem:");
    queryRunner.update(conn, "CREATE TABLE SimpleEntity(id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR)");
    queryRunner.update(conn, "CREATE TABLE EnumEntity(id INT AUTO_INCREMENT PRIMARY KEY, anEnum VARCHAR)");
    queryRunner.update(conn, "CREATE TABLE MultiplePropertyEntity(id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR, age INT, birthDate DATE)");
    queryRunner.update(conn, "CREATE TABLE NonUpdatableEntity(id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR, notUpdated VARCHAR, notInserted VARCHAR)");
    queryRunner.update(conn, "CREATE TABLE EntityWithJoin(id INT AUTO_INCREMENT PRIMARY KEY, simple_id INT, custom_enum_fk INT, oneToOne_id INT, customSuffix_csfk INT)");
  }
  
  @After
  public void after() throws SQLException {
    conn.close();
  }
  
  @Test
  public void should_join_many_to_one() throws Exception {
    Integer simpleEntityId = queryRunner.insert(conn, "INSERT INTO SimpleEntity(name) VALUES(?)", new ScalarHandler<Integer>(), "my name");
    Integer entityWithJoinId = queryRunner.insert(conn, "INSERT INTO EntityWithJoin(simple_id) VALUES(?)", new ScalarHandler<Integer>(), simpleEntityId);
    
    EntityWithJoin entityWithJoin = queryRunner.query(conn, "SELECT EntityWithJoin.*, SimpleEntity.* FROM EntityWithJoin, SimpleEntity WHERE EntityWithJoin.simple_id = SimpleEntity.id AND EntityWithJoin.id = ?", beanHandler, entityWithJoinId);
    
    assertEquals(Long.valueOf(simpleEntityId), entityWithJoin.simple.getId());
    assertEquals("my name", entityWithJoin.simple.getName());
  }
  
  @Test
  public void should_ignore_join_if_not_in_select() throws SQLException {
    Integer simpleEntityId = queryRunner.insert(conn, "INSERT INTO SimpleEntity(name) VALUES(?)", new ScalarHandler<Integer>(), "my name");
    Integer entityWithJoinId = queryRunner.insert(conn, "INSERT INTO EntityWithJoin(simple_id) VALUES(?)", new ScalarHandler<Integer>(), simpleEntityId);
    
    EntityWithJoin entityWithJoin = queryRunner.query(conn, "SELECT EntityWithJoin.* FROM EntityWithJoin WHERE EntityWithJoin.id = ?", beanHandler, entityWithJoinId);
    
    assertNull(entityWithJoin.simple);
    assertNull(entityWithJoin.oneToOne);
  }
  
  @Test
  public void should_join_many_to_one_on_specified_join_column() throws SQLException {
    Integer enumEntityId = queryRunner.insert(conn, "INSERT INTO EnumEntity(anEnum) VALUES(?)", idHandler, EnumEntity.SomeEnum.VALUE_2.toString());
    Integer entityWithJoinId = queryRunner.insert(conn, "INSERT INTO EntityWithJoin(custom_enum_fk) VALUES(?)", idHandler, enumEntityId);
    
    EntityWithJoin entityWithJoin = queryRunner.query(conn, "SELECT EntityWithJoin.*, EnumEntity.* FROM EntityWithJoin, EnumEntity WHERE EntityWithJoin.custom_enum_fk = EnumEntity.id AND EntityWithJoin.id = ?", beanHandler, entityWithJoinId);
    
    assertEquals(Long.valueOf(enumEntityId), entityWithJoin.enumEntity.id);
    assertEquals(EnumEntity.SomeEnum.VALUE_2, entityWithJoin.enumEntity.anEnum);
  }
  
  @Test
  public void should_join_many_to_one_with_custom_fk_suffix() throws SQLException {
    Integer customSuffixId = queryRunner.insert(conn, "INSERT INTO NonUpdatableEntity(name) VALUES(?)", new ScalarHandler<Integer>(), "my name");
    Integer entityWithJoinId = queryRunner.insert(conn, "INSERT INTO EntityWithJoin(customSuffix_csfk) VALUES(?)", idHandler, customSuffixId);
    BeanHandler<EntityWithJoin> customSuffixHandler = new BeanHandler<EntityWithJoin>(EntityWithJoin.class, new BasicRowProcessor(new JpaBeanProcessor("_csfk")));
    
    EntityWithJoin entityWithJoin = queryRunner.query(conn, "SELECT EntityWithJoin.*, NonUpdatableEntity.* FROM EntityWithJoin, NonUpdatableEntity WHERE EntityWithJoin.customSuffix_csfk = NonUpdatableEntity.id AND EntityWithJoin.id = ?", customSuffixHandler, entityWithJoinId);
    
    assertEquals(Long.valueOf(customSuffixId), entityWithJoin.customSuffix.getId());
  }

  @Test
  public void should_join_one_to_one() throws SQLException {
    Integer oneToOneId = queryRunner.insert(conn, "INSERT INTO MultiplePropertyEntity(name, age) VALUES(?,?)", idHandler, "my name", 5);
    Integer entityWithJoinId = queryRunner.insert(conn, "INSERT INTO EntityWithJoin(oneToOne_id) VALUES(?)", idHandler, oneToOneId);
    
    EntityWithJoin entityWithJoin = queryRunner.query(conn, "SELECT EntityWithJoin.*, MultiplePropertyEntity.* FROM EntityWithJoin, MultiplePropertyEntity WHERE EntityWithJoin.oneToOne_id = MultiplePropertyEntity.id AND EntityWithJoin.id = ?", beanHandler, entityWithJoinId);
    
    assertEquals(Long.valueOf(oneToOneId), entityWithJoin.oneToOne.id);
    assertEquals("my name", entityWithJoin.oneToOne.name);
    assertEquals(5, entityWithJoin.oneToOne.age);
    assertNull(entityWithJoin.oneToOne.birthDate);
  }
}
