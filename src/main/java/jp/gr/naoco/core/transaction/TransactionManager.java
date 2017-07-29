package jp.gr.naoco.core.transaction;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.NamingException;

import jp.gr.naoco.core.conf.Configuration;
import jp.gr.naoco.core.conf.Configuration.DBConfiguration;
import jp.gr.naoco.core.connection.ConnectionFactory;
import jp.gr.naoco.core.exception.ConnectionInstantiationException;
import jp.gr.naoco.core.log.LaolLogger;
import jp.gr.naoco.db.entity.ORMapper;

public class TransactionManager {
    private static ThreadLocal<TransactionStack> transactionStack_ = new ThreadLocal<TransactionStack>();

    /**
     * ここはキャッシュではなく、スレッドを横断したクローズ対象コネクションの集合なので、java.util.concurrentは使わずに、
     * 取得と追加で同期をとるようにする
     */
    private static Set<ConnectionWrapper> connectionSet_ = Collections
            .synchronizedSet(new HashSet<ConnectionWrapper>());

    private static final int MAX_UNCLOSED_STATEMENT_NUM = 30;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private TransactionManager() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    public static Connection getCurrentConnection() {
        return getTransactionStack_().getCurrentConnection();
    }

    public static String getCurrentConnectionDriverName() {
        return getTransactionStack_().getCurrentConnectionDriverName();
    }

    public static void reserveRollback() {
        getTransactionStack_().reserveRollback();
    }

    public static void commitForce() throws SQLException {
        getTransactionStack_().commitForce();
    }

    public static void rollbackForce() throws SQLException {
        getTransactionStack_().rollbackForce();
    }

    public static void commitAllTransaction() throws SQLException {
        getTransactionStack_().commitAll();
    }

    public static void rollbackAllTransaction() throws Exception {
        getTransactionStack_().rollbackAll();
    }

    public static PreparedStatement storePreparedStatement(String sql) throws SQLException {
        TransactionStack stack = getTransactionStack_();
        PreparedStatement st = stack.getStoredPreparedStatement(sql);
        if (null != st) {
            return st;
        }
        st = prepareStatement(sql);
        stack.storePreparedStatement(sql, st);
        return st;
    }

    public static void closeAll() throws Exception {
        LOG.info("start closeAll");

        Exception e = null;
        TreeSet<ConnectionWrapper> connectionSet = new TreeSet<ConnectionWrapper>(new Comparator<ConnectionWrapper>() {
            @Override
            public int compare(ConnectionWrapper o1, ConnectionWrapper o2) {
                if ((null == o1) && (null == o2)) {
                    return 0;
                } else if (null == o1) {
                    return -1;
                } else if (null == o2) {
                    return 1;
                }
                return System.identityHashCode(o1) - System.identityHashCode(o2);
            }

        });
        synchronized (connectionSet_) {
            connectionSet.addAll(connectionSet_);
        }
        for (ConnectionWrapper connection : connectionSet) {
            try {
                if (!connection.get().isClosed()) {
                    LOG.info("try close connection:" + connection.get().getClientInfo().toString());
                    synchronized (connection) {
                        try {
                            connection.get().rollback();
                        } catch (SQLException e2) {
                            // nothing to do.
                        }
                        connection.get().close();
                    }
                    LOG.info("success close connection");
                }
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
                if (null == e) {
                    e = ex;
                }
            }
        }
        transactionStack_ = new ThreadLocal<TransactionStack>();
        connectionSet_.clear();
        if (null != e) {
            throw e;
        }

        LOG.info("end closeAll");
    }

    public static CallableStatement prepareCall(String sql) throws SQLException {
        CallableStatement statement = getCurrentConnection().prepareCall(sql);
        getTransactionStack_().setSyncClosePreparedStatement(statement);
        return statement;
    }

