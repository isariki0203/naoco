package jp.gr.naoco.core.connection;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * JDDIデータソースを利用してDBコネクションを取得する
 */
public class DataSourceConnectionFactory implements ConnectionFactory {
    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public DataSourceConnectionFactory() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public Connection create(String lookupName) throws SQLException, NamingException {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(lookupName);
        return ds.getConnection();
    }
}
