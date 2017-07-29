package jp.gr.naoco.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.gr.naoco.core.NaocoCoreFacade;
import jp.gr.naoco.core.NaocoCoreInitializer;
import jp.gr.naoco.db.GenericDAO;
import jp.gr.naoco.db.entity.AbstractEntity;
import jp.gr.naoco.db.entity.annotation.Column;
import jp.gr.naoco.db.entity.annotation.Id;
import jp.gr.naoco.db.entity.annotation.Table;
import jp.gr.naoco.sample.dummy.DummyConnection;
import jp.gr.naoco.sample.dummy.DummyPreparedStatement;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GenericDAOTest01 {

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Test Preparations

    @Before
    public void setup() throws Exception {
        DummyConnection.clearIdCount();
        NaocoCoreInitializer.initialize(GenericDAOTest01.class.getName() + "_test", null);
        NaocoCoreFacade.startTransaction("java:comp/env/jdbc/test01");
    }

    @After
    public void teardown() throws Exception {
        NaocoCoreFacade.commitTransaction();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Test Cases

    /**
     * update：Entityにすべての値を設定
     * 
     * @throws Exception
     */
    @Test
    public void test01() throws Exception {
        // 設定値の宣言
        final String sql = "UPDATE TEST_TABLE SET STRING_VALUE=?, LONG_VALUE=?, DOUBLE_VALUE=?, UTIL_DATE_VALUE=?, SQL_TIMESTAMP_VALUE=?, SQL_DATE_VALUE=?, SQL_TIME_VALUE=? WHERE STRING_ID=? AND LONG_ID=? AND DOUBLE_ID=?";

        // エンティティの設定
        TestEntity entity = new TestEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        entity.setDouble_id(2.2D);
        entity.setString_value("string value");
        entity.setLong_value(3L);
        entity.setDouble_value(4.4D);
        entity.setUtil_date_value(new java.util.Date(1400735867000L));
        entity.setSql_timestamp_value(new java.sql.Timestamp(1400835867000L));
        entity.setSql_date_value(new java.sql.Date(1400835867000L));
        entity.setSql_time_value(new java.sql.Time(1400835867000L));

        GenericDAO.update(entity);
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    /**
     * update：Entityに一部のカラムのみ設定
     * 
     * @throws Exception
     */
    @Test
    public void test02() throws Exception {
        // 設定値の宣言
        final String sql = "UPDATE TEST_TABLE SET STRING_VALUE=? WHERE STRING_ID=? AND LONG_ID=? AND DOUBLE_ID=?";

        // エンティティの設定
        TestEntity entity = new TestEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        entity.setDouble_id(2.2D);
        entity.setString_value("string value");

        GenericDAO.update(entity);
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    /**
     * update：Entityに主キーを設定せずに実行してエラー
     * 
     * @throws Exception
     */
    @Test
    public void test03() throws Exception {
        // 設定値の宣言
        final String sql = "UPDATE TEST_TABLE SET STRING_VALUE=? WHERE STRING_ID=? AND LONG_ID=? AND DOUBLE_ID=?";

        // エンティティの設定
        TestEntity entity = new TestEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        // entity.setDouble_id(2.2D);
        entity.setString_value("string value");
        entity.setLong_value(3L);
        entity.setDouble_value(4.4D);
        entity.setUtil_date_value(new java.util.Date(1400735867000L));
        entity.setSql_timestamp_value(new java.sql.Timestamp(1400835867000L));
        entity.setSql_date_value(new java.sql.Date(1400835867000L));
        entity.setSql_time_value(new java.sql.Time(1400835867000L));

        try {
            GenericDAO.update(entity);
        } catch (jp.gr.naoco.db.exception.QueryRenderingException e) {
            System.out.println(e.getMessage());
            return;
        }
        Assert.fail();
    }

    /**
     * insert：Entityにすべての値を設定
     * 
     * @throws Exception
     */
    @Test
    public void test04() throws Exception {
        // 設定値の宣言
        final String sql = "INSERT INTO TEST_TABLE(STRING_ID, LONG_ID, DOUBLE_ID, STRING_VALUE, LONG_VALUE, DOUBLE_VALUE, UTIL_DATE_VALUE, SQL_TIMESTAMP_VALUE, SQL_DATE_VALUE, SQL_TIME_VALUE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // エンティティの設定
        TestEntity entity = new TestEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        entity.setDouble_id(2.2D);
        entity.setString_value("string value");
        entity.setLong_value(3L);
        entity.setDouble_value(4.4D);
        entity.setUtil_date_value(new java.util.Date(1400735867000L));
        entity.setSql_timestamp_value(new java.sql.Timestamp(1400835867000L));
        entity.setSql_date_value(new java.sql.Date(1400835867000L));
        entity.setSql_time_value(new java.sql.Time(1400835867000L));

        GenericDAO.insert(entity);
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    /**
     * insert：Entityに一部のカラムのみ設定
     * 
     * @throws Exception
     */
    @Test
    public void test05() throws Exception {
        // 設定値の宣言
        final String sql = "INSERT INTO TEST_TABLE(STRING_ID, LONG_ID, DOUBLE_ID, STRING_VALUE) VALUES (?, ?, ?, ?)";

        // エンティティの設定
        TestEntity entity = new TestEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        entity.setDouble_id(2.2D);
        entity.setString_value("string value");

        GenericDAO.insert(entity);
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    /**
     * insert：Entityに主キーを設定せずに実行してエラー
     * 
     * @throws Exception
     */
    @Test
    public void test06() throws Exception {
        // エンティティの設定
        TestEntity entity = new TestEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        // entity.setDouble_id(2.2D);
        entity.setString_value("string value");
        entity.setLong_value(3L);
        entity.setDouble_value(4.4D);
        entity.setUtil_date_value(new java.util.Date(1400735867000L));
        entity.setSql_timestamp_value(new java.sql.Timestamp(1400835867000L));
        entity.setSql_date_value(new java.sql.Date(1400835867000L));
        entity.setSql_time_value(new java.sql.Time(1400835867000L));

        try {
            GenericDAO.insert(entity);
        } catch (jp.gr.naoco.db.exception.QueryRenderingException e) {
            System.out.println(e.getMessage());
            return;
        }
        Assert.fail();
    }

    /**
     * insertBatch(List<? extends AbstractEntity>)
     * 
     * @throws Exception
     */
    @Test
    public void test07() throws Exception {
        // 設定値の宣言
        final String sql = "INSERT INTO TEST_TABLE(STRING_ID, LONG_ID, DOUBLE_ID, STRING_VALUE, LONG_VALUE, DOUBLE_VALUE, UTIL_DATE_VALUE, SQL_TIMESTAMP_VALUE, SQL_DATE_VALUE, SQL_TIME_VALUE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // エンティティの設定
        List<TestEntity> entityList = new ArrayList<TestEntity>(3);
        {
            TestEntity entity = new TestEntity();
            entity.setString_id("string id 01");
            entity.setLong_id(11L);
            entity.setDouble_id(12.2D);
            entity.setString_value("string value 01");
            entity.setLong_value(3L);
            entity.setDouble_value(4.4D);
            entity.setUtil_date_value(new java.util.Date(1400735867000L));
            entity.setSql_timestamp_value(new java.sql.Timestamp(1400835867000L));
            entity.setSql_date_value(new java.sql.Date(1400835867000L));
            entity.setSql_time_value(new java.sql.Time(1400835867000L));
            entityList.add(entity);
        }
        {
            TestEntity entity = new TestEntity();
            entity.setString_id("string id 02");
            entity.setLong_id(21L);
            entity.setDouble_id(22.2D);
            entity.setString_value("string value 02");
            entity.setLong_value(3L);
            entity.setDouble_value(4.4D);
            entity.setUtil_date_value(new java.util.Date(1400735867000L));
            entity.setSql_timestamp_value(new java.sql.Timestamp(1400835867000L));
            entity.setSql_date_value(new java.sql.Date(1400835867000L));
            entity.setSql_time_value(new java.sql.Time(1400835867000L));
            entityList.add(entity);
        }
        {
            TestEntity entity = new TestEntity();
            entity.setString_id("string id 03");
            entity.setLong_id(31L);
            entity.setDouble_id(32.2D);
            entity.setString_value("string value 03");
            entityList.add(entity);
        }

        GenericDAO.insertBatch(entityList);
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    /**
     * startInsertBatch(AbstractEntit>)
     * 
     * @throws Exception
     */
    @Test
    public void test08() throws Exception {
        // 設定値の宣言
        final String sql = "INSERT INTO TEST_TABLE(STRING_ID, LONG_ID, DOUBLE_ID, STRING_VALUE, LONG_VALUE, DOUBLE_VALUE, UTIL_DATE_VALUE, SQL_TIMESTAMP_VALUE, SQL_DATE_VALUE, SQL_TIME_VALUE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // エンティティの設定
        GenericDAO.BatchInsert batch = null;
        {
            TestEntity entity = new TestEntity();
            entity.setString_id("string id 01");
            entity.setLong_id(11L);
            entity.setDouble_id(12.2D);
            entity.setString_value("string value 01");
            entity.setLong_value(3L);
            entity.setDouble_value(4.4D);
            entity.setUtil_date_value(new java.util.Date(1400735867000L));
            entity.setSql_timestamp_value(new java.sql.Timestamp(1400835867000L));
            entity.setSql_date_value(new java.sql.Date(1400835867000L));
            entity.setSql_time_value(new java.sql.Time(1400835867000L));
            batch = GenericDAO.startInsertBatch(entity);
            batch.addBatch(entity);
        }
        {
            TestEntity entity = new TestEntity();
            entity.setString_id("string id 02");
            entity.setLong_id(21L);
            entity.setDouble_id(22.2D);
            entity.setString_value("string value 02");
            entity.setLong_value(3L);
            entity.setDouble_value(4.4D);
            entity.setUtil_date_value(new java.util.Date(1400735867000L));
            entity.setSql_timestamp_value(new java.sql.Timestamp(1400835867000L));
            entity.setSql_date_value(new java.sql.Date(1400835867000L));
            entity.setSql_time_value(new java.sql.Time(1400835867000L));
            batch.addBatch(entity);
        }
        {
            TestEntity entity = new TestEntity();
            entity.setString_id("string id 03");
            entity.setLong_id(31L);
            entity.setDouble_id(32.2D);
            entity.setString_value("string value 03");
            batch.addBatch(entity);
        }

        batch.executeBatch();
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    /**
     * delete：正常系
     * 
     * @throws Exception
     */
    @Test
    public void test09() throws Exception {
        // 設定値の宣言
        final String sql = "DELETE FROM TEST_TABLE WHERE STRING_ID=? AND LONG_ID=? AND DOUBLE_ID=?";

        // エンティティの設定
        TestEntity entity = new TestEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        entity.setDouble_id(2.2D);

        GenericDAO.delete(entity);
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    /**
     * delete：異常系
     * 
     * @throws Exception
     */
    @Test
    public void test10() throws Exception {
        // エンティティの設定
        TestEntity entity = new TestEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        // entity.setDouble_id(2.2D);

        try {
            GenericDAO.delete(entity);
        } catch (jp.gr.naoco.db.exception.QueryRenderingException e) {
            System.out.println(e.getMessage());
            return;
        }
    }

    /**
     * find(AbstractEntity)：正常系
     * 
     * @throws Exception
     */
    @Test
    public void test11() throws Exception {
        // 設定値の宣言
        final String sql = "SELECT STRING_ID, LONG_ID, DOUBLE_ID, STRING_VALUE, LONG_VALUE, DOUBLE_VALUE, UTIL_DATE_VALUE, SQL_TIMESTAMP_VALUE, SQL_DATE_VALUE, SQL_TIME_VALUE FROM TEST_TABLE WHERE STRING_ID=? AND LONG_ID=? AND DOUBLE_ID=?";

        // エンティティの設定
        TestEntity entity = new TestEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        entity.setDouble_id(2.2D);

        GenericDAO.find(entity);
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    /**
     * find(AbstractEntity)：異常系
     * 
     * @throws Exception
     */
    @Test
    public void test12() throws Exception {
        // エンティティの設定
        TestEntity entity = new TestEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        // entity.setDouble_id(2.2D);

        try {
            GenericDAO.find(entity);
        } catch (jp.gr.naoco.db.exception.QueryRenderingException e) {
            System.out.println(e.getMessage());
            return;
        }
    }

    /**
     * find(AbstractEntity, boolean)：正常系
     * 
     * @throws Exception
     */
    @Test
    public void test13() throws Exception {
        // 設定値の宣言
        final String sql = "SELECT STRING_ID, LONG_ID, DOUBLE_ID, STRING_VALUE, LONG_VALUE, DOUBLE_VALUE, UTIL_DATE_VALUE, SQL_TIMESTAMP_VALUE, SQL_DATE_VALUE, SQL_TIME_VALUE FROM TEST_TABLE WHERE STRING_ID=? AND LONG_ID=? AND DOUBLE_ID=? FOR UPDATE";

        // エンティティの設定
        TestEntity entity = new TestEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        entity.setDouble_id(2.2D);

        GenericDAO.find(entity, true);
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    /**
     * findAll：正常系
     * 
     * @throws Exception
     */
    @Test
    public void test14() throws Exception {
        // 設定値の宣言
        final String sql = "SELECT STRING_ID, LONG_ID, DOUBLE_ID, STRING_VALUE, LONG_VALUE, DOUBLE_VALUE, UTIL_DATE_VALUE, SQL_TIMESTAMP_VALUE, SQL_DATE_VALUE, SQL_TIME_VALUE FROM TEST_TABLE ORDER BY STRING_ID ASC, LONG_ID ASC, DOUBLE_ID ASC";

        GenericDAO.findAll(TestEntity.class);
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    /**
     * executeSelect：正常系
     * 
     * @throws Exception
     */
    @Test
    public void test15() throws Exception {
        // 設定値の宣言
        final String sqlFile = GenericDAOTest01.class.getName().replaceAll("\\.", "/") + "_test15.sql";
        final String sql01 = "SELECT /*+ HINT BODY */ STRING_ID, LONG_ID, DOUBLE_ID, STRING_VALUE, UTIL_DATE_VALUE FROM TEST_TABLE WHERE STRING_ID = ? AND LONG_ID = ? AND DOUBLE_ID = ? AND UTIL_DATE_VALUE < ? AND SQL_DATE_VALUE < ? ORDER BY STRING_ID, LONG_ID, DOUBLE_ID";
        final String sql02 = "SELECT /*+ HINT BODY */ STRING_ID, LONG_ID, DOUBLE_ID, STRING_VALUE, UTIL_DATE_VALUE FROM TEST_TABLE WHERE STRING_ID = ? AND LONG_ID = ? AND DOUBLE_ID = ? AND SQL_TIMESTAMP_VALUE < ? ORDER BY STRING_ID, LONG_ID, DOUBLE_ID";

        // キーの設定
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("string_id", "STRING ID COND 01");
        map.put("long_id", 100L);
        map.put("double_id", 200.1D);
        map.put("date_value", new java.util.Date(1401135867000L));
        map.put("isUtilDate", true);
        map.put("isSqlTimestamp", false);

        GenericDAO.executeSelect(sqlFile, map, TestEntity.class);
        Assert.assertEquals(sql01, DummyPreparedStatement.lastSql());

        // キーの設定
        map = new HashMap<String, Object>();
        map.put("string_id", "STRING ID COND 02");
        map.put("long_id", 300L);
        map.put("double_id", 400.1D);
        map.put("date_value", new java.util.Date(1401135867000L));
        map.put("isUtilDate", false);
        map.put("isSqlTimestamp", true);

        GenericDAO.executeSelect(sqlFile, map, TestEntity.class);
        Assert.assertEquals(sql02, DummyPreparedStatement.lastSql());
    }

    /**
     * executeSelectSequential：正常系
     * 
     * @throws Exception
     */
    @Test
    public void test16() throws Exception {
        // 設定値の宣言
        final String sqlFile = GenericDAOTest01.class.getName().replaceAll("\\.", "/") + "_test16.sql";
        final String sql01 = "SELECT /*+ HINT BODY */ STRING_ID, LONG_ID, DOUBLE_ID, STRING_VALUE, UTIL_DATE_VALUE FROM TEST_TABLE WHERE STRING_ID = ? AND LONG_ID = ? AND DOUBLE_ID = ? AND UTIL_DATE_VALUE < ? AND SQL_DATE_VALUE < ? ORDER BY STRING_ID, LONG_ID, DOUBLE_ID";
        final String sql02 = "SELECT /*+ HINT BODY */ STRING_ID, LONG_ID, DOUBLE_ID, STRING_VALUE, UTIL_DATE_VALUE FROM TEST_TABLE WHERE STRING_ID = ? AND LONG_ID = ? AND DOUBLE_ID = ? AND UTIL_DATE_VALUE < ? AND SQL_TIMESTAMP_VALUE < ? ORDER BY STRING_ID, LONG_ID, DOUBLE_ID";

        // キーの設定
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("string_id", "STRING ID COND 01");
        map.put("long_id", 100L);
        map.put("double_id", 200.1D);
        map.put("date_value", new java.util.Date(1401135867000L));
        map.put("isSqlTimestamp", false);

        GenericDAO.executeSelectSequential(sqlFile, map, TestEntity.class);
        Assert.assertEquals(sql01, DummyPreparedStatement.lastSql());

        // キーの設定
        map = new HashMap<String, Object>();
        map.put("string_id", "STRING ID COND 02");
        map.put("long_id", 300L);
        map.put("double_id", 400.1D);
        map.put("date_value", new java.util.Date(1401135867000L));
        map.put("isSqlTimestamp", true);

        GenericDAO.executeSelectSequential(sqlFile, map, TestEntity.class);
        Assert.assertEquals(sql02, DummyPreparedStatement.lastSql());
    }

    /**
     * executeSelectCount：正常系
     * 
     * @throws Exception
     */
    @Test
    public void test17() throws Exception {
        // 設定値の宣言
        final String sqlFile = GenericDAOTest01.class.getName().replaceAll("\\.", "/") + "_test17.sql";
        final String sql01 = "SELECT COUNT(*) FROM TEST_TABLE WHERE STRING_ID = ? AND LONG_ID = ? AND DOUBLE_ID = ? AND UTIL_DATE_VALUE < ? ORDER BY STRING_ID, LONG_ID, DOUBLE_ID";
        final String sql02 = "SELECT COUNT(*) FROM TEST_TABLE WHERE STRING_ID = ? AND LONG_ID = ? AND DOUBLE_ID = ? AND SQL_TIMESTAMP_VALUE < ? ORDER BY STRING_ID, LONG_ID, DOUBLE_ID";
        final String sql03 = "SELECT COUNT(*) FROM TEST_TABLE WHERE STRING_ID = ? AND LONG_ID = ? AND DOUBLE_ID = ? AND SQL_DATE_VALUE < ? ORDER BY STRING_ID, LONG_ID, DOUBLE_ID";
        final String sql04 = "SELECT COUNT(*) FROM TEST_TABLE WHERE STRING_ID = ? AND LONG_ID = ? AND DOUBLE_ID = ? ORDER BY STRING_ID, LONG_ID, DOUBLE_ID";

        // キーの設定
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("string_id", "STRING ID COND 01");
        map.put("long_id", 100L);
        map.put("double_id", 200.1D);
        map.put("date_value", new java.util.Date(1401135867000L));
        map.put("isUtilDate", true);
        map.put("isSqlTimestamp", false);
        map.put("isSqlDate", false);

        GenericDAO.executeSelectSequential(sqlFile, map, TestEntity.class);
        Assert.assertEquals(sql01, DummyPreparedStatement.lastSql());

        // キーの設定
        map = new HashMap<String, Object>();
        map.put("string_id", "STRING ID COND 02");
        map.put("long_id", 300L);
        map.put("double_id", 400.1D);
        map.put("date_value", new java.util.Date(1401135867000L));
        map.put("isUtilDate", false);
        map.put("isSqlTimestamp", true);
        map.put("isSqlDate", false);

        GenericDAO.executeSelectSequential(sqlFile, map, TestEntity.class);
        Assert.assertEquals(sql02, DummyPreparedStatement.lastSql());

        // キーの設定
        map = new HashMap<String, Object>();
        map.put("string_id", "STRING ID COND 02");
        map.put("long_id", 500L);
        map.put("double_id", 600.1D);
        map.put("date_value", new java.util.Date(1401235867000L));
        map.put("isUtilDate", false);
        map.put("isSqlTimestamp", false);
        map.put("isSqlDate", true);

        GenericDAO.executeSelectSequential(sqlFile, map, TestEntity.class);
        Assert.assertEquals(sql03, DummyPreparedStatement.lastSql());

        // キーの設定
        map = new HashMap<String, Object>();
        map.put("string_id", "STRING ID COND 02");
        map.put("long_id", 700L);
        map.put("double_id", 800.1D);
        map.put("date_value", new java.util.Date(1401335867000L));
        map.put("isUtilDate", false);
        map.put("isSqlTimestamp", false);
        map.put("isSqlDate", false);

        GenericDAO.executeSelectSequential(sqlFile, map, TestEntity.class);
        Assert.assertEquals(sql04, DummyPreparedStatement.lastSql());
    }

    /**
     * executeUpdate：正常系
     * 
     * @throws Exception
     */
    @Test
    public void test18() throws Exception {
        // 設定値の宣言
        final String sqlFile = GenericDAOTest01.class.getName().replaceAll("\\.", "/") + "_test18.sql";
        final String sql01 = "UPDATE TEST_TABLE SET UTIL_DATE_VALUE = ?, SQL_TIMESTAMP_VALUE = ?, SQL_DATE_VALUE = ? WHERE STRING_ID = ? AND LONG_ID = ? AND DOUBLE_ID = ?";

        // キーの設定
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("string_id", "STRING ID COND 01");
        map.put("long_id", 100L);
        map.put("double_id", 200.1D);
        map.put("date_value", new java.util.Date(1401135867000L));

        GenericDAO.executeUpdate(sqlFile, map);
        Assert.assertEquals(sql01, DummyPreparedStatement.lastSql());
    }

    /**
     * executeUpdate：正常系
     * 
     * @throws Exception
     */
    @Test
    public void test19() throws Exception {
        // 設定値の宣言
        final String sequenceName = "TEST_SEQ";
        final String sql01 = "SELECT TEST_SEQ.NEXTVAL FROM DUAL";

        GenericDAO.nextvalSequence(sequenceName);
        Assert.assertEquals(sql01, DummyPreparedStatement.lastSql());
    }

    /**
     * update：サブクラスEntityにすべての値を設定
     * 
     * @throws Exception
     */
    @Test
    public void test20() throws Exception {
        // 設定値の宣言
        final String sql = "UPDATE SUB_TABLE SET ADDED_VALUE=?, STRING_VALUE=?, LONG_VALUE=?, DOUBLE_VALUE=?, UTIL_DATE_VALUE=?, SQL_TIMESTAMP_VALUE=?, SQL_DATE_VALUE=?, SQL_TIME_VALUE=? WHERE ADDED_ID=? AND STRING_ID=? AND LONG_ID=? AND DOUBLE_ID=?";

        // エンティティの設定
        SubEntity entity = new SubEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        entity.setDouble_id(2.2D);
        entity.setString_value("string value");
        entity.setLong_value(3L);
        entity.setDouble_value(4.4D);
        entity.setUtil_date_value(new java.util.Date(1400735867000L));
        entity.setSql_timestamp_value(new java.sql.Timestamp(1400835867000L));
        entity.setSql_date_value(new java.sql.Date(1400835867000L));
        entity.setSql_time_value(new java.sql.Time(1400835867000L));

        entity.setAdded_id(123L);
        entity.setAdded_value("added value");

        GenericDAO.update(entity);
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    /**
     * insert：サブクラスEntityにすべての値を設定
     * 
     * @throws Exception
     */
    @Test
    public void test21() throws Exception {
        // 設定値の宣言
        final String sql = "INSERT INTO SUB_TABLE(ADDED_ID, STRING_ID, LONG_ID, DOUBLE_ID, ADDED_VALUE, STRING_VALUE, LONG_VALUE, DOUBLE_VALUE, UTIL_DATE_VALUE, SQL_TIMESTAMP_VALUE, SQL_DATE_VALUE, SQL_TIME_VALUE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // エンティティの設定
        SubEntity entity = new SubEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        entity.setDouble_id(2.2D);
        entity.setString_value("string value");
        entity.setLong_value(3L);
        entity.setDouble_value(4.4D);
        entity.setUtil_date_value(new java.util.Date(1400735867000L));
        entity.setSql_timestamp_value(new java.sql.Timestamp(1400835867000L));
        entity.setSql_date_value(new java.sql.Date(1400835867000L));
        entity.setSql_time_value(new java.sql.Time(1400835867000L));

        entity.setAdded_id(123L);
        entity.setAdded_value("addes value");

        GenericDAO.insert(entity);
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    /**
     * find(AbstractEntity)：サブクラス正常系
     * 
     * @throws Exception
     */
    @Test
    public void test22() throws Exception {
        // 設定値の宣言
        final String sql = "SELECT ADDED_ID, STRING_ID, LONG_ID, DOUBLE_ID, ADDED_VALUE, STRING_VALUE, LONG_VALUE, DOUBLE_VALUE, UTIL_DATE_VALUE, SQL_TIMESTAMP_VALUE, SQL_DATE_VALUE, SQL_TIME_VALUE FROM SUB_TABLE WHERE ADDED_ID=? AND STRING_ID=? AND LONG_ID=? AND DOUBLE_ID=?";

        // エンティティの設定
        SubEntity entity = new SubEntity();
        entity.setString_id("string id");
        entity.setLong_id(1L);
        entity.setDouble_id(2.2D);
        entity.setAdded_id(123L);

        GenericDAO.find(entity);
        Assert.assertEquals(sql, DummyPreparedStatement.lastSql());
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes

    @Table(name = "TEST_TABLE")
    public static class TestEntity extends AbstractEntity {
        @Id(name = "STRING_ID")
        private String string_id;
        @Id(name = "LONG_ID")
        private long long_id;
        @Id(name = "DOUBLE_ID")
        private double double_id;
        @Column(name = "STRING_VALUE")
        private String string_value;
        @Column(name = "LONG_VALUE")
        private long long_value;
        @Column(name = "DOUBLE_VALUE")
        private double double_value;
        @Column(name = "UTIL_DATE_VALUE")
        private java.util.Date util_date_value;
        @Column(name = "SQL_TIMESTAMP_VALUE")
        private java.sql.Timestamp sql_timestamp_value;
        @Column(name = "SQL_DATE_VALUE")
        private java.sql.Date sql_date_value;
        @Column(name = "SQL_TIME_VALUE")
        private java.sql.Time sql_time_value;

        public String getString_id() {
            return string_id;
        }

        public void setString_id(String string_id) {
            this.string_id = string_id;
            setFieldNameSet_.add("STRING_ID");
        }

        public long getLong_id() {
            return long_id;
        }

        public void setLong_id(long long_id) {
            this.long_id = long_id;
            setFieldNameSet_.add("LONG_ID");
        }

        public double getDouble_id() {
            return double_id;
        }

        public void setDouble_id(double double_id) {
            this.double_id = double_id;
            setFieldNameSet_.add("DOUBLE_ID");
        }

        public String getString_value() {
            return string_value;
        }

        public void setString_value(String string_value) {
            this.string_value = string_value;
            setFieldNameSet_.add("STRING_VALUE");
        }

        public long getLong_value() {
            return long_value;
        }

        public void setLong_value(long long_value) {
            this.long_value = long_value;
            setFieldNameSet_.add("LONG_VALUE");
        }

        public double getDouble_value() {
            return double_value;
        }

        public void setDouble_value(double double_value) {
            this.double_value = double_value;
            setFieldNameSet_.add("DOUBLE_VALUE");
        }

        public java.util.Date getUtil_date_value() {
            return util_date_value;
        }

        public void setUtil_date_value(java.util.Date util_date_value) {
            this.util_date_value = util_date_value;
            setFieldNameSet_.add("UTIL_DATE_VALUE");
        }

        public java.sql.Timestamp getSql_timestamp_value() {
            return sql_timestamp_value;
        }

        public void setSql_timestamp_value(java.sql.Timestamp sql_timestamp_value) {
            this.sql_timestamp_value = sql_timestamp_value;
            setFieldNameSet_.add("SQL_TIMESTAMP_VALUE");
        }

        public java.sql.Date getSql_date_value() {
            return sql_date_value;
        }

        public void setSql_date_value(java.sql.Date sql_date_value) {
            this.sql_date_value = sql_date_value;
            setFieldNameSet_.add("SQL_DATE_VALUE");
        }

        public java.sql.Time getSql_time_value() {
            return sql_time_value;
        }

        public void setSql_time_value(java.sql.Time sql_time_value) {
            this.sql_time_value = sql_time_value;
            setFieldNameSet_.add("SQL_TIME_VALUE");
        }
    }

    // ///////////////////////

    @Table(name = "SUB_TABLE")
    public static class SubEntity extends TestEntity {
        @Id(name = "ADDED_ID")
        private Long added_id;

        @Column(name = "ADDED_VALUE")
        private String added_value;

        public Long getAdded_id() {
            return added_id;
        }

        public void setAdded_id(Long added_id) {
            this.added_id = added_id;
            setFieldNameSet_.add("ADDED_ID");
        }

        public String getAdded_value() {
            return added_value;
        }

        public void setAdded_value(String added_value) {
            this.added_value = added_value;
            setFieldNameSet_.add("ADDED_VALUE");
        }
    }
}