    public static CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        CallableStatement statement = getCurrentConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
        getTransactionStack_().setSyncClosePreparedStatement(statement);
        return statement;
    }

    public static CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        CallableStatement statement = getCurrentConnection().prepareCall(sql, resultSetType, resultSetConcurrency,
                resultSetHoldability);
        getTransactionStack_().setSyncClosePreparedStatement(statement);
        return statement;
    }

    public static PreparedStatement prepareStatement(String sql) throws SQLException {
        PreparedStatement statement = getCurrentConnection().prepareStatement(sql);
        getTransactionStack_().setSyncClosePreparedStatement(statement);
        return statement;
    }

    public static PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        PreparedStatement statement = getCurrentConnection().prepareStatement(sql, autoGeneratedKeys);
        getTransactionStack_().setSyncClosePreparedStatement(statement);
        return statement;
    }

    public static PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        PreparedStatement statement = getCurrentConnection().prepareStatement(sql, columnIndexes);
        getTransactionStack_().setSyncClosePreparedStatement(statement);
        return statement;
    }

    public static PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        PreparedStatement statement = getCurrentConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
        getTransactionStack_().setSyncClosePreparedStatement(statement);
        return statement;
    }

    public static PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        PreparedStatement statement = getCurrentConnection().prepareStatement(sql, resultSetType, resultSetConcurrency,
                resultSetHoldability);
        getTransactionStack_().setSyncClosePreparedStatement(statement);
        return statement;
    }

    public static PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        PreparedStatement statement = getCurrentConnection().prepareStatement(sql, columnNames);
        getTransactionStack_().setSyncClosePreparedStatement(statement);
        return statement;
    }

    public static void startTransaction(String lookupName) throws ConnectionInstantiationException {
        ConnectionWrapper connection = getNewConnection_(lookupName);
        String driverName = getDriverName_(connection.get(), lookupName);
        getTransactionStack_().pushTransaction(lookupName, connection, driverName);
        LOG.debug("start transaction:" + lookupName);
    }

    public static void commitTransaction(boolean needsCommit) throws SQLException {
        getTransactionStack_().popCommit(needsCommit);
    }

    public static void rollbackTransaction(boolean needsRollback) throws SQLException {
        getTransactionStack_().popRollback(needsRollback);
    }

    public static void takeoverTransaction(String lookupName) throws ConnectionInstantiationException {
        if (!getTransactionStack_().pushVirtual(lookupName)) {
            startTransaction(lookupName);
        }
    }

    // ///////////////////////

    private static TransactionStack getTransactionStack_() {
        TransactionStack stack = transactionStack_.get();
        if (null == stack) {
            stack = new TransactionStack();
            transactionStack_.set(stack);
        }
        return stack;
    }

    private static ConnectionWrapper getNewConnection_(String lookupName) throws ConnectionInstantiationException {
        DBConfiguration dbConfig = Configuration.getDbConfig(lookupName);
        try {
            ConnectionFactory factory = (ConnectionFactory) dbConfig.getConnectionFactory().newInstance();
            Connection connection = retryConnect(factory, lookupName, dbConfig.getRetry(), dbConfig.getSleep());
            connection.setAutoCommit(false);
            ConnectionWrapper wrapper = new ConnectionWrapper(connection);
            connectionSet_.add(wrapper);
            return wrapper;
        } catch (Exception e) {
            throw new ConnectionInstantiationException("Error in connection create:" + lookupName, e);
        }
    }

    private static Connection retryConnect(ConnectionFactory factory, String lookupName, int count, long interval)
            throws SQLException, ClassNotFoundException, NamingException, InterruptedException {
        try {
            long before = System.currentTimeMillis();
            Connection connection = factory.create(lookupName);
            long after = System.currentTimeMillis();
            LOG.info("GET CONNECTION(" + lookupName + ") " + (after - before) + "msec");
            return connection;
        } catch (SQLException e) {
            LOG.info(e.getMessage(), e);
            count -= 1;
            if (0 <= count) {
                Thread.sleep(interval);
                return retryConnect(factory, lookupName, count, interval);
            }
            throw e;
        }
    }

    private static String getDriverName_(Connection connection, String lookupName)
            throws ConnectionInstantiationException {
        try {
            return connection.getMetaData().getDriverName();
        } catch (SQLException e) {
            throw new ConnectionInstantiationException("Error in getteing connection driver nane:" + lookupName, e);
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes

    private static class TransactionStack {
        private LinkedList<TransactionStackElem> stack_ = new LinkedList<TransactionStackElem>();

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public TransactionStack() {
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        public void pushTransaction(String lookupName, ConnectionWrapper connection, String driverName) {
            stack_.push(new TransactionStackElemSubstance(lookupName, connection, driverName));
            LOG.trace("start transaction depth[" + (stack_.size() - 1) + "]." + lookupName);
        }

        public boolean pushVirtual(String lookupName) {
            if (stack_.isEmpty()) {
                return false;
            }
            TransactionStackElem needsPush = null;
            for (TransactionStackElem elem : stack_) {
                if ((elem instanceof TransactionStackElemSubstance) && elem.getLookupName().equals(lookupName)) {
                    needsPush = elem;
                    break;
                }
            }
            if (null != needsPush) {
                stack_.push(new TransactionStackElemVirtual((TransactionStackElemSubstance) needsPush));
                LOG.trace("takeover transaction depth[" + (stack_.size() - 1) + "]." + lookupName);
                return true;
            }
            return false;
        }

        public Connection getCurrentConnection() {
            if (stack_.isEmpty()) {
                throw new IllegalStateException("transaction is not start.");
            }
            return stack_.getFirst().getConnection();
        }

        public String getCurrentConnectionDriverName() {
            if (stack_.isEmpty()) {
                throw new IllegalStateException("transaction is not start.");
            }
            return stack_.getFirst().getDriverName();
        }

        public void reserveRollback() {
            TransactionStackElem elem = stack_.getFirst();
            elem.reserveRollback();
        }

        public void commitForce() throws SQLException {
            TransactionStackElem elem = stack_.getFirst();
            Connection connection = elem.getConnection();
            if (elem.isRollbackReserved()) {
                // 接続のチェック（クローズ済み、読み取り専用）はしないで、SQLException浮揚を容認する。
                synchronized (connection) {
                    connection.rollback();
                }
                LOG.debug("success rollback.");
            } else {
                // 接続のチェック（クローズ済み、読み取り専用）はしないで、SQLException浮揚を容認する。
                synchronized (connection) {
                    connection.commit();
                }
                LOG.debug("success commit.");
            }
            elem.cancelRollback();
        }

        public void rollbackForce() throws SQLException {
            TransactionStackElem elem = stack_.getFirst();
            // 接続のチェック（クローズ済み、読み取り専用）はしないで、SQLException浮揚を容認する。
            Connection connection = elem.getConnection();
            synchronized (connection) {
                connection.rollback();
            }
            LOG.debug("success rollback.");
            elem.cancelRollback();
        }

        public void popCommit(boolean needsCommit) throws SQLException {
            if (!stack_.isEmpty()) {
                TransactionStackElem elem = stack_.pop();
                if (elem.isRollbackReserved()) {
                    elem.rollbackTransaction(needsCommit);
                    LOG.trace("success rollback depth[" + stack_.size() + "].");
                } else {
                    elem.commitTransaction(needsCommit);
                    LOG.trace("success commit depth[" + stack_.size() + "].");
                }
            }
        }

        public void popRollback(boolean needsRollback) throws SQLException {
            if (!stack_.isEmpty()) {
                TransactionStackElem elem = stack_.pop();
                elem.rollbackTransaction(needsRollback);
                LOG.trace("success rollback depth[" + stack_.size() + "].");
            }
        }

        public void commitAll() throws SQLException {
            SQLException e = null;
            while (!stack_.isEmpty()) {
                TransactionStackElem elem = stack_.pop();
                try {
                    elem.commitTransaction(true);
                } catch (SQLException ex) {
                    if (null == e) {
                        e = ex;
                    }
                }
            }
            if (null != e) {
                throw e;
            }
        }

        public void rollbackAll() throws Exception {
            Exception e = null;
            while (!stack_.isEmpty()) {
                TransactionStackElem elem = stack_.pop();
                try {
                    elem.rollbackTransaction(true);
                } catch (Exception ex) {
                    if (null == e) {
                        e = ex;
                    }
                }
            }
            if (null != e) {
                throw e;
            }
        }

        public void setSyncClosePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
            stack_.getFirst().setSyncClosePreparedStatement(preparedStatement);
        }

        public void storePreparedStatement(String sql, PreparedStatement st) {
            if (stack_.isEmpty()) {
                throw new IllegalStateException("transaction is not start.");
            }
            stack_.getFirst().storePreparedStatement(sql, st);
        }

        public PreparedStatement getStoredPreparedStatement(String sql) {
            if (stack_.isEmpty()) {
                throw new IllegalStateException("transaction is not start.");
            }
            return stack_.getFirst().getStoredPreparedStatement(sql);
        }
    }

    private static interface TransactionStackElem {
        public String getLookupName();

        public void commitTransaction(boolean needsCommit) throws SQLException;

        public void rollbackTransaction(boolean needsRollback) throws SQLException;

        public Connection getConnection();

        public void reserveRollback();

        public void cancelRollback();

        public boolean isRollbackReserved();

        public void setSyncClosePreparedStatement(PreparedStatement preparedStatement) throws SQLException;

        public String getDriverName();

        public void storePreparedStatement(String sql, PreparedStatement st);

        public PreparedStatement getStoredPreparedStatement(String sql);
    }

    private static class TransactionStackElemSubstance implements TransactionStackElem {
        private ConnectionWrapper connection_;

        private String lookupName_;

        private boolean isRollbackReserved_ = false;

        private LinkedList<PreparedStatement> statementStack_ = new LinkedList<PreparedStatement>();

        private LinkedList<ORMapper.ResultSetIterator> iteratorStack_ = new LinkedList<ORMapper.ResultSetIterator>();

        private String driverName_;

        private HashMap<String, PreparedStatement> storedPreparedStatement_ = new HashMap<String, PreparedStatement>();

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        private TransactionStackElemSubstance(String lookupName, ConnectionWrapper connection, String driverName) {
            lookupName_ = lookupName;
            connection_ = connection;
            driverName_ = driverName;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public String getLookupName() {
            return lookupName_;
        }

        @Override
        public void commitTransaction(boolean needsCommit) throws SQLException {
            Connection connection = connection_.get();
            synchronized (connection_) {
                for (PreparedStatement st : storedPreparedStatement_.values()) {
                    st.executeBatch();
                }
                try {
                    if (!connection.isClosed() && // コネクションがクローズ済でないこと
                            !connection.isReadOnly() && // コネクションが読み取り専用でないこと
                            needsCommit) {
                        connection.commit();
                    }
                } finally {
                    try {
                        storedPreparedStatement_.clear();
                        closeStatement();
                    } finally {
                        try {
                            if (!connection.isClosed()) {
                                connection.close();
                                LOG.debug("connection committed and closed.");
                            }
                        } finally {
                            connectionSet_.remove(connection_);
                            connection_ = null;
                        }
                    }
                }
            }
        }

        @Override
        public void rollbackTransaction(boolean needsRollback) throws SQLException {
            Connection connection = connection_.get();
            synchronized (connection_) {
                try {
                    if (!connection.isClosed() && // コネクションがクローズ済でないこと
                            !connection.isReadOnly() && // コネクションが読み取り専用でないこと
                            needsRollback) {
                        connection.rollback();
                    }
                } finally {
                    try {
                        storedPreparedStatement_.clear();
                        closeStatement();
                    } finally {
                        try {
                            if (!connection.isClosed()) {
                                connection.close();
                                LOG.debug("connection rollback and closed.");
                            }
                        } finally {
                            connectionSet_.remove(connection_);
                            connection_ = null;
                        }
                    }
                }
            }
        }

        @Override
        public Connection getConnection() {
            return connection_.get();
        }

        @Override
        public void reserveRollback() {
            isRollbackReserved_ = true;
        }

        @Override
        public void cancelRollback() {
            isRollbackReserved_ = false;
        }

        @Override
        public boolean isRollbackReserved() {
            return isRollbackReserved_;
        }

        @Override
        public void setSyncClosePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
            if (MAX_UNCLOSED_STATEMENT_NUM < statementStack_.size()) {
                gabageStatement();
            }
            statementStack_.push(preparedStatement);
        }

        private void gabageStatement() throws SQLException {
            // タイムアウトしたResultSetIteratorのクローズ
            {
                LinkedList<ORMapper.ResultSetIterator> newStack = new LinkedList<ORMapper.ResultSetIterator>();
                while (!iteratorStack_.isEmpty()) {
                    ORMapper.ResultSetIterator iterator = iteratorStack_.pop();
                    if (!iterator.closeTimeout()) {
                        newStack.push(iterator);
                    }
                }
                iteratorStack_ = newStack;
            }
            // クローズ済みStatementの削除
            {
                LinkedList<PreparedStatement> newStack = new LinkedList<PreparedStatement>();
                while (!statementStack_.isEmpty()) {
                    PreparedStatement statement = statementStack_.pop();
                    if ((null != statement) && !statement.isClosed()) {
                        newStack.push(statement);
                    }
                }
                statementStack_ = newStack;
            }
        }

        private void closeStatement() throws SQLException {
            SQLException e = null;
            while (!statementStack_.isEmpty()) {
                PreparedStatement statement = statementStack_.pop();
                if (statement.isClosed()) {
                    continue;
                }
                try {
                    statement.close();
                } catch (SQLException ex) {
                    LOG.error(ex.getMessage(), ex);
                    if (null == e) {
                        e = ex;
                    }
                }
            }
            if (null != e) {
                throw e;
            }
        }

        @Override
        public String getDriverName() {
            return driverName_;
        }

        @Override
        public void storePreparedStatement(String sql, PreparedStatement st) {
            storedPreparedStatement_.put(sql, st);
        }

        @Override
        public PreparedStatement getStoredPreparedStatement(String sql) {
            return storedPreparedStatement_.get(sql);
        }
    }

    private static class TransactionStackElemVirtual implements TransactionStackElem {
        private TransactionStackElemSubstance substance_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        private TransactionStackElemVirtual(TransactionStackElemSubstance substance) {
            substance_ = substance;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public String getLookupName() {
            return substance_.getLookupName();
        }

        @Override
        public void commitTransaction(boolean needsCommit) throws SQLException {
            LOG.trace("taken over transaction(Dummy Commit!).");
            substance_ = null;
        }

        @Override
        public void rollbackTransaction(boolean needsRollback) throws SQLException {
            LOG.trace("taken over transaction(Dummy Rollback!).");
            substance_ = null;
        }

        @Override
        public Connection getConnection() {
            return substance_.getConnection();
        }

        @Override
        public void reserveRollback() {
            substance_.reserveRollback();
        }

        @Override
        public void cancelRollback() {
            substance_.cancelRollback();
        }

        @Override
        public boolean isRollbackReserved() {
            return substance_.isRollbackReserved();
        }

        @Override
        public void setSyncClosePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
            substance_.setSyncClosePreparedStatement(preparedStatement);
        }

        @Override
        public String getDriverName() {
            return substance_.getDriverName();
        }

        @Override
        public void storePreparedStatement(String sql, PreparedStatement st) {
            substance_.storePreparedStatement(sql, st);
        }

        @Override
        public PreparedStatement getStoredPreparedStatement(String sql) {
            return substance_.getStoredPreparedStatement(sql);
        }
    }

    private static class ConnectionWrapper {
        private Connection connection_;

        public ConnectionWrapper(Connection connection) {
            if (null == connection) {
                throw new IllegalArgumentException("Connection is null.");
            }
            connection_ = connection;
        }

        public Connection get() {
            return connection_;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ConnectionWrapper)) {
                return false;
            }
            return super.equals(o);
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(TransactionManager.class.getName());
}
