package jp.gr.naoco.sample.dummy;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class DummyConnection implements Connection {
    public enum STATUS {
        ERROR, //
        UNCOMMITED_UNCLOSED, //
        COMMITTED_UNCLOSED, //
        ROLLBACKED_UNCLOSED, //
        COMMITTED_CLOSED, //
        ROLLBACKED_CLOSED, //
        UNCOMMITED_CLOSED; //
    }

    private static int count = 1;

    private static final String LOCK = "LOCK";

    private int num_;

    private String lookupName_;

    private boolean isCommitted_ = false;

    private boolean isRollbacked_ = false;

    private boolean isClosed_ = false;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public DummyConnection(String lookupName) {
        lookupName_ = lookupName;
        synchronized (LOCK) {
            num_ = count;
            count++;
        }
        System.out.println("DummyConnection#create: lookupName=" + lookupName_ + ", num=" + num_);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods (for test)

    /** この接続のIDを取得する */
    public int getId() {
        return num_;
    }

    /** この接続の状態を、未コミット未クローズに戻す */
    public void clear() {
        isCommitted_ = false;
        isRollbacked_ = false;
        isClosed_ = false;

    }

    /**
     * この接続の状態を返却する。
     */
    public STATUS getStatus() {
        if (!isCommitted_ && !isRollbacked_ && !isClosed_) {
            return STATUS.UNCOMMITED_UNCLOSED;
        }
        if (isCommitted_ && !isRollbacked_ && !isClosed_) {
            return STATUS.COMMITTED_UNCLOSED;
        }
        if (!isCommitted_ && isRollbacked_ && !isClosed_) {
            return STATUS.ROLLBACKED_UNCLOSED;
        }
        if (isCommitted_ && !isRollbacked_ && isClosed_) {
            return STATUS.COMMITTED_CLOSED;
        }
        if (!isCommitted_ && isRollbacked_ && isClosed_) {
            return STATUS.ROLLBACKED_CLOSED;
        }
        if (!isCommitted_ && !isRollbacked_ && isClosed_) {
            return STATUS.UNCOMMITED_CLOSED;
        }
        return STATUS.ERROR;
    }

    /** この接続のルックアップネームを取得する */
    public String getLookupName() {
        return lookupName_;
    }

    /** IDのカウントを初期値(1)に戻す */
    public static void clearIdCount() {
        count = 1;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Overriden of interface Connection

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public Statement createStatement() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new DummyPreparedStatement(lookupName_, num_, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        System.out.println("DummyConnection#setAutoCommit:" + autoCommit);
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public void commit() throws SQLException {
        isRollbacked_ = false;
        isCommitted_ = true;
        System.out.println("DummyConnection#commit: lookupName=" + lookupName_ + ", num=" + num_);
    }

    @Override
    public void rollback() throws SQLException {
        isRollbacked_ = true;
        isCommitted_ = false;
        System.out.println("DummyConnection#rollback: lookupName=" + lookupName_ + ", num=" + num_);
    }

    @Override
    public void close() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        System.out.println("DummyConnection#close: lookupName=" + lookupName_ + ", num=" + num_);
        isClosed_ = true;
    }

    @Override
    public boolean isClosed() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return isClosed_;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return new DummyDatabaseMetadata();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public String getCatalog() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public int getHoldability() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public Clob createClob() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public Blob createBlob() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public NClob createNClob() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return "";
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        Properties result = new Properties();
        result.setProperty("className", "DummyConnection");
        return result;
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public String getSchema() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }
}
