package jp.gr.naoco.external.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import jp.gr.naoco.core.NaocoCoreInitializer;
import jp.gr.naoco.core.log.LogAdaptor;

/**
 * naocoが出力するログを、Log4jで出力させるためのアダプタ
 * <p>
 * naocoが出力するログ（発行直前のSQL文など）をLog4jで出力させるためのアダプタクラス。
 * </p>
 * <p>
 * 使用法は以下のように、{@link NaocoCoreInitializer#initialize} の第二引数で本クラスのインスタンスを指定して呼び出すことで、
 * その後からロードされたnaocoのクラスについて、出力するログがLog4jによるものとなる。
 * </p>
 *
 * <pre>
 * NaocoCoreInitializer.initialize("BaseName", <b>new Log4jLogAdaptor()</b>);
 * </pre>
 *
 * @author naoco0917
 */
public class Log4jLogAdaptor implements LogAdaptor {

    private Logger logger_;

    private String name_;

    private boolean requiredDebug_ = false;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public Log4jLogAdaptor() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public void fatal(String message) {
        logger_.log(name_, Level.FATAL, message, null);
    }

    @Override
    public void fatal(String message, Throwable t) {
        logger_.log(name_, Level.FATAL, message, t);
    }

    @Override
    public void error(String message) {
        logger_.log(name_, Level.ERROR, message, null);
    }

    @Override
    public void error(String message, Throwable t) {
        logger_.log(name_, Level.ERROR, message, t);
    }

    @Override
    public void warn(String message) {
        logger_.log(name_, Level.WARN, message, null);
    }

    @Override
    public void warn(String message, Throwable t) {
        logger_.log(name_, Level.WARN, message, t);
    }

    @Override
    public void info(String message) {
        logger_.log(name_, Level.INFO, message, null);
    }

    @Override
    public void info(String message, Throwable t) {
        logger_.log(name_, Level.INFO, message, t);
    }

    @Override
    public void debug(String message) {
        logger_.log(name_, Level.DEBUG, message, null);
    }

    @Override
    public void debug(String message, Throwable t) {
        logger_.log(name_, Level.DEBUG, message, t);
    }

    @Override
    public void sql(String message) {
        logger_.log(name_, Level.DEBUG, message, null);
    }

    @Override
    public void trace(String message) {
        logger_.log(name_, Level.TRACE, message, null);
    }

    @Override
    public void initialize(String name) {
        logger_ = Logger.getLogger(name);
        name_ = name;
        if ((null != logger_.getEffectiveLevel()) && (Level.DEBUG_INT == logger_.getEffectiveLevel().toInt())) {
            requiredDebug_ = true;
        }
    }

    @Override
    public boolean requiredDebugLevel() {
        return requiredDebug_;
    }

}
