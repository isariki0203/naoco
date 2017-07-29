package jp.gr.naoco.external.dbcp;

import java.util.ArrayList;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.gr.naoco.core.NaocoCoreFacade;
import jp.gr.naoco.core.NaocoCoreInitializer;
import jp.gr.naoco.core.log.LaolLogger;
import jp.gr.naoco.external.dbcp.DBCPDataSourceConnectionFactory;

public class DBCPDataSourceConnectionFactoryTest01 {

    private static final Object LOCK = new Object();
    private static final Object SLEEP = new Object();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Test Preparations

    @Before
    public void setup() throws Exception {
        DBCPDataSourceConnectionFactory.clear();
    }

    @After
    public void teardown() throws Exception {
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Test Cases

    /**
     * 正常系０１
     * 1スレッド、maxTotal=1で実行
     * 【注意】試験結果はログのスレッドIDとコネクションオブジェクトのハッシュ値から目視で確認すること。
     * 
     * @throws Exception
     */
    @Test
    public void test01() throws Exception {
        NaocoCoreInitializer.initialize(DBCPDataSourceConnectionFactoryTest01.class.getName() + "_test01", null);

        Thread thread01 = new Thread(new ConnectionRunner());
        thread01.start();

        synchronized (SLEEP) {
            SLEEP.wait(3000);
        }
        synchronized (LOCK) {
            LOCK.notifyAll();
        }
        thread01.join();
    }

    /**
     * 正常系０２
     * 3スレッド、maxTotal=1で実行
     * 【注意】試験結果はログのスレッドIDとコネクションオブジェクトのハッシュ値から目視で確認すること。
     * 
     * @throws Exception
     */
    @Test
    public void test02() throws Exception {
        NaocoCoreInitializer.initialize(DBCPDataSourceConnectionFactoryTest01.class.getName() + "_test02", null);

        Thread thread01 = new Thread(new ConnectionRunner());
        Thread thread02 = new Thread(new ConnectionRunner());
        Thread thread03 = new Thread(new ConnectionRunner());
        thread01.start();
        thread02.start();
        thread03.start();

        synchronized (SLEEP) {
            SLEEP.wait(3000);
        }
        synchronized (LOCK) {
            LOCK.notifyAll();
        }
        thread01.join();
        thread02.join();
        thread03.join();
    }

    /**
     * 正常系０３
     * 3スレッド、maxTotal=3で実行
     * 【注意】試験結果はログのスレッドIDとコネクションオブジェクトのハッシュ値から目視で確認すること。
     * 
     * @throws Exception
     */
    @Test
    public void test03() throws Exception {
        NaocoCoreInitializer.initialize(DBCPDataSourceConnectionFactoryTest01.class.getName() + "_test03", null);

        Thread thread01 = new Thread(new ConnectionRunner());
        Thread thread02 = new Thread(new ConnectionRunner());
        Thread thread03 = new Thread(new ConnectionRunner());
        thread01.start();
        thread02.start();
        thread03.start();

        synchronized (SLEEP) {
            SLEEP.wait(3000);
        }
        synchronized (LOCK) {
            LOCK.notifyAll();
        }
        thread01.join();
        thread02.join();
        thread03.join();
    }

    /**
     * 正常系０４
     * 30スレッド、maxTotal=3で実行
     * 【注意】試験結果はログのスレッドIDとコネクションオブジェクトのハッシュ値から目視で確認すること。
     * 
     * @throws Exception
     */
    @Test
    public void test04() throws Exception {
        NaocoCoreInitializer.initialize(DBCPDataSourceConnectionFactoryTest01.class.getName() + "_test04", null);

        ArrayList<Thread> threadList = new ArrayList<Thread>(30);
        for (int i = 0; i < 30; i++) {
            Thread thread = new Thread(new ConnectionRunner());
            thread.start();
            threadList.add(thread);
        }

        synchronized (SLEEP) {
            SLEEP.wait(3000);
        }
        synchronized (LOCK) {
            LOCK.notifyAll();
        }

        for (Thread thread : threadList) {
            thread.join();
        }
    }

    /**
     * 正常系０５
     * 100スレッド、maxTotal=10で実行
     * 【注意】試験結果はログのスレッドIDとコネクションオブジェクトのハッシュ値から目視で確認すること。
     * 
     * @throws Exception
     */
    @Test
    public void test05() throws Exception {
        NaocoCoreInitializer.initialize(DBCPDataSourceConnectionFactoryTest01.class.getName() + "_test05", null);

        ArrayList<Thread> threadList = new ArrayList<Thread>(100);
        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(new ConnectionRunner());
            thread.start();
            threadList.add(thread);
        }

        synchronized (SLEEP) {
            SLEEP.wait(3000);
        }
        synchronized (LOCK) {
            LOCK.notifyAll();
        }

        for (Thread thread : threadList) {
            thread.join();
        }
    }

