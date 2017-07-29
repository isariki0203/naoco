package jp.gr.naoco.core.conf;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jp.gr.naoco.core.annotation.Interceptor;
import jp.gr.naoco.core.connection.ConnectionFactory;
import jp.gr.naoco.core.connection.DataSourceConnectionFactory;
import jp.gr.naoco.core.connection.JDBCConnectionFactory;
import jp.gr.naoco.core.exception.ConfigurationException;
import jp.gr.naoco.core.factory.AbstractInstanceFactory;
import jp.gr.naoco.core.log.LaolLogger;
import jp.gr.naoco.core.transaction.TransactionManager;

/**
 * 設定ファイルの設定内容管理
 * <p>
 * 本ライブラリが使用する、以下の設定内容を管理する。
 * </p>
 * <ul>
 * <li>DB接続先</li>
 * <li>インターセプタークラス</li>
 * <li>DIコンテナ</li>
 * </ul>
 * <p>
 * 設定はproperties形式のファイルに、それぞれ以下の書式で記述する
 * </p>
 * <p>
 * DB設定（JDBCプレーン）
 *
 * <pre>
 * #ルックアップ名（DB設定を一意に識別する文字列）
 * db.lookupName.1=java:comp/env/jdbc/oracle01
 * #コネクションファクトリクラス名（jp.gr.naoco.core.connection.ConnectionFactoryのサブクラス名）
 * db.factory.1=jp.gr.naoco.core.connection.JDBCConnectionFactory
 * #ドライバクラス名
 * db.driver.1=oracle.jdbc.driver.OracleDriver
 * #DB URL
 * db.url.1=jdbc:oracle:thin:@localhost:1521:ORCL
 * #DBユーザ名
 * db.user.1=user
 * #DBパスワード
 * db.password.1=password
 * #DB接続リトライ回数（オプション：未設定時は0）
 * db.retry.1=3
 * #DB接続リトライ時スリープ時間msec（オプション：未設定時は0）
 * db.sleep.1=3000
 * </pre>
 * </p>
 * <p>
 * DB設定（JNDIルックアップ）（設定なしの場合は、呼出し時のルックアップ名でJNDIルックアップによる接続取得を試みる)
 *
 * <pre>
 * #JNDIルックアップ名（DB設定を一意に識別する文字列）
 * db.lookupName.2=java:comp/env/jdbc/oracle01
 * #コネクションファクトリクラス名（jp.gr.naoco.core.connection.ConnectionFactoryのサブクラス名）
 * db.factory.2=jp.gr.naoco.core.connection.DataSourceConnectionFactory
 * #DB接続リトライ回数（オプション：未設定時は0）
 * db.retry.2=3
 * #DB接続リトライ時スリープ時間msec（オプション：未設定時は0）
 * db.sleep.2=3000
 * </pre>
 * </p>
 * <p>
 * インターセプタークラス設定
 *
 * <pre>
 * interceptor.1=intercept.LogInterceptor
 * interceptor.2=intercept.ParameterOutputInterceptor
 * </pre>
 * </p>
 * <p>
 * DIコンテナ設定
 *
 * <pre>
 * di.interface.1=logic.SampleLogic
 * di.instanceClass.1=logic.impl.SampleLogicImpl
 * </pre>
 * </p>
 */
public class Configuration {
    private volatile static Map<String, DBConfiguration> dbConfigMap_ = new ConcurrentHashMap<String, DBConfiguration>();

    private volatile static Map<String, Class<?>> diConfigMap_ = new ConcurrentHashMap<String, Class<?>>();

    private volatile static Map<String, Object> diInstanceMap_ = new ConcurrentHashMap<String, Object>();

    private volatile static InterceptorConfiguration interceptor_ = new InterceptorConfiguration();

    private volatile static DBCloserHooker dbCloserHooker_ = DBCloserHooker.FIRST_TIME_HOOKER;

    private volatile static Map<String, List<ConfigrationToPropertiesKey>> dbConfigPropertiesMap_ = new ConcurrentHashMap<String, List<ConfigrationToPropertiesKey>>();

    // ///////////////////////

