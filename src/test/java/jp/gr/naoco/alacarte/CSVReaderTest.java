package jp.gr.naoco.alacarte;

import java.io.CharArrayReader;
import java.util.LinkedHashMap;
import java.util.List;

import jp.gr.naoco.alacarte.CSVReader;
import jp.gr.naoco.alacarte.CSVReader.ErrorInfo;
import jp.gr.naoco.alacarte.ZipFileWriter.CSVConfig;
import jp.gr.naoco.db.entity.AbstractEntity;
import jp.gr.naoco.db.entity.annotation.Column;
import jp.gr.naoco.db.entity.annotation.Id;
import jp.gr.naoco.db.entity.annotation.Table;

import org.junit.Test;

public class CSVReaderTest {

    String CSV_BODY01 = "\"STRING_ID\",\"LONG_ID\",\"DOUBLE_ID\",\"STRING_VALUE\",\"LONG_VALUE\",\"DOUBLE_VALUE\",\"UTIL_DATE_VALUE\",\"SQL_TIMESTAMP_VALUE\",\"SQL_DATE_VALUE\",\"SQL_TIME_VALUE\""
            + "\n" + "\"id01\",\"2\",\"1.0\",\"hoge\"\",nhige\",\"\",\"\",\"1970/05/24 06:21:18\",\"\",\"\",\"\"" + "\n"
            + "\"id02\",\"4\",\"3.0\",\"fooo\"\"\",\"\",\"\",\"\",\"\",\"1970/05/24 09:07:58\",\"\"" + "\n"
            + "\"id03\",\"6\",\"5.0\",\"hoge" + "\n" + "hige\"\"\n\",\"\",\"\",\"\",\"1970/05/24 11:54:38\",\"\",\"\"";
    String CSV_BODY02 = "\"STRING_ID\",\"LONG_ID\",\"DOUBLE_ID\",\"STRING_VALUE\",\"LONG_VALUE\",\"DOUBLE_VALUE\",\"UTIL_DATE_VALUE\",\"SQL_TIMESTAMP_VALUE\",\"SQL_DATE_VALUE\",\"SQL_TIME_VALUE\""
            + "\n" + "\"id04\",\"8\",\"7.0\",\"あああ\"\"いいい\",\"\",\"\",\"1970/06/04 20:07:58\",\"\",\"\",\"\"" + "\n"
            + "\"id05\",\"10\",\"9.0\",\",あああ\"\",いいい,\"\"\n\"\"\",\"\",\"\",\"\",\"\",\"1970/06/04 22:54:38\",\"\""
            + "\n" + "\"id06\",\"12\",\"11.0\",\"かかか" + "\n" + "ききき\",\"\",\"\",\"\",\"1970/05/24 11:54:38\",\"\",\"\"";
    String CSV_BODY03 = "\"STRING_ID\",\"LONG_ID\",\"DOUBLE_ID\",\"STRING_VALUE\",\"LONG_VALUE\",\"DOUBLE_VALUE\",\"UTIL_DATE_VALUE\",\"SQL_TIMESTAMP_VALUE\",\"SQL_DATE_VALUE\",\"SQL_TIME_VALUE\""
            + "\n" + "\"id07\",\"14\",\"13.0\",\"BOMB!\",\"\",\"\",\"1970/06/04 20:07:58\",\"\",\"\",\"\"" + "\n"
            + "\"id08\",\"16\",\"15.0\",\"\",\"\",\"\",\"\",\"\",\"1970/06/04 22:54:38\",\"\"" + "\n" + "\"hogehoge"
            + "\n" + "hagehage" + "\n" + "\"fooooooo" + "\n" + "\"id09\",\"18\",\"17.0\",\"かかか" + "\n"
            + "ききき\",\"\",\"\",\"\",\"1970/05/24 11:54:38\",\"\",\"\"" + "\n"
            + "\"id10\",\"20\",\"19.0\",\"さしすせそ012345\",\"\",\"\",\"\",\"1978/05/24 11:54:38\",\"\",\"1978/05/24 23:12:44\""
            + "\n" + "\"id11\",\"22\",\"21.0\",\"さしすせそ012345\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"";
    String CSV_BODY04 = "\"STRING_ID\",\"LONG_ID\",\"DOUBLE_ID\",\"STRING_VALUE\",\"LONG_VALUE\",\"DOUBLE_VALUE\",\"UTIL_DATE_VALUE\",\"SQL_TIMESTAMP_VALUE\",\"SQL_DATE_VALUE\",\"SQL_TIME_VALUE\"";

    // /////////////////////////////////////////////////////////////////////////////////////////////

    /** ヘッダ行の読み取りなし、文字列配列でCSVを読み取り */
    @Test
    public void test01() throws Exception {
        System.out.println("******* test01");
        CharArrayReader reader = new CharArrayReader(CSV_BODY01.toCharArray());
        CSVReader<?> csv = new CSVReader(reader, new CSVConfig(), false);
        String[] values = null;
        while (null != (values = csv.readLine())) {
            for (int i = 0; i < values.length; i++) {
                System.out.print(values[i] + ", ");
            }
            System.out.println("");
        }
        csv.close();
        System.out.println("");
    }

