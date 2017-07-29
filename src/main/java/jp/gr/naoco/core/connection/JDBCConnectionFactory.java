package jp.gr.naoco.core.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import jp.gr.naoco.core.conf.Configuration;
import jp.gr.naoco.core.conf.Configuration.DBConfiguration;

/**
 * DriverManagerを利用してDBコネクションを取得する
 */
public class JDBCConnectionFactory implements ConnectionFactory {
    static {
        Configuration.dbCloserHook();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public JDBCConnectionFactory() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public Connection create(String lookupName) throws SQLException, ClassNotFoundException {
        DBConfiguration dbConfig = Configuration.getDbConfig(lookupName);
        Class.forName(dbConfig.getDriver());
        return DriverManager.getConnection(dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword());
    }
}
