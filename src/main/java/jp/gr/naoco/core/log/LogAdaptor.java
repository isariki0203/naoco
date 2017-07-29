package jp.gr.naoco.core.log;

/**
 * 本ライブラリのロギング処理を、利用側アプリケーションのロギング処理に接続するためのインターフェース
 */
public interface LogAdaptor {
    public void fatal(String message);

    public void fatal(String message, Throwable t);

    public void error(String message);

    public void error(String message, Throwable t);

    public void warn(String message);

    public void warn(String message, Throwable t);

    public void info(String message);

    public void info(String message, Throwable t);

    public void debug(String message);

    public void debug(String message, Throwable t);

    public void trace(String message);

    public void sql(String message);

    public void initialize(String name);

    public boolean requiredDebugLevel();
}
