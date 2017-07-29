package jp.gr.naoco.test.testtools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.dbunit.Assertion;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.datatype.AbstractDataType;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.TimestampDataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.excel.XlsDataSetWriter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.oracle.OracleDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.naoco.alacarte.SafetySimpleDateFormat;
import jp.gr.naoco.core.NaocoCoreFacade;
import jp.gr.naoco.core.conf.Configuration;
import jp.gr.naoco.core.transaction.TransactionManager;

/**
 * DBUnitを使用してJUnit試験をする際に使用するユーティリティクラス
 * <p>
 * インポート・エクスポート・比較対象とするExcelファイルの形式は、基本はDBUnitのExcel形式であるが、 本クラス内で以下の処理を行う
 * </p>
 * <ol>
 * <li>エクセルのセル上に"&lt;empty&gt;"と記載した場合、DBには""（空文字列）を設定する。<br/>
 * DB上の値が空文字列の場合は"&lt;empty&gt;"をExcelのセルに出力する。<br/>
 * Excelのセルの値"&lt;empty&gt;"とDBの空文字列は同値と評価する。</li>
 * <li>エクセルのセル上に"&lt;null&gt;"と記載した場合、DBにはNULL（ヌル値）を設定する。<br/>
 * DB上の値がNULLの場合は"&lt;null&gt;"をExcelのセルに出力する。<br/>
 * Excelのセルの値"&lt;null&gt;"とDBのNULLは同値と評価する。</li>
 * <li>DBのデータ型が、DATE、TIME、TIMESTAMPのカラムについては、"yyyy-MM-dd HH:mm:ss"形式の文字列を Excelのセルに出力する。</li>
 * </ol>
 *
 * @author naoco0917
 */
public final class DBUnitTestUtil {

    /** Excelファイルのセル上でこの値を設定した場合、DBにはNULL値を格納・出力する */
    public static final String NULL_EXCEL_CELL_VALUE = "<null>";

    /** Excelファイルのセル上でこの値を設定した場合、DBには空文字列を格納・出力する */
    public static final String EMPTY_EXCEL_CELL_VALUE = "<empty>";

    private static final SafetySimpleDateFormat FORMAT = new SafetySimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private DBUnitTestUtil() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * DBUnit形式のExcelファイルを読み込んで、DBをリプレースする。
     * <p>
     * 呼出し前に、LaolCoreInitializerあるいは ..laol.core.conf.Configuration により、DBの設定を取得すること。
     * </p>
     *
     * @param lookupName DB接続設定のルックアップ名
     * @param importXlsPath 読み込み対象Excelファイルのパス
     * @throws Exception
     */
    public static void importExcel(String lookupName, String importXlsPath) throws Exception {
        try {
            TransactionManager.takeoverTransaction(lookupName);
            Connection con = NaocoCoreFacade.getConnection();
            DatabaseConnection connection = new DatabaseConnection(con,
                    Configuration.getDbConfig(lookupName).getUser());
            IDataSet dataSet = new XlsDataSet(new FileInputStream(new File(importXlsPath)));
            ReplacementDataSet replacedSet = convertNullEmpty(dataSet);
            ITableIterator tables = replacedSet.iterator();
            while (tables.next()) {
                ITable table = tables.getTable();
                IDataSet ds = new DefaultDataSet(table);
                DatabaseOperation.CLEAN_INSERT.execute(connection, ds);
                NaocoCoreFacade.commitForce();
            }

            NaocoCoreFacade.commitTransaction();
        } catch (Exception e) {
            NaocoCoreFacade.rollbackTransaction();
            e.printStackTrace(System.err);
            throw e;
        }
    }

