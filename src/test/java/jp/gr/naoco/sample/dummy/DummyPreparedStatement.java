package jp.gr.naoco.sample.dummy;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class DummyPreparedStatement implements PreparedStatement {
    private static int count_ = 1;

    private static String lastSql_ = null;

    private int num_;

    private String lookupName_;

    private int connectionNum_;

    private boolean isClosed_ = false;

    private String sql_ = null;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public DummyPreparedStatement(String lookupName, int connectionNum, String sql) {
        lookupName_ = lookupName;
        connectionNum_ = connectionNum;
        num_ = count_;
        setSql(sql);
        count_++;
        System.out.println("create statement(num=" + num_ + ", lookupName=" + lookupName_ + ", connectionNum="
                + connectionNum_ + ")");
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods (for test)

    /** 接続の状態を未接続に戻す */
    public void clear() {
        isClosed_ = false;
    }

    /** このステートメントで最後に実行したSQL文を取得する */
    public String sql() {
        return sql_;
    }

    /** このステートメントに関連するDummyConnectionのIDを取得する */
    public int getConnectionId() {
        return connectionNum_;
    }

    /** DummyPreparedStatementで最後に実行したSQL文を取得する */
    public static String lastSql() {
        return lastSql_;
    }

    private void setSql(String sql) {
        sql_ = sql;
        lastSql_ = sql;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Overriden of interface PreparedStatement

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        setSql(sql);
        return new DummyResultSet();
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        setSql(sql);
        return 0;
    }

    @Override
    public void close() throws SQLException {
        isClosed_ = true;
        System.out.println("close statement(num=" + num_ + ", lookupName=" + lookupName_ + ", connectionNum="
                + connectionNum_ + ")");
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public int getMaxRows() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void cancel() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
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
    public void setCursorName(String name) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        setSql(sql);
        return false;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return new DummyResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public int getFetchDirection() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public int getFetchSize() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        setSql(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public int[] executeBatch() throws SQLException {
        System.out.println("executeBatch");
        return null;
    }

    @Override
    public Connection getConnection() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return new DummyResultSet();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        setSql(sql);
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        setSql(sql);
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        setSql(sql);
        return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        setSql(sql);
        return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        setSql(sql);
        return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        setSql(sql);
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public boolean isPoolable() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

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
    public ResultSet executeQuery() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return new DummyResultSet();
    }

    @Override
    public int executeUpdate() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void clearParameters() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public boolean execute() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public void addBatch() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
    }
}