    private static final String DB_PREFIX = "db.";

    private static final String DB_LOOKUPNAME_PREFFIX = DB_PREFIX + "lookupName.";

    private static final String DB_CONNECTION_FACTORY_PREFIX = DB_PREFIX + "factory.";

    private static final String DB_DRIVER_PREFIX = DB_PREFIX + "driver.";

    private static final String DB_URL_PREFIX = DB_PREFIX + "url.";

    private static final String DB_USER_PREFIX = DB_PREFIX + "user.";

    private static final String DB_PASSWORD_PREFIX = DB_PREFIX + "password.";

    private static final String DB_RETRY_COUNT_PREFIX = DB_PREFIX + "retry.";

    private static final String DB_SLEEP_INTERVAL_PREFIX = DB_PREFIX + "sleep.";

    private static final String INTERCEPTOR_PREFIX = "interceptor.";

    private static final String DI_INTERFACE_PREFIX = "di.interface.";

    private static final String DI_INSTANCE_CLASS_PREFIX = "di.instanceClass.";

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private Configuration() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // methods

    /**
     * 指定したbasenameのpropertiesファイルの内容で設定を読み込む。
     *
     * @param basename
     */
    public static void initialize(String basename) {
        LOG.debug("start (basename=" + basename + ")");

        ResourceBundle bundle = ResourceBundle.getBundle(basename);
        initDb(bundle);
        initInterceptor(bundle);
        initDI(bundle);

        diInstanceMap_.clear();

        // InstanceFactoryサブクラスのキャッシュを全削除する。
        AbstractInstanceFactory.clearAllCache();

        LOG.debug("end (basename=" + basename + ")");
    }

    /**
     * 設定済のDB設定を取得する
     *
     * @param lookupName
     *            DB設定を識別する文字列
     * @return lookupNameに対応するDB設定を保持したオブジェクト
     */
    public static DBConfiguration getDbConfig(String lookupName) {
        DBConfiguration obj = dbConfigMap_.get(lookupName);
        if (null == obj) {
            // 未定義の場合はDataSource経由取得とする。
            addDbConfig(lookupName, 0, 0L);
            obj = dbConfigMap_.get(lookupName);
        }

        return obj;
    }

    /**
     * 設定済のインターセプター対象クラスのリストを取得する
     *
     * @return インターセプター対象クラスのリスト
     */
    public static List<Class<?>> getIinterceptorList() {
        return interceptor_.getInterceptorList();
    }

    /**
     * 設定済のDI設定より、インターフェースクラスに対するインスタンスを取得する
     *
     * @param interfaceClass
     *            インターフェースクラス
     * @return 指定したインターフェースクラスのインスタンス
     */
    public static Object createDIInstance(Class<?> interfaceClass)
            throws IllegalAccessException, InstantiationException {
        Class<?> clazz = diConfigMap_.get(interfaceClass.getName());
        if (null != clazz) {
            return clazz.newInstance();
        }
        return diInstanceMap_.get(interfaceClass.getName());
    }

    /**
     * DataSource経由で取得するDBコネクションのlookupName設定を追加する
     *
     * @param lookupName
     *            JNDI経由でDBコネクションを取得するためのDB設定を識別する文字列
     */
    public static void addDbConfig(String lookupName, int retryCount, long sleepInterval) {
        DBConfiguration conf = new DBConfiguration(lookupName, DataSourceConnectionFactory.class.getName(), null, null,
                null, null, Integer.toString(retryCount), Long.toString(sleepInterval));
        checkDBConf(conf);
        dbConfigMap_.put(lookupName, conf);

        LOG.info("(lookupName=" + lookupName + ")");
    }

    /**
     * DB設定を追加する
     * <p>
     * コネクションファクトリは{@link JDBCConnectionFactory}が設定される。
     * </p>
     *
     * @param lookupName
     *            DB設定を識別する文字列
     * @param driver
     *            DBドライバ
     * @param url
     *            DB接続先URL
     * @param user
     *            DB接続先ユーザ名
     * @param password
     *            DB接続先パスワード
     */
    public static void addDbConfig(String lookupName, String driver, String url, String user, String password) {
        addDbConfig(lookupName, JDBCConnectionFactory.class, driver, url, user, password, 0, 0L);
    }

