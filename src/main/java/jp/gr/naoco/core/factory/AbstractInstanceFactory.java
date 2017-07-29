package jp.gr.naoco.core.factory;

import java.util.concurrent.ConcurrentHashMap;

/** ファクトリを一元管理するためのスーパークラス */
public abstract class AbstractInstanceFactory {
    private static ConcurrentHashMap<String, InstanceFactory<?>> factoryMap_ = new ConcurrentHashMap<String, InstanceFactory<?>>();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public AbstractInstanceFactory() {
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * インターフェース名に対するInstanceFactoryを返却する。
     * <p>
     * 本メソッドは、オブジェクトに対するクラス変数のインジェクションを行う際の共通処理ユーティリティとして使用することを想定しており、 <b><i>アプリケーション内の各ビジネスロジック等での使用は推奨しない</i></b>。
     * </p>
     * <p>
     * 取得可能なInstanceFactoryは、SingletoneInstanceFactory、 ThreadLocalInstanceFactoryいずれかのクラス、あるいは派生クラス
     * でなければならず、かつ、同一のプロセスかつクラスローダ内で、インターフェース名に対して最初に生成したFactoryを取得する。
     * </p>
     * <p>
     * 本メソッドを使用する前提として、対象となるInstanceFactoryの派生クラスは、一つの生成対象となるインスタンスの
     * インターフェースに対して、一つのInstanceFactoryインスタンスのみ作成することを想定している。
     * </p>
     * <p>
     * そのため、一つの生成対象インターフェースに対して、複数の生成対象派生クラスから、 InstanceFactoryのインスタンス毎に異なるクラスの生成対象インスタンスを返却するような実装をしたInstanceFactoryの
     * 派生クラスについては、 返却したInstanceFactoryがどのようなインスタンスを返却するか保障ができないため、 本メソッドでInstanceFactoryを取得するべきではない。
     * </p>
     *
     * @param interfaceName
     * @return
     */
    public static InstanceFactory<?> getFactory(String interfaceName) {
        return factoryMap_.get(interfaceName);
    }

    /**
     * 各ファクトリのインスタンスキャッシュを削除する。
     */
    public static void clearAllCache() {
        synchronized (factoryMap_) {
            for (InstanceFactory<?> factory : factoryMap_.values()) {
                AbstractInstanceFactory fac = (AbstractInstanceFactory) factory;
                fac.clearCache();
            }
        }
    }

    protected abstract void clearCache();

    /**
     * SingletoneInstanceFactory および、
     * ThreadLocalInstanceFactoryコンストラクタ内で自身を登録するためのメソッド
     */
    protected void set(InstanceFactory<?> thisFactory) {
        if (thisFactory instanceof SingletoneInstanceFactory) {
            Class<?> interfaceClass = ((SingletoneInstanceFactory<?>) thisFactory).getInterfaceClass();
            if ((null == interfaceClass) || (null != factoryMap_.get(interfaceClass.getName()))) {
                return;
            }
        } else if (thisFactory instanceof ThreadLocalInstanceFactory) {
            Class<?> interfaceClass = ((ThreadLocalInstanceFactory<?>) thisFactory).getInterfaceClass();
            if ((null == interfaceClass) || (null != factoryMap_.get(interfaceClass.getName()))) {
                return;
            }
        }

        if (thisFactory instanceof SingletoneInstanceFactory) {
            factoryMap_.put(((SingletoneInstanceFactory<?>) thisFactory).getInterfaceClass().getName(), thisFactory);
        } else if (thisFactory instanceof ThreadLocalInstanceFactory) {
            factoryMap_.put(((ThreadLocalInstanceFactory<?>) thisFactory).getInterfaceClass().getName(), thisFactory);
        }
    }
}
