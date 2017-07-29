package jp.gr.naoco.core.log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 本ライブラリのロガー
 * <p>
 * 本ライブラリの利用側でロギング処理を保持している場合は、{@link LogAdaptor}を実装して本クラスの {@link setLogAdaptor} で設定することにより、本ライブラリのログ出力を利用側のログ出力設定に従って
 * 出力させることができる。
 * </p>
 * <p>
 * {@link setLogAdaptor}によるログアダプタの設定を行わなかった場合、本ライブラリは標準出力と 標準エラー出力に、ログのメッセージ内容を出力する。
 * </p>
 */
public class LaolLogger {
    private String className_;

    private LogAdaptor adaptor_;

    private static LogAdaptor adaptorBase_ = new LogAdaptor() {
        private String name_;

        @Override
        public void fatal(String message) {
            System.err.println(getMessageHeader() + message);
        }

        @Override
        public void fatal(String message, Throwable t) {
            System.err.println(getMessageHeader() + message);
            t.printStackTrace(System.err);
        }

        @Override
        public void error(String message) {
            System.err.println(getMessageHeader() + message);
        }

        @Override
        public void error(String message, Throwable t) {
            System.err.println(getMessageHeader() + message);
            t.printStackTrace(System.err);
        }

        @Override
        public void warn(String message) {
            System.err.println(getMessageHeader() + message);
        }

        @Override
        public void warn(String message, Throwable t) {
            System.err.println(getMessageHeader() + message);
            t.printStackTrace(System.err);
        }

        @Override
        public void info(String message) {
            System.out.println(getMessageHeader() + message);
        }

        @Override
        public void info(String message, Throwable t) {
            System.out.println(getMessageHeader() + message);
            t.printStackTrace(System.out);
        }

        @Override
        public void debug(String message) {
            System.out.println(getMessageHeader() + message);
        }

        @Override
        public void debug(String message, Throwable t) {
            System.out.println(getMessageHeader() + message);
            t.printStackTrace(System.out);
        }

        @Override
        public void sql(String message) {
            System.out.println(getMessageHeader() + message);
        }

        @Override
        public void initialize(String name) {
            name_ = name;
        }

        private String getMessageHeader() {
            StringBuilder builder = new StringBuilder();
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            builder.append(format.format(new Date()));
            DecimalFormat nFormat = new DecimalFormat("00000");
            builder.append(" [Thread-").append(nFormat.format(Thread.currentThread().getId())).append("] ");
            StackTraceElement[] elems = new Throwable().getStackTrace();
            for (StackTraceElement elem : elems) {
                if (elem.getClassName().startsWith(name_)) {
                    builder.append(elem.getClassName());
                    builder.append("#");
                    builder.append(elem.getMethodName());
                    builder.append(" ");
                    return builder.toString();
                }
            }
            builder.append(name_);
            builder.append("#");
            builder.append("<null> ");
            return builder.toString();
        }

        @Override
        public void trace(String message) {
            System.out.println(getMessageHeader() + message);
        }

        @Override
        public boolean requiredDebugLevel() {
            return true;
        }
    };

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public LaolLogger(String className) {
        className_ = className;
        try {
            adaptor_ = adaptorBase_.getClass().newInstance();
        } catch (Exception e) {
            // nothing to do;
        }
        adaptor_.initialize(className_);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    public static void setLogAdaptor(LogAdaptor adaptor) {
        adaptorBase_ = adaptor;
    }

    public void fatal(String message) {
        adaptor_.fatal(message);
    }

    public void fatal(String message, Throwable t) {
        adaptor_.fatal(message, t);
    }

    public void error(String message) {
        adaptor_.error(message);
    }

    public void error(String message, Throwable t) {
        adaptor_.error(message, t);
    }

    public void warn(String message) {
        adaptor_.warn(message);
    }

    public void warn(String message, Throwable t) {
        adaptor_.warn(message, t);
    }

    public void info(String message) {
        adaptor_.info(message);
    }

    public void info(String message, Throwable t) {
        adaptor_.info(message, t);
    }

    public void debug(String message) {
        adaptor_.debug(message);
    }

    public void debug(String message, Throwable t) {
        adaptor_.debug(message, t);
    }

    public void sql(String message) {
        adaptor_.debug(message);
    }

    public void trace(String message) {
        adaptor_.trace(message);
    }

    public boolean requiredDebugLevel() {
        return adaptor_.requiredDebugLevel();
    }
}
