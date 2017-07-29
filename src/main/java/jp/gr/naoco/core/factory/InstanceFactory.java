package jp.gr.naoco.core.factory;

import jp.gr.naoco.core.transaction.TransactionBarrierFactory;

/**
 * インスタンスを取得するためのファクトリ。
 * <p>
 * 本インターフェースはインスタンスの取得するためのクラスを抽象化する。
 * </p>
 * <p>
 * 本クラスのサブクラスである以下のクラスでは、特定のスコープ（プロセス、スレッド）でインスタンスを共有し、 かつ、 {@link TransactionBarrierFactory}
 * により、メソッドに対してトランザクションの生成とインターセプターの呼出しで ラップしたインスタンスを生成する。
 * </p>
 * <ul>
 * <li>{@link SingletonInstanceFactory}</li>
 * <li>{@link ThreadLocalInstanceFactory}</li>
 * </ul>
 *
 * @param <T>
 * @see {@link TransactionBarrierFactory}, {@link SingletonInstanceFactory}, {@link ThreadLocalInstanceFactory}
 */
public interface InstanceFactory<T> {
    public T getInsatnce();

    public void setNeedsBarrier(boolean needsBarrier);
}
