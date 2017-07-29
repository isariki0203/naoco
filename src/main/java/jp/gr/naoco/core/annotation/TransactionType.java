package jp.gr.naoco.core.annotation;

/**
 * トランザクションのタイプを定義する
 * <p>
 * {@link @Transaction}のtypeに設定する。
 * </p>
 * <p>
 * 以下のトランザクションのタイプを定義する。
 * <ul>
 * <li>TAKEOVER:呼出し元で同名のlookupNameのトランザクションが存在する場合は、そのトランザクションを継承する。 したがって、メソッド完了時もトランザクションは終了しない。同名のトランザクションが存在しない場合は
 * 新たなトランザクションを開始し、メソッド終了時にトランザクションを終了する。</li
 * <li>NEW:呼出し元のトランザクション有無にかかわらず、新たなトランザクションを開始する。 メソッド終了時は必ずトランザクションを終了する。</li>
 * </ul>
 *
 * @author naoco0917
 */
public enum TransactionType {
    TAKEOVER, NEW
}
