package jp.gr.naoco.core.transaction;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * インターセプターが利用するコンテキスト（データ共有オブジェクト）
 * <p>
 * インターセプターがインターセプトされた処理のクラス、メソッド、引数、実行後の返却値を参照したり、 インターセプター間でデータをやり取りするためのオブジェクト。
 * </p>
 */
public class InterceptorContext {

    private Object instance_;

    private Object[] args_;

    private Class<?> class_;

    private Method method_;

    private Object result_;

    private Throwable error_;

    private HashMap<Object, Object> attributeMap_;

    private static ThreadLocal<LinkedList<InterceptorContext>> instanceStack_ = new ThreadLocal<LinkedList<InterceptorContext>>();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private InterceptorContext() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods
    /**
     * インターセプトされた処理のインスタンスを取得する
     *
     * @return インターセプトされた処理のクラス
     */
    public Object getInvokeInstance() {
        return instance_;
    }

    /**
     * インターセプトされた処理のクラスを取得する
     *
     * @return インターセプトされた処理のクラス
     */
    public Class<?> getInvokeClass() {
        return class_;
    }

    /**
     * インターセプトされた処理のメソッドを取得留守
     *
     * @return インターセプトされた処理のメソッド
     */
    public Method getInvokeMethod() {
        return method_;
    }

    /**
     * インターセプトされた処理の呼出し時の引数を取得する
     *
     * @return インターセプトされた処理の呼出し時の引数
     */
    public Object[] getInvokeArgs() {
        return args_;
    }

    /**
     * インターセプトされた処理の呼出し後の返却値を取得する
     * <p>
     * 処理実行前はnullを返却する。
     * </p>
     *
     * @return インターセプトされた処理の呼出し後の返却値
     */
    public Object getInvokeResult() {
        return result_;
    }

    /**
     * 処理実行中に発生したエラーオブジェクトを取得する
     */
    public Throwable getError() {
        return error_;
    }

    /**
     * インターセプター間でやり取りする任意のオブジェクトを取得する
     *
     * @param key
     *            オブジェクトを取得するためのキー（存在しない場合はnullを返却）
     */
    public Object getAttribute(Object key) {
        return attributeMap_.get(key);
    }

    /**
     * インターセプター間でやり取りする任意のオブジェクトを設定する
     *
     * @param key
     *            オブジェクトを取得する際にオブジェクトを特定するためのキー
     * @param value
     *            設定するオブジェクト
     */
    public void setAttribute(Object key, Object value) {
        attributeMap_.put(key, value);
    }

    /**
     * インターセプターコンテキストを取得する
     *
     * @return インターセプターコンテキスト
     */
    public static InterceptorContext getInstance() {
        InterceptorContext instance = instanceStack_.get().getFirst();
        if (null == instance) {
            throw new IllegalStateException("InterceptorContext is not initialized.");
        }
        return instance;
    }

    protected static void initContext(Object instance, Class<?> clazz, Method method, Object[] args) {
        LinkedList<InterceptorContext> stack = instanceStack_.get();
        if (null == stack) {
            stack = new LinkedList<InterceptorContext>();
            instanceStack_.set(stack);
        }
        InterceptorContext context = new InterceptorContext();
        context.instance_ = instance;
        context.class_ = clazz;
        context.method_ = method;
        context.args_ = args;
        context.attributeMap_ = (stack.isEmpty() ? new HashMap<Object, Object>() : stack.getFirst().attributeMap_);
        stack.push(context);
    }

    protected void setInvokeResult(Object result) {
        result_ = result;
    }

    protected static void finalizeContext() {
        instanceStack_.get().pop();
    }

    protected void setError(Throwable t) {
        error_ = t;
    }
}