    /**
     * 異常系０１
     * 3スレッド、maxTotal=1, maxIdle=0で実行
     * maxIdle=0なので、使用済みコネクションはプールに戻されず、新たなコネクションが割り当てられる。
     * 【注意】試験結果はログのスレッドIDとコネクションオブジェクトのハッシュ値から目視で確認すること。
     * 
     * @throws Exception
     */
    @Test
    public void test06() throws Exception {
        NaocoCoreInitializer.initialize(DBCPDataSourceConnectionFactoryTest01.class.getName() + "_test06", null);

        Thread thread01 = new Thread(new ConnectionRunner());
        Thread thread02 = new Thread(new ConnectionRunner());
        Thread thread03 = new Thread(new ConnectionRunner());
        thread01.start();
        thread02.start();
        thread03.start();

        synchronized (SLEEP) {
            SLEEP.wait(3000);
        }
        synchronized (LOCK) {
            LOCK.notifyAll();
        }
        thread01.join();
        thread02.join();
        thread03.join();
    }

    /**
     * 異常系０２
     * 3スレッド、maxTotal=1, db.maxWaitMillis.1=1で実行
     * db.maxWaitMillis.1=1なので、使用済みコネクションはタイムアウトとなり、リトライでコネクションが取得される。
     * 【注意】試験結果はログのスレッドIDとコネクションオブジェクトのハッシュ値から目視で確認すること。
     * 
     * @throws Exception
     */
    @Test
    public void test07() throws Exception {
        NaocoCoreInitializer.initialize(DBCPDataSourceConnectionFactoryTest01.class.getName() + "_test07", null);

        Thread thread01 = new Thread(new ConnectionRunner());
        Thread thread02 = new Thread(new ConnectionRunner());
        Thread thread03 = new Thread(new ConnectionRunner());
        thread01.start();
        thread02.start();
        thread03.start();

        synchronized (SLEEP) {
            SLEEP.wait(3000);
        }
        synchronized (LOCK) {
            LOCK.notifyAll();
        }
        thread01.join();
        thread02.join();
        thread03.join();
    }

    /**
     * 正常系０６
     * （9スレッド、maxTotal=3）×3スキーマで実行
     * 【注意】試験結果はログのスレッドIDとコネクションオブジェクトのハッシュ値から目視で確認すること。
     * 
     * @throws Exception
     */
    @Test
    public void test08() throws Exception {
        NaocoCoreInitializer.initialize(DBCPDataSourceConnectionFactoryTest01.class.getName() + "_test08", null);

        ArrayList<Thread> threadList = new ArrayList<Thread>(27);
        for (int i = 0; i < 9; i++) {
            Thread thread = new Thread(new ConnectionRunner("java:comp/env/jdbc/test01"));
            thread.start();
            threadList.add(thread);
        }
        for (int i = 0; i < 9; i++) {
            Thread thread = new Thread(new ConnectionRunner("java:comp/env/jdbc/test02"));
            thread.start();
            threadList.add(thread);
        }
        for (int i = 0; i < 9; i++) {
            Thread thread = new Thread(new ConnectionRunner("java:comp/env/jdbc/test03"));
            thread.start();
            threadList.add(thread);
        }

        synchronized (SLEEP) {
            SLEEP.wait(3000);
        }
        synchronized (LOCK) {
            LOCK.notifyAll();
        }

        for (Thread thread : threadList) {
            thread.join();
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    public static class ConnectionRunner implements Runnable {

        private String lookupName_;

        public ConnectionRunner() {
            this("java:comp/env/jdbc/test01");
        }

        public ConnectionRunner(String lookupName) {
            lookupName_ = lookupName;
        }

        @Override
        public void run() {
            try {
                LOG.info("thread start.");
                synchronized (LOCK) {
                    LOCK.wait();
                }
                NaocoCoreFacade.startTransaction(lookupName_);
                DelegatingConnection<?> connection = (DelegatingConnection<?>) NaocoCoreFacade.getConnection();
                LOG.info("****** THREAD_ID=" + Thread.currentThread().getId() + " CONNECTION_HASH="
                        + System.identityHashCode(connection.getInnermostDelegateInternal()));
                synchronized (this) {
                    this.wait(1000);
                }
                NaocoCoreFacade.commitTransaction();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(DBCPDataSourceConnectionFactoryTest01.class.getName());
}