    /** ヘッダ行の読み取りあり、文字列配列でCSVを読み取り */
    @Test
    public void test02() throws Exception {
        System.out.println("******* test02");
        CharArrayReader reader = new CharArrayReader(CSV_BODY01.toCharArray());
        CSVReader<?> csv = new CSVReader(reader, new CSVConfig(), true);
        String[] values = null;
        while (null != (values = csv.readLine())) {
            for (int i = 0; i < values.length; i++) {
                System.out.print(values[i] + ", ");
            }
            System.out.println("");
        }
        csv.close();
        System.out.println("");
    }

    /** ヘッダ行の読み取りあり、文字列配列でCSVを読み取り（日本語を含む文字列） */
    @Test
    public void test03() throws Exception {
        System.out.println("******* test03");
        CharArrayReader reader = new CharArrayReader(CSV_BODY02.toCharArray());
        CSVReader<?> csv = new CSVReader(reader, new CSVConfig(), true);
        String[] values = null;
        while (null != (values = csv.readLine())) {
            for (int i = 0; i < values.length; i++) {
                System.out.print(values[i] + ", ");
            }
            System.out.println("");
        }
        csv.close();
        System.out.println("");
    }

    /** ヘッダ行の読み取りあり、エラー行を含むCSVを読み取り */
    @Test
    public void test04() throws Exception {
        System.out.println("******* test04");
        CharArrayReader reader = new CharArrayReader(CSV_BODY03.toCharArray());
        CSVReader<?> csv = new CSVReader(reader, new CSVConfig(), true);
        String[] values = null;
        while (null != (values = csv.readLine())) {
            for (int i = 0; i < values.length; i++) {
                System.out.print(values[i] + ", ");
            }
            System.out.println("");
        }
        List<ErrorInfo> errorList = csv.getErrorList();
        for (ErrorInfo errorInfo : errorList) {
            System.out.print("【error】");
            System.out.print("line:" + errorInfo.getFileLine_());
            System.out.print(" csv:" + errorInfo.getCsvLine_());
            System.out.print(" body:" + errorInfo.getLine_());
            System.out.println("");
        }
        csv.close();
        System.out.println("");
    }

    /** ヘッダ行の読み取りあり、マップでCSVを読み取り（日本語を含む文字列） */
    @Test
    public void test05() throws Exception {
        System.out.println("******* test05");
        CharArrayReader reader = new CharArrayReader(CSV_BODY02.toCharArray());
        CSVReader<?> csv = new CSVReader(reader, new CSVConfig(), true);
        LinkedHashMap<String, String> map = null;
        while (null != (map = csv.readMap())) {
            for (String key : map.keySet()) {
                String value = map.get(key);
                System.out.print(key + ":" + value + ", ");
            }
            System.out.println("");
        }
        csv.close();
        System.out.println("");
    }

    /** ヘッダ行の読み取りあり、エンティティでCSVを読み取り（日本語を含む文字列） */
    @Test
    public void test06() throws Exception {
        System.out.println("******* test06");
        CharArrayReader reader = new CharArrayReader(CSV_BODY02.toCharArray());
        CSVReader<TestEntity> csv = new CSVReader<TestEntity>(reader, new CSVConfig(), TestEntity.class);
        TestEntity entity = null;
        while (null != (entity = csv.readEntity())) {
            System.out.println("STRING_ID:" + entity.getString_id() + //
                    " LONG_ID:" + entity.getLong_id() + //
                    " DOUBLE_ID:" + entity.getDouble_id() + //
                    " STRING_VALUE:" + entity.getString_value() + //
                    " java.util.Date_VALUE:" + entity.getUtil_date_value() + //
                    " java.sql.date_VALUE:" + entity.getSql_date_value() + //
                    " java.sql.Timestamp_VALUE:" + entity.getSql_timestamp_value() + //
                    " java.sql.time_VALUE:" + entity.getSql_time_value());
        }
        csv.close();
        System.out.println("");
    }

    /** ヘッダ行の読み取りあり、エンティティでエラーを含むCSVを読み取り */
    @Test
    public void test07() throws Exception {
        System.out.println("******* test07");
        CharArrayReader reader = new CharArrayReader(CSV_BODY03.toCharArray());
        CSVReader<TestEntity> csv = new CSVReader<TestEntity>(reader, new CSVConfig(), TestEntity.class);
        TestEntity entity = null;
        while (null != (entity = csv.readEntity())) {
            System.out.println("STRING_ID:" + entity.getString_id() + //
                    " LONG_ID:" + entity.getLong_id() + //
                    " DOUBLE_ID:" + entity.getDouble_id() + //
                    " STRING_VALUE:" + entity.getString_value() + //
                    " java.util.Date_VALUE:" + entity.getUtil_date_value() + //
                    " java.sql.date_VALUE:" + entity.getSql_date_value() + //
                    " java.sql.Timestamp_VALUE:" + entity.getSql_timestamp_value() + //
                    " java.sql.time_VALUE:" + entity.getSql_time_value());
        }
        List<ErrorInfo> errorList = csv.getErrorList();
        for (ErrorInfo errorInfo : errorList) {
            System.out.print("【error】");
            System.out.print("line:" + errorInfo.getFileLine_());
            System.out.print(" csv:" + errorInfo.getCsvLine_());
            System.out.print(" body:" + errorInfo.getLine_());
            System.out.println("");
        }
        csv.close();
        System.out.println("");
    }

