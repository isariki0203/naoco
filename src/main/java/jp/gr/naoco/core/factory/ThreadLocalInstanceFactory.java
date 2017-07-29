package jp.gr.naoco.core.factory;

import jp.gr.naoco.core.conf.Configuration;
import jp.gr.naoco.core.exception.ReflectionException;
import jp.gr.naoco.core.log.LaolLogger;
import jp.gr.naoco.core.transaction.TransactionBarrierFactory;

/**
 * ファクトリインスタンスに対して、スレッドで一個のみインスタンスを持つインターセプト対象クラスの抽象ファクトリ。
 * <p>
 * 本クラスでは、指定したインターフェースに対する実装クラスのインスタンスを、 <b>スレッド毎で一個のみ</b>生成し、そのインスタンスを取得する {@link getInstance()} メソッドを保持する。
 * </p>
 * <p>
 * 本クラスを利用するためには、生成対象のインターフェースと実装クラスを返却する {@link getInterfaceClass}と {@link getInstanceClass}を定義して使用する。
 * </p>
 * <p>
 * 本クラスで取得するインスタンスは、実装クラスのアノテーションに{@link @Transaction}、 {@link @Interceptee} が付与されている場合は、インターフェースメソッドの呼び出し前後で、
 * トランザクションの生成と終了、インターセプターの処理の呼出しを行う。
 * </p>
 *
 * @param <T>
 */
public class ThreadLocalInstanceFactory<T> extends AbstractInstanceFactory implements InstanceFactory<T> {
    private ThreadLocal<T> instance_ = new ThreadLocal<T>();

    private boolean needsBarrier_ = true;

    private Class<T> interfaceClass_ = null;

    private Class<?> instanceClass_ = null;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    protected ThreadLocalInstanceFactory() {
        set(this);
    }

    /**
     * インターフェースに対する実装クラスの取得を、LaolCoreInitializerの初期設定、
     * あるいはConfigurationによる設定に依存する場合は、 本コンストラクタにてインターフェースクラスのみを指定。
     *
     * @param interfaceClass
     *            取得するインスタンスのインターフェースクラス。（実装クラスは設定に依存）
     */
    public ThreadLocalInstanceFactory(Class<T> interfaceClass) {
        interfaceClass_ = interfaceClass;
        set(this);
    }

    /**
     * インターフェースに対するデフォルト実装クラスを指定する。
     * <p>
     * LaolCoreInitializerの初期設定、 あるいはConfigurationによる設定が存在する場合は、 本コンストラクタした実装クラスは無視し、設定側のクラスを取得する。
     * </p>
     *
     * @param interfaceClass
     *            取得するインスタンスのインターフェースクラス。
     * @param instanceClass
     *            取得するインスタンスのクラス（設定が存在する場合はそちらを優先）
     */
    public ThreadLocalInstanceFactory(Class<T> interfaceClass, Class<?> instanceClass) {
        interfaceClass_ = interfaceClass;
        instanceClass_ = instanceClass;
        set(this);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * <p>
     * インスタンスを取得する。
     * </p>
     * <p>
     * 同一スレッド内で共有するインスタンスを取得する。
     * </p>
     * <p>
     * インスタンスは{@link getInterfaceClass}指定したインターフェース型で返却される。 インスタンスの元は {@link getInstanceClass}が返却するクラスのインスタンスであるが、
     * トランザクションの生成やインターセプターの呼出しのために、{@link TransactionBarriered}内部で {@link java.lang.reflect.Proxy}
     * により新たに生成したインスタンスを返却する。
     * </p>
     *
     * @return getInterfaceClassメソッドで指定した型のインスタンス（スレッド内で共有）
     */
    @Override
    @SuppressWarnings("unchecked")
    public T getInsatnce() {
        T instance = instance_.get();
        if (null == instance) {
            try {
                Object configuredInstance = Configuration.createDIInstance(getInterfaceClass());
                if (null == configuredInstance) {
                    instance = (T) getInstanceClass().newInstance();
                } else {
                    instance = (T) configuredInstance;
                }
                if (needsBarrier_) {
                    instance = (T) TransactionBarrierFactory.createBarrier(getInterfaceClass(), instance);
                }
                instance_.set(instance);

                LOG.debug("success create barriered instance :" + getInstanceClass().getName());
            } catch (IllegalAccessException e) {
                LOG.error(e.getMessage(), e);
                throw new ReflectionException(e);
            } catch (InstantiationException e) {
                LOG.error(e.getMessage(), e);
                throw new ReflectionException(e);
            }
        }
        return instance;
    }

    @Override
    public void setNeedsBarrier(boolean needsBarrier) {
        needsBarrier_ = needsBarrier;
    }

    @Override
    protected void clearCache() {
        instance_ = new ThreadLocal<T>();
    }

    protected Class<?> getInstanceClass() {
        return instanceClass_;
    }

    protected Class<T> getInterfaceClass() {
        return interfaceClass_;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger
    private static final LaolLogger LOG = new LaolLogger(ThreadLocalInstanceFactory.class.getName());
}
