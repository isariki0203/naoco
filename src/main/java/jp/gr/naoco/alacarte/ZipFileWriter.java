package jp.gr.naoco.alacarte;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import jp.gr.naoco.db.entity.AbstractEntity;
import jp.gr.naoco.db.entity.annotation.Column;
import jp.gr.naoco.db.entity.annotation.CsvIgnore;
import jp.gr.naoco.db.entity.annotation.Id;

public class ZipFileWriter implements Closeable {

    private CSVConfig csvConfig_;

    private String lineSeparator_;

    private BufferedWriter writer_;

    private OutputStreamWriter pipe_;

    private ZipOutputStream zip_;

    private FileOutputStream file_;

    private List<Method> csvGetterMethodList_;

    private String csvNullValue_ = "";

    private HashMap<String, CSVFormatter> specifiedCsvFormatter_ = new HashMap<String, CSVFormatter>();

    private static final SafetySimpleDateFormat DEFAULT_DATE_FORMAT = new SafetySimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static final HashMap<String, CSVFormatter> DEFAULT_CSV_FORMATTER = new HashMap<String, CSVFormatter>();
    static {
        DEFAULT_CSV_FORMATTER.put(String.class.getName(), new CSVFormatter() {
            @Override
            public String format(Object value) {
                return value.toString();
            }
        });
        DEFAULT_CSV_FORMATTER.put(Integer.class.getName(), new CSVFormatter() {
            @Override
            public String format(Object value) {
                return Integer.toString((int) value);
            }
        });
        DEFAULT_CSV_FORMATTER.put(Long.class.getName(), new CSVFormatter() {
            @Override
            public String format(Object value) {
                return Long.toString((long) value);
            }
        });
        DEFAULT_CSV_FORMATTER.put(Double.class.getName(), new CSVFormatter() {
            @Override
            public String format(Object value) {
                return Double.toString((double) value);
            }
        });
        DEFAULT_CSV_FORMATTER.put(BigDecimal.class.getName(), new CSVFormatter() {
            @Override
            public String format(Object value) {
                return ((BigDecimal) value).toPlainString();
            }
        });
        DEFAULT_CSV_FORMATTER.put(java.util.Date.class.getName(), new CSVFormatter() {
            @Override
            public String format(Object value) {
                return DEFAULT_DATE_FORMAT.format((java.util.Date) value);
            }
        });
        DEFAULT_CSV_FORMATTER.put(java.sql.Date.class.getName(), new CSVFormatter() {
            @Override
            public String format(Object value) {
                return DEFAULT_DATE_FORMAT.format((java.util.Date) value);
            }
        });
        DEFAULT_CSV_FORMATTER.put(java.sql.Timestamp.class.getName(), new CSVFormatter() {
            @Override
            public String format(Object value) {
                return DEFAULT_DATE_FORMAT.format((java.util.Date) value);
            }
        });
        DEFAULT_CSV_FORMATTER.put(java.sql.Time.class.getName(), new CSVFormatter() {
            @Override
            public String format(Object value) {
                return DEFAULT_DATE_FORMAT.format((java.util.Date) value);
            }
        });
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    /**
     * 新しく {@code ZipFileWriter} オブジェクトを構築します。
     * 
     * @param outfile zipファイルの出力先
     * @param charset 書き込んだ文字列のエンコードタイプ
     * @throws FileNotFoundException outfileが作成できない状態の場合
     * @throws IOException
     */
    public ZipFileWriter(File outfile, Charset charset) throws FileNotFoundException, IOException {
        this(outfile, charset, new CSVConfig(), System.getProperty("line.separator"));
    }

    /**
     * 新しく {@code ZipFileWriter} オブジェクトを構築します。
     * 
     * @param outfile zipファイルの出力先
     * @param charset 書き込んだ文字列のエンコードタイプ
     * @param csvConfig 本クラスのwriteCSVを使用する場合の、CSVの出力形式（区切り文字、囲み文字、囲み文字のエスケープ有無）
     * @throws FileNotFoundException outfileが作成できない状態の場合
     * @throws IOException
     */
    public ZipFileWriter(File outfile, Charset charset, ZipFileWriter.CSVConfig csvConfig)
            throws FileNotFoundException, IOException {
        this(outfile, charset, csvConfig, System.getProperty("line.separator"));
    }

    /**
     * 新しく {@code ZipFileWriter} オブジェクトを構築します。
     * 
     * @param outfile zipファイルの出力先
     * @param charset 書き込んだ文字列のエンコードタイプ
     * @param lineSeparator 改行文字列
     * @throws FileNotFoundException outfileが作成できない状態の場合
     * @throws IOException
     */
    public ZipFileWriter(File outfile, Charset charset, String lineSeparator)
            throws FileNotFoundException, IOException {
        this(outfile, charset, new CSVConfig(), lineSeparator);
    }

    /**
     * 新しく {@code ZipFileWriter} オブジェクトを構築します。
     * 
     * @param outfile zipファイルの出力先
     * @param charset 書き込んだ文字列のエンコードタイプ
     * @param csvConfig 本クラスのwriteCSVを使用する場合の、CSVの出力形式（区切り文字、囲み文字、囲み文字のエスケープ有無）
     * @param lineSeparator 改行文字列
     * @throws FileNotFoundException outfileが作成できない状態の場合
     * @throws IOException
     */
    public ZipFileWriter(File outfile, Charset charset, ZipFileWriter.CSVConfig csvConfig, String lineSeparator)
            throws FileNotFoundException, IOException {
        csvConfig_ = csvConfig;
        lineSeparator_ = lineSeparator;

        try {
            file_ = new FileOutputStream(outfile);
            zip_ = new ZipOutputStream(file_);
            pipe_ = new OutputStreamWriter(zip_, charset);
            writer_ = new BufferedWriter(pipe_);
        } catch (FileNotFoundException | RuntimeException e) {
            if (null != file_) {
                file_.close();
            }
            throw e;
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * 指定したパス名で新たなzipエントリ（Zip展開時のファイルパス）の書き込みを開始
     * 
     * @param path zipエントリのパス名
     * @throws IOException
     */
    public void setZipEntry(String path) throws IOException {
        writer_.flush();
        zip_.putNextEntry(new ZipEntry(path));
    }

    /**
     * 指定したパス名とAbstractEntityの型で新たなzipエントリ（Zip展開時のファイルパス）の書き込みを開始
     * 
     * @param entityClass writeEntity2CSVを使用する際のエンティティの型
     * @param path zipエントリのパス名
     * @throws IOException
     */
    public <T extends AbstractEntity> void setZipEntry4Entity(Class<T> entityClass, String path) throws IOException {
        setZipEntry4Entity(entityClass, path, "");
    }

    /**
     * 指定したパス名とAbstractEntityの型で新たなzipエントリ（Zip展開時のファイルパス）の書き込みを開始
     * 
     * @param entityClass writeEntity2CSVを使用する際のエンティティの型
     * @param path zipエントリのパス名
     * @param csvNullValue 値がnullの場合にCSVに出力する文字列
     * @throws IOException
     */
    public <T extends AbstractEntity> void setZipEntry4Entity(Class<T> entityClass, String path, String csvNullValue)
            throws IOException {
        Field[] fields = entityClass.getDeclaredFields();
        ArrayList<String> columnNameList = new ArrayList<String>();
        csvNullValue_ = csvNullValue;
        csvGetterMethodList_ = new LinkedList<Method>();
        specifiedCsvFormatter_ = new HashMap<String, CSVFormatter>();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Id id = field.getAnnotation(Id.class);
            Column column = field.getAnnotation(Column.class);
            CsvIgnore csvIgnore = field.getAnnotation(CsvIgnore.class); // 無視カラム用
            if (((null != id) || (null != column)) && csvIgnore == null) {
                try {
                    Method getter = new PropertyDescriptor(field.getName(), entityClass).getReadMethod();
                    csvGetterMethodList_.add(getter);
                } catch (IntrospectionException e) {
                    LOG.debug(field.getName() + "is error.", e);
                }
                if (null != id) {
                    columnNameList.add(id.name());
                } else if (null != column) {
                    columnNameList.add(column.name());
                }
            }
        }
        setZipEntry(path);
        writeCSV(columnNameList.toArray());
    }

    /**
     * 現在のZipエントリについて、エンティティのデータ型に対するCSVFormatterを追加で設定する。
     * 
     * @param targetClass
     * @param formatter
     */
    public void setCSVFormatter(Class<?> targetClass, CSVFormatter formatter) {
        specifiedCsvFormatter_.put(targetClass.getName(), formatter);
    }

    /**
     * 文字列を書き込む
     * 
     * @param value
     * @throws IOException
     */
    public void write(String value) throws IOException {
        writer_.write(value);
    }

    /**
     * 指定した文字列と末尾に改行文字列を書き込む
     * 
     * @param value
     * @throws IOException
     */
    public void writeLine(String value) throws IOException {
        writer_.write(value);
        writer_.write(lineSeparator_);
    }

    /**
     * 指定した配列を要素として、CSV形式で文字列を書き込む
     * 
     * @param values
     * @throws IOException
     */
    public void writeCSV(Object... values) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            Object obj = values[i];
            String value = obj.toString();
            if (csvConfig_.isEscapeEnclosure_) {
                value = value.replaceAll(csvConfig_.enclosure_, csvConfig_.enclosure_ + csvConfig_.enclosure_);
            }
            builder.append(csvConfig_.enclosure_);
            builder.append(value);
            builder.append(csvConfig_.enclosure_);
            builder.append(csvConfig_.separator_);
        }
        writeLine(builder.substring(0, builder.length() - csvConfig_.separator_.length()));
    }

