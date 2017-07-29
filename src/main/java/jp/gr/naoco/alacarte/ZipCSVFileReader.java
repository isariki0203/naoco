package jp.gr.naoco.alacarte;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import jp.gr.naoco.alacarte.CSVReader.CSVReadException;
import jp.gr.naoco.alacarte.CSVReader.ErrorInfo;
import jp.gr.naoco.alacarte.ZipFileWriter.CSVConfig;

/**
 * ZIPファイルをCSV形式で読み込む
 * <p>
 * 文字列配列で読み込む場合の実装例
 * </p>
 * 
 * <pre>
 * Usage:
 *    ZipCSVFileReader reader = null;
 *    try {
 *        reader = new ZipCSVFileReader(input, new CSVConfig());
 *        ZipEntry entry = null;
 *        while(null != (entry = reader.nextZipEntry())) {
 *            entry; // 現在読込中のZipのエントリ（展開後のファイル名などが格納）
 *            reader.setNext();
 *            String[] result = null;
 *            while(null != (result = reader.readCSV())) {
 *                result; // CSV解釈で分解した文字列配列
 *            }
 *            // エラー有無を確認
 *            if(reader.hasErrorLines()) {
 *                List&lt;ErrorInfo&gt; errorList = reader.getErrorList(); // 読み取り失敗した行の情報を取得
 *            }
 *        }
 *    } finally {
 *        reader.close();
 *    }
 * </pre>
 * <p>
 * エンティティオブジェクトで読み込む場合の実装例
 * </p>
 * 
 * <pre>
 * Usage:
 *    ZipCSVFileReader reader = null;
 *    try {
 *        reader = new ZipCSVFileReader(input, new CSVConfig());
 *        ZipEntry entry = null;
 *        while(null != (entry = reader.nextZipEntry())) {
 *            entry; // 現在読込中のZipのエントリ（展開後のファイル名などが格納）
 *            reader.setNext(entityClass);
 *            Entity entity = null;
 *            while(null != (result = (Entity)reader.readCSV2Entity())) {
 *                entity; // CSV解釈で分解した文字列を格納したエンティティ
 *            }
 *            // エラー有無を確認
 *            if(reader.hasErrorLines()) {
 *                List&lt;ErrorInfo&gt; errorList = reader.getErrorList(); // 読み取り失敗した行の情報を取得
 *            }
 *        }
 *    } finally {
 *        reader.close();
 *    }
 * </pre>
 * 
 * @author naoco0917
 */
public class ZipCSVFileReader implements Closeable {

    private InputStream in_ = null;

    private BufferedInputStream buf_ = null;

    private ZipInputStream zip_ = null;

    private InputStreamReader ir_ = null;

    private CSVReader<?> reader_ = null;

    private CSVConfig csvConfig_ = null;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public ZipCSVFileReader(InputStream input, CSVConfig csvConfig) throws IOException {
        in_ = input;
        buf_ = new BufferedInputStream(in_);
        zip_ = new ZipInputStream(buf_);
        ir_ = new InputStreamReader(zip_);
        csvConfig_ = csvConfig;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    public ZipEntry nextZipEntry() throws IOException {
        return zip_.getNextEntry();
    }

    /**
     * 新しいZipEntryの読込を開始するときに一度呼出す。
     * 
     * @throws IOException
     * @throws CSVReadException
     */
    public <T> void setNext() throws IOException, CSVReadException {
        try {
            reader_ = new CSVReader<T>(ir_, csvConfig_, true);
        } catch (CSVReadException e) {
            // nothing to do
        }
    }

    /**
     * 新しいZipEntryの読込を開始するときに一度呼出す。
     * 
     * @param entityClass このZipエントリの値を設定するオブジェクトのクラス
     * @throws IOException CSV読込時のエラー
     * @throws CSVReadException CSVヘッダ行をCSV解釈している最中に発生するエラー
     */
    public <T> void setNext(Class<T> entityClass) throws IOException, CSVReadException {
        reader_ = new CSVReader<T>(ir_, csvConfig_, entityClass);
    }

    public List<ErrorInfo> getErrorList() {
        return reader_.getErrorList();
    }

    public boolean hasErrorLines() {
        return reader_.hasErrorLines();
    }

    public String[] readCSV() throws IOException {
        String[] result = reader_.readLine();
        return result;
    }

    public LinkedHashMap<String, String> readCSV2Map() throws IOException {
        LinkedHashMap<String, String> result = reader_.readMap();
        return result;
    }

    public Object readCSV2Entity() throws IOException {
        Object result = reader_.readEntity();
        return result;
    }

    @Override
    public void close() throws IOException {
        try {
            if (null != reader_) {
                reader_.close();
            }
            if (null != ir_) {
                ir_.close();
            }
            if (null != zip_) {
                zip_.close();
            }
            if (null != buf_) {
                buf_.close();
            }
        } finally {
            if (null != in_) {
                in_.close();
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static Logger LOG = Logger.getLogger(ZipCSVFileReader.class);
}
