package jp.gr.naoco.core;

import jp.gr.naoco.core.conf.Configuration;
import jp.gr.naoco.core.log.LaolLogger;
import jp.gr.naoco.core.log.LogAdaptor;

/**
 * 本ライブラリの初期設定処理の呼出し全般を定義するファサードクラス。
 * <p>
 * 初期処理の内容は{@link initialize()}メソッドを参照。
 * </p>
 */
public class NaocoCoreInitializer {
    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private NaocoCoreInitializer() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * 本ライブラリの初期設定を行う。
     * <p>
     * 本ライブラリについて、以下の使用前の初期設定を行う。
     * </p>
     * <ul>
     * <li>ログ出力先。指定なし（null）の場合は標準出力、標準エラー出力。</li>
     * <li>設定ファイルの読み込み。（DB接続設定、インターセプター設定、DIコンテナー設定）</li>
     * </ul>
     *
     * @param confFileBasename
     *            properties形式設定ファイルのベース名。nullの場合は設定ファイルの読み込みをしない。
     * @param logAdaptor
     *            ログの出力先を設定するオブジェクト。nullの場合は、本ライブラリのログ出力は標準出力、標準エラー出力となる。
     * @throws ClassNotFoundException
     * @See {@link Configuration}, {@link logAdaptor}
     */
    public static void initialize(String confFileBasename, LogAdaptor logAdaptor) throws ClassNotFoundException {
        // ログアダプタの設定
        if (null != logAdaptor) {
            LaolLogger.setLogAdaptor(logAdaptor);
        }
        // 設定ファイルの読み込み
        if (null != confFileBasename) {
            Configuration.initialize(confFileBasename);
        }
    }
}
