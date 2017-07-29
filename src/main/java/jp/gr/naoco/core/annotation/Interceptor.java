package jp.gr.naoco.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * インターセプタークラスを示す
 * <p>
 * 本アノテーションをクラスに設定した場合、インターセプター対象のクラスと認識される。
 * </p>
 * <p>
 * 実際に対象クラスをインターセプター対象とする場合は、本アノテーションの付与のほかに、 {@link Configuration} でインターセプターの設定をし、インターセプト対象の処理については、
 * {@link TransactionBarrierFacotry} でインスタンスを取得する必要がある。
 * </p>
 * <p>
 * 本アノテーションを付与したクラスについて、クラス内のpublicメソッドに以下のアノテーションを付与することで、 実際に処理の呼出しをする。
 * </p>
 * <ul>
 * <li>{@link @BeforeStartTransaction}：トランザクション開始前、処理実行前に呼出し
 * <li>{@link @AfterStartTransaction}：トランザクション開始後、処理実行前に呼出し
 * <li>{@link @BeforeEndTransaction}：トランザクション終了前、処理実行後に呼出し
 * <li>{@link @AfterEndTransaction}：トランザクション終了後、処理実行後に呼出し
 * </ul>
 * <p>
 * インターセプター対象メソッドは上記のいずれかのアノテーションを付与するのに加え、<b>引数なしで定義しなければならない</b>。
 * <p>
 * nameによりインターセプト対象のクラスとインターセプタークラスの関連付けを行う。{@link @Interceptee}で 指定したnameと、本アノテーションのnameが一致するもののみを、実際の処理ではインターセプターとして
 * 呼び出す。未指定時は"__default__"が設定される。
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Interceptor {
    public String name() default "__default__";
}
