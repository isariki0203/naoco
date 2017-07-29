package jp.gr.naoco.chain;

import org.apache.log4j.Logger;

import jp.gr.naoco.core.log.LogAdaptor;

public class OriginalLogAdaptor implements LogAdaptor {

    public static final Logger log = Logger.getRootLogger();

    @Override
    public void fatal(String message) {
        log.fatal(message);
    }

    @Override
    public void fatal(String message, Throwable t) {
        log.fatal(message, t);
    }

    @Override
    public void error(String message) {
        log.error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        log.error(message, t);
    }

    @Override
    public void warn(String message) {
        log.warn(message);
    }

    @Override
    public void warn(String message, Throwable t) {
        log.warn(message, t);

    }

    @Override
    public void info(String message) {
        log.info(message);

    }

    @Override
    public void info(String message, Throwable t) {
        log.info(message, t);
    }

    @Override
    public void debug(String message) {
        log.debug(message);
    }

    @Override
    public void debug(String message, Throwable t) {
        log.debug(message, t);
    }

    @Override
    public void sql(String message) {
        log.debug(message);
    }

    @Override
    public void trace(String message) {
        // nothing to do.

    }

    @Override
    public void initialize(String name) {
        // nothing to do
    }

    @Override
    public boolean requiredDebugLevel() {
        return true;
    }
}
