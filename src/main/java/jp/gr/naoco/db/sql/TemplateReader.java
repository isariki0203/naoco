package jp.gr.naoco.db.sql;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import jp.gr.naoco.core.log.LaolLogger;
import jp.gr.naoco.db.exception.QueryTemplateException;

public class TemplateReader {
    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private TemplateReader() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * テンプレートファイルを読み込み、行単位で文字列を取得するIteratorを返却する。
     * <p>
     * 既にTemplateAnalyzerがキャッシュ済みの場合はnullを返却する。
     * </p>
     * 
     * @param path
     *            現在のスレッドのクラスローダが参照するクラスパスからの、ファイルの相対パス（パスセパレータは"/"）
     * @param needsCached
     *            既にTemplateAnalyzerがキャッシュ済みの場合に再読み込みをした結果を取得する場合はtrue、
     *            キャッシュ済の場合にnullを取得する場合はfalse
     * @return 読み込んだテンプレートファイルを行単位で取得するIterator
     * @throw QueryTemplateException SQLテンプレートの読み込みに失敗した場合
     */
    public static Iterator<String> readTemplate(String path) {
        return readTemplate(path, false);
    }

    /**
     * テンプレートファイルを読み込み、行単位で文字列を取得するIteratorを返却する。
     * <p>
     * 既にTemplateAnalyzerがキャッシュ済みの場合はnullを返却する。
     * </p>
     * 
     * @param path
     *            現在のスレッドのクラスローダが参照するクラスパスからの、ファイルの相対パス（パスセパレータは"/"）
     * @param needsCached
     *            既にTemplateAnalyzerがキャッシュ済みの場合に再読み込みをした結果を取得する場合はtrue、
     *            キャッシュ済の場合にnullを取得する場合はfalse
     * @return 読み込んだテンプレートファイルを行単位で取得するIterator
     * @throw QueryTemplateException SQLテンプレートの読み込みに失敗した場合
     */
    public static Iterator<String> readTemplate(String path, boolean needsCached) {
        if (!needsCached && (null != TemplateAnalyzer.ELEM_CACHE.get(path))) {
            return null;
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (null == cl) {
            throw new QueryTemplateException("Class Loader is not found.");
        }

        InputStream in = cl.getResourceAsStream(path);
        if (null == in) {
            throw new QueryTemplateException("SQL template is not found.:" + path);
        }

        InputStreamReader ir = new InputStreamReader(in);
        BufferedReader buf = new BufferedReader(ir);
        TemplateLineIterator iterator = new TemplateLineIterator(buf, ir, in);
        return iterator;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    protected static class TemplateLineIterator implements Iterator<String> {
        private final BufferedReader buf_;

        private final Closeable[] cl_;

        private String line_ = null;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        private TemplateLineIterator(BufferedReader buf, Closeable... cl) {
            buf_ = buf;
            cl_ = cl;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public boolean hasNext() {
            try {
                line_ = buf_.readLine();
                boolean result = (null != line_);
                if (!result) {
                    buf_.close();
                    for (Closeable cl : cl_) {
                        if (null != cl) {
                            cl.close();
                        }
                    }
                }
                return result;
            } catch (IOException e) {
                try {
                    if (null != buf_) {
                        buf_.close();
                        for (Closeable cl : cl_) {
                            if (null != cl) {
                                cl.close();
                            }
                        }
                    }
                } catch (IOException e2) {
                    LOG.error(e2.getMessage(), e2);
                }
                throw new RuntimeException(e);
            }
        }

        @Override
        public String next() {
            return line_;
        }

        @Override
        public void remove() {
            try {
                if (null != buf_) {
                    buf_.close();
                    for (Closeable cl : cl_) {
                        if (null != cl) {
                            cl.close();
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(TemplateReader.class.getName());
}
