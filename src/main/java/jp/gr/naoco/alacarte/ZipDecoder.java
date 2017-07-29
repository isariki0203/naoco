package jp.gr.naoco.alacarte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jp.gr.naoco.alacarte.ZipEncoder.FileProducer;
import jp.gr.naoco.alacarte.ZipEncoder.StreamBuffer;
import jp.gr.naoco.chain.Chain;
import jp.gr.naoco.chain.Consumer;
import jp.gr.naoco.chain.ConsumerFilter;
import jp.gr.naoco.chain.ConsumerProducer;
import jp.gr.naoco.chain.Container;
import jp.gr.naoco.chain.queue.ProducerQueue;

import org.apache.log4j.Logger;

public class ZipDecoder {

    private int bufferSize_ = 1024;

    private boolean parallel_ = false;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public ZipDecoder() {
        // nothing to do
    }

    public ZipDecoder(int bufferSize, boolean parallel) {
        bufferSize_ = bufferSize;
        parallel_ = parallel;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * Zipアーカイブをデコードする
     * 
     * @param zipFile
     *            解凍対象のzipファイル
     * @param outputFile
     *            解凍先のディレクトリ
     * @throws IOException
     */
    public void decode(File zipFile, File outputDir) {
        // 出力先の状態確認
        if (!outputDir.exists()) {
            boolean result = outputDir.mkdirs();
            if (!result) {
                throw new IllegalStateException(outputDir.getAbsoluteFile() + " can not create directory.");
            }
        } else if (!outputDir.isDirectory() && !outputDir.canWrite()) {
            throw new IllegalStateException(outputDir.getAbsoluteFile() + " can not output.");
        }

        // 処理の実行
        Chain chain = new Chain(new FileProducer(zipFile, false))
                // ファイルをZipInputStreamとして読み込み
                .direct(new ZipInputStreamRelayer(bufferSize_, outputDir.getAbsolutePath()));
        // 単独スレッドで順次処理
        if (parallel_) {
            chain = chain.parallel(new FileOutputStreamConsumer(outputDir));
        }
        // 別スレッドで並行処理
        else {
            chain = chain.direct(new FileOutputStreamConsumer(outputDir));
        }
        chain.execute();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    private static class ZipInputStreamRelayer extends ConsumerProducer {
        private int bufferSize_;

        private String outputPath_;

        private static final ConsumerFilter FILTER = new ConsumerFilter() {
            @Override
            public boolean accept(Container container) {
                return ((container.get() instanceof InputStream) || container.get() instanceof String);
            }
        };

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public ZipInputStreamRelayer(int bufferSize, String outputPath) {
            bufferSize_ = bufferSize;
            outputPath_ = outputPath;
            if (!outputPath.endsWith(System.getProperty("file.separator"))) {
                outputPath_ = outputPath_ + System.getProperty("file.separator");
            }
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public void execute_(Container container, ProducerQueue queue) {
            // String（ファイル名）がContainerで来た場合はスキップする。
            if (container.get() instanceof String) {
                return;
            }

            // InputStreamから、ZipEntry毎にバイト配列を順次読み込んで次の処理に渡す
            InputStream in = (InputStream) container.get();
            ZipInputStream zip = null;
            ZipEntry entry = null;
            String separator = System.getProperty("file.separator");
            separator = ("\\".equals(separator) ? "\\\\" : separator);
            try {
                zip = new ZipInputStream(in);
                byte[] bytes = new byte[bufferSize_];
                int len = 0;
                while (null != (entry = zip.getNextEntry())) {
                    queue.offer(new Container(outputPath_ + entry.getName().replaceAll("\\/", separator)));
                    LOG.debug(entry.getName());
                    while (0 <= (len = zip.read(bytes))) {
                        StreamBuffer obj = new StreamBuffer(bytes, len);
                        queue.offer(new Container(obj));
                        bytes = new byte[bufferSize_];
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (null != zip) {
                        zip.close();
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

    private static class FileOutputStreamConsumer extends Consumer {

        private File file_ = null;

        private FileOutputStream out_ = null;

        private static final ConsumerFilter FILTER = new ConsumerFilter() {
            @Override
            public boolean accept(Container container) {
                return ((container.get() instanceof String) || (container.get() instanceof StreamBuffer));
            }
        };

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public FileOutputStreamConsumer(File file) {
            file_ = file;
            if (!file.isDirectory()) {
                try {
                    out_ = new FileOutputStream(file_);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public void execute_(Container container) {
            Object input = container.get();

            // 入力がStringの場合は新たなFileOutputStreamをセットして終了
            if (input instanceof String) {
                try {
                    File outputFile = new File((String) input);
                    File outputDir = outputFile.getParentFile();
                    if (null != outputDir) {
                        outputDir.mkdirs();
                    }
                    out_ = new FileOutputStream(outputFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            StreamBuffer bytes = (StreamBuffer) input;
            try {
                out_.write(bytes.getBytes(), 0, bytes.getLength());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void finalize() {
            try {
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

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static Logger LOG = Logger.getLogger(ZipEncoder.class);
}
