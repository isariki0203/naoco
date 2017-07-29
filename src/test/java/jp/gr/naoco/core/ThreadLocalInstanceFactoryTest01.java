package jp.gr.naoco.core;

import java.util.Arrays;

import jp.gr.naoco.core.NaocoCoreFacade;
import jp.gr.naoco.core.NaocoCoreInitializer;
import jp.gr.naoco.core.annotation.Transaction;
import jp.gr.naoco.core.annotation.TransactionType;
import jp.gr.naoco.core.factory.ThreadLocalInstanceFactory;
import jp.gr.naoco.sample.dummy.DummyConnection;
import jp.gr.naoco.sample.dummy.DummyPreparedStatement;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ThreadLocalInstanceFactoryが生成するインスタンスについて、メソッド実行中のトランザクションについての確認
 * 
 * @author naoco0917
 */
public class ThreadLocalInstanceFactoryTest01 {

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
     * ThreadLocalInstanceFactoryで取得したインスタンスについて、メソッド内の接続操作と、メソッド終了後の接続クローズを確認
     * 
     * @throws Exception
     */
    @Test
    public void test01() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String sql01 = "test sql01";

        // 初期設定
        NaocoCoreInitializer.initialize(ThreadLocalInstanceFactoryTest01.class.getName() + "_test01", null);

        // 実行クラスの定義
        class Handler01 implements TestHandler {
            private DummyConnection connection_;

            private String sql_;

            private String lookupname_;

            public DummyConnection getMyConnection() {
                return connection_;
            }

            public void setLookupName(String lookupname) {
                lookupname_ = lookupname;
            }

            public void setSql(String sql) {
                sql_ = sql;
            }