    /**
     * DBのレコードをDBUnit形式で出力する。
     * <p>
     * 呼出し前に、LaolCoreInitializerあるいは ..laol.core.conf.Configuration により、DBの設定を取得すること。
     * </p>
     *
     * @param lookupName DB接続設定のルックアップ名
     * @param importXlsPath 出力先Excelファイルのパス
     * @param tableNames 出力対象テーブル名の配列
     * @throws Exception
     */
    public static void exportExcel(String lookupName, String exportXlsPath, String[] tableNames) throws Exception {
        try {
            TransactionManager.takeoverTransaction(lookupName);
            Connection con = NaocoCoreFacade.getConnection();
            DatabaseConnection connection = new DatabaseConnection(con,
                    Configuration.getDbConfig(lookupName).getUser());
            IDataSet partialDataSet = connection.createDataSet(tableNames);
            ReplacementDataSet replacedSet = disconvertNullEmpty(partialDataSet);
            new DateToStringXlsDataSetWriter().write(replacedSet, new FileOutputStream(new File(exportXlsPath)));

            NaocoCoreFacade.commitTransaction();
        } catch (Exception e) {
            NaocoCoreFacade.rollbackTransaction();
            e.printStackTrace(System.err);
            throw e;
        }
    }

    /**
     * DBに格納されているレコードが期待通りの結果であるかを検証する。
     * <p>
     * 呼出し前に、LaolCoreInitializerあるいは ..laol.core.conf.Configuration により、DBの設定を取得すること。
     * </p>
     *
     * @param lookupName DB接続設定のルックアップ名
     * @param expectXlsPath 期待するDBレコードを定義したDBUnit形式のExcelファイルのパス
     * @param tableNames 検証対象テーブル名の配列 expectXlsPathで指定したExcelファイルで定義したテーブルと同じであること。
     * @throws Exception
     */
    public static void assertDB(String lookupName, String expectXlsPath, String[] tableNames) throws Exception {
        assertDB(lookupName, expectXlsPath, tableNames, new String[] {"CREATE_DATE", "UPDATE_DATE" });
    }