    /**
     * DB設定を追加する
     *
     * @param lookupName
     *            DB設定を識別する文字列
     * @param factoryClass
     *            コネクションを取得するための{@link ConnectionFactory}サブクラス
     * @param driver
     *            DBドライバ
     * @param url
     *            DB接続先URL
     * @param user
     *            DB接続先ユーザ名
     * @param password
     *            DB接続先パスワード
     * @param retryCount
     *            DB接続リトライ回数
     * @param sleepInterval
     *            DB接続リトライ時のスリープ時間（msec）
     */
    public static void addDbConfig(String lookupName, Class<?> factoryClass, String driver, String url, String user,
            String password, int retryCount, long sleepInterval) {
        DBConfiguration conf = new DBConfiguration(lookupName, factoryClass.getName(), driver, url, user, password,
                Integer.toString(retryCount), Long.toString(sleepInterval));
        checkDBConf(conf);

        dbConfigMap_.put(lookupName, conf);
        LOG.info("(" + "lookupName=" + lookupName + ", factoryClass" + factoryClass.getName() + ", driver=" + driver
                + ", url=" + url + ", user=" + user + ", password=" + password + ")");
    }

    /**
     * インターセプター対象クラスを追加する
     *
     * @param className
     *            インターセプタ対象クラス名
     */
    public static void addInterceptor(String className) {
        checkInterceptor(className);
        interceptor_.addInterceptor(className);

        LOG.info("(className=" + className + ")");
    }

    /**
     * DI設定を追加する
     *
     * @param interfaceClass
     *            インターフェースクラス
     * @param instanceClass
     *            インスタンスクラス
     */
    public static void addDI(Class<?> interfaceClass, Class<?> instanceClass) {
        checkDIClasses(interfaceClass, instanceClass);
        diConfigMap_.put(interfaceClass.getName(), instanceClass);

        LOG.info("DependencyInjection Configuration :interface=" + interfaceClass.getName() + ", instance="
                + instanceClass.getName());
    }

    /**
     * DI設定について、インターフェースに対する実体クラスのインスタンスを指定する
     *
     * @param interfaceClass
     *            インターフェースクラス
     * @param instance
     *            インターフェースクラスに対するインスタンス
     */
    public static void addDIInstance(Class<?> interfaceClass, Object instance) {
        checkDIClasses(interfaceClass, instance.getClass());
        diInstanceMap_.put(interfaceClass.getName(), instance);

        LOG.info("DependencyInjection Configuration(Instance direct) :interface=" + interfaceClass.getName()
                + ", instance=" + instance.getClass().getName());
    }

    public static void addDBConfigToPropertiesList(String keyName, List<ConfigrationToPropertiesKey> ctpKeyList) {
        dbConfigPropertiesMap_.put(keyName, ctpKeyList);
    }

    public static void dbCloserHook() {
        dbCloserHooker_.addHookDBCloser();
    }

    // ///////////////////////

