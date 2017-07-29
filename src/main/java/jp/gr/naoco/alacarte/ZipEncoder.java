package jp.gr.naoco.alacarte;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import jp.gr.naoco.chain.Chain;
import jp.gr.naoco.chain.Consumer;
import jp.gr.naoco.chain.ConsumerFilter;
import jp.gr.naoco.chain.ConsumerProducer;
import jp.gr.naoco.chain.Container;
import jp.gr.naoco.chain.Producer;
import jp.gr.naoco.chain.queue.ProducerQueue;

/**
 * ストリームあるいはファイルをZip圧縮した結果をストリームに出力するか、ファイルに出力する。
 * 
 * @author naoco0917
 */
public class ZipEncoder {

    private int compressionLevel_ = Deflater.DEFAULT_COMPRESSION;

    private int bufferSize_ = 1024;

    private boolean parallel_ = false;

    private static final String TEMP_SUFFIX = ".tmp";

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    /**
     * このコンストラクタは new ZipEncoder(Deflater.DEFAULT_COMPRESSION, 1024, false)
     * を呼び出すのと同等。
     */
    public ZipEncoder() {
        // nothing to do
    }

    /**
     * Zipエンコーダを生成
     * 
     * @param compressionLevel
     *            ZipOutputStream#setLevelで指定する圧縮レベル
     * @param bufferSize
     *            InputStream読み出し時のバッファサイズ
     * @param parallel
     *            Zip出力を現在のスレッドに対して、別スレッド実行とする場合はtrue、順次実行とする場合はfalse
     */
    public ZipEncoder(int compressionLevel, int bufferSize, boolean parallel) {
        compressionLevel_ = compressionLevel;
        bufferSize_ = bufferSize;
        parallel_ = parallel;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * InputStreamの内容をZip圧縮してファイルに出力する
     * 
     * @param in
     *            圧縮対象データのストリーム
     * @param outputFile
     *            出力先ファイル
     * @param entryName
     *            出力Zipのエントリ名（解凍後のファイル名）
     * @throws IOException
     */
    public void output(InputStream in, File outputFile, String entryName) throws IOException {
        output_(new InputStreamProducer(in, entryName), outputFile);
    }

    /**
     * ファイルあるいはディレクトリのZipアーカイブを作成する
     * 
     * @param inputFile
     *            圧縮対象ファイルあるいはディレクトリ
     * @param outputFile
     *            圧縮先アーカイブファイル
     * @throws IOException
     */
    public void output(File inputFile, File outputFile) throws IOException {
        output_(new FileProducer(inputFile, true), outputFile);
    }

    /**
     * InputStreamの内容をZip圧縮した結果を、指定したOutputStreamに出力する
     * 
     * @param in
     *            圧縮対象データのストリーム
     * @param out
     *            圧縮結果出力先のストリーム
     * @param entryName
     *            出力Zipのエントリ名（解凍後のファイル名）
     * @throws IOException
     */
    public void encode(InputStream in, OutputStream out, String entryName) throws IOException {
        // 実行チェーンの生成
        Chain chain = new Chain(new InputStreamProducer(in, entryName))
                // InputStreamの読み込み
                .direct(new InputStreamRelayer(bufferSize_))
                // ZipEntryの設定
                .direct(new ZipEntryRelayer());
        // 並行処理によるZip出力
        if (parallel_) {
            chain = chain.parallel(new ZipOutputStreamConsumer(out, compressionLevel_));
        }
        // 順次処理によるZip出力
        else {
            chain = chain.direct(new ZipOutputStreamConsumer(out, compressionLevel_));
        }
        // 処理の実行
        chain.execute();
    }

    private void output_(Producer producer, File outputFile) throws IOException {
        if (outputFile.exists() && !outputFile.canWrite()) {
            throw new IllegalArgumentException(outputFile.getAbsolutePath() + " can not over write.");
        }

        // 出力先一時ファイルを指定し、OutputStreamを生成。
        File tmpFile = new File(outputFile.getAbsoluteFile() + TEMP_SUFFIX);
        FileOutputStream out = new FileOutputStream(tmpFile);

        // 実行チェーンの生成
        Chain chain = new Chain(producer)
                // InputStreamの読み込み
                .direct(new InputStreamRelayer(bufferSize_))
                // ZipEntryの設定
                .direct(new ZipEntryRelayer());
        // 並行処理によるZip出力
        if (parallel_) {
            chain = chain.parallel(new ZipOutputStreamConsumer(out, compressionLevel_));
        }
        // 順次処理によるZip出力
        else {
            chain = chain.direct(new ZipOutputStreamConsumer(out, compressionLevel_));
        }
        // 処理の実行
        chain.execute();
        LOG.debug("create:" + tmpFile.getAbsolutePath());

        // 一時ファイルを本ファイルへ移動
        outputFile.delete();
        boolean result = tmpFile.renameTo(outputFile);
        if (!result) {
            throw new IllegalArgumentException(outputFile.getAbsolutePath() + " can not over write.");
        }
        LOG.debug("rename:" + tmpFile.getAbsolutePath() + " to " + outputFile.getAbsolutePath());
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    /** 一つのInputStreamについて、Zip圧縮処理を開始する。 */
    private static class InputStreamProducer implements Producer {

        private InputStream in_;

        private String entryName_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor
        public InputStreamProducer(InputStream in, String entryName) {
            in_ = in;
            entryName_ = entryName;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public void execute(ProducerQueue queue) {
            queue.offer(new Container(entryName_));
            queue.offer(new Container(in_));
        }
    }

    // ///////////////////////

    /**
     * 指定したファイルパスについて、Zip圧縮処理を開始する。パスがディレクトリの場合は、再帰的にディレクトリ内の各ファイルを圧縮対象とするようにする。
     */
    public static class FileProducer implements Producer {

        private File file_;

        private boolean allowDirectory_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public FileProducer(File file, boolean allowDirectory) {
            file_ = file;
            allowDirectory_ = allowDirectory;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public void execute(ProducerQueue queue) {
            // ファイルの状態確認
            if (!file_.exists() || !file_.canRead()) {
                throw new IllegalArgumentException(file_.getAbsolutePath() + " can not read.");
            } else if (!allowDirectory_ && file_.isDirectory()) {
                throw new IllegalArgumentException(file_.getAbsolutePath() + " is directory.");
            }

            // パスとFileInputStreamをキューへ格納
            if (file_.isFile()) {
                queue.offer(new Container(file_.getName()));
                try {
                    queue.offer(new Container(new FileInputStream(file_)));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else if (allowDirectory_ && file_.isDirectory()) {
                setRecursively(file_, queue, file_.getAbsolutePath());
            }
        }

        private void setRecursively(File file, ProducerQueue queue, String rootpath) {
            if (!file.isDirectory() || !file.exists() || !file.canRead()) {
                return;
            }

            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File child = files[i];
                if (child.isDirectory()) {
                    setRecursively(child, queue, rootpath);
                    continue;
                }

                String path = child.getAbsolutePath();
                path = path.substring(rootpath.length() + 1).replaceAll("\\\\", "/");
                queue.offer(new Container(path));
                try {
                    queue.offer(new Container(new FileInputStream(child)));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(child.getAbsolutePath(), e);
                }
            }
        }
    }

    // ///////////////////////

    /** InputStream を部分バイト配列毎に次の処理へ移譲する。 */
    private static class InputStreamRelayer extends ConsumerProducer {

        private int bufferSize_;

        private static final ConsumerFilter FILTER = new ConsumerFilter() {
            @Override
            public boolean accept(Container container) {
                return (container.get() instanceof InputStream);
            }
        };

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public InputStreamRelayer(int bufferSize) {
            bufferSize_ = bufferSize;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public void execute_(Container container, ProducerQueue queue) {
            // InputStreamからバイト配列を順次読み込んで次の処理に渡す
            InputStream in = (InputStream) container.get();
            BufferedInputStream buf = null;
            try {
                buf = new BufferedInputStream(in);
                byte[] bytes = new byte[bufferSize_];
                int len = 0;
                while (0 <= (len = buf.read(bytes))) {
                    StreamBuffer obj = new StreamBuffer(bytes, len);
                    queue.offer(new Container(obj));
                    bytes = new byte[bufferSize_];
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (null != buf) {
                        buf.close();
                    }
                    if (null != in) {
                        in.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public ConsumerFilter getDefaultFilter() {
            return FILTER;
        }
    }

    // ///////////////////////

    /** パス名からZipEntryを生成して次の処理へ移譲する。 */
    private static class ZipEntryRelayer extends ConsumerProducer {

        private static final ConsumerFilter FILTER = new ConsumerFilter() {
            @Override
            public boolean accept(Container container) {
                return (container.get() instanceof String);
            }
        };

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public ZipEntryRelayer() {
            // nothing to do
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public void execute_(Container container, ProducerQueue queue) {
            Object input = container.get();
            queue.offer(new Container(new ZipEntry((String) input)));
        }

        @Override
        public ConsumerFilter getDefaultFilter() {
            return FILTER;
        }
    }

    // ///////////////////////

    /** Zip圧縮結果をOutputStreamに出力する。 */
    private static class ZipOutputStreamConsumer extends Consumer {

        private OutputStream out_ = null;

        private ZipOutputStream zip_ = null;

        private static final ConsumerFilter FILTER = new ConsumerFilter() {
            @Override
            public boolean accept(Container container) {
                return ((container.get() instanceof ZipEntry) || (container.get() instanceof StreamBuffer));
            }
        };

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public ZipOutputStreamConsumer(OutputStream out, int compressionLevel) {
            out_ = out;
            zip_ = new ZipOutputStream(out);
            if (Deflater.DEFAULT_COMPRESSION != compressionLevel) {
                zip_.setLevel(compressionLevel);
            }
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public void execute_(Container container) {
            Object input = container.get();

            // 入力がZipEntryの場合はZipEntryをセットして終了
            if (input instanceof ZipEntry) {
                try {
                    zip_.flush();
                    zip_.putNextEntry((ZipEntry) input);
                    LOG.debug(((ZipEntry) input).getName());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            StreamBuffer bytes = (StreamBuffer) input;
            try {
                zip_.write(bytes.getBytes(), 0, bytes.getLength());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void finalize() {
            try {
                zip_.closeEntry();
                zip_.close();
                out_.close();
                LOG.debug("output finished.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public ConsumerFilter getDefaultFilter() {
            return FILTER;
        }
    }

    // ///////////////////////

    public static class StreamBuffer {
        private byte[] bytes_;
        private int length_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public StreamBuffer(byte[] bytes, int length) {
            bytes_ = bytes;
            length_ = length;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        public byte[] getBytes() {
            return bytes_;
        }

        public int getLength() {
            return length_;
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static Logger LOG = Logger.getLogger(ZipEncoder.class);
}
