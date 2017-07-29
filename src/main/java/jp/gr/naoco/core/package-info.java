/**
 * naoco（Newgeneration Aspect Oriented Compact Objects）のコアとなる機能パッケージです。
 * <p>
 * laol.coreでは以下の機能を保有するライブラリを提供します。
 * </p>
 * <ul>
 * <li>DBトランザクションの管理</li>
 * <li>インターセプター機能</li>
 * <li>トランザクション境界の設定とインターセプター呼出しをするProxy生成ファクトリと定義アノテーション</li>
 * </ul>
 * <p>
 * naocoのコア機能を利用する場合、本パッケージとその配下のパッケージのクラス群の中で、以下のクラスを主に使用します。
 * </p>
 * <ol>
 * <li>{@link jp.gr.naoco.core.LaolCoreInitialiser}にて、 LaolにDB接続情報やインターセプター情報などの初期設定の読み込みを行わせる。</li>
 * <li>トランザクション境界や本処理前後にインターセプターの処理を追加させたいクラスについて、インターフェースとサブクラスを定義し、 {@link jp.gr.naoco.core.factory.InstanceFactory}
 * のサブクラスのいずれかで、 ファクトリインスタンスを生成する。（当該クラスの利用者はそのファクトリからインターフェースをインスタンスを取得して処理を呼び出すようにする）</li>
 * <li>上記のクラスについて、 {@link jp.gr.naoco.core.annotation}配下のアノテーションを クラスやメソッドに付与して、トランザクション境界対象やインターセプター対象のクラスであることの宣言をする。
 * <li>インターセプターについても、上記と同様{@link jp.gr.naoco.core.annotation}配下のアノテーションを 付与して、インターセプターであることを宣言する。
 * <li>{@link jp.gr.naoco.core.NaocoCoreFacade}にて、トランザクション境界内の
 * 現在のコネクションや現在のコネクションから生成したステートメントの取得、現在のコネクションに対するコミット、ロールバック、ロールバック予約を 実行する。</li>
 * </ol>
 * <p>
 * EntityからSQL文の生成と実行などをする機能は、{@link jp.gr.naoco.db}パッケージ配下のクラスにて 提供します。
 * </p>
 * <p>
 * 外部ライブラリとの連携による機能拡張は、{@link jp.gr.naoco.external}パッケージ配下のクラスにて提供します。
 * </p>
 * <p>
 * マルチスレッドによるProducer-Consumerパターンを実現するための機能は、{@link jp.gr.naoco.chain}パッケージ配下のクラスにて提供します。
 * </p>
 *
 * @author naoco0917
 */
package jp.gr.naoco.core;
