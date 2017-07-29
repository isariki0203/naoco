package jp.gr.naoco.db.sql;

import java.util.HashMap;
import java.util.LinkedList;

import jp.gr.naoco.core.NaocoCoreFacade;
import jp.gr.naoco.core.NaocoCoreInitializer;
import jp.gr.naoco.db.sql.TemplateAnalyzer;
import jp.gr.naoco.db.sql.elem.SqlElem;
import jp.gr.naoco.sample.dummy.DummyConnection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TemplateAnalyzerTest01 {

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Test Preparations

    @Before
    public void setup() throws Exception {
        DummyConnection.clearIdCount();
        NaocoCoreInitializer.initialize(TemplateAnalyzerTest01.class.getName() + "_test", null);
        NaocoCoreFacade.startTransaction("java:comp/env/jdbc/test01");
    }

    @After
    public void teardown() throws Exception {
        NaocoCoreFacade.commitTransaction();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Test Cases

    // ////////////////////////////////////////////////////
    // コメント関連試験

    /**
     * 正常ケース：クエリ本文１行のみ
     */
    @Test
    public void test001() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test001", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：コメント（\/* ... *\/）１行のみ
     */
    @Test
    public void test002() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("/* comment */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test002", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：コメント（-- ）１行のみ
     */
    @Test
    public void test003() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("-- comment");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test003", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数（５）行
     */
    @Test
    public void test004() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN1, COLUMN2");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE COLUMN1='HOGE'");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test004", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN1, COLUMN2 FROM TABLE WHERE COLUMN1='HOGE' ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：先頭コメントあり、クエリ本文複数（５）行
     */
    @Test
    public void test005() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("/* HEADER COMMENT */");
        sql.add("SELECT");
        sql.add("COLUMN1, COLUMN2");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE COLUMN1='HOGE'");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test005", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN1, COLUMN2 FROM TABLE WHERE COLUMN1='HOGE' ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：行中コメントあり、クエリ本文複数（５）行
     */
    @Test
    public void test006() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT -- SELECT");
        sql.add("COLUMN1, COLUMN2 -- COLUMNS");
        sql.add("FROM -- FROM");
        sql.add("TABLE -- TABLES");
        sql.add("WHERE COLUMN1='HOGE' -- CONDS");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test006", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN1, COLUMN2 FROM TABLE WHERE COLUMN1='HOGE' ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：末尾コメントあり、クエリ本文複数（５）行
     */
    @Test
    public void test007() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN1, COLUMN2");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE COLUMN1='HOGE'");
        sql.add("/* FOOTER COMMENT */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test007", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN1, COLUMN2 FROM TABLE WHERE COLUMN1='HOGE' ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：先頭、行中、末尾コメントあり、クエリ本文複数（５）行
     */
    @Test
    public void test008() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("/* HEADER COMMENT */");
        sql.add("SELECT -- SELECT");
        sql.add("COLUMN1, COLUMN2 -- COLUMNS");
        sql.add("/* BODY COMMENT 1");
        sql.add("BODY COMMENT 2");
        sql.add("BODY COMMENT 3 */");
        sql.add("FROM -- FROM");
        sql.add("TABLE -- TABLES");
        sql.add("WHERE COLUMN1='HOGE' -- CONDS");
        sql.add("/* FOOTER COMMENT */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test008", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN1, COLUMN2 FROM TABLE WHERE COLUMN1='HOGE' ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：先頭（複数行）、行中、末尾（複数行）コメントあり、クエリ本文複数（５）行
     */
    @Test
    public void test009() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("/* HEADER COMMENT1");
        sql.add("HEADER COMMENT2");
        sql.add("HEADER COMMENT3 */-- add comment");
        sql.add("SELECT -- SELECT");
        sql.add("COLUMN1, COLUMN2 -- COLUMNS");
        sql.add("/* BODY COMMENT 1");
        sql.add("BODY COMMENT 2");
        sql.add("BODY COMMENT 3 */");
        sql.add("FROM -- FROM");
        sql.add("TABLE -- TABLES");
        sql.add("WHERE COLUMN1='HOGE' -- CONDS");
        sql.add("/* FOOTER COMMENT1");
        sql.add("FOOTER COMMENT2");
        sql.add("FOOTER COMMENT3 */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test009", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN1, COLUMN2 FROM TABLE WHERE COLUMN1='HOGE' ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    // ////////////////////////////////////////////////////
    // 可変部分関連試験

    /**
     * 正常ケース：クエリ本文１行のみ、可変部分1件
     */
    @Test
    public void test100() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE COLUMN = #column#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test100", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("column", "value");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE COLUMN =  ? ", builder.toString());
        Assert.assertEquals(1, parameterList.size());
        Assert.assertEquals("value", parameterList.get(0));
    }

    /**
     * 正常ケース：クエリ本文１行のみ、可変部分3件
     */
    @Test
    public void test101() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE COLUMN1 = #column1# AND COLUMN2 = #column2# AND COLUMN3 = #column3#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test101", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("column1", "value1");
        variableMap.put("column2", "value2");
        variableMap.put("column3", "value3");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE COLUMN1 =  ?  AND COLUMN2 =   ?  AND COLUMN3 =   ? ",
                builder.toString());
        Assert.assertEquals(3, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
    }

    /**
     * 正常ケース：クエリ本文１行のみ、可変部分3件、3件中2件は同じキー名称
     */
    @Test
    public void test102() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE COLUMN1 = #column1# AND COLUMN2 = #column2# AND COLUMN3 = #column1#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test102", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("column1", "value1");
        variableMap.put("column2", "value2");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE COLUMN1 =  ?  AND COLUMN2 =   ?  AND COLUMN3 =   ? ",
                builder.toString());
        Assert.assertEquals(3, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value1", parameterList.get(2));
    }

    /**
     * 正常ケース：クエリ本文１行のみ、可変部分3件、可変部分1/3件はマップで未指定
     */
    @Test
    public void test103() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE COLUMN1 = #column1# AND COLUMN2 = #column2# AND COLUMN3 = #column3#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test103", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("column2", "value2");
        variableMap.put("column3", "value3");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE COLUMN1 =  ?  AND COLUMN2 =   ?  AND COLUMN3 =   ? ",
                builder.toString());
        Assert.assertEquals(3, parameterList.size());
        Assert.assertEquals(null, parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
    }

    /**
     * 正常ケース：クエリ本文１行のみ、可変部分3件、可変部分1/3件はIterable指定
     */
    @Test
    public void test104() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE COLUMN1 = #column1# AND COLUMN2 = #column2# AND COLUMN3 IN #column3#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test104", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("column1", "value1");
        variableMap.put("column2", "value2");
        LinkedList<String> variable3List = new LinkedList<String>();
        variable3List.add("value3");
        variable3List.add("value4");
        variable3List.add("value5");
        variableMap.put("column3", variable3List);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE COLUMN1 =  ?  AND COLUMN2 =   ?  AND COLUMN3 IN   (?,?,?) ",
                builder.toString());
        Assert.assertEquals(5, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
        Assert.assertEquals("value4", parameterList.get(3));
        Assert.assertEquals("value5", parameterList.get(4));
    }

    /**
     * 正常ケース：クエリ本文5行のみ、コメントあり、可変部分5件
     */
    @Test
    public void test105() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("/* HEADER COMMENT1");
        sql.add("HEADER COMMENT2");
        sql.add("HEADER COMMENT3 */");
        sql.add("SELECT -- select");
        sql.add("COLUMN1, COLUMN2, COLUMN3 -- columns");
        sql.add("FROM -- from");
        sql.add("TABLE -- table");
        sql.add("WHERE COLUMN1=#column1# AND COLUMN2=#column2# AND COLUMN3=#column3# AND COLUMN4=#column4# AND COLUMN5=#column5 -- cond");
        sql.add("/* FOOTER COMMENT1");
        sql.add("FOOTER COMMENT2");
        sql.add("FOOTER COMMENT3 */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test105", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("column1", "value1");
        variableMap.put("column2", "value2");
        variableMap.put("column3", "value3");
        variableMap.put("column4", "value4");
        variableMap.put("column5", "value5");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN1, COLUMN2, COLUMN3 FROM TABLE WHERE COLUMN1= ?  AND COLUMN2=  ?  AND COLUMN3=  ?  AND COLUMN4=  ?  AND COLUMN5=  ? ",
                builder.toString());
        Assert.assertEquals(5, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
        Assert.assertEquals("value4", parameterList.get(3));
        Assert.assertEquals("value5", parameterList.get(4));
    }

    // ////////////////////////////////////////////////////
    // 条件部分関連試験

    /**
     * 正常ケース：クエリ本文１行のみ、条件部分1件、trueにより表示
     */
    @Test
    public void test200() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE /* IF #cond1# */ COLUMN IS NULL /* ENDIF #cond1# */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test200", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE COLUMN IS NULL ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文１行のみ、条件部分1件、falseにより非表示
     */
    @Test
    public void test201() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE /* IF #cond1# */ COLUMN IS NULL /* ENDIF #cond1# */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test201", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分1件、未指定により非表示
     */
    @Test
    public void test202() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE /* IF #cond1# */ COLUMN IS NULL /* ENDIF #cond1# */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test202", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分1件、trueにより表示
     */
    @Test
    public void test203() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test203", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE COLUMN1 IS NULL ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分1件、falseにより非表示
     */
    @Test
    public void test204() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test204", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分1件、未指定により非表示
     */
    @Test
    public void test205() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test205", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分3件、true、false、未指定をそれぞれ指定
     */
    @Test
    public void test206() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN2 IS NULL");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN3 IS NULL");
        sql.add("-- ENDIF #cond3#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test206", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond2", false);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE COLUMN1 IS NULL ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分3件、false、未指定、trueをそれぞれ指定
     */
    @Test
    public void test207() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN2 IS NULL");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN3 IS NULL");
        sql.add("-- ENDIF #cond3#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test207", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        variableMap.put("cond3", true);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE AND COLUMN3 IS NULL ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分3件、未指定、true、falseをそれぞれ指定
     */
    @Test
    public void test208() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN2 IS NULL");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN3 IS NULL");
        sql.add("-- ENDIF #cond3#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test208", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond2", true);
        variableMap.put("cond3", false);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE AND COLUMN2 IS NULL ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分5件、条件部分3/5は同じキー文字列でtrueを指定
     */
    @Test
    public void test209() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN2 IS NULL");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN3 IS NULL");
        sql.add("-- ENDIF #cond3#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN4 IS NULL");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN5 IS NULL");
        sql.add("-- ENDIF #cond2#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test209", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        variableMap.put("cond2", true);
        variableMap.put("cond3", false);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE AND COLUMN2 IS NULL AND COLUMN4 IS NULL AND COLUMN5 IS NULL ",
                builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分5件、条件部分3/5は同じキー文字列でfalseを指定
     */
    @Test
    public void test210() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN2 IS NULL");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN3 IS NULL");
        sql.add("-- ENDIF #cond3#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN4 IS NULL");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN5 IS NULL");
        sql.add("-- ENDIF #cond2#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test210", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond2", false);
        variableMap.put("cond3", true);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE COLUMN1 IS NULL AND COLUMN3 IS NULL ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分5件、条件部分3/5は同じキー文字列で未指定
     */
    @Test
    public void test211() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN2 IS NULL");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN3 IS NULL");
        sql.add("-- ENDIF #cond3#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN4 IS NULL");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN5 IS NULL");
        sql.add("-- ENDIF #cond2#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test211", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond3", true);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE COLUMN1 IS NULL AND COLUMN3 IS NULL ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分5件、条件部分3/5は同じキー文字列で未指定
     */
    @Test
    public void test212() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN2 IS NULL");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN3 IS NULL");
        sql.add("-- ENDIF #cond3#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN4 IS NULL");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN5 IS NULL");
        sql.add("-- ENDIF #cond2#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test212", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond3", true);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE COLUMN1 IS NULL AND COLUMN3 IS NULL ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文１行のみ、条件部分1件、trueにより表示、条件部分内に可変部分1件
     */
    @Test
    public void test213() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE /* IF #cond1# */ COLUMN = #var1# /* ENDIF #cond1# */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test213", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("var1", "value1");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE COLUMN =  ? ", builder.toString());
        Assert.assertEquals(1, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
    }

    /**
     * 正常ケース：クエリ本文１行のみ、条件部分1件、falseにより表示、条件部分内に可変部分1件
     */
    @Test
    public void test214() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE /* IF #cond1# */ COLUMN = #var1# /* ENDIF #cond1# */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test214", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        variableMap.put("var1", "value1");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文１行のみ、条件部分1件、未指定により表示、条件部分内に可変部分1件
     */
    @Test
    public void test215() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE /* IF #cond1# */ COLUMN = #var1# /* ENDIF #cond1# */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test215", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        variableMap.put("var1", "value1");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分3件、各条件部分に可変部分を3件、条件部分は全てtrue
     */
    @Test
    public void test216() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("AND COLUMN2 = #var2# -- column2");
        sql.add("AND COLUMN3 = #var3# -- column3");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN4 = #var4# -- column4");
        sql.add("AND COLUMN5 = #var5# -- column5");
        sql.add("AND COLUMN6 = #var6# -- column6");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN7 = #var7# -- column7");
        sql.add("AND COLUMN8 = #var8# -- column8");
        sql.add("AND COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond3#");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test216", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond2", true);
        variableMap.put("cond3", true);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COLUMN1 =  ? AND COLUMN2 =  ? AND COLUMN3 =  ? AND COLUMN4 =  ? AND COLUMN5 =  ? AND COLUMN6 =  ? AND COLUMN7 =  ? AND COLUMN8 =  ? AND COLUMN9 =  ? ",
                builder.toString());
        Assert.assertEquals(9, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
        Assert.assertEquals("value4", parameterList.get(3));
        Assert.assertEquals("value5", parameterList.get(4));
        Assert.assertEquals("value6", parameterList.get(5));
        Assert.assertEquals("value7", parameterList.get(6));
        Assert.assertEquals("value8", parameterList.get(7));
        Assert.assertEquals("value9", parameterList.get(8));
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分3件、各条件部分に可変部分を3件、条件部分は1件のみtrue
     */
    @Test
    public void test217() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("AND COLUMN2 = #var2# -- column2");
        sql.add("AND COLUMN3 = #var3# -- column3");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN4 = #var4# -- column4");
        sql.add("AND COLUMN5 = #var5# -- column5");
        sql.add("AND COLUMN6 = #var6# -- column6");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN7 = #var7# -- column7");
        sql.add("AND COLUMN8 = #var8# -- column8");
        sql.add("AND COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond3#");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test217", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        variableMap.put("cond2", false);
        variableMap.put("cond3", true);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE AND COLUMN7 =  ? AND COLUMN8 =  ? AND COLUMN9 =  ? ",
                builder.toString());
        Assert.assertEquals(3, parameterList.size());
        Assert.assertEquals("value7", parameterList.get(0));
        Assert.assertEquals("value8", parameterList.get(1));
        Assert.assertEquals("value9", parameterList.get(2));
    }

    /**
     * 正常ケース：クエリ本文１行のみ、条件部分1件、trueによりELSE句を非表示
     */
    @Test
    public void test218() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE /* IF #cond1# *//* ELSE #cond1# */ COLUMN IS NULL /* ENDIF #cond1# */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test218", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文１行のみ、条件部分1件、falseによりELSE句を表示
     */
    @Test
    public void test219() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE /* IF #cond1# *//* ELSE #cond1# */ COLUMN IS NULL /* ENDIF #cond1# */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test219", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE COLUMN IS NULL ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分1件、未指定により非表示
     */
    @Test
    public void test220() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT * FROM TABLE WHERE /* IF #cond1# *//* ELSE #cond1# */ COLUMN IS NULL /* ENDIF #cond1# */");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test220", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分1件、trueによりELSE句を非表示
     */
    @Test
    public void test221() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test221", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分1件、falseによりELSE句を表示
     */
    @Test
    public void test222() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test222", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE COLUMN1 IS NULL ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分1件、未設定によりELSE句を非表示
     */
    @Test
    public void test223() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN1 IS NULL");
        sql.add("-- ENDIF #cond1#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test223", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分3件、各条件部分に可変部分を3件、条件部分は全てfalseでELSE句を表示
     */
    @Test
    public void test224() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("AND COLUMN2 = #var2# -- column2");
        sql.add("AND COLUMN3 = #var3# -- column3");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- IF #cond2#");
        sql.add("-- ELSE #cond2#");
        sql.add("AND COLUMN4 = #var4# -- column4");
        sql.add("AND COLUMN5 = #var5# -- column5");
        sql.add("AND COLUMN6 = #var6# -- column6");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond3#");
        sql.add("-- ELSE #cond3#");
        sql.add("AND COLUMN7 = #var7# -- column7");
        sql.add("AND COLUMN8 = #var8# -- column8");
        sql.add("AND COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond3#");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test224", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        variableMap.put("cond2", false);
        variableMap.put("cond3", false);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COLUMN1 =  ? AND COLUMN2 =  ? AND COLUMN3 =  ? AND COLUMN4 =  ? AND COLUMN5 =  ? AND COLUMN6 =  ? AND COLUMN7 =  ? AND COLUMN8 =  ? AND COLUMN9 =  ? ",
                builder.toString());
        Assert.assertEquals(9, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
        Assert.assertEquals("value4", parameterList.get(3));
        Assert.assertEquals("value5", parameterList.get(4));
        Assert.assertEquals("value6", parameterList.get(5));
        Assert.assertEquals("value7", parameterList.get(6));
        Assert.assertEquals("value8", parameterList.get(7));
        Assert.assertEquals("value9", parameterList.get(8));
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分3件、各条件部分に可変部分を3件、
     * 条件部分はtrue1件でIF句を表示、false1件でELSE句を表示、未設定1件非表示
     */
    @Test
    public void test225() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("AND COLUMN2 = #var2# -- column2");
        sql.add("AND COLUMN3 = #var3# -- column3");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- IF #cond2#");
        sql.add("-- ELSE #cond2#");
        sql.add("AND COLUMN4 = #var4# -- column4");
        sql.add("AND COLUMN5 = #var5# -- column5");
        sql.add("AND COLUMN6 = #var6# -- column6");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond3#");
        sql.add("-- ELSE #cond3#");
        sql.add("AND COLUMN7 = #var7# -- column7");
        sql.add("AND COLUMN8 = #var8# -- column8");
        sql.add("AND COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond3#");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test225", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond2", false);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE AND COLUMN4 =  ? AND COLUMN5 =  ? AND COLUMN6 =  ? ",
                builder.toString());
        Assert.assertEquals(3, parameterList.size());
        Assert.assertEquals("value4", parameterList.get(0));
        Assert.assertEquals("value5", parameterList.get(1));
        Assert.assertEquals("value6", parameterList.get(2));
    }

    /**
     * 正常ケース：クエリ本文複数行、条件部分3件、各条件部分に可変部分を3件、条件部分は1件のみfalseでELSE句を表示
     */
    @Test
    public void test226() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN11 = #var1# -- column1");
        sql.add("AND COLUMN12 = #var2# -- column2");
        sql.add("AND COLUMN13 = #var3# -- column3");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("AND COLUMN2 = #var2# -- column2");
        sql.add("AND COLUMN3 = #var3# -- column3");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN14 = #var4# -- column4");
        sql.add("AND COLUMN15 = #var5# -- column5");
        sql.add("AND COLUMN16 = #var6# -- column6");
        sql.add("-- ELSE #cond2#");
        sql.add("AND COLUMN4 = #var4# -- column4");
        sql.add("AND COLUMN5 = #var5# -- column5");
        sql.add("AND COLUMN6 = #var6# -- column6");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN17 = #var7# -- column7");
        sql.add("AND COLUMN18 = #var8# -- column8");
        sql.add("AND COLUMN19 = #var9# -- column9");
        sql.add("-- ELSE #cond3#");
        sql.add("AND COLUMN7 = #var7# -- column7");
        sql.add("AND COLUMN8 = #var8# -- column8");
        sql.add("AND COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond3#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test226", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond2", false);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COLUMN11 =  ? AND COLUMN12 =  ? AND COLUMN13 =  ? AND COLUMN4 =  ? AND COLUMN5 =  ? AND COLUMN6 =  ? ORDER BY COLUMN1 ASC ",
                builder.toString());
        Assert.assertEquals(6, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
        Assert.assertEquals("value4", parameterList.get(3));
        Assert.assertEquals("value5", parameterList.get(4));
        Assert.assertEquals("value6", parameterList.get(5));
    }

    /**
     * 条件部分を3重ネスト、全てtrueで表示
     */
    @Test
    public void test227() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("AND COLUMN2 = #var2# -- column2");
        sql.add("AND COLUMN3 = #var3# -- column3");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN4 = #var4# -- column4");
        sql.add("AND COLUMN5 = #var5# -- column5");
        sql.add("AND COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN7 = #var7# -- column7");
        sql.add("AND COLUMN8 = #var8# -- column8");
        sql.add("AND COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond3#");
        sql.add("AND COLUMN10 = #var10# -- column7");
        sql.add("AND COLUMN11 = #var11# -- column8");
        sql.add("AND COLUMN12 = #var12# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("AND COLUMN13 = #var13# -- column7");
        sql.add("AND COLUMN14 = #var14# -- column8");
        sql.add("AND COLUMN15 = #var15# -- column9");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test227", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond2", true);
        variableMap.put("cond3", true);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COLUMN1 =  ? AND COLUMN2 =  ? AND COLUMN3 =  ? AND COLUMN4 =  ? AND COLUMN5 =  ? AND COLUMN6 =  ? AND COLUMN7 =  ? AND COLUMN8 =  ? AND COLUMN9 =  ? AND COLUMN10 =  ? AND COLUMN11 =  ? AND COLUMN12 =  ? AND COLUMN13 =  ? AND COLUMN14 =  ? AND COLUMN15 =  ? ORDER BY COLUMN1 ASC ",
                builder.toString());
        Assert.assertEquals(15, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
        Assert.assertEquals("value4", parameterList.get(3));
        Assert.assertEquals("value5", parameterList.get(4));
        Assert.assertEquals("value6", parameterList.get(5));
        Assert.assertEquals("value7", parameterList.get(6));
        Assert.assertEquals("value8", parameterList.get(7));
        Assert.assertEquals("value9", parameterList.get(8));
        Assert.assertEquals("value10", parameterList.get(9));
        Assert.assertEquals("value11", parameterList.get(10));
        Assert.assertEquals("value12", parameterList.get(11));
        Assert.assertEquals("value13", parameterList.get(12));
        Assert.assertEquals("value14", parameterList.get(13));
        Assert.assertEquals("value15", parameterList.get(14));
    }

    /**
     * 条件部分を3重ネスト、ネスト内で最も深い条件部分のみfalse
     */
    @Test
    public void test228() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("AND COLUMN2 = #var2# -- column2");
        sql.add("AND COLUMN3 = #var3# -- column3");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN4 = #var4# -- column4");
        sql.add("AND COLUMN5 = #var5# -- column5");
        sql.add("AND COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN7 = #var7# -- column7");
        sql.add("AND COLUMN8 = #var8# -- column8");
        sql.add("AND COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond3#");
        sql.add("AND COLUMN10 = #var10# -- column7");
        sql.add("AND COLUMN11 = #var11# -- column8");
        sql.add("AND COLUMN12 = #var12# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("AND COLUMN13 = #var13# -- column7");
        sql.add("AND COLUMN14 = #var14# -- column8");
        sql.add("AND COLUMN15 = #var15# -- column9");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test228", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond2", true);
        variableMap.put("cond3", false);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COLUMN1 =  ? AND COLUMN2 =  ? AND COLUMN3 =  ? AND COLUMN4 =  ? AND COLUMN5 =  ? AND COLUMN6 =  ? AND COLUMN10 =  ? AND COLUMN11 =  ? AND COLUMN12 =  ? AND COLUMN13 =  ? AND COLUMN14 =  ? AND COLUMN15 =  ? ORDER BY COLUMN1 ASC ",
                builder.toString());
        Assert.assertEquals(12, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
        Assert.assertEquals("value4", parameterList.get(3));
        Assert.assertEquals("value5", parameterList.get(4));
        Assert.assertEquals("value6", parameterList.get(5));
        Assert.assertEquals("value10", parameterList.get(6));
        Assert.assertEquals("value11", parameterList.get(7));
        Assert.assertEquals("value12", parameterList.get(8));
        Assert.assertEquals("value13", parameterList.get(9));
        Assert.assertEquals("value14", parameterList.get(10));
        Assert.assertEquals("value15", parameterList.get(11));
    }

    /**
     * 条件部分を3重ネスト、ネスト内で最も浅い条件部分のみfalse
     */
    @Test
    public void test229() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("AND COLUMN2 = #var2# -- column2");
        sql.add("AND COLUMN3 = #var3# -- column3");
        sql.add("-- IF #cond2#");
        sql.add("AND COLUMN4 = #var4# -- column4");
        sql.add("AND COLUMN5 = #var5# -- column5");
        sql.add("AND COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("AND COLUMN7 = #var7# -- column7");
        sql.add("AND COLUMN8 = #var8# -- column8");
        sql.add("AND COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond3#");
        sql.add("AND COLUMN10 = #var10# -- column7");
        sql.add("AND COLUMN11 = #var11# -- column8");
        sql.add("AND COLUMN12 = #var12# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("AND COLUMN13 = #var13# -- column7");
        sql.add("AND COLUMN14 = #var14# -- column8");
        sql.add("AND COLUMN15 = #var15# -- column9");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test229", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        variableMap.put("cond2", true);
        variableMap.put("cond3", true);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE ORDER BY COLUMN1 ASC ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 条件部分を3重ネスト、全てfalseでELSE句表示
     */
    @Test
    public void test230() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("AND COLUMN2 = #var2# -- column2");
        sql.add("AND COLUMN3 = #var3# -- column3");
        sql.add("-- IF #cond2#");
        sql.add("-- ELSE #cond2#");
        sql.add("AND COLUMN4 = #var4# -- column4");
        sql.add("AND COLUMN5 = #var5# -- column5");
        sql.add("AND COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("-- ELSE #cond3#");
        sql.add("AND COLUMN7 = #var7# -- column7");
        sql.add("AND COLUMN8 = #var8# -- column8");
        sql.add("AND COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond3#");
        sql.add("AND COLUMN10 = #var10# -- column7");
        sql.add("AND COLUMN11 = #var11# -- column8");
        sql.add("AND COLUMN12 = #var12# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("AND COLUMN13 = #var13# -- column7");
        sql.add("AND COLUMN14 = #var14# -- column8");
        sql.add("AND COLUMN15 = #var15# -- column9");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test230", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        variableMap.put("cond2", false);
        variableMap.put("cond3", false);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COLUMN1 =  ? AND COLUMN2 =  ? AND COLUMN3 =  ? AND COLUMN4 =  ? AND COLUMN5 =  ? AND COLUMN6 =  ? AND COLUMN7 =  ? AND COLUMN8 =  ? AND COLUMN9 =  ? AND COLUMN10 =  ? AND COLUMN11 =  ? AND COLUMN12 =  ? AND COLUMN13 =  ? AND COLUMN14 =  ? AND COLUMN15 =  ? ORDER BY COLUMN1 ASC ",
                builder.toString());
        Assert.assertEquals(15, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
        Assert.assertEquals("value4", parameterList.get(3));
        Assert.assertEquals("value5", parameterList.get(4));
        Assert.assertEquals("value6", parameterList.get(5));
        Assert.assertEquals("value7", parameterList.get(6));
        Assert.assertEquals("value8", parameterList.get(7));
        Assert.assertEquals("value9", parameterList.get(8));
        Assert.assertEquals("value10", parameterList.get(9));
        Assert.assertEquals("value11", parameterList.get(10));
        Assert.assertEquals("value12", parameterList.get(11));
        Assert.assertEquals("value13", parameterList.get(12));
        Assert.assertEquals("value14", parameterList.get(13));
        Assert.assertEquals("value15", parameterList.get(14));
    }

    /**
     * 条件部分を3重ネスト、ネスト内で最も深い条件部分のみtrueでELSE句非表示ｓ
     */
    @Test
    public void test231() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("AND COLUMN2 = #var2# -- column2");
        sql.add("AND COLUMN3 = #var3# -- column3");
        sql.add("-- IF #cond2#");
        sql.add("-- ELSE #cond2#");
        sql.add("AND COLUMN4 = #var4# -- column4");
        sql.add("AND COLUMN5 = #var5# -- column5");
        sql.add("AND COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("-- ELSE #cond3#");
        sql.add("AND COLUMN7 = #var7# -- column7");
        sql.add("AND COLUMN8 = #var8# -- column8");
        sql.add("AND COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond3#");
        sql.add("AND COLUMN10 = #var10# -- column7");
        sql.add("AND COLUMN11 = #var11# -- column8");
        sql.add("AND COLUMN12 = #var12# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("AND COLUMN13 = #var13# -- column7");
        sql.add("AND COLUMN14 = #var14# -- column8");
        sql.add("AND COLUMN15 = #var15# -- column9");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test231", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        variableMap.put("cond2", false);
        variableMap.put("cond3", true);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COLUMN1 =  ? AND COLUMN2 =  ? AND COLUMN3 =  ? AND COLUMN4 =  ? AND COLUMN5 =  ? AND COLUMN6 =  ? AND COLUMN10 =  ? AND COLUMN11 =  ? AND COLUMN12 =  ? AND COLUMN13 =  ? AND COLUMN14 =  ? AND COLUMN15 =  ? ORDER BY COLUMN1 ASC ",
                builder.toString());
        Assert.assertEquals(12, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
        Assert.assertEquals("value4", parameterList.get(3));
        Assert.assertEquals("value5", parameterList.get(4));
        Assert.assertEquals("value6", parameterList.get(5));
        Assert.assertEquals("value10", parameterList.get(6));
        Assert.assertEquals("value11", parameterList.get(7));
        Assert.assertEquals("value12", parameterList.get(8));
        Assert.assertEquals("value13", parameterList.get(9));
        Assert.assertEquals("value14", parameterList.get(10));
        Assert.assertEquals("value15", parameterList.get(11));
    }

    /**
     * 条件部分を3重ネスト、ネスト内で最も浅い条件部分のみtrueでELSE句非表示
     */
    @Test
    public void test232() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("AND COLUMN2 = #var2# -- column2");
        sql.add("AND COLUMN3 = #var3# -- column3");
        sql.add("-- IF #cond2#");
        sql.add("-- ELSE #cond2#");
        sql.add("AND COLUMN4 = #var4# -- column4");
        sql.add("AND COLUMN5 = #var5# -- column5");
        sql.add("AND COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("-- ELSE #cond3#");
        sql.add("AND COLUMN7 = #var7# -- column7");
        sql.add("AND COLUMN8 = #var8# -- column8");
        sql.add("AND COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond3#");
        sql.add("AND COLUMN10 = #var10# -- column7");
        sql.add("AND COLUMN11 = #var11# -- column8");
        sql.add("AND COLUMN12 = #var12# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("AND COLUMN13 = #var13# -- column7");
        sql.add("AND COLUMN14 = #var14# -- column8");
        sql.add("AND COLUMN15 = #var15# -- column9");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test232", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond2", false);
        variableMap.put("cond3", false);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE ORDER BY COLUMN1 ASC ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 条件部分をIF句とELSE句の両方に3重ネスト
     * 1:true, 2:true, 3:true
     */
    @Test
    public void test233() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN2 = #var2# -- column2");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN3 = #var3# -- column3");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN4 = #var4# -- column4");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN5 = #var5# -- column5");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN7 = #var7# -- column7");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN8 = #var8# -- column8");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN10 = #var10# -- column10");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN11 = #var11# -- column11");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN12 = #var12# -- column12");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN13 = #var13# -- column13");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN14 = #var14# -- column14");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN15 = #var15# -- column15");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN16 = #var16# -- column16");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN17 = #var17# -- column17");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN18 = #var18# -- column18");
        sql.add("-- ENDIF #cond2#");
        sql.add("COLUMN19 = #var19# -- column19");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test233", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond2", true);
        variableMap.put("cond3", true);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        variableMap.put("var16", "value16");
        variableMap.put("var17", "value17");
        variableMap.put("var18", "value18");
        variableMap.put("var19", "value19");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COLUMN1 =  ? COLUMN2 =  ? COLUMN3 =  ? COLUMN5 =  ? ORDER BY COLUMN1 ASC ",
                builder.toString());
        Assert.assertEquals(4, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
        Assert.assertEquals("value5", parameterList.get(3));
    }

    /**
     * 条件部分をIF句とELSE句の両方に3重ネスト
     * 1:false, 2:true, 3:true
     */
    @Test
    public void test234() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN2 = #var2# -- column2");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN3 = #var3# -- column3");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN4 = #var4# -- column4");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN5 = #var5# -- column5");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN7 = #var7# -- column7");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN8 = #var8# -- column8");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN10 = #var10# -- column10");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN11 = #var11# -- column11");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN12 = #var12# -- column12");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN13 = #var13# -- column13");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN14 = #var14# -- column14");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN15 = #var15# -- column15");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN16 = #var16# -- column16");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN17 = #var17# -- column17");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN18 = #var18# -- column18");
        sql.add("-- ENDIF #cond2#");
        sql.add("COLUMN19 = #var19# -- column19");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test234", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        variableMap.put("cond2", true);
        variableMap.put("cond3", true);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        variableMap.put("var16", "value16");
        variableMap.put("var17", "value17");
        variableMap.put("var18", "value18");
        variableMap.put("var19", "value19");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COLUMN10 =  ? COLUMN11 =  ? COLUMN12 =  ? COLUMN14 =  ? COLUMN19 =  ? ORDER BY COLUMN1 ASC ",
                builder.toString());
        Assert.assertEquals(5, parameterList.size());
        Assert.assertEquals("value10", parameterList.get(0));
        Assert.assertEquals("value11", parameterList.get(1));
        Assert.assertEquals("value12", parameterList.get(2));
        Assert.assertEquals("value14", parameterList.get(3));
        Assert.assertEquals("value19", parameterList.get(4));
    }

    /**
     * 条件部分をIF句とELSE句の両方に3重ネスト
     * 1:true, 2:false, 3:true
     */
    @Test
    public void test235() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN2 = #var2# -- column2");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN3 = #var3# -- column3");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN4 = #var4# -- column4");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN5 = #var5# -- column5");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN7 = #var7# -- column7");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN8 = #var8# -- column8");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN10 = #var10# -- column10");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN11 = #var11# -- column11");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN12 = #var12# -- column12");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN13 = #var13# -- column13");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN14 = #var14# -- column14");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN15 = #var15# -- column15");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN16 = #var16# -- column16");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN17 = #var17# -- column17");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN18 = #var18# -- column18");
        sql.add("-- ENDIF #cond2#");
        sql.add("COLUMN19 = #var19# -- column19");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test235", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond2", false);
        variableMap.put("cond3", true);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        variableMap.put("var16", "value16");
        variableMap.put("var17", "value17");
        variableMap.put("var18", "value18");
        variableMap.put("var19", "value19");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COLUMN1 =  ? COLUMN6 =  ? COLUMN7 =  ? COLUMN9 =  ? ORDER BY COLUMN1 ASC ",
                builder.toString());
        Assert.assertEquals(4, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value6", parameterList.get(1));
        Assert.assertEquals("value7", parameterList.get(2));
        Assert.assertEquals("value9", parameterList.get(3));
    }

    /**
     * 条件部分をIF句とELSE句の両方に3重ネスト
     * 1:true, 2:true, 3:false
     */
    @Test
    public void test236() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN2 = #var2# -- column2");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN3 = #var3# -- column3");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN4 = #var4# -- column4");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN5 = #var5# -- column5");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN7 = #var7# -- column7");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN8 = #var8# -- column8");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN10 = #var10# -- column10");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN11 = #var11# -- column11");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN12 = #var12# -- column12");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN13 = #var13# -- column13");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN14 = #var14# -- column14");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN15 = #var15# -- column15");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN16 = #var16# -- column16");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN17 = #var17# -- column17");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN18 = #var18# -- column18");
        sql.add("-- ENDIF #cond2#");
        sql.add("COLUMN19 = #var19# -- column19");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test236", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        variableMap.put("cond2", true);
        variableMap.put("cond3", false);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        variableMap.put("var16", "value16");
        variableMap.put("var17", "value17");
        variableMap.put("var18", "value18");
        variableMap.put("var19", "value19");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COLUMN1 =  ? COLUMN2 =  ? COLUMN4 =  ? COLUMN5 =  ? ORDER BY COLUMN1 ASC ",
                builder.toString());
        Assert.assertEquals(4, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value4", parameterList.get(2));
        Assert.assertEquals("value5", parameterList.get(3));
    }

    /**
     * 条件部分をIF句とELSE句の両方に3重ネスト
     * 1:false, 2:false, 3:未設定
     */
    @Test
    public void test237() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN2 = #var2# -- column2");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN3 = #var3# -- column3");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN4 = #var4# -- column4");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN5 = #var5# -- column5");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN7 = #var7# -- column7");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN8 = #var8# -- column8");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN10 = #var10# -- column10");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN11 = #var11# -- column11");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN12 = #var12# -- column12");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN13 = #var13# -- column13");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN14 = #var14# -- column14");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN15 = #var15# -- column15");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN16 = #var16# -- column16");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN17 = #var17# -- column17");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN18 = #var18# -- column18");
        sql.add("-- ENDIF #cond2#");
        sql.add("COLUMN19 = #var19# -- column19");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test237", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        variableMap.put("cond2", false);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        variableMap.put("var16", "value16");
        variableMap.put("var17", "value17");
        variableMap.put("var18", "value18");
        variableMap.put("var19", "value19");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT COLUMN FROM TABLE WHERE COLUMN10 =  ? COLUMN15 =  ? COLUMN18 =  ? COLUMN19 =  ? ORDER BY COLUMN1 ASC ",
                builder.toString());
        Assert.assertEquals(4, parameterList.size());
        Assert.assertEquals("value10", parameterList.get(0));
        Assert.assertEquals("value15", parameterList.get(1));
        Assert.assertEquals("value18", parameterList.get(2));
        Assert.assertEquals("value19", parameterList.get(3));
    }

    /**
     * 条件部分をIF句とELSE句の両方に3重ネスト
     * 1:false, 2:未設定, 3:true
     */
    @Test
    public void test238() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN2 = #var2# -- column2");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN3 = #var3# -- column3");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN4 = #var4# -- column4");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN5 = #var5# -- column5");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN7 = #var7# -- column7");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN8 = #var8# -- column8");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN10 = #var10# -- column10");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN11 = #var11# -- column11");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN12 = #var12# -- column12");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN13 = #var13# -- column13");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN14 = #var14# -- column14");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN15 = #var15# -- column15");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN16 = #var16# -- column16");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN17 = #var17# -- column17");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN18 = #var18# -- column18");
        sql.add("-- ENDIF #cond2#");
        sql.add("COLUMN19 = #var19# -- column19");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test238", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        variableMap.put("cond3", true);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        variableMap.put("var16", "value16");
        variableMap.put("var17", "value17");
        variableMap.put("var18", "value18");
        variableMap.put("var19", "value19");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE COLUMN10 =  ? COLUMN19 =  ? ORDER BY COLUMN1 ASC ",
                builder.toString());
        Assert.assertEquals(2, parameterList.size());
        Assert.assertEquals("value10", parameterList.get(0));
        Assert.assertEquals("value19", parameterList.get(1));
    }

    /**
     * 条件部分をIF句とELSE句の両方に3重ネスト
     * 1:未設定, 2:true, 3:true
     */
    @Test
    public void test239() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT");
        sql.add("COLUMN");
        sql.add("FROM");
        sql.add("TABLE");
        sql.add("WHERE");
        sql.add("-- IF #cond1#");
        sql.add("COLUMN1 = #var1# -- column1");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN2 = #var2# -- column2");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN3 = #var3# -- column3");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN4 = #var4# -- column4");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN5 = #var5# -- column5");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN6 = #var6# -- column6");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN7 = #var7# -- column7");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN8 = #var8# -- column8");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN9 = #var9# -- column9");
        sql.add("-- ENDIF #cond2#");
        sql.add("-- ELSE #cond1#");
        sql.add("COLUMN10 = #var10# -- column10");
        sql.add("-- IF #cond2#");
        sql.add("COLUMN11 = #var11# -- column11");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN12 = #var12# -- column12");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN13 = #var13# -- column13");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN14 = #var14# -- column14");
        sql.add("-- ELSE #cond2#");
        sql.add("COLUMN15 = #var15# -- column15");
        sql.add("-- IF #cond3#");
        sql.add("COLUMN16 = #var16# -- column16");
        sql.add("-- ELSE #cond3#");
        sql.add("COLUMN17 = #var17# -- column17");
        sql.add("-- ENDIF #cond3#");
        sql.add("COLUMN18 = #var18# -- column18");
        sql.add("-- ENDIF #cond2#");
        sql.add("COLUMN19 = #var19# -- column19");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN1 ASC");
        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test239", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond2", true);
        variableMap.put("cond3", true);
        variableMap.put("var1", "value1");
        variableMap.put("var2", "value2");
        variableMap.put("var3", "value3");
        variableMap.put("var4", "value4");
        variableMap.put("var5", "value5");
        variableMap.put("var6", "value6");
        variableMap.put("var7", "value7");
        variableMap.put("var8", "value8");
        variableMap.put("var9", "value9");
        variableMap.put("var10", "value10");
        variableMap.put("var11", "value11");
        variableMap.put("var12", "value12");
        variableMap.put("var13", "value13");
        variableMap.put("var14", "value14");
        variableMap.put("var15", "value15");
        variableMap.put("var16", "value16");
        variableMap.put("var17", "value17");
        variableMap.put("var18", "value18");
        variableMap.put("var19", "value19");
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT COLUMN FROM TABLE WHERE ORDER BY COLUMN1 ASC ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    // ////////////////////////////////////////////////////
    // 繰返し部分関連試験

    /**
     * 繰返し部分の要素0件
     */
    @Test
    public void test300() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT *");
        sql.add("FROM TABLE");
        sql.add("WHERE 1=0");
        sql.add("-- FOREACH #loop1#");
        sql.add("OR COLUMN = #value#");
        sql.add("-- ENDFOREACH #loop1#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test300", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> loop1 = new LinkedList<Object>();
        variableMap.put("loop1", loop1);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE 1=0 ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }

    /**
     * 繰返し部分の要素1件
     */
    @Test
    public void test301() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT *");
        sql.add("FROM TABLE");
        sql.add("WHERE 1=0");
        sql.add("-- FOREACH #loop1#");
        sql.add("OR COLUMN = #value#");
        sql.add("-- ENDFOREACH #loop1#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test301", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> loop1 = new LinkedList<Object>();
        variableMap.put("loop1", loop1);
        HashMap<String, Object> subMap1 = new HashMap<String, Object>();
        subMap1.put("value", "value1");
        loop1.add(subMap1);
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE 1=0 OR COLUMN =  ? ", builder.toString());
        Assert.assertEquals(1, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
    }

    /**
     * 繰返し部分の要素10件
     */
    @Test
    public void test302() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT *");
        sql.add("FROM TABLE");
        sql.add("WHERE 1=0");
        sql.add("-- FOREACH #loop1#");
        sql.add("OR COLUMN = #value#");
        sql.add("-- ENDFOREACH #loop1#");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test302", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> loop = new LinkedList<Object>();
        variableMap.put("loop1", loop);
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value1");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value2");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value3");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value4");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value5");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value6");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value7");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value8");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value9");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value10");
            loop.add(subMap);
        }
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT * FROM TABLE WHERE 1=0 OR COLUMN =  ? OR COLUMN =  ? OR COLUMN =  ? OR COLUMN =  ? OR COLUMN =  ? OR COLUMN =  ? OR COLUMN =  ? OR COLUMN =  ? OR COLUMN =  ? OR COLUMN =  ? ",
                builder.toString());
        Assert.assertEquals(10, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
        Assert.assertEquals("value4", parameterList.get(3));
        Assert.assertEquals("value5", parameterList.get(4));
        Assert.assertEquals("value6", parameterList.get(5));
        Assert.assertEquals("value7", parameterList.get(6));
        Assert.assertEquals("value8", parameterList.get(7));
        Assert.assertEquals("value9", parameterList.get(8));
        Assert.assertEquals("value10", parameterList.get(9));
    }

    /**
     * 繰返し部分の要素3件、繰返し内部に条件部分あり
     */
    @Test
    public void test303() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT *");
        sql.add("FROM TABLE");
        sql.add("WHERE 1=0");
        sql.add("-- FOREACH #loop1#");
        sql.add("-- IF #cond1#");
        sql.add("OR COLUMN = #value#");
        sql.add("-- ENDIF #cond1#");
        sql.add("-- ENDFOREACH #loop1#");
        sql.add("ORDER BY COLUMN ASC");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test303", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> loop = new LinkedList<Object>();
        variableMap.put("loop1", loop);
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value1");
            subMap.put("cond1", true);
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value2");
            subMap.put("cond1", false);
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value3");
            loop.add(subMap);
        }
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE 1=0 OR COLUMN =  ? ORDER BY COLUMN ASC ", builder.toString());
        Assert.assertEquals(1, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
    }

    /**
     * 条件部分内に繰返し部分あり、条件部分true
     */
    @Test
    public void test304() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT *");
        sql.add("FROM TABLE");
        sql.add("WHERE 1=0");
        sql.add("-- IF #cond1#");
        sql.add("-- FOREACH #loop1#");
        sql.add("OR COLUMN1 = #value#");
        sql.add("-- ENDFOREACH #loop1#");
        sql.add("-- ELSE #cond1#");
        sql.add("-- FOREACH #loop1#");
        sql.add("OR COLUMN2 = #value#");
        sql.add("-- ENDFOREACH #loop1#");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN ASC");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test304", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", true);
        LinkedList<Object> loop = new LinkedList<Object>();
        variableMap.put("loop1", loop);
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value1");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value2");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value3");
            loop.add(subMap);
        }
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT * FROM TABLE WHERE 1=0 OR COLUMN1 =  ? OR COLUMN1 =  ? OR COLUMN1 =  ? ORDER BY COLUMN ASC ",
                builder.toString());
        Assert.assertEquals(3, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
    }

    /**
     * 条件部分内に繰返し部分あり、条件部分false
     */
    @Test
    public void test305() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT *");
        sql.add("FROM TABLE");
        sql.add("WHERE 1=0");
        sql.add("-- IF #cond1#");
        sql.add("-- FOREACH #loop1#");
        sql.add("OR COLUMN1 = #value#");
        sql.add("-- ENDFOREACH #loop1#");
        sql.add("-- ELSE #cond1#");
        sql.add("-- FOREACH #loop1#");
        sql.add("OR COLUMN2 = #value#");
        sql.add("-- ENDFOREACH #loop1#");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN ASC");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test305", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("cond1", false);
        LinkedList<Object> loop = new LinkedList<Object>();
        variableMap.put("loop1", loop);
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value1");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value2");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value3");
            loop.add(subMap);
        }
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals(
                "SELECT * FROM TABLE WHERE 1=0 OR COLUMN2 =  ? OR COLUMN2 =  ? OR COLUMN2 =  ? ORDER BY COLUMN ASC ",
                builder.toString());
        Assert.assertEquals(3, parameterList.size());
        Assert.assertEquals("value1", parameterList.get(0));
        Assert.assertEquals("value2", parameterList.get(1));
        Assert.assertEquals("value3", parameterList.get(2));
    }

    /**
     * 条件部分内に繰返し部分あり、条件部分未設定
     */
    @Test
    public void test306() throws Exception {
        // SQLテンプレート
        LinkedList<String> sql = new LinkedList<String>();
        sql.add("SELECT *");
        sql.add("FROM TABLE");
        sql.add("WHERE 1=0");
        sql.add("-- IF #cond1#");
        sql.add("-- FOREACH #loop1#");
        sql.add("OR COLUMN1 = #value#");
        sql.add("-- ENDFOREACH #loop1#");
        sql.add("-- ELSE #cond1#");
        sql.add("-- FOREACH #loop1#");
        sql.add("OR COLUMN2 = #value#");
        sql.add("-- ENDFOREACH #loop1#");
        sql.add("-- ENDIF #cond1#");
        sql.add("ORDER BY COLUMN ASC");

        // SQLテンプレート分析結果取得
        SqlElem firstElem = TemplateAnalyzer.analyze("test306", sql.iterator());

        // SQLテンプレート分析結果からPreparedStatment用クエリとパラメータリスト生成
        StringBuilder builder = new StringBuilder();
        HashMap<String, Object> variableMap = new HashMap<String, Object>();
        LinkedList<Object> loop = new LinkedList<Object>();
        variableMap.put("loop1", loop);
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value1");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value2");
            loop.add(subMap);
        }
        {
            HashMap<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("value", "value3");
            loop.add(subMap);
        }
        LinkedList<Object> parameterList = new LinkedList<Object>();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);

        // 生成結果の検証
        Assert.assertEquals("SELECT * FROM TABLE WHERE 1=0 ORDER BY COLUMN ASC ", builder.toString());
        Assert.assertEquals(0, parameterList.size());
    }
}