    /**
     * 指定したエンティティをCSV形式で書き込む
     * 
     * @param entity
     * @throws IOException
     */
    public <T extends AbstractEntity> void writeEntity2CSV(T entity) throws IOException {
        String[] values = new String[csvGetterMethodList_.size()];
        Iterator<Method> getteres = csvGetterMethodList_.iterator();
        for (int i = 0; getteres.hasNext(); i++) {
            Method getter = getteres.next();
            try {
                Object value = getter.invoke(entity);
                if (null == value) {
                    values[i] = csvNullValue_;
                    continue;
                }
                CSVFormatter formatter = specifiedCsvFormatter_.get(value.getClass().getName());
                if (null != formatter) {
                    values[i] = formatter.format(value);
                    continue;
                }
                formatter = DEFAULT_CSV_FORMATTER.get(value.getClass().getName());
                if (null != formatter) {
                    values[i] = formatter.format(value);
                    continue;
                }
                values[i] = "unsupport type:" + value.getClass().getName();
            } catch (InvocationTargetException e) {
                LOG.debug(getter.getName() + "is error.", e);
            } catch (IllegalAccessException e) {
                LOG.debug(getter.getName() + "is error.", e);
            }
        }
        writeCSV(values);
    }

    /**
     * ストリームを閉じる
     * 
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        try {
            if (null != writer_) {
                writer_.close();
            }
            if (null != pipe_) {
                pipe_.close();
            }
            if (null != zip_) {
                zip_.close();
            }
        } finally {
            if (null != file_) {
                file_.close();
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    public static class CSVConfig {
        private String enclosure_ = "\"";

        private String separator_ = ",";

        private boolean isEscapeEnclosure_ = true;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public CSVConfig() {
            // nothing to do
        }

        public CSVConfig(String enclosure, String separator, boolean isEscapeEnclosure) {
            enclosure_ = ((null == enclosure) ? "" : enclosure);
            separator_ = ((null == separator) ? "" : separator);
            isEscapeEnclosure_ = isEscapeEnclosure;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        public String getEnclosure() {
            return enclosure_;
        }

        public String getSeparator() {
            return separator_;
        }

        public boolean isEscapeEnclosure() {
            return isEscapeEnclosure_;
        }
    }

    // ///////////////////////

    public static interface CSVFormatter {
        public String format(Object value);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static Logger LOG = Logger.getLogger(ZipFileWriter.class);
}
