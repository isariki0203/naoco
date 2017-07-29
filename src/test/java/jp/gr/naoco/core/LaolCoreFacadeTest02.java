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
 * LaolCoreFacadeのメソッドを使用して、複数スレッド間でトランザクションを操作した際の処理を確認
 * 
 * @author naoco0917
 */
public class LaolCoreFacadeTest02 {

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Test Preparations

    @Before
    public void setup() throws Exception {
        DummyConnection.clearIdCount();
    }

    @After
    public void teardown() throws Exception {
        synchronized (this) {
            this.wait(3000);
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 同一の接続設定について、複数スレッドで並行してトランザクションを作成
     * 
     * @throws Exception
     */
    @Test
    public void test01() throws Exception {
        System.out.println(this.getClass() + "#test01 start");
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest02.class.getName() + "_test01", null);

        // スレッド処理の宣言
        Runnable runnable02 = new Runnable() {
            @Override
            public void run() {
                try {
                    // トランザクション2の開始
                    NaocoCoreFacade.startTransaction(lookupname01);
                    DummyConnection connection02 = (DummyConnection) NaocoCoreFacade.getConnection();
                    Assert.assertEquals(2, connection02.getId());
                    // ステートメント1の取得
                    DummyPreparedStatement statement02 = (DummyPreparedStatement) NaocoCoreFacade
                            .prepareStatement(sql02);
                    Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
                    Assert.assertEquals(2, statement02.getConnectionId());
                    Assert.assertEquals(sql02, statement02.sql());

                    // 待機
                    synchronized (sql02) {
                        sql02.wait();
                    }

                    // トランザクションの終了
                    NaocoCoreFacade.commitTransaction();
                    Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection02.getStatus());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        Runnable runnable03 = new Runnable() {
            @Override
            public void run() {
                try {
                    // トランザクション2の開始
                    NaocoCoreFacade.startTransaction(lookupname01);
                    DummyConnection connection03 = (DummyConnection) NaocoCoreFacade.getConnection();
                    Assert.assertEquals(3, connection03.getId());
                    // ステートメント1の取得
                    DummyPreparedStatement statement03 = (DummyPreparedStatement) NaocoCoreFacade
                            .prepareStatement(sql03);
                    Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
                    Assert.assertEquals(3, statement03.getConnectionId());
                    Assert.assertEquals(sql03, statement03.sql());

                    // 待機
                    synchronized (sql03) {
                        sql03.wait();
                    }

                    // トランザクションの終了
                    NaocoCoreFacade.commitTransaction();
                    Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // トランザクション1の開始
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        // ステートメント1の取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());

        // トランザクション2を別スレッドで開始
        Thread thread02 = new Thread(runnable02);
        thread02.start();

        synchronized (this) {
            this.wait(100);
        }

        // トランザクション3を別スレッドで開始
        Thread thread03 = new Thread(runnable03);
        thread03.start();

        synchronized (this) {
            this.wait(100);
        }
        // トランザクション2の終了
        synchronized (sql02) {
            sql02.notifyAll();
        }
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());

        // トランザクション3の終了
        synchronized (sql03) {
            sql03.notifyAll();
        }
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());

        // トランザクション1の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection01.getStatus());
    }

    /**
     * 異なる接続設定について、複数スレッドで並行してトランザクションを作成
     * 
     * @throws Exception
     */
    @Test
    public void test02() throws Exception {
        System.out.println(this.getClass() + "#test02 start");
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String lookupname02 = "java:comp/env/jdbc/test02";
        final String lookupname03 = "java:comp/env/jdbc/test03";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest02.class.getName() + "_test02", null);

        // スレッド処理の宣言
        Runnable runnable02 = new Runnable() {
            @Override
            public void run() {
                try {
                    // トランザクション2の開始
                    NaocoCoreFacade.startTransaction(lookupname02);
                    DummyConnection connection02 = (DummyConnection) NaocoCoreFacade.getConnection();
                    Assert.assertEquals(2, connection02.getId());
                    Assert.assertEquals(lookupname02, connection02.getLookupName());
                    // ステートメント1の取得
                    DummyPreparedStatement statement02 = (DummyPreparedStatement) NaocoCoreFacade
                            .prepareStatement(sql02);
                    Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
                    Assert.assertEquals(2, statement02.getConnectionId());
                    Assert.assertEquals(sql02, statement02.sql());

                    // 待機
                    synchronized (sql02) {
                        sql02.wait();
                    }

                    // トランザクションの終了
                    NaocoCoreFacade.commitTransaction();
                    Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection02.getStatus());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        Runnable runnable03 = new Runnable() {
            @Override
            public void run() {
                try {
                    // トランザクション2の開始
                    NaocoCoreFacade.startTransaction(lookupname03);
                    DummyConnection connection03 = (DummyConnection) NaocoCoreFacade.getConnection();
                    Assert.assertEquals(3, connection03.getId());
                    Assert.assertEquals(lookupname03, connection03.getLookupName());
                    // ステートメント1の取得
                    DummyPreparedStatement statement03 = (DummyPreparedStatement) NaocoCoreFacade
                            .prepareStatement(sql03);
                    Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
                    Assert.assertEquals(3, statement03.getConnectionId());
                    Assert.assertEquals(sql03, statement03.sql());

                    // 待機
                    synchronized (sql03) {
                        sql03.wait();
                    }

                    // トランザクションの終了
                    NaocoCoreFacade.commitTransaction();
                    Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection03.getStatus());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // トランザクション1の開始
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        // ステートメント1の取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());

        // トランザクション2を別スレッドで開始
        Thread thread02 = new Thread(runnable02);
        thread02.start();

        synchronized (this) {
            this.wait(100);
        }
        // トランザクション3を別スレッドで開始
        Thread thread03 = new Thread(runnable03);
        thread03.start();

        synchronized (this) {
            this.wait(100);
        }

        // トランザクション2の終了
        synchronized (sql02) {
            sql02.notifyAll();
        }
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());

        // トランザクション3の終了
        synchronized (sql03) {
            sql03.notifyAll();
        }
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());

        // トランザクション1の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, connection01.getStatus());
    }

    /**
     * 複数スレッドで使用しているトランザクションをTransactionManager#closeAllで接続を閉じる
     * 
     * @throws Exception
     */
    @Test
    public void test03() throws Exception {
        System.out.println(this.getClass() + "#test03 start");
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String lookupname02 = "java:comp/env/jdbc/test02";
        final String lookupname03 = "java:comp/env/jdbc/test03";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";

        // 初期設定
        NaocoCoreInitializer.initialize(LaolCoreFacadeTest02.class.getName() + "_test03", null);

        // スレッド処理の宣言
        Runnable runnable02 = new Runnable() {
            @Override
            public void run() {
                try {
                    // トランザクション2の開始
                    NaocoCoreFacade.startTransaction(lookupname02);
                    DummyConnection connection02 = (DummyConnection) NaocoCoreFacade.getConnection();
                    Assert.assertEquals(2, connection02.getId());
                    Assert.assertEquals(lookupname02, connection02.getLookupName());
                    // ステートメント1の取得
                    DummyPreparedStatement statement02 = (DummyPreparedStatement) NaocoCoreFacade
                            .prepareStatement(sql02);
                    Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection02.getStatus());
                    Assert.assertEquals(2, statement02.getConnectionId());
                    Assert.assertEquals(sql02, statement02.sql());

                    // 待機
                    synchronized (sql02) {
                        sql02.wait();
                    }

                    // コネクション接続済みの確認
                    Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection02.getStatus());
                    NaocoCoreFacade.commitTransaction();
                    Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection02.getStatus());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        Runnable runnable03 = new Runnable() {
            @Override
            public void run() {
                try {
                    // トランザクション2の開始
                    NaocoCoreFacade.startTransaction(lookupname03);
                    DummyConnection connection03 = (DummyConnection) NaocoCoreFacade.getConnection();
                    Assert.assertEquals(3, connection03.getId());
                    Assert.assertEquals(lookupname03, connection03.getLookupName());
                    // ステートメント1の取得
                    DummyPreparedStatement statement03 = (DummyPreparedStatement) NaocoCoreFacade
                            .prepareStatement(sql03);
                    Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection03.getStatus());
                    Assert.assertEquals(3, statement03.getConnectionId());
                    Assert.assertEquals(sql03, statement03.sql());

                    // 待機
                    synchronized (sql03) {
                        sql03.wait();
                    }

                    // コネクション接続済みの確認
                    Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection03.getStatus());
                    NaocoCoreFacade.commitTransaction();
                    Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection03.getStatus());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // トランザクション1の開始
        NaocoCoreFacade.startTransaction(lookupname01);
        DummyConnection connection01 = (DummyConnection) NaocoCoreFacade.getConnection();
        Assert.assertEquals(1, connection01.getId());
        // ステートメント1の取得
        DummyPreparedStatement statement01 = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql01);
        Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection01.getStatus());
        Assert.assertEquals(1, statement01.getConnectionId());
        Assert.assertEquals(sql01, statement01.sql());

        // トランザクション2を別スレッドで開始
        Thread thread02 = new Thread(runnable02);
        thread02.start();

        synchronized (this) {
            this.wait(100);
        }

        // トランザクション3を別スレッドで開始
        Thread thread03 = new Thread(runnable03);
        thread03.start();

        synchronized (this) {
            this.wait(100);
        }

        // 接続を強制クローズ
        TransactionManager.closeAll();
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection01.getStatus());

        // トランザクション2の終了
        synchronized (sql02) {
            sql02.notifyAll();
        }
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection01.getStatus());

        // トランザクション3の終了
        synchronized (sql03) {
            sql03.notifyAll();
        }
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection01.getStatus());

        // トランザクション1の終了
        NaocoCoreFacade.commitTransaction();
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, connection01.getStatus());
    }
}