    /**
     * DBに格納されているレコードが期待通りの結果であるかを検証する。
     * <p>
     * 呼出し前に、LaolCoreInitializerあるいは ..laol.core.conf.Configuration により、DBの設定を取得すること。
     * </p>
     *
     * @param lookupName DB接続設定のルックアップ名
     * @param expectXlsPath 期待するDBレコードを定義したDBUnit形式のExcelファイルのパス
     * @param tableNames 検証対象テーブル名の配列 expectXlsPathで指定したExcelファイルで定義したテーブルと同じであること。
     * @param ignoreColumnNames 検証対象外とするカラム名称の配列。
     * @throws Exception
     */
    public static void assertDB(String lookupName, String expectXlsPath, String[] tableNames,
            String[] ignoreColumnNames) throws Exception {
        try {
            TransactionManager.takeoverTransaction(lookupName);
            Connection con = NaocoCoreFacade.getConnection();
            DatabaseConnection connection = new DatabaseConnection(con,
                    Configuration.getDbConfig(lookupName).getUser());
            DatabaseConfig dbConfig = connection.getConfig();
            dbConfig.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new DateToStringDataTypeFactory());
            IDataSet expectSet = new XlsDataSet(new FileInputStream(new File(expectXlsPath)));
            ReplacementDataSet replacedExpect = convertNullEmpty(expectSet);
            IDataSet actualSet = connection.createDataSet(tableNames);
            ReplacementDataSet replacedActual = convertNullEmpty(actualSet);
            for (ITable expectTable : replacedExpect.getTables()) {
                Assertion.assertEqualsIgnoreCols(replacedExpect, replacedActual,
                        expectTable.getTableMetaData().getTableName(), ignoreColumnNames);
            }
            NaocoCoreFacade.commitTransaction();
        } catch (Exception e) {
            NaocoCoreFacade.rollbackTransaction();
            e.printStackTrace(System.err);
            throw e;
        }

    }

    /**
     * 指定テーブルをバックアップ
     */
    public static void backup(String lookupName, String path, String[] tableNames) throws Exception {
        try {
            TransactionManager.takeoverTransaction(lookupName);
            Connection con = NaocoCoreFacade.getConnection();
            DatabaseConnection connection = new DatabaseConnection(con);
            QueryDataSet partialDataSet = new QueryDataSet(connection);
            for (int i = 0; i < tableNames.length; i++) {
                partialDataSet.addTable(tableNames[i]);
            }
            File file = new File(path);
            FlatXmlDataSet.write(partialDataSet, new FileOutputStream(file));
            NaocoCoreFacade.commitTransaction();
        } catch (Exception e) {
            NaocoCoreFacade.rollbackTransaction();
            e.printStackTrace(System.err);
            throw e;
        }
    }

    /**
     * リストア
     */
    public static void restore(String lookupName, String path) throws Exception {
        try {
            TransactionManager.takeoverTransaction(lookupName);
            Connection con = NaocoCoreFacade.getConnection();
            DatabaseConnection connection = new DatabaseConnection(con);
            File bkFile = new File(path);
            IDataSet dataSet = new FlatXmlDataSet(bkFile);
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
            NaocoCoreFacade.commitTransaction();
        } catch (Exception e) {
            NaocoCoreFacade.rollbackTransaction();
            e.printStackTrace(System.err);
            throw e;
        }
    }

    // ///////////////////////

    private static ReplacementDataSet convertNullEmpty(IDataSet set) throws Exception {
        ReplacementDataSet result = new ReplacementDataSet(set);
        result.addReplacementObject(NULL_EXCEL_CELL_VALUE, null);
        result.addReplacementObject(EMPTY_EXCEL_CELL_VALUE, "");
        return result;
    }

    private static ReplacementDataSet disconvertNullEmpty(IDataSet set) throws Exception {
        ReplacementDataSet result = new ReplacementDataSet(set);
        result.addReplacementObject(null, NULL_EXCEL_CELL_VALUE);
        result.addReplacementObject("", EMPTY_EXCEL_CELL_VALUE);
        return result;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes

    public static class TestUtilClassLoader extends ClassLoader {
        private static final String RESOURCE = "impl=" + DBUnitTestUtil.TestUtilDateTimeProvider.class.getName();
        private final ClassLoader parent_;

        public TestUtilClassLoader(ClassLoader parent) {
            parent_ = parent;
        }

        @Override
        public Class loadClass(String name) throws ClassNotFoundException {
            return parent_.loadClass(name);
        }

        @Override
        public InputStream getResourceAsStream(String path) {
            if ("date-time-provider.properties".equals(path)) {
                return new ByteArrayInputStream(RESOURCE.getBytes(Charset.forName("UTF8")));
            }
            return parent_.getResourceAsStream(path);
        }
    }

    // ///////////////////////

    public static class TestUtilDateTimeProvider {
        private static long time_;

        public long currentTimeMillis() {
            return time_;
        }

        public Date getDate() {
            return new Date(currentTimeMillis());
        }
    }

    // ///////////////////////

    public static class DateToStringDataTypeFactory extends OracleDataTypeFactory {
        @Override
        public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
            if ((sqlType == Types.DATE) || "DATE".equals(sqlTypeName.toUpperCase())) {
                return DateToStringDataType.INSTANCE;
            } else if ((sqlType == Types.TIMESTAMP) || "TIMESTAMP".equals(sqlTypeName.toUpperCase())) {
                return DateToStringDataType.INSTANCE;
            } else if ((sqlType == Types.TIME) || "TIME".equals(sqlTypeName.toUpperCase())) {
                return DateToStringDataType.INSTANCE;
            } else {
                return super.createDataType(sqlType, sqlTypeName);
            }
        }
    }

    // ///////////////////////

    public static class DateToStringDataType extends AbstractDataType {

        public static final DateToStringDataType INSTANCE = new DateToStringDataType();

        public DateToStringDataType() {
            super("TIMESTAMP", 93, Timestamp.class, false);
        }

        @Override
        protected int compareNonNulls(Object value1, Object value2) throws TypeCastException {
            if (value1 instanceof java.util.Date) {
                value1 = FORMAT.format(value1);
            }
            if (value2 instanceof java.util.Date) {
                value2 = FORMAT.format(value2);
            }
            return super.compareNonNulls(value1, value2);
        }

        /* 以下、 org.dbunit.dataset.datatype.TimestampDataType のソースをコピペ */

        private static final BigInteger ONE_BILLION = new BigInteger("1000000000");
        private static final Pattern TIMEZONE_REGEX = Pattern.compile("(.*)(?:\\W([+-][0-2][0-9][0-5][0-9]))");
        private static final Logger logger = LoggerFactory.getLogger(TimestampDataType.class);

        @Override
        public Object typeCast(Object value) throws TypeCastException {
            logger.debug("typeCast(value={}) - start", value);

            if ((value == null) || (value == ITable.NO_VALUE)) {
                return null;
            }

            if (value instanceof Timestamp) {
                return value;
            }

            if (value instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) value;
                return new Timestamp(date.getTime());
            }

            if (value instanceof Long) {
                Long date = (Long) value;
                return new Timestamp(date.longValue());
            }

            if (value instanceof String) {
                String stringValue = value.toString();
                String zoneValue = null;

                Matcher tzMatcher = TIMEZONE_REGEX.matcher(stringValue);
                if ((tzMatcher.matches()) && (tzMatcher.group(2) != null)) {
                    stringValue = tzMatcher.group(1);
                    zoneValue = tzMatcher.group(2);
                }

                Timestamp ts = null;
                if (stringValue.length() == 10) {
                    try {
                        long time = java.sql.Date.valueOf(stringValue).getTime();
                        ts = new Timestamp(time);
                    } catch (IllegalArgumentException e) {
                    }
                }

                if (ts == null) {
                    try {
                        ts = Timestamp.valueOf(stringValue);
                    } catch (IllegalArgumentException e) {
                        throw new TypeCastException(value, this, e);
                    }

                }

                if (zoneValue != null) {
                    BigInteger time = BigInteger.valueOf(ts.getTime() / 1000L * 1000L).multiply(ONE_BILLION)
                            .add(BigInteger.valueOf(ts.getNanos()));
                    int hours = Integer.parseInt(zoneValue.substring(1, 3));
                    int minutes = Integer.parseInt(zoneValue.substring(3, 5));
                    BigInteger offsetAsSeconds = BigInteger.valueOf(hours * 3600 + minutes * 60);
                    BigInteger offsetAsNanos = offsetAsSeconds.multiply(BigInteger.valueOf(1000L))
                            .multiply(ONE_BILLION);
                    if (zoneValue.charAt(0) == '+')
                        time = time.subtract(offsetAsNanos);
                    else {
                        time = time.add(offsetAsNanos);
                    }
                    BigInteger[] components = time.divideAndRemainder(ONE_BILLION);
                    ts = new Timestamp(components[0].longValue());
                    ts.setNanos(components[1].intValue());
                }

                return ts;
            }

            throw new TypeCastException(value, this);
        }

        @Override
        public boolean isDateTime() {
            logger.debug("isDateTime() - start");

            return true;
        }

        @Override
        public Object getSqlValue(int column, ResultSet resultSet) throws SQLException, TypeCastException {
            if (logger.isDebugEnabled()) {
                logger.debug("getSqlValue(column={}, resultSet={}) - start", new Integer(column), resultSet);
            }
            Timestamp value = resultSet.getTimestamp(column);
            if ((value == null) || (resultSet.wasNull())) {
                return null;
            }
            return value;
        }

        @Override
        public void setSqlValue(Object value, int column, PreparedStatement statement)
                throws SQLException, TypeCastException {
            if (logger.isDebugEnabled()) {
                logger.debug("setSqlValue(value={}, column={}, statement={}) - start",
                        new Object[] {value, new Integer(column), statement });
            }

            statement.setTimestamp(column, (Timestamp) typeCast(value));
        }
    }

    // ///////////////////////

    public static class DateToStringXlsDataSetWriter extends XlsDataSetWriter {

        @Override
        protected void setDateCell(HSSFCell cell, Date value, HSSFWorkbook workbook) {
            cell.setCellValue(new HSSFRichTextString(FORMAT.format(value)));
        }
    }
}
