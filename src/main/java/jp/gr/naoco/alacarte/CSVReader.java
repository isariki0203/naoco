package jp.gr.naoco.alacarte;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import jp.gr.naoco.alacarte.ZipFileWriter.CSVConfig;

public class CSVReader<T> implements Closeable {
    private BufferedReader reader_;

    private CSVConfig csvConfig_;

    private Method[] setterMethods_;

    private String[] columnNames_;

    private int lineCounter_ = 0;

    private int csvCounter_ = 0;

    private Class<T> class_ = null;

    private List<ErrorInfo> errorInfoList_ = new LinkedList<ErrorInfo>();

    private HashMap<String, CSVParser<?>> specifiedCsvParser_ = new HashMap<String, CSVParser<?>>();

    private boolean isNotEndedLine_ = false;

    private LinkedList<String> unAnalyzedLineList_ = new LinkedList<String>();

    // ///////////////////////

    private static final SafetySimpleDateFormat DEFAULT_DATE_FORMAT = new SafetySimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static final HashMap<String, CSVParser<?>> DEFAULT_CSV_PARSER = new HashMap<String, CSVParser<?>>();
    static {
        DEFAULT_CSV_PARSER.put(String.class.getName(), new CSVParser<String>() {
            @Override
            public String parse(String value) {
                return value.toString();
            }
        });
        DEFAULT_CSV_PARSER.put(Integer.class.getName(), new CSVParser<Integer>() {
            @Override
            public Integer parse(String value) {
                if (value.isEmpty()) {
                    return null;
                }
                return Integer.parseInt(value);
            }
        });
        DEFAULT_CSV_PARSER.put(Long.class.getName(), new CSVParser<Long>() {
            @Override
            public Long parse(String value) {
                if (value.isEmpty()) {
                    return null;
                }
                return Long.parseLong(value);
            }
        });
        DEFAULT_CSV_PARSER.put(Double.class.getName(), new CSVParser<Double>() {
            @Override
            public Double parse(String value) {
                if (value.isEmpty()) {
                    return null;
                }
                return Double.parseDouble(value);
            }
        });
        DEFAULT_CSV_PARSER.put(BigDecimal.class.getName(), new CSVParser<BigDecimal>() {
            @Override
            public BigDecimal parse(String value) {
                if (value.isEmpty()) {
                    return null;
                }
                return new BigDecimal(value);
            }
        });
        DEFAULT_CSV_PARSER.put(java.util.Date.class.getName(), new CSVParser<java.util.Date>() {
            @Override
            public java.util.Date parse(String value) throws ParseException {
                if (value.isEmpty()) {
                    return null;
                }
                return DEFAULT_DATE_FORMAT.parse(value);
            }
        });
        DEFAULT_CSV_PARSER.put(java.sql.Date.class.getName(), new CSVParser<java.sql.Date>() {
            @Override
            public java.sql.Date parse(String value) throws ParseException {
                if (value.isEmpty()) {
                    return null;
                }
                return new java.sql.Date(DEFAULT_DATE_FORMAT.parse(value).getTime());
            }
        });
        DEFAULT_CSV_PARSER.put(java.sql.Timestamp.class.getName(), new CSVParser<java.sql.Timestamp>() {
            @Override
            public java.sql.Timestamp parse(String value) throws ParseException {
                if (value.isEmpty()) {
                    return null;
                }
                return new java.sql.Timestamp(DEFAULT_DATE_FORMAT.parse(value).getTime());
            }
        });
        DEFAULT_CSV_PARSER.put(java.sql.Time.class.getName(), new CSVParser<java.sql.Time>() {
            @Override
            public java.sql.Time parse(String value) throws ParseException {
                if (value.isEmpty()) {
                    return null;
                }
                return new java.sql.Time(DEFAULT_DATE_FORMAT.parse(value).getTime());
            }
        });
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    /**
     * 新しく {@code CSVReader} オブジェクトを構築します。
     * 
     * @param reader 読み込むCSVのリーダーオブジェクト
     * @param csvConfig CSVの定義を設定したオブジェクト
     * @throws IOException
     */
    public CSVReader(Reader reader, CSVConfig csvConfig, boolean hasHeader) throws IOException, CSVReadException {
        this(reader, csvConfig, null, hasHeader);
    }

    /**
     * 新しく {@code CSVReader} オブジェクトを構築します。
     * 
     * @param reader 読み込むCSVのリーダーオブジェクト
     * @param csvConfig CSVの定義を設定したオブジェクト
     * @param clazz JavaBeans（エンティティ）クラス
     * @throws IOException
     */
    public CSVReader(Reader reader, CSVConfig csvConfig, Class<T> clazz) throws IOException, CSVReadException {
        this(reader, csvConfig, clazz, true);
    }

    private CSVReader(Reader reader, CSVConfig csvConfig, Class<T> clazz, boolean hasHeader)
            throws IOException, CSVReadException {
        reader_ = new BufferedReader(reader);
        csvConfig_ = csvConfig;
        class_ = clazz;

        // エンティティクラスの指定なし、かつヘッダ無しの場合、ここで処理終了。（一行目からデータ行として読み取り）
        if ((null == clazz) && !hasHeader) {
            return;
        }

        // ヘッダ行を読込み
        String[] columns = null;
        columns = analyzeNextLine(reader_.readLine());
        lineCounter_++;
        if (null == columns) {
            return;
        }

        // 列名配列を取得
        columnNames_ = Arrays.copyOf(columns, columns.length);
        csvCounter_++;

        // エンティティクラスの指定がある場合
        if (null != class_) {
            setterMethods_ = new Method[columns.length];
            Method[] methods = class_.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                // setterメソッド以外はスキップ
                if (!method.getName().startsWith("set")) {
                    continue;
                }

                // プロパティ名を取得
                String propName = method.getName().substring(3).toLowerCase();

                // 列毎に検査
                for (int j = 0; j < columns.length; j++) {
                    String column = columns[j];
                    // プロパティ名と一致する場合は、その列数のメソッド配列にメソッドを格納
                    if (column.toLowerCase().equals(propName)) {
                        if (1 == method.getParameterTypes().length) {
                            setterMethods_[j] = method;
                            break;
                        }
                    }
                }
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * 一行を読み込んで、CSV形式で解釈した結果を取得
     * 
     * @return
     * @throws IOException
     */
    public String[] readLine() throws IOException {
        String[] result = _readLine();
        csvCounter_++;
        return result;
    }

    /**
     * 一行を読み込んで、列名をキーとしてCSV形式で解釈した結果を値とするマップを取得
     * 
     * @return 列名をキーとしてCSV形式で解釈した結果を値とするマップ
     * @throws IOException
     */
    public LinkedHashMap<String, String> readMap() throws IOException {
        // ヘッダ行を読み込んでいない場合はエラー
        if (null == columnNames_) {
            throw new IllegalStateException("this CSVReader is not required column names.");
        }

        // CSVカラム配列を取得
        String[] columns = _readLine();
        if (null == columns) {
            return null;
        }

        // 取得した配列をマップに格納
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        for (int i = 0; i < columns.length; i++) {
            if (i < columnNames_.length) {
                map.put(columnNames_[i], columns[i]);
            }
        }
        csvCounter_++;
        return map;
    }

    /**
     * 一行を読み込んで、CSV形式で解釈した結果を、コンストラクタで指定したクラスのオブジェクトに設定したものを取得
     * 
     * @return
     * @throws IOException
     */
    public T readEntity() throws IOException {
        // エンティティクラス未指定の場合はエラー
        if (null == class_) {
            throw new IllegalStateException("this CSVReader is not specified entity class.");
        }

        // CSVカラム配列を取得
        RETRY: for (int a = 0; a < Integer.MAX_VALUE;) {
            String[] columns = _readLine();
            if (null == columns) {
                return null;
            }

            try {
                // 返却エンティティを生成
                T result = class_.newInstance();

                // カラム毎の処理
                for (int i = 0; i < columns.length; i++) {
                    // カラムのセッターを取得
                    String column = columns[i];
                    if (setterMethods_.length <= i) {
                        break;
                    }
                    Method method = setterMethods_[i];
                    if (null == method) {
                        continue;
                    }
                    // エンティティの型を取得
                    Class<?> paramType = method.getParameterTypes()[0];
                    // エンティティの型に対応するパーサーを取得
                    CSVParser<?> parser = null;
                    parser = specifiedCsvParser_.get(paramType.getName());
                    if (null == parser) {
                        parser = DEFAULT_CSV_PARSER.get(paramType.getName());
                    }
                    // 未定義の型の場合はエラー
                    if (null == parser) {
                        errorInfoList_.add(new ErrorInfo(csvCounter_, lineCounter_, ErrorCode.PARSE_ERROR));
                    }
                    try {
                        // エンティティの型に合わせて文字列変換
                        Object param = parser.parse(column);
                        // セッターの実行
                        method.invoke(result, new Object[] {param });
                    } catch (Exception e) {
                        // 読み取りエラーが発生した場合は、エラーリストにエラー内容を格納して次の行へ進む。
                        int fileLine = lineCounter_ - (unAnalyzedLineList_.size() - 1);
                        for (String line : unAnalyzedLineList_) {
                            errorInfoList_
                                    .add(new ErrorInfo(csvCounter_, fileLine, ErrorCode.PARSE_ERROR).setLine(line));
                            fileLine++;
                        }
                        a++;
                        continue RETRY;
                    }
                }
                csvCounter_++;
                return result;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * データ型に対するCSV文字列の解釈（パース）方法を設定
     * 
     * @param clazz
     * @param csvParser
     */
    public <C> void setCSVParser(Class<C> clazz, CSVParser<C> csvParser) {
        specifiedCsvParser_.put(clazz.getName(), csvParser);
    }

    /**
     * 読み込んだCSV解釈行数を取得
     * 
     * @return このリーダがCSVとして解釈した行数（ヘッダ行を含む）
     */
    public int getCSVLines() {
        return csvCounter_;
    }

    /**
     * 読み込んだファイル行数を取得
     * 
     * @return このリーダが読み込んだ行数（ヘッダ行を含む）
     */
    public int getFileLines() {
        return lineCounter_;
    }

    /**
     * このリーダが読み込みに失敗した行の情報を取得
     * 
     * @return 失敗した行の情報のリスト
     */
    public List<ErrorInfo> getErrorList() {
        return errorInfoList_;
    }

    /**
     * このリーダが読み込みに失敗した行が存在するか否かを取得
     * 
     * @return 読込失敗行が存在する場合は true
     */
    public boolean hasErrorLines() {
        return !errorInfoList_.isEmpty();
    }

    @Override
    public void close() throws IOException {
        reader_.close();
    }

    // ///////////////////////

    public String[] _readLine() throws IOException {
        RETRY: for (int i = 0; i < Integer.MAX_VALUE; i++) {
            try {
                String nextLine = reader_.readLine();
                lineCounter_++;
                return analyzeNextLine(nextLine);
            } catch (CSVReadException e) {
                int fileLine = lineCounter_ - (unAnalyzedLineList_.size() - 1);
                for (String line : unAnalyzedLineList_) {
                    errorInfoList_.add(e.getErrorInfo().clone().setLine(line).setFileLine(fileLine++));
                }
                continue RETRY;
            }
        }
        return null;
    }

    private String[] analyzeNextLine(String nextLine) throws IOException, CSVReadException {
        unAnalyzedLineList_.clear();
        return ((null == nextLine) ? null : analyzeNextLine(nextLine, null, null));
    }

    private String[] analyzeNextLine(String nextLine, String currentColumnValue, ArrayList<String> resultList)
            throws IOException, CSVReadException {
        unAnalyzedLineList_.add(nextLine);
        resultList = analyzeCsv(nextLine, currentColumnValue, resultList);

        // 前行の読込が途中で終わった場合
        if (isNotEndedLine_) {
            // 次の行もCSV同行として読込み
            nextLine = reader_.readLine();
            lineCounter_++;
            // 次行がない場合はエラー
            if (null == nextLine) {
                throw new CSVReadException(new ErrorInfo(csvCounter_, lineCounter_, ErrorCode.INVALID_EOF), null);
            }
            // 次行を再帰で読込み
            String value = resultList.remove(resultList.size() - 1) + System.getProperty("line.separator");
            analyzeNextLine(nextLine, value, resultList);
        }
        return resultList.toArray(new String[] {});
    }

    private ArrayList<String> analyzeCsv(final String line, String currentColumnValue, ArrayList<String> currentList)
            throws CSVReadException {
        String current = null;
        ArrayList<String> resultList = ((null == currentList) ? new ArrayList<String>() : currentList);
        StringBuilder currentColumn = ((null == currentColumnValue) ? null : new StringBuilder(currentColumnValue));
        boolean isEncStarted = (null != currentColumnValue);
        boolean isEncEnded = false;
        boolean isEscaping = false;
        isNotEndedLine_ = false;

        String enc = csvConfig_.getEnclosure();
        int encLen = enc.length();
        String sep = csvConfig_.getSeparator();

        // 一文字ずつ検査
        for (int i = 0; i < line.length(); i++) {
            current = line.substring(i, i + 1);
            if (null == currentColumn) {
                currentColumn = new StringBuilder();
                isEncStarted = false;
                isEncEnded = false;
            }
            currentColumn.append(current);

            // 囲み文字有りの場合の検査
            if (!enc.isEmpty()) {
                // 囲み文字開始の検査
                if (!isEncStarted) {
                    // 囲み文字開始不正の例外浮揚
                    if (!enc.startsWith(currentColumn.toString())) { // 囲み文字と現在の文字列が不一致
                        throw new CSVReadException(
                                new ErrorInfo(csvCounter_, lineCounter_, ErrorCode.INVALID_ENCLOSURE), null);
                    }
                    // 囲み文字開始の検知
                    else if (enc.equals(currentColumn.toString())) { // 囲み文字と現在の文字列が一致
                        isEncStarted = true;
                        currentColumn = new StringBuilder();
                        continue;
                    }
                    // 囲み文字未定の場合
                    continue;
                }

                // 囲み文字終了の検査
                if (!isEncEnded && // 囲み文字の終了未認識
                        currentColumn.toString().endsWith(enc) && // 現在の文字列が囲み文字で終わる場合
                        !isEscaping) { // 前文字がエスケープ文字出なかった場合
                    // 囲み文字のエスケープの場合は処理継続
                    if (csvConfig_.isEscapeEnclosure() && // 囲み文字のエスケープが有効であること
                            ((i + encLen) < line.length()) // 読み取り残文字数が囲み文字文字数以上あるこｔ
                            && (line.startsWith(enc, (i + 1)))) { // 読み取り残文字列囲み文字であること
                        currentColumn.delete((currentColumn.length() - encLen), currentColumn.length());
                        isEscaping = true;
                        continue;
                    }
                    // 囲み文字終了の場合
                    isEncEnded = true;
                    resultList.add(currentColumn.substring(0, (currentColumn.length() - encLen)));
                    currentColumn = new StringBuilder();
                    continue;
                } else if (isEscaping) {
                    isEscaping = false;
                }

                // 区切り文字の検査
                if (isEncStarted && isEncEnded) {
                    // 既に当該カラムの終了囲み文字認識済で、区切り文字ではない場合
                    if (!sep.startsWith(currentColumn.toString())) {
                        throw new CSVReadException(
                                new ErrorInfo(csvCounter_, lineCounter_, ErrorCode.INVALID_ENCLOSURE), null);
                    }
                    // 囲み文字が出現した場合
                    if (currentColumn.toString().equals(sep)) {
                        currentColumn = null;
                        continue;
                    }
                    // それ以外
                    continue;
                }
            }
            // 囲み文字なしの場合の検査
            else {
                // 区切り文字の検査
                if (currentColumn.toString().endsWith(sep)) {
                    resultList.add(currentColumn.substring(0, currentColumn.length() - sep.length()));
                    currentColumn = null;
                }
            }
        }
        if (isEncStarted && !isEncEnded && (null != currentColumn)) {
            isNotEndedLine_ = true;
            resultList.add(currentColumn.toString());
        } else if (enc.isEmpty()) {
            if (null == currentColumn) {
                resultList.add(null);
            } else {
                resultList.add(currentColumn.toString());
            }
        }
        return resultList;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    public static class CSVReadException extends Exception {

        private static final long serialVersionUID = 584426745386619909L;

        private ErrorInfo errorInfo_;

        private String failRead_;

        public CSVReadException(ErrorInfo errorInfo, String failRead) {
            super("cannnot read as CSV:" + errorInfo.getCsvLine_());
            errorInfo_ = errorInfo;
            failRead_ = failRead;
        }

        public ErrorInfo getErrorInfo() {
            return errorInfo_;
        }

        public String getFailRead() {
            return failRead_;
        }
    }

    // ///////////////////////

    public static enum ErrorCode {
        INVALID_BREAK_LINE, // 改行位置不正
        INVALID_ENCLOSURE, // 囲み文字不正
        INVALID_COLUMN_NUM, // カラム数不正
        INVALID_EOF, // ファイル終了位置不正
        PARSE_ERROR; // 文字列変換エラー
    }

    // ///////////////////////

    public static class ErrorInfo {
        private int csvLine_;

        private int fileLine_;

        private ErrorCode errorCode_;

        private String line_;

        public ErrorInfo(int csvLine, int fileLine, ErrorCode errorCode) {
            csvLine_ = csvLine;
            fileLine_ = fileLine;
            errorCode_ = errorCode;
        }

        public int getCsvLine_() {
            return csvLine_;
        }

        public int getFileLine_() {
            return fileLine_;
        }

        public String getLine_() {
            return line_;
        }

        public ErrorCode getErrorCode_() {
            return errorCode_;
        }

        private ErrorInfo setLine(String line) {
            line_ = line;
            return this;
        }

        private ErrorInfo setFileLine(int fileLine) {
            fileLine_ = fileLine;
            return this;
        }

        @Override
        protected ErrorInfo clone() {
            return new ErrorInfo(csvLine_, fileLine_, errorCode_);
        }
    }

    // ///////////////////////

    public static interface CSVParser<T> {
        public T parse(String value) throws Exception;
    }
}