    private static void initDb(ResourceBundle bundle) {
        LOG.debug("start initDb");

        Enumeration<String> keys = bundle.getKeys();
        LinkedList<String> numList = new LinkedList<String>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.startsWith(DB_LOOKUPNAME_PREFFIX)) {
                String value = key.substring(DB_LOOKUPNAME_PREFFIX.length());
                try {
                    Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            key + ": is not pattern \"" + DB_LOOKUPNAME_PREFFIX + "[number]\".");
                }
                numList.add(value);
            }
        }

        {
            Map<String, DBConfiguration> dbConfigMap = new ConcurrentHashMap<String, DBConfiguration>();
            for (String num : numList) {
                DBConfiguration conf = new DBConfiguration(getString(bundle, DB_LOOKUPNAME_PREFFIX + num),
                        getString(bundle, DB_CONNECTION_FACTORY_PREFIX + num),
                        getString(bundle, DB_DRIVER_PREFIX + num), getString(bundle, DB_URL_PREFIX + num),
                        getString(bundle, DB_USER_PREFIX + num), getString(bundle, DB_PASSWORD_PREFIX + num),
                        getString(bundle, DB_RETRY_COUNT_PREFIX + num),
                        getString(bundle, DB_SLEEP_INTERVAL_PREFIX + num), bundle, num);
                checkDBConf(conf);
                dbConfigMap.put(conf.getLookupName(), conf);

                LOG.debug("DB Configuration :lookupName=" + conf.getLookupName() + ", factory="
                        + conf.getConnectionFactory().getName() + ", driver=" + conf.getDriver() + ", url="
                        + conf.getUrl() + ", user=" + conf.getUser() + ", password=" + conf.getPassword() + ", retry="
                        + conf.getRetry() + ", sleep=" + conf.getSleep());
            }
            dbConfigMap_ = dbConfigMap;
        }

        LOG.debug("end initDb");
    }

    private static void checkDBConf(DBConfiguration conf) {
        if (!isImplementInterface(conf.getConnectionFactory(), ConnectionFactory.class)) {
            throw new ConfigurationException(
                    "is not implements interface of ConnectionFactory:" + conf.getConnectionFactory().getName());
        }

        if (DataSourceConnectionFactory.class.equals(conf.getConnectionFactory())) {
            DataSource ds = null;
            try {
                Context ctx = new InitialContext();
                ds = (DataSource) ctx.lookup(conf.getLookupName());
            } catch (NamingException e) {
                throw new ConfigurationException(
                        "DataSource(Connection Definition) is not found:" + conf.getLookupName(), e);
            }
            /*
             * try {
             * ds.getConnection().close();
             * } catch (SQLException e) {
             * throw new ConfigurationException("Connect DB is faild:" + conf.getLookupName(), e);
             * }
             */
        } else if (JDBCConnectionFactory.class.equals(conf.getConnectionFactory())) {
            /*
             * try { Class.forName(conf.getDriver()); } catch
             * (ClassNotFoundException e) { throw new ConfigurationException(
             * "class(DB Driver) is not found: lookupName=" +
             * conf.getLookupName() + ", driverName=" + conf.getDriver(), e); }
             * try { DriverManager.getConnection(conf.getUrl(), conf.getUser(),
             * conf.getPassword()).close(); } catch (SQLException e) { throw new
             * ConfigurationException( "Connect DB is faild: lookupName=" +
             * conf.getLookupName() + ", driverName=" + conf.getDriver() +
             * ", url=" + conf.getUrl() + ", user=" + conf.getUser() +
             * ", password=" + conf.getPassword(), e); }
             */
        }
    }

    private static void initInterceptor(ResourceBundle bundle) {
        LOG.debug("start initInterceptor");

        Enumeration<String> keys = bundle.getKeys();
        TreeMap<Integer, String> numMap = new TreeMap<Integer, String>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.startsWith(INTERCEPTOR_PREFIX)) {
                String value = key.substring(INTERCEPTOR_PREFIX.length());
                int intValue;
                try {
                    intValue = Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            key + ": is not pattern \"" + INTERCEPTOR_PREFIX + "[number]\".");
                }
                numMap.put(intValue, value);
            }
        }

        {
            InterceptorConfiguration interceptor = new InterceptorConfiguration();
            for (String num : numMap.values()) {
                String value = getString(bundle, INTERCEPTOR_PREFIX + num);
                checkInterceptor(value);
                interceptor.addInterceptor(value);
                LOG.debug("interceptor[" + num + "]:" + value);
            }
            interceptor_ = interceptor;
        }

        LOG.debug("end initInterceptor");
    }

    private static void checkInterceptor(String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("class(interceptor) not found:" + className, e);
        }

        if (null == clazz.getAnnotation(Interceptor.class)) {
            throw new ConfigurationException("interceptor class has not @Interceptor annotation:" + className);
        }
    }

    private static void initDI(ResourceBundle bundle) {
        LOG.debug("start initDI");

        Enumeration<String> keys = bundle.getKeys();
        LinkedList<String> numList = new LinkedList<String>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.startsWith(DI_INTERFACE_PREFIX)) {
                String value = key.substring(DI_INTERFACE_PREFIX.length());
                try {
                    Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            key + ": is not pattern \"" + DI_INTERFACE_PREFIX + "[number]\".");
                }
                numList.add(value);
            }
        }

        {
            Map<String, Class<?>> diConfigMap = new ConcurrentHashMap<String, Class<?>>();
            for (String num : numList) {
                String interfaceStr = getString(bundle, DI_INTERFACE_PREFIX + num);
                String instanceStr = getString(bundle, DI_INSTANCE_CLASS_PREFIX + num);
                Class<?> interfaceClass = null;
                try {
                    interfaceClass = Class.forName(interfaceStr);
                } catch (ClassNotFoundException e) {
                    throw new ConfigurationException("class(DI interface) not found:" + interfaceStr);
                }
                Class<?> instanceClass = null;
                try {
                    instanceClass = Class.forName(instanceStr);
                } catch (ClassNotFoundException e) {
                    throw new ConfigurationException("class(DI instance) not found:" + instanceStr);
                }
                checkDIClasses(interfaceClass, instanceClass);
                diConfigMap.put(interfaceStr, instanceClass);

                LOG.debug("DependencyInjection Configuration :interface=" + interfaceStr + ", instance=" + instanceStr);
            }
            diConfigMap_ = diConfigMap;
        }

        LOG.debug("end initDI");
    }

    private static void checkDIClasses(Class<?> interfaceClass, Class<?> instanceClass) {
        if (!isImplementInterface(instanceClass, interfaceClass)) {
            throw new ConfigurationException(
                    instanceClass.getName() + " is not implement interface :" + interfaceClass);
        }
    }

    private static String getString(ResourceBundle bundle, String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private static boolean isImplementInterface(Class<?> targetClass, Class<?> interfaceClass) {
        for (Class<?> having : targetClass.getInterfaces()) {
            if (interfaceClass.equals(having)) {
                return true;
            }
        }
        Class<?> superClass = targetClass.getSuperclass();
        if (Object.class.equals(superClass)) {
            return false;
        }
        return isImplementInterface(superClass, interfaceClass);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes

    public static final class HookedDBCloser implements Runnable {

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        private HookedDBCloser() {
            // nothing to do.
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public void run() {
            try {
                TransactionManager.closeAll();
                System.out.println("TransactionManager#closeAll() completed.");
            } catch (Throwable e) {
                e.printStackTrace(System.err);
            }
        }
    }

    public interface DBCloserHooker {
        public static final DBCloserHooker FIRST_TIME_HOOKER = new DBCloserHooker() {
            @Override
            public void addHookDBCloser() {
                synchronized (FIRST_TIME_HOOKER) {
                    if (FIRST_TIME_HOOKER == dbCloserHooker_) {
                        Runtime.getRuntime().addShutdownHook(new Thread(new HookedDBCloser()));
                        dbCloserHooker_ = NOP_HOOKER;
                    }
                }
            }
        };

        public static final DBCloserHooker NOP_HOOKER = new DBCloserHooker() {
            @Override
            public void addHookDBCloser() {
            }
        };

        public void addHookDBCloser();
    }

    private static final class InterceptorConfiguration {
        private final List<Class<?>> interceptorList_ = new CopyOnWriteArrayList<Class<?>>();

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        private InterceptorConfiguration() {
            // nothing to do
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        private void addInterceptor(String className) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("class(interceptor) not found:" + className, e);
            }
            interceptorList_.add(clazz);
        }

        private List<Class<?>> getInterceptorList() {
            return interceptorList_;
        }

        private void clear() {
            interceptorList_.clear();
        }
    }

    public static final class DBConfiguration {
        private final String lookupName_;

        private final Class<?> connectionFactory_;

        private final String driver_;

        private final String url_;

        private final String user_;

        private final String password_;

        private int retry_ = 0;

        private long sleep_ = 0L;

        private HashMap<String, String> additionalConfMap_;

        private Map<String, Properties> propertiesMap_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public DBConfiguration(String lookupName, String connectionFactory, String driver, String url, String user,
                String password, String retryCount, String sleepInterval) {
            this(lookupName, connectionFactory, driver, url, user, password, retryCount, sleepInterval, null, null);
        }

        public DBConfiguration(String lookupName, String connectionFactory, String driver, String url, String user,
                String password, String retryCount, String sleepInterval, ResourceBundle bundle, String num) {
            try {
                connectionFactory_ = Class.forName(connectionFactory);
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("class(ConnectionFactory) not found:" + connectionFactory, e);
            }
            List<Class<?>> classList = Arrays.asList(connectionFactory_.getInterfaces());
            if (!classList.contains(ConnectionFactory.class)) {
                throw new ConfigurationException(connectionFactory + " is not ConnectionFactory Class.");
            }
            lookupName_ = lookupName;
            driver_ = driver;
            url_ = url;
            user_ = user;
            password_ = password;
            try {
                if (null != retryCount) {
                    retry_ = Integer.parseInt(retryCount);
                }
            } catch (NumberFormatException e) {
                LOG.warn("retry count is not number:" + retryCount);
            }
            try {
                if (null != sleepInterval) {
                    sleep_ = Long.parseLong(sleepInterval);
                }
            } catch (NumberFormatException e) {
                LOG.warn("sleep intervale(msec) is not number:" + sleepInterval);
            }

            additionalConfMap_ = new HashMap<String, String>();
            if ((null != bundle) && (num != null)) {
                Enumeration<String> keys = bundle.getKeys();
                String keySuffix = "." + num;
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    if (key.startsWith(DB_PREFIX) && key.endsWith(keySuffix)) {
                        String value = bundle.getString(key);
                        String mapKey = key.substring(DB_PREFIX.length(), key.length() - keySuffix.length());
                        additionalConfMap_.put(mapKey, value);
                    }
                }
            }

            createProperties();
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        public String getLookupName() {
            return lookupName_;
        }

        public Class<?> getConnectionFactory() {
            return connectionFactory_;
        }

        public String getDriver() {
            return driver_;
        }

        public String getUrl() {
            return url_;
        }

        public String getUser() {
            return user_;
        }

        public String getPassword() {
            return password_;
        }

        public int getRetry() {
            return retry_;
        }

        public long getSleep() {
            return sleep_;
        }

        public Properties getProperties(String keyName) {
            Properties prop = propertiesMap_.get(keyName);
            if (null == prop) {
                createProperties(keyName);
                prop = propertiesMap_.get(keyName);
            }
            return prop;
        }

        private void createProperties() {
            propertiesMap_ = new HashMap<String, Properties>();
            for (String propKey : dbConfigPropertiesMap_.keySet()) {
                createProperties(propKey);
            }
        }

        private void createProperties(String propKey) {
            Properties prop = new Properties();
            List<ConfigrationToPropertiesKey> ctpKeyList = dbConfigPropertiesMap_.get(propKey);
            for (ConfigrationToPropertiesKey ctpKey : ctpKeyList) {
                String value = additionalConfMap_.get(ctpKey.confKey_);
                if (null == value) {
                    if (null != ctpKey.defaultValue_) {
                        prop.setProperty(ctpKey.propKey_, ctpKey.defaultValue_);
                        LOG.debug(propKey + "[" + lookupName_ + "] " + ctpKey.propKey_ + "=" + ctpKey.defaultValue_);
                    }
                } else {
                    prop.setProperty(ctpKey.propKey_, value);
                    LOG.debug(propKey + "[" + lookupName_ + "] " + ctpKey.propKey_ + "=" + value);
                }
            }
            propertiesMap_.put(propKey, prop);

        }
    }

    public static final class ConfigrationToPropertiesKey {
        private String confKey_;
        private String propKey_;
        private String defaultValue_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public ConfigrationToPropertiesKey(String key) {
            this(key, key);
        }

        public ConfigrationToPropertiesKey(String confKey, String propKey) {
            this(confKey, propKey, null);
        }

        public ConfigrationToPropertiesKey(String confKey, String propKey, String defaultValue) {
            confKey_ = confKey;
            propKey_ = propKey;
            defaultValue_ = defaultValue;
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(Configuration.class.getName());
}
