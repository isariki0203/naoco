package jp.gr.naoco.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jp.gr.naoco.core.transaction.TransactionBarrierFactory;

/**
 * インターセプト対象クラスを示す
 * <p>
 * 本アノテーションをクラスに設定した場合、インターセプト対象のクラスと認識される。
 * </p>
 * <p>
 * 実際にメソッドの呼出し前後でインターセプターを呼出すためには、設定したクラスに対してインターフェースを定義し、 {@link TransactionBarrierFactory}
 * で新たにインスタンスを生成することにより、インターフェースで 定義したメソッドの呼出し前後で、インターセプターが呼び出される。
 * </p>
 * <p>
 * nameによりインターセプタークラスとインターセプト対象クラスの関連付けを行う。{@link @Interceptor}で 指定したnameと、本アノテーションのnameが一致するもののみを、実際の処理ではインターセプターとして
 * 呼び出す。未指定時は"__default__"が設定される。
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Interceptee {
    public String[] name() default "__default__";
}