            @Override
            public void handle(TestHandler... handle) throws Exception {
                // 接続の取得
                connection_ = (DummyConnection) NaocoCoreFacade.getConnection();
                Assert.assertEquals(1, connection_.getId());
                Assert.assertEquals(lookupname_, connection_.getLookupName());
                // ステートメントの取得
                DummyPreparedStatement statement = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql_);
                Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection_.getStatus());
                Assert.assertEquals(1, statement.getConnectionId());
                Assert.assertEquals(sql_, statement.sql());
            }
        }

        // インスタンスを生成
        TestInterface instance = new ThreadLocalInstanceFactory<TestInterface>(TestInterface.class, Test01Impl.class)
                .getInsatnce();
        Handler01 handler = new Handler01();
        handler.setLookupName(lookupname01);
        handler.setSql(sql01);

        // トランザクション境界内の処理を実行
        instance.method(handler);

        // 処理終了後のコネクションの状態を確認
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, handler.getMyConnection().getStatus());
    }

    /**
     * ThreadLocalInstanceFactoryで取得したインスタンスについて、メソッド内の接続操作と、メソッド終了後のロールバックを確認
     * 
     * @throws Exception
     */
    @Test
    public void test02() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String sql01 = "test sql01";

        // 初期設定
        NaocoCoreInitializer.initialize(ThreadLocalInstanceFactoryTest01.class.getName() + "_test02", null);

        // 実行クラスの定義
        class Handler01 implements TestHandler {
            private DummyConnection connection_;

            private String sql_;

            private String lookupname_;

            public DummyConnection getMyConnection() {
                return connection_;
            }

            public void setLookupName(String lookupname) {
                lookupname_ = lookupname;
            }

            public void setSql(String sql) {
                sql_ = sql;
            }

            @Override
            public void handle(TestHandler... handle) throws Exception {
                // 接続の取得
                connection_ = (DummyConnection) NaocoCoreFacade.getConnection();
                Assert.assertEquals(1, connection_.getId());
                Assert.assertEquals(lookupname_, connection_.getLookupName());
                // ステートメントの取得
                DummyPreparedStatement statement = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql_);
                Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection_.getStatus());
                Assert.assertEquals(1, statement.getConnectionId());
                Assert.assertEquals(sql_, statement.sql());
                // 例外を浮揚
                throw new Exception("for test");
            }
        }

        // インスタンスを生成
        TestInterface instance = new ThreadLocalInstanceFactory<TestInterface>(TestInterface.class, Test01Impl.class)
                .getInsatnce();
        Handler01 handler = new Handler01();
        handler.setLookupName(lookupname01);
        handler.setSql(sql01);

        // トランザクション境界内の処理を実行
        try {
            instance.method(handler);
            Assert.fail();
        } catch (Exception e) {
        }

        // 処理終了後のコネクションの状態を確認
        Assert.assertEquals(DummyConnection.STATUS.ROLLBACKED_CLOSED, handler.getMyConnection().getStatus());
    }

    /**
     * トランザクション境界をTakeOverでネストした場合に、処理終了後に接続が完了することを確認する
     * 
     * @throws Exception
     */
    @Test
    public void test03() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";

        // 初期設定
        NaocoCoreInitializer.initialize(ThreadLocalInstanceFactoryTest01.class.getName() + "_test03", null);

        // 実行クラスの定義
        class Handler01 implements TestHandler {
            private DummyConnection connection_;

            private String sql_;

            private String lookupname_;

            public DummyConnection getMyConnection() {
                return connection_;
            }

            public void setLookupName(String lookupname) {
                lookupname_ = lookupname;
            }

            public void setSql(String sql) {
                sql_ = sql;
            }

            @Override
            public void handle(TestHandler... handle) throws Exception {
                // 接続の取得
                connection_ = (DummyConnection) NaocoCoreFacade.getConnection();
                Assert.assertEquals(1, connection_.getId());
                Assert.assertEquals(lookupname_, connection_.getLookupName());
                // ステートメントの取得
                DummyPreparedStatement statement = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql_);
                Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection_.getStatus());
                Assert.assertEquals(1, statement.getConnectionId());
                Assert.assertEquals(sql_, statement.sql());

                // 次の処理を呼出し
                TestInterface instance = new ThreadLocalInstanceFactory<TestInterface>(TestInterface.class,
                        Test01Impl.class).getInsatnce();
                instance.method(Arrays.copyOfRange(handle, 1, 3));
            }
        }

        class Handler02 implements TestHandler {
            private DummyConnection connection_;

            private String sql_;

            private String lookupname_;

            public DummyConnection getMyConnection() {
                return connection_;
            }

            public void setLookupName(String lookupname) {
                lookupname_ = lookupname;
            }

            public void setSql(String sql) {
                sql_ = sql;
            }

            @Override
            public void handle(TestHandler... handle) throws Exception {
                // 接続の取得
                connection_ = (DummyConnection) NaocoCoreFacade.getConnection();
                Assert.assertEquals(1, connection_.getId());
                Assert.assertEquals(lookupname_, connection_.getLookupName());
                // ステートメントの取得
                DummyPreparedStatement statement = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql_);
                Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection_.getStatus());
                Assert.assertEquals(1, statement.getConnectionId());
                Assert.assertEquals(sql_, statement.sql());

                // 次の処理を呼出し
                TestInterface instance = new ThreadLocalInstanceFactory<TestInterface>(TestInterface.class,
                        Test01Impl.class).getInsatnce();
                instance.method(Arrays.copyOfRange(handle, 1, 2));
            }
        }

        class Handler03 implements TestHandler {
            private DummyConnection connection_;

            private String sql_;

            private String lookupname_;

            public DummyConnection getMyConnection() {
                return connection_;
            }

            public void setLookupName(String lookupname) {
                lookupname_ = lookupname;
            }

            public void setSql(String sql) {
                sql_ = sql;
            }

            @Override
            public void handle(TestHandler... handle) throws Exception {
                // 接続の取得
                connection_ = (DummyConnection) NaocoCoreFacade.getConnection();
                Assert.assertEquals(1, connection_.getId());
                Assert.assertEquals(lookupname_, connection_.getLookupName());
                // ステートメントの取得
                DummyPreparedStatement statement = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql_);
                Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection_.getStatus());
                Assert.assertEquals(1, statement.getConnectionId());
                Assert.assertEquals(sql_, statement.sql());
            }
        }

        // インスタンスを生成
        TestInterface instance = new ThreadLocalInstanceFactory<TestInterface>(TestInterface.class, Test01Impl.class)
                .getInsatnce();
        Handler01 handler01 = new Handler01();
        handler01.setLookupName(lookupname01);
        handler01.setSql(sql01);

        Handler02 handler02 = new Handler02();
        handler02.setLookupName(lookupname01);
        handler02.setSql(sql02);

        Handler03 handler03 = new Handler03();
        handler03.setLookupName(lookupname01);
        handler03.setSql(sql03);

        // トランザクション境界内の処理を実行
        instance.method(handler01, handler02, handler03);

        // 処理終了後のコネクションの状態を確認
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, handler01.getMyConnection().getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, handler02.getMyConnection().getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, handler03.getMyConnection().getStatus());
    }

    /**
     * トランザクション境界をNewでネストした場合に、処理終了後に接続が完了することを確認する
     * 
     * @throws Exception
     */
    @Test
    public void test04() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";

        // 初期設定
        NaocoCoreInitializer.initialize(ThreadLocalInstanceFactoryTest01.class.getName() + "_test04", null);

        // 実行クラスの定義
        class Handler01 implements TestHandler {
            private DummyConnection connection_;

            private String sql_;

            private String lookupname_;

            public DummyConnection getMyConnection() {
                return connection_;
            }

            public void setLookupName(String lookupname) {
                lookupname_ = lookupname;
            }

            public void setSql(String sql) {
                sql_ = sql;
            }

            @Override
            public void handle(TestHandler... handle) throws Exception {
                // 接続の取得
                connection_ = (DummyConnection) NaocoCoreFacade.getConnection();
                Assert.assertEquals(1, connection_.getId());
                Assert.assertEquals(lookupname_, connection_.getLookupName());
                // ステートメントの取得
                DummyPreparedStatement statement = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql_);
                Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection_.getStatus());
                Assert.assertEquals(1, statement.getConnectionId());
                Assert.assertEquals(sql_, statement.sql());

                // 次の処理を呼出し
                TestInterface instance = new ThreadLocalInstanceFactory<TestInterface>(TestInterface.class,
                        Test11Impl.class).getInsatnce();
                instance.method(Arrays.copyOfRange(handle, 1, 3));
            }
        }

        class Handler02 implements TestHandler {
            private DummyConnection connection_;

            private String sql_;

            private String lookupname_;

            public DummyConnection getMyConnection() {
                return connection_;
            }

            public void setLookupName(String lookupname) {
                lookupname_ = lookupname;
            }

            public void setSql(String sql) {
                sql_ = sql;
            }

            @Override
            public void handle(TestHandler... handle) throws Exception {
                // 接続の取得
                connection_ = (DummyConnection) NaocoCoreFacade.getConnection();
                Assert.assertEquals(2, connection_.getId());
                Assert.assertEquals(lookupname_, connection_.getLookupName());
                // ステートメントの取得
                DummyPreparedStatement statement = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql_);
                Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection_.getStatus());
                Assert.assertEquals(2, statement.getConnectionId());
                Assert.assertEquals(sql_, statement.sql());

                // 次の処理を呼出し
                TestInterface instance = new ThreadLocalInstanceFactory<TestInterface>(TestInterface.class,
                        Test11Impl.class).getInsatnce();
                instance.method(Arrays.copyOfRange(handle, 1, 2));
            }
        }

        class Handler03 implements TestHandler {
            private DummyConnection connection_;

            private String sql_;

            private String lookupname_;

            public DummyConnection getMyConnection() {
                return connection_;
            }

            public void setLookupName(String lookupname) {
                lookupname_ = lookupname;
            }

            public void setSql(String sql) {
                sql_ = sql;
            }

            @Override
            public void handle(TestHandler... handle) throws Exception {
                // 接続の取得
                connection_ = (DummyConnection) NaocoCoreFacade.getConnection();
                Assert.assertEquals(3, connection_.getId());
                Assert.assertEquals(lookupname_, connection_.getLookupName());
                // ステートメントの取得
                DummyPreparedStatement statement = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql_);
                Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection_.getStatus());
                Assert.assertEquals(3, statement.getConnectionId());
                Assert.assertEquals(sql_, statement.sql());
            }
        }

        // インスタンスを生成
        TestInterface instance = new ThreadLocalInstanceFactory<TestInterface>(TestInterface.class, Test11Impl.class)
                .getInsatnce();
        Handler01 handler01 = new Handler01();
        handler01.setLookupName(lookupname01);
        handler01.setSql(sql01);

        Handler02 handler02 = new Handler02();
        handler02.setLookupName(lookupname01);
        handler02.setSql(sql02);

        Handler03 handler03 = new Handler03();
        handler03.setLookupName(lookupname01);
        handler03.setSql(sql03);

        // トランザクション境界内の処理を実行
        instance.method(handler01, handler02, handler03);

        // 処理終了後のコネクションの状態を確認
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, handler01.getMyConnection().getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, handler02.getMyConnection().getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, handler03.getMyConnection().getStatus());
    }

    /**
     * 異なるルックアップ名でトランザクション境界をネストした場合に、処理終了後に接続が完了することを確認する
     * 
     * @throws Exception
     */
    @Test
    public void test05() throws Exception {
        // 設定値の宣言
        final String lookupname01 = "java:comp/env/jdbc/test01";
        final String lookupname02 = "java:comp/env/jdbc/test02";
        final String lookupname03 = "java:comp/env/jdbc/test03";
        final String sql01 = "test sql01";
        final String sql02 = "test sql02";
        final String sql03 = "test sql03";

        // 初期設定
        NaocoCoreInitializer.initialize(ThreadLocalInstanceFactoryTest01.class.getName() + "_test05", null);

        // 実行クラスの定義
        class Handler01 implements TestHandler {
            private DummyConnection connection_;

            private String sql_;

            private String lookupname_;

            public DummyConnection getMyConnection() {
                return connection_;
            }

            public void setLookupName(String lookupname) {
                lookupname_ = lookupname;
            }

            public void setSql(String sql) {
                sql_ = sql;
            }

            @Override
            public void handle(TestHandler... handle) throws Exception {
                // 接続の取得
                connection_ = (DummyConnection) NaocoCoreFacade.getConnection();
                Assert.assertEquals(1, connection_.getId());
                Assert.assertEquals(lookupname_, connection_.getLookupName());
                // ステートメントの取得
                DummyPreparedStatement statement = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql_);
                Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection_.getStatus());
                Assert.assertEquals(1, statement.getConnectionId());
                Assert.assertEquals(sql_, statement.sql());

                // 次の処理を呼出し
                TestInterface instance = new ThreadLocalInstanceFactory<TestInterface>(TestInterface.class,
                        Test02Impl.class).getInsatnce();
                instance.method(Arrays.copyOfRange(handle, 1, 3));
            }
        }

        class Handler02 implements TestHandler {
            private DummyConnection connection_;

            private String sql_;

            private String lookupname_;

            public DummyConnection getMyConnection() {
                return connection_;
            }

            public void setLookupName(String lookupname) {
                lookupname_ = lookupname;
            }

            public void setSql(String sql) {
                sql_ = sql;
            }

            @Override
            public void handle(TestHandler... handle) throws Exception {
                // 接続の取得
                connection_ = (DummyConnection) NaocoCoreFacade.getConnection();
                Assert.assertEquals(2, connection_.getId());
                Assert.assertEquals(lookupname_, connection_.getLookupName());
                // ステートメントの取得
                DummyPreparedStatement statement = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql_);
                Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection_.getStatus());
                Assert.assertEquals(2, statement.getConnectionId());
                Assert.assertEquals(sql_, statement.sql());

                // 次の処理を呼出し
                TestInterface instance = new ThreadLocalInstanceFactory<TestInterface>(TestInterface.class,
                        Test03Impl.class).getInsatnce();
                instance.method(Arrays.copyOfRange(handle, 1, 2));
            }
        }

        class Handler03 implements TestHandler {
            private DummyConnection connection_;

            private String sql_;

            private String lookupname_;

            public DummyConnection getMyConnection() {
                return connection_;
            }

            public void setLookupName(String lookupname) {
                lookupname_ = lookupname;
            }

            public void setSql(String sql) {
                sql_ = sql;
            }

            @Override
            public void handle(TestHandler... handle) throws Exception {
                // 接続の取得
                connection_ = (DummyConnection) NaocoCoreFacade.getConnection();
                Assert.assertEquals(3, connection_.getId());
                Assert.assertEquals(lookupname_, connection_.getLookupName());
                // ステートメントの取得
                DummyPreparedStatement statement = (DummyPreparedStatement) NaocoCoreFacade.prepareStatement(sql_);
                Assert.assertEquals(DummyConnection.STATUS.UNCOMMITED_UNCLOSED, connection_.getStatus());
                Assert.assertEquals(3, statement.getConnectionId());
                Assert.assertEquals(sql_, statement.sql());
            }
        }

        // インスタンスを生成
        TestInterface instance = new ThreadLocalInstanceFactory<TestInterface>(TestInterface.class, Test11Impl.class)
                .getInsatnce();
        Handler01 handler01 = new Handler01();
        handler01.setLookupName(lookupname01);
        handler01.setSql(sql01);

        Handler02 handler02 = new Handler02();
        handler02.setLookupName(lookupname02);
        handler02.setSql(sql02);

        Handler03 handler03 = new Handler03();
        handler03.setLookupName(lookupname03);
        handler03.setSql(sql03);

        // トランザクション境界内の処理を実行
        instance.method(handler01, handler02, handler03);

        // 処理終了後のコネクションの状態を確認
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, handler01.getMyConnection().getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, handler02.getMyConnection().getStatus());
        Assert.assertEquals(DummyConnection.STATUS.COMMITTED_CLOSED, handler03.getMyConnection().getStatus());
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes

    public static interface TestInterface {
        public void method(TestHandler... handle) throws Exception;
    }

    public static interface TestHandler {
        public void handle(TestHandler... handle) throws Exception;
    }

    // ///////////////////////

    @Transaction(lookupName = "java:comp/env/jdbc/test01", type = TransactionType.TAKEOVER)
    public static class Test01Impl implements TestInterface {
        @Override
        public void method(TestHandler... handle) throws Exception {
            handle[0].handle(handle);
        }
    }

    @Transaction(lookupName = "java:comp/env/jdbc/test01", type = TransactionType.NEW)
    public static class Test11Impl implements TestInterface {
        @Override
        public void method(TestHandler... handle) throws Exception {
            handle[0].handle(handle);
        }
    }

    @Transaction(lookupName = "java:comp/env/jdbc/test02", type = TransactionType.TAKEOVER)
    public static class Test02Impl implements TestInterface {
        @Override
        public void method(TestHandler... handle) throws Exception {
            handle[0].handle(handle);
        }
    }

    @Transaction(lookupName = "java:comp/env/jdbc/test03", type = TransactionType.TAKEOVER)
    public static class Test03Impl implements TestInterface {
        @Override
        public void method(TestHandler... handle) throws Exception {
            handle[0].handle(handle);
        }
    }
}
