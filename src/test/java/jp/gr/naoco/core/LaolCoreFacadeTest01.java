package jp.gr.naoco.core;

import jp.gr.naoco.core.NaocoCoreFacade;
import jp.gr.naoco.core.NaocoCoreInitializer;
import jp.gr.naoco.core.transaction.TransactionManager;
import jp.gr.naoco.sample.dummy.DummyConnection;
import jp.gr.naoco.sample.dummy.DummyPreparedStatement;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LaolCoreFacadeのメソッドを使用して、単一スレッド内トランザクションを操作した際の処理を確認
 * 
 * @author naoco0917
 */
public class LaolCoreFacadeTest01 {

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Test Preparations

    @Before
    public void setup() throws Exception {
        DummyConnection.clearIdCount();
    }

    @After
    public void teardown() throws Exception {
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Test Cases

    /**
     * 単一接続、単一ステートメントの取得、強制コミット、トランザクション通常終了
     * <ol>
     * <li>接続1のトランザクションを開始する</li>
     * <li>接続1のトランザクションからステートメント1を生成する</li>
     * <li>接続1のトランザクションを強制コミットする</li>
     * <li>接続1のトランザクションをコミットしてトランザクションを終了する</li>
     * </ol>
     */
    @Test
    public void test01() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String sql01 = "test sql01";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest01.class.getName() + "_test01", null);