    /** ヘッダ行のみ */
    @Test
    public void test08() throws Exception {
        System.out.println("******* test08");
        CharArrayReader reader = new CharArrayReader(CSV_BODY04.toCharArray());
        CSVReader<TestEntity> csv = new CSVReader<TestEntity>(reader, new CSVConfig(), TestEntity.class);
        TestEntity entity = null;
        while (null != (entity = csv.readEntity())) {
            System.out.println("STRING_ID:" + entity.getString_id() + //
                    " LONG_ID:" + entity.getLong_id() + //
                    " DOUBLE_ID:" + entity.getDouble_id() + //
                    " STRING_VALUE:" + entity.getString_value() + //
                    " java.util.Date_VALUE:" + entity.getUtil_date_value() + //
                    " java.sql.date_VALUE:" + entity.getSql_date_value() + //
                    " java.sql.Timestamp_VALUE:" + entity.getSql_timestamp_value() + //
                    " java.sql.time_VALUE:" + entity.getSql_time_value());
        }
        List<ErrorInfo> errorList = csv.getErrorList();
        for (ErrorInfo errorInfo : errorList) {
            System.out.print("【error】");
            System.out.print("line:" + errorInfo.getFileLine_());
            System.out.print(" csv:" + errorInfo.getCsvLine_());
            System.out.print(" body:" + errorInfo.getLine_());
            System.out.println("");
        }
        csv.close();
        System.out.println("");
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes

    @Table(name = "TEST_TABLE")
    public static class TestEntity extends AbstractEntity {
        @Id(name = "STRING_ID")
        private String string_id;
        @Id(name = "LONG_ID")
        private Long long_id;
        @Id(name = "DOUBLE_ID")
        private Double double_id;
        @Column(name = "STRING_VALUE")
        private String string_value;
        @Column(name = "LONG_VALUE")
        private Long long_value;
        @Column(name = "DOUBLE_VALUE")
        private Double double_value;
        @Column(name = "UTIL_DATE_VALUE")
        private java.util.Date util_date_value;
        @Column(name = "SQL_TIMESTAMP_VALUE")
        private java.sql.Timestamp sql_timestamp_value;
        @Column(name = "SQL_DATE_VALUE")
        private java.sql.Date sql_date_value;
        @Column(name = "SQL_TIME_VALUE")
        private java.sql.Time sql_time_value;

        public String getString_id() {
            return string_id;
        }

        public void setString_id(String string_id) {
            this.string_id = string_id;
            setFieldNameSet_.add("STRING_ID");
        }

        public Long getLong_id() {
            return long_id;
        }

        public void setLong_id(Long long_id) {
            this.long_id = long_id;
            setFieldNameSet_.add("LONG_ID");
        }

        public Double getDouble_id() {
            return double_id;
        }

        public void setDouble_id(Double double_id) {
            this.double_id = double_id;
            setFieldNameSet_.add("DOUBLE_ID");
        }

        public String getString_value() {
            return string_value;
        }

        public void setString_value(String string_value) {
            if ("BOMB!".equals(string_value)) {
                throw new RuntimeException("for test.");
            }
            this.string_value = string_value;
            setFieldNameSet_.add("STRING_VALUE");
        }

        public Long getLong_value() {
            return long_value;
        }

        public void setLong_value(Long long_value) {
            this.long_value = long_value;
            setFieldNameSet_.add("LONG_VALUE");
        }

        public Double getDouble_value() {
            return double_value;
        }

        public void setDouble_value(Double double_value) {
            this.double_value = double_value;
            setFieldNameSet_.add("DOUBLE_VALUE");
        }

        public java.util.Date getUtil_date_value() {
            return util_date_value;
        }

        public void setUtil_date_value(java.util.Date util_date_value) {
            this.util_date_value = util_date_value;
            setFieldNameSet_.add("UTIL_DATE_VALUE");
        }

        public java.sql.Timestamp getSql_timestamp_value() {
            return sql_timestamp_value;
        }

        public void setSql_timestamp_value(java.sql.Timestamp sql_timestamp_value) {
            this.sql_timestamp_value = sql_timestamp_value;
            setFieldNameSet_.add("SQL_TIMESTAMP_VALUE");
        }

        public java.sql.Date getSql_date_value() {
            return sql_date_value;
        }

        public void setSql_date_value(java.sql.Date sql_date_value) {
            this.sql_date_value = sql_date_value;
            setFieldNameSet_.add("SQL_DATE_VALUE");
        }

        public java.sql.Time getSql_time_value() {
            return sql_time_value;
        }

        public void setSql_time_value(java.sql.Time sql_time_value) {
            this.sql_time_value = sql_time_value;
            setFieldNameSet_.add("SQL_TIME_VALUE");
        }
    }

}
