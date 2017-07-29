package jp.gr.naoco.external.dbcp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

import jp.gr.naoco.core.conf.Configuration;
import jp.gr.naoco.core.conf.Configuration.ConfigrationToPropertiesKey;
import jp.gr.naoco.core.conf.Configuration.DBConfiguration;
import jp.gr.naoco.core.connection.ConnectionFactory;

/**
 * DBCPを利用してDBコネクションを取得する。
 * <p>
 * naocoの設定ファイルの db.factory.X の値に本クラスのクラス名を指定することで、DB接続の取得にDBCP（DBコネクションプール）を使用するようにする。
 * </p>
 * <p>
 * DBCPの設定は、naoco設定ファイルに記述する。
 * </p>
 *
 * <pre>
 * db.lookupName.1=java:comp/env/jdbc/oracle
 * db.factory.1=jp.gr.naoco.external.dbcp.DBCPDataSourceConnectionFactory
 * db.driver.1=oracle.jdbc.driver.OracleDriver
 * db.url.1=jdbc:oracle:thin:@oracle_database_ip:1521:DBNAME
 * db.user.1=user
 * db.password.1=password
 * db.retry.1=3
 * db.sleep.1=5000
 * # 下記はDBCP設定
 * db.initialSize.1=2
 * db.validationQuery.1=SELECT COUNT(*) FROM DUAL
 * </pre>
 * <p>
 * DBCPの設定をnaoco設定ファイルに記述しなかった場合、以下の項目の値はデフォルト値として設定される。それ以外の値は未設定となる。
 * </p>
 * <ul>
 * <li>maxTotal:100</li>
 * <li>maxIdle:100</li>
 * <li>initialSize:5</li>
 * <li>maxWaitMillis:3000</li>
 * </ul>
 * <h3>実行時に必要な外部ライブラリ</h3> 本クラスの実行には、Struts2の実行で必要な各ライブラリのほか、DBCPの以下のライブラリが必要となる。
 * <ul>
 * <li>commons-dbcp2-2.0.1.jar</li>
 * <li>commons-pool2-2.3.jar</li>
 *
 * @author naoco0917
 */
public class DBCPDataSourceConnectionFactory implements ConnectionFactory {
    private static final List<ConfigrationToPropertiesKey> CONF2PROP_KEY_LIST = new ArrayList<ConfigrationToPropertiesKey>();
    static {
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("defaultAutoCommit"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("defaultReadOnly"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("defaultTransactionIsolation"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("defaultCatalog"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("cacheState"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("driver", "driverClassName"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("lifo"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("maxTotal", "maxTotal", "100"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("maxIdle", "maxIdle", "100"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("minIdle"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("initialSize", "initialSize", "5"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("maxWaitMillis", "maxWaitMillis", "3000"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("testOnCreate"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("testOnBorrow"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("testOnReturn"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("timeBetweenEvictionRunsMillis"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("numTestsPerEvictionRun"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("minEvictableIdleTimeMillis"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("softMinEvictableIdleTimeMillis"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("evictionPolicyClassName"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("testWhileIdle"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("password"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("url"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("user", "username"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("validationQuery"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("validationQueryTimeout"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("jmxName"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("connectionInitSqls"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("accessToUnderlyingConnectionAllowed"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("removeAbandonedOnBorrow"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("removeAbandonedOnMaintenance"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("removeAbandonedTimeout"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("logAbandoned"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("poolPreparedStatements"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("maxOpenPreparedStatements"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("connectionProperties"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("maxConnLifetimeMillis"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("rollbackOnReturn"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("enableAutoCommitOnReturn"));
        CONF2PROP_KEY_LIST.add(new ConfigrationToPropertiesKey("defaultQueryTimeout"));
        Configuration.addDBConfigToPropertiesList(DBCPDataSourceConnectionFactory.class.getName(), CONF2PROP_KEY_LIST);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public DBCPDataSourceConnectionFactory() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public Connection create(String lookupName) throws SQLException, NamingException {
        try {
            BasicDataSource ds = DataSourceFactory.create(lookupName);
            return ds.getConnection();
        } catch (SQLException e) {
            throw e;
        } catch (NamingException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void clear() {
        DataSourceFactory.factoryMap_.clear();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    private static abstract class DataSourceFactory {
        protected BasicDataSource datasource_ = null;

        protected static final ConcurrentHashMap<String, DataSourceFactory> factoryMap_ = new ConcurrentHashMap<String, DataSourceFactory>();

        public static BasicDataSource create(String lookupName) throws NamingException {
            DataSourceFactory factory = factoryMap_.get(lookupName);
            if (null == factory) {
                synchronized (factoryMap_) {
                    factory = factoryMap_.get(lookupName);
                    if (null == factory) {
                        factoryMap_.put(lookupName, new ConcretizedFactory());
                        factory = factoryMap_.get(lookupName);
                    }
                }
            }
            factory._init(lookupName);
            return factory.getDataSource();
        }

        private BasicDataSource getDataSource() {
            return datasource_;
        }

        public abstract void _init(String lookupName) throws NamingException;
    }

    private static class ConcretizedFactory extends DataSourceFactory {
        @Override
        public void _init(String lookupName) throws NamingException {
            synchronized (this) {
                if (null != datasource_) {
                    return;
                }
                DBConfiguration conf = Configuration.getDbConfig(lookupName);
                Properties properties = conf.getProperties(DBCPDataSourceConnectionFactory.class.getName());
                try {
                    datasource_ = BasicDataSourceFactory.createDataSource(properties);
                } catch (NamingException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if ((null != datasource_) && (!datasource_.isClosed())) {
                                datasource_.close();
                            }
                        } catch (Throwable t) {
                            t.printStackTrace(System.err);
                        }
                    }
                }));
                factoryMap_.put(lookupName, new NoActionFactory(datasource_));
            }
        }
    }

    private static class NoActionFactory extends DataSourceFactory {

        public NoActionFactory(BasicDataSource datasource) {
            datasource_ = datasource;
        }

        @Override
        public void _init(String lookupName) throws NamingException {
            // nothing to do
        }
    }
}