        // 接続の取得
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        // ステートメントの取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());
        // 接続のコミット
        NaocoCoreFacade.commitForce();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_UNCLOSED, connection01.getStatus());
        // 接続の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection01.getStatus());
    }

    /**
     * 単一接続、単一ステートメントの取得、強制ロールバック、トランザクション通常終了
     * <ol>
     * <li>接続1のトランザクションを開始する</li>
     * <li>接続1のトランザクションからステートメント1を生成する</li>
     * <li>接続1のトランザクションを強制ロールバックする</li>
     * <li>接続1のトランザクションをコミットしてトランザクションを終了する</li>
     * </ol>
     */
    @Test
    public void test02() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String sql01 = "test sql01";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest01.class.getName() + "_test02", null);

        // 接続の取得
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        // ステートメントの取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());
        // 接続のロールバック
        NaocoCoreFacade.rollbackForce();
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_UNCLOSED, connection01.getStatus());
        // 接続の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection01.getStatus());
    }

    /**
     * 単一接続、単一ステートメントの取得、トランザクションエラー終了
     * <ol>
     * <li>接続1のトランザクションを開始する</li>
     * <li>接続1のトランザクションからステートメント1を生成する</li>
     * <li>接続1のトランザクションをロールバックしてトランザクションを終了する</li>
     * </ol>
     */
    @Test
    public void test03() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String sql01 = "test sql01";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest01.class.getName() + "_test03", null);

        // 接続の取得
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        // ステートメントの取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());
        // 接続の終了
        NaocoCoreFacade.rollbackTransaction();
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection01.getStatus());
    }

    /**
     * 単一接続でトランザクションをTakeOverをした際に、すべて同一の接続の処理であることを確認
     * <ol>
     * <li>接続1のトランザクション1を開始する</li>
     * <li>接続1のトランザクション1からステートメント1を生成する</li>
     * <li>接続1でトランザクション2をTakeOverして開始する</li>
     * <li>接続1のトランザクション2からステートメント2を生成する</li>
     * <li>接続1でトランザクション3をTakeOverして開始する</li>
     * <li>接続1のトランザクション3からステートメント3を生成する</li>
     * <li>接続1のトランザクション3をコミットしてトランザクションを終了する</li>
     * <li>接続1のトランザクション2をコミットしてトランザクションを終了する</li>
     * <li>接続1のトランザクション1をコミットしてトランザクションを終了する</li>
     * </ol>
     */
    @Test
    public void test04() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest01.class.getName() + "_test04", null);

        // トランザクション1の開始
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        // ステートメント1の取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());

        // トランザクション2の開始
        TransactionManager.takeoverTransaction(lookupname01);
        DummyConnection connection02 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection02.getId());
        // ステートメント1の取得
        DummyPreparedStatement statement02 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql02);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(1, statement02.getConnectionId());
        Assert.assertEquals(sql02, statement02.sql());

        // トランザクション3の開始
        TransactionManager.takeoverTransaction(lookupname01);
        DummyConnection connection03 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection03.getId());
        // ステートメント3の取得
        DummyPreparedStatement statement03 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql03);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(1, statement03.getConnectionId());
        Assert.assertEquals(sql03, statement03.sql());

        // トランザクションの終了
        // トランザクション3の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // トランザクション2の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // トランザクション1の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection01.getStatus());
    }

    /**
     * 同一の接続設定でトランザクションをネストした際に、各接続の処理であることを確認
     * <ol>
     * <li>接続1のトランザクション1を開始する</li>
     * <li>接続1のトランザクション1からステートメント1を生成する</li>
     * <li>接続2のトランザクション2開始する</li>
     * <li>接続1のトランザクション2からステートメント2を生成する</li>
     * <li>接続3のトランザクション3開始する</li>
     * <li>接続1のトランザクション3からステートメント3を生成する</li>
     * <li>接続3のトランザクション3をコミットしてトランザクションを終了する</li>
     * <li>接続2のトランザクション2をコミットしてトランザクションを終了する</li>
     * <li>接続1のトランザクション1をコミットしてトランザクションを終了する</li>
     * </ol>
     */
    @Test
    public void test05() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest01.class.getName() + "_test05", null);

        // トランザクション1の開始
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        // ステートメント1の取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());

        // トランザクション2の開始
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection02 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(2, connection02.getId());
        // ステートメント1の取得
        DummyPreparedStatement statement02 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql02);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(2, statement02.getConnectionId());
        Assert.assertEquals(sql02, statement02.sql());

        // トランザクション3の開始
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection03 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(3, connection03.getId());
        // ステートメント3の取得
        DummyPreparedStatement statement03 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql03);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
        Assert.assertEquals(3, statement03.getConnectionId());
        Assert.assertEquals(sql03, statement03.sql());

        // トランザクションの終了
        // トランザクション3の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // トランザクション2の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // トランザクション1の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection01.getStatus());
    }

    /**
     * 個別の接続設定でトランザクションをネストした際に、各接続の処理であることを確認
     * <ol>
     * <li>接続1のトランザクション1を開始する</li>
     * <li>接続1のトランザクション1からステートメント1を生成する</li>
     * <li>接続2のトランザクション2開始する</li>
     * <li>接続1のトランザクション2からステートメント2を生成する</li>
     * <li>接続3のトランザクション3開始する</li>
     * <li>接続1のトランザクション3からステートメント3を生成する</li>
     * <li>接続3のトランザクション3をコミットしてトランザクションを終了する</li>
     * <li>接続2のトランザクション2をコミットしてトランザクションを終了する</li>
     * <li>接続1のトランザクション1をコミットしてトランザクションを終了する</li>
     * </ol>
     */
    @Test
    public void test06() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String lookupname02 = "java:comp/env/jdbc/test02";
        final String lookupname03 = "java:comp/env/jdbc/test03";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest01.class.getName() + "_test06", null);

        // トランザクション1の開始
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        Assert.assertEquals(lookupname01, connection01.getLookupName());
        // ステートメント1の取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());

        // トランザクション2の開始
        NaocoCoreFacade.startTransaction(lookupname02);
        DummyConnection connection02 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(2, connection02.getId());
        Assert.assertEquals(lookupname02, connection02.getLookupName());
        // ステートメント1の取得
        DummyPreparedStatement statement02 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql02);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(2, statement02.getConnectionId());
        Assert.assertEquals(sql02, statement02.sql());

        // トランザクション3の開始
        NaocoCoreFacade.startTransaction(lookupname03);
        DummyConnection connection03 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(3, connection03.getId());
        Assert.assertEquals(lookupname03, connection03.getLookupName());
        // ステートメント3の取得
        DummyPreparedStatement statement03 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql03);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
        Assert.assertEquals(3, statement03.getConnectionId());
        Assert.assertEquals(sql03, statement03.sql());

        // トランザクションの終了
        // トランザクション3の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // トランザクション2の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // トランザクション1の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection01.getStatus());
    }

    /**
     * 異なるの接続設定でトランザクションをネストを横断して、トランザクションのTakeOverが正常に動作することを確認
     * <ol>
     * <li>接続1のトランザクション1を開始する</li>
     * <li>接続1のトランザクション1からステートメント1を生成する</li>
     * <li>接続2のトランザクション2開始する</li>
     * <li>接続1のトランザクション2からステートメント2を生成する</li>
     * <li>接続3のトランザクション3開始する</li>
     * <li>接続1のトランザクション3からステートメント3を生成する</li>
     * <li>接続1でトランザクション4をTakeOverして開始する</li>
     * <li>接続1のトランザクション4からステートメント4を生成する</li>
     * <li>接続3のトランザクション4をコミットしてトランザクションを終了する</li>
     * <li>接続3のトランザクション3をコミットしてトランザクションを終了する</li>
     * <li>接続2のトランザクション2をコミットしてトランザクションを終了する</li>
     * <li>接続1のトランザクション1をコミットしてトランザクションを終了する</li>
     * </ol>
     */
    @Test
    public void test07() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String lookupname02 = "java:comp/env/jdbc/test02";
        final String lookupname03 = "java:comp/env/jdbc/test03";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";
        final String sql04 = "test sql04";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest01.class.getName() + "_test07", null);

        // トランザクション1の開始
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        Assert.assertEquals(lookupname01, connection01.getLookupName());
        // ステートメント1の取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());

        // トランザクション2の開始
        NaocoCoreFacade.startTransaction(lookupname02);
        DummyConnection connection02 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(2, connection02.getId());
        Assert.assertEquals(lookupname02, connection02.getLookupName());
        // ステートメント1の取得
        DummyPreparedStatement statement02 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql02);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(2, statement02.getConnectionId());
        Assert.assertEquals(sql02, statement02.sql());

        // トランザクション3の開始
        NaocoCoreFacade.startTransaction(lookupname03);
        DummyConnection connection03 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(3, connection03.getId());
        Assert.assertEquals(lookupname03, connection03.getLookupName());
        // ステートメント3の取得
        DummyPreparedStatement statement03 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql03);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
        Assert.assertEquals(3, statement03.getConnectionId());
        Assert.assertEquals(sql03, statement03.sql());

        // トランザクション1をTakeOverしてトランザクション4を開始
        TransactionManager.takeoverTransaction(lookupname01);
        DummyConnection connection04 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection04.getId());
        Assert.assertEquals(lookupname01, connection04.getLookupName());
        // ステートメント4の取得
        DummyPreparedStatement statement04 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql04);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection04.getStatus());
        Assert.assertEquals(1, statement04.getConnectionId());
        Assert.assertEquals(sql04, statement04.sql());

        // トランザクションの終了
        // トランザクション4の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection04.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // トランザクション3の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection04.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // トランザクション2の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection04.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // トランザクション1の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection04.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection01.getStatus());
    }

    /**
     * 単一接続、単一ステートメントの取得、ロールバック予約、トランザクション通常終了
     * <ol>
     * <li>接続1のトランザクションを開始する</li>
     * <li>接続1のトランザクションからステートメント1を生成する</li>
     * <li>接続1のトランザクションのロールバック予約をする</li>
     * <li>接続1のトランザクションをコミットしてトランザクションを終了する</li>
     * </ol>
     */
    @Test
    public void test08() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String sql01 = "test sql01";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest01.class.getName() + "_test08", null);

        // 接続の取得
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        // ステートメントの取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());
        // ロールバック予約
        NaocoCoreFacade.reserveRollback();
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // 接続の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection01.getStatus());
    }

    /**
     * 異なるの接続設定でトランザクションをネストを横断して、トランザクションのTakeOverをした際に、ロールバック予約が正常に動作することを確認
     * <ol>
     * <li>接続1のトランザクション1を開始する</li>
     * <li>接続1のトランザクション1からステートメント1を生成する</li>
     * <li>接続2のトランザクション2開始する</li>
     * <li>接続1のトランザクション2からステートメント2を生成する</li>
     * <li>接続3のトランザクション3開始する</li>
     * <li>接続1のトランザクション3からステートメント3を生成する</li>
     * <li>接続1でトランザクション4をTakeOverして開始する</li>
     * <li>接続1のトランザクション4からステートメント4を生成する</li>
     * <li>接続1のトランザクション4でロールバック予約をする</li>
     * <li>接続3のトランザクション4をコミットしてトランザクションを終了する</li>
     * <li>接続3のトランザクション3をコミットしてトランザクションを終了する</li>
     * <li>接続2のトランザクション2をコミットしてトランザクションを終了する</li>
     * <li>接続1のトランザクション1をコミットしてトランザクションを終了する</li>
     * </ol>
     */
    @Test
    public void test09() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String lookupname02 = "java:comp/env/jdbc/test02";
        final String lookupname03 = "java:comp/env/jdbc/test03";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";
        final String sql04 = "test sql04";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest01.class.getName() + "_test09", null);

        // トランザクション1の開始
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        Assert.assertEquals(lookupname01, connection01.getLookupName());
        // ステートメント1の取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());

        // トランザクション2の開始
        NaocoCoreFacade.startTransaction(lookupname02);
        DummyConnection connection02 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(2, connection02.getId());
        Assert.assertEquals(lookupname02, connection02.getLookupName());
        // ステートメント1の取得
        DummyPreparedStatement statement02 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql02);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(2, statement02.getConnectionId());
        Assert.assertEquals(sql02, statement02.sql());

        // トランザクション3の開始
        NaocoCoreFacade.startTransaction(lookupname03);
        DummyConnection connection03 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(3, connection03.getId());
        Assert.assertEquals(lookupname03, connection03.getLookupName());
        // ステートメント3の取得
        DummyPreparedStatement statement03 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql03);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
        Assert.assertEquals(3, statement03.getConnectionId());
        Assert.assertEquals(sql03, statement03.sql());

        // トランザクション1をTakeOverしてトランザクション4を開始
        TransactionManager.takeoverTransaction(lookupname01);
        DummyConnection connection04 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection04.getId());
        Assert.assertEquals(lookupname01, connection04.getLookupName());
        // ステートメント4の取得
        DummyPreparedStatement statement04 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql04);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection04.getStatus());
        Assert.assertEquals(1, statement04.getConnectionId());
        Assert.assertEquals(sql04, statement04.sql());
        // トランザクション4でロールバック予約
        TransactionManager.reserveRollback();
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection04.getStatus());

        // トランザクションの終了
        // トランザクション4の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection04.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // トランザクション3の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection04.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // トランザクション2の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection04.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        // トランザクション1の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection04.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection01.getStatus());
    }

    /**
     * 個別の接続設定でトランザクションをネストした際に、TransactionManager#commitAllTransactionで全てのトランザクションを一括終了する
     * <ol>
     * <li>接続1のトランザクション1を開始する</li>
     * <li>接続1のトランザクション1からステートメント1を生成する</li>
     * <li>接続2のトランザクション2開始する</li>
     * <li>接続1のトランザクション2からステートメント2を生成する</li>
     * <li>接続3のトランザクション3開始する</li>
     * <li>接続1のトランザクション3からステートメント3を生成する</li>
     * <li>全てのトランザクションコミットしてトランザクションを終了する</li>
     * </ol>
     */
    @Test
    public void test10() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String lookupname02 = "java:comp/env/jdbc/test02";
        final String lookupname03 = "java:comp/env/jdbc/test03";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest01.class.getName() + "_test10", null);

        // トランザクション1の開始
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        Assert.assertEquals(lookupname01, connection01.getLookupName());
        // ステートメント1の取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());

        // トランザクション2の開始
        NaocoCoreFacade.startTransaction(lookupname02);
        DummyConnection connection02 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(2, connection02.getId());
        Assert.assertEquals(lookupname02, connection02.getLookupName());
        // ステートメント1の取得
        DummyPreparedStatement statement02 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql02);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(2, statement02.getConnectionId());
        Assert.assertEquals(sql02, statement02.sql());

        // トランザクション3の開始
        NaocoCoreFacade.startTransaction(lookupname03);
        DummyConnection connection03 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(3, connection03.getId());
        Assert.assertEquals(lookupname03, connection03.getLookupName());
        // ステートメント3の取得
        DummyPreparedStatement statement03 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql03);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
        Assert.assertEquals(3, statement03.getConnectionId());
        Assert.assertEquals(sql03, statement03.sql());

        // トランザクションの終了
        TransactionManager.commitAllTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection01.getStatus());
    }

    /**
     * 個別の接続設定でトランザクションをネストした際に、TransactionManager#rollbackAllTransaction で全てのトランザクションを一括終了する
     * <ol>
     * <li>接続1のトランザクション1を開始する</li>
     * <li>接続1のトランザクション1からステートメント1を生成する</li>
     * <li>接続2のトランザクション2開始する</li>
     * <li>接続1のトランザクション2からステートメント2を生成する</li>
     * <li>接続3のトランザクション3開始する</li>
     * <li>接続1のトランザクション3からステートメント3を生成する</li>
     * <li>全てのトランザクションをロールバックしてトランザクションを終了する</li>
     * </ol>
     */
    @Test
    public void test11() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String lookupname02 = "java:comp/env/jdbc/test02";
        final String lookupname03 = "java:comp/env/jdbc/test03";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest01.class.getName() + "_test11", null);

        // トランザクション1の開始
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        Assert.assertEquals(lookupname01, connection01.getLookupName());
        // ステートメント1の取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());

        // トランザクション2の開始
        NaocoCoreFacade.startTransaction(lookupname02);
        DummyConnection connection02 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(2, connection02.getId());
        Assert.assertEquals(lookupname02, connection02.getLookupName());
        // ステートメント1の取得
        DummyPreparedStatement statement02 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql02);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
        Assert.assertEquals(2, statement02.getConnectionId());
        Assert.assertEquals(sql02, statement02.sql());

        // トランザクション3の開始
        NaocoCoreFacade.startTransaction(lookupname03);
        DummyConnection connection03 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(3, connection03.getId());
        Assert.assertEquals(lookupname03, connection03.getLookupName());
        // ステートメント3の取得
        DummyPreparedStatement statement03 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql03);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
        Assert.assertEquals(3, statement03.getConnectionId());
        Assert.assertEquals(sql03, statement03.sql());

        // トランザクションの終了
        TransactionManager.rollbackAllTransaction();
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection03.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection02.getStatus());
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection01.getStatus());
    }
}
