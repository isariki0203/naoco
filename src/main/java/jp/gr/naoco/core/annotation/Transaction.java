package jp.gr.naoco.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jp.gr.naoco.core.transaction.TransactionBarrierFactory;

/**
 * DBトランザクション対象クラスを示す
 * <p>
 * 本アノテーションをクラスに設定した場合、lookupNameで指定した名称のDBコネクションにより、 メソッドの開始から終了まででトランザクションが定義される。
 * </p>
 * <p>
 * 実際にトランザクションを張るためには、設定したクラスに対してインターフェースを定義し、 {@link TransactionBarrierFactory} で新たにインスタンスを生成することにより、インターフェースで
 * 定義したメソッドがトランザクションの対象となる。
 * </p>
 * <p>
 * 多重でトランザクション処理を呼び出す場合、呼出し元のトランザクションを継承するか、 新たなトランザクションを生成するかについて、typeで指定をする。 （未指定の場合は継承（
 * {@link TransactionType.TAKEOVER}））
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transaction {
    public String lookupName();

    public TransactionType type() default TransactionType.TAKEOVER;
}
