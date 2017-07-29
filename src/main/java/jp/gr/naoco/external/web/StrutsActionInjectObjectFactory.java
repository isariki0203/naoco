package jp.gr.naoco.external.web;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ObjectFactory;

import jp.gr.naoco.external.inject.LogicInjectorBridge;

/**
 * Struts2にて、 AbstractInstanceFactoryのサブクラスから取得するインスタンスを、
 * Actionクラスのインスタンスにインジェクションするための ObjectFactory派生クラス。
 * <p>
 * 本クラスは<b>Struts2環境での使用を目的としている</b>。AbstractInstanceFactoryのサブクラスで定義した、 各インターフェースの実体クラスのインスタンス取得結果を、クラス変数 およびメソッドに注入する。
 * </p>
 * <p>
 * <h3>設定方法</h3> struts.xmlに以下の記述を追加する。 <code>
 * <struts>
 *     <!-- 下記の１行を追加 -->
 *     <constant name="struts.objectFactory" value="jp.gr.naoco.external.web.StrutsActionInjectObjectFactory" />
 *       :
 * </struts>
 * </code>
 * </p>
 * <p>
 * <h3>Actionの実装方法（InjectorBridgeインターフェースの実行実装クラスとしてGuiceInjectorを使用した場合）</h3>
 * インジェクション対象となるクラス変数について、@Injectアノテーションを追加する。
 * 
 * <pre>
 * \@Inject
 * private SampleLogic logic_;
 * </pre>
 * </p>
 * <p>
 * <h3>前提条件</h3> インジェクション対象となるインターフェースのAbstractInstanceFactoryインスタンスは、 ObjectFactoryが呼び出される前に生成されているか、
 * インジェクション対象のデータ型の中のstaticフィールド上で生成するようにしておく必要がある。 具体的に以下のいずれかの方法で、AbstractInstanceFactoryのサブクラスのインスタンスを生成すること。
 * <ul>
 * <li>インジェクション対象のインターフェースのstaticフィールドで、AbstractInstanceFactoryサブクラスのインスタンスを生成する。
 * <li>アプリケーションの初期化処理（ServletであればServletContextEvent#contextInitializedメソッド内） でインスタンスを生成する。
 * </ul>
 * </p>
 * <p>
 * </ul>
 * </p>
 * 
 * @author naoco0917
 */
public class StrutsActionInjectObjectFactory extends ObjectFactory {

    private static final long serialVersionUID = 2566445872315617569L;

    private static final Map<String, List<Class<?>>> CALLED_CLASS_NAME_MAP = new ConcurrentHashMap<String, List<Class<?>>>();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public StrutsActionInjectObjectFactory() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @SuppressWarnings("unchecked")
    @Override
    public Object buildBean(Class clazz, Map<String, Object> extraContext) throws Exception {
        try {
            // Struts（xwork2)のActionSupport派生クラスのみInjection対象
            if (!ActionSupport.class.isAssignableFrom(clazz)) {
                // 通常通りクラスからインスタンスを生成して返却
                return super.buildBean(clazz, extraContext);
            }

            LogicInjectorBridge injector = LogicInjectorBridge.FACTORY.getInsatnce();

            // インスタンスをInjector経由で生成して返却
            return injector.newInstance(clazz);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final Logger LOG = Logger.getLogger(StrutsActionInjectObjectFactory.class);
}
