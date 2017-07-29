package jp.gr.naoco.core.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import jp.gr.naoco.core.annotation.BypassCommitRollback;
import jp.gr.naoco.core.annotation.BypassInterceptor;
import jp.gr.naoco.core.annotation.BypassTransaction;
import jp.gr.naoco.core.annotation.Interceptee;
import jp.gr.naoco.core.annotation.Transaction;
import jp.gr.naoco.core.log.LaolLogger;

public class TransactionBarrierFactory {
    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private TransactionBarrierFactory() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    public static Object createBarrier(Class<?> interfaceClass, Object logicInstance) {
        InvocationHandler handler = new TxBarrierInvocationHandler(logicInstance);
        Object barriered = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] {interfaceClass },
                handler);
        return barriered;
    }

    private static Transaction getTransactionAnnotation_(Method method, Class<?> instanceClass) {
        return instanceClass.getAnnotation(Transaction.class);
    }

    private static boolean needsTransaction_(Method method, Class<?> instanceClass) throws NoSuchMethodException {
        return ((null == instanceClass.getMethod(method.getName(), method.getParameterTypes())
                .getAnnotation(BypassTransaction.class)) && (null != getTransactionAnnotation_(method, instanceClass)));
    }

    private static boolean needsInterceptor_(Method method, Class<?> instanceClass) throws NoSuchMethodException {
        if (null == instanceClass.getAnnotation(Interceptee.class)) {
            return false;
        }
        if (null != instanceClass.getMethod(method.getName(), method.getParameterTypes())
                .getAnnotation(BypassInterceptor.class)) {
            return false;
        }
        return true;
    }

    private static boolean needsCommitRollback_(Method method, Class<?> instanceClass) throws NoSuchMethodException {
        return (null == instanceClass.getMethod(method.getName(), method.getParameterTypes())
                .getAnnotation(BypassCommitRollback.class));
    }

    private static String[] getInterceptorKey_(Method method, Class<?> instanceClass) throws NoSuchMethodException {
        Interceptee annotation = instanceClass.getAnnotation(Interceptee.class);
        return ((null != annotation) ? annotation.name() : null);
    }

    private static String getLookupName_(Method method, Class<?> instanceClass) {
        Transaction annotation = instanceClass.getAnnotation(Transaction.class);
        return ((null != annotation) ? annotation.lookupName() : null);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    public static class TxBarrierInvocationHandler implements InvocationHandler {
        private Object instance_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        public TxBarrierInvocationHandler(Object instance) {
            instance_ = instance;
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        /** {@inheritDoc} */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final boolean needsTransaction = needsTransaction_(method, instance_.getClass());
            final boolean needsInterceptor = needsInterceptor_(method, instance_.getClass());
            final boolean needsCommitRollback = needsCommitRollback_(method, instance_.getClass());
            InterceptorInvocator interceptor = null;
            if (needsInterceptor) {
                String[] interceptorKeys = getInterceptorKey_(method, instance_.getClass());
                interceptor = ((null != interceptorKeys) ? new InterceptorInvocator(interceptorKeys) : null);
            }
            Object result = null;
            try {
                InterceptorContext.initContext(instance_, instance_.getClass(), method, args);
                // トランザクション開始前、メソッド呼出し前のインターセプター
                if (needsInterceptor) {
                    interceptor.invokeBeforeStartTransaction();
                }
                final String lookupName = getLookupName_(method, instance_.getClass());
                final Transaction anno = getTransactionAnnotation_(method, instance_.getClass());
                try {
                    // トランザクションの開始
                    if (needsTransaction && (null != anno)) {
                        switch (anno.type()) {
                        case NEW:
                            TransactionManager.startTransaction(lookupName);
                            break;
                        case TAKEOVER:
                            TransactionManager.takeoverTransaction(lookupName);
                            break;
                        default:
                            // nothing to do
                        }
                    }

                    // トランザクション開始後、メソッド実行前のインターセプター
                    if (needsInterceptor) {
                        interceptor.invokeAfterStartTransaction();
                    }

                    // メソッドの実行
                    try {
                        result = method.invoke(instance_, args);
                    } catch (Throwable t) {
                        Throwable cause;
                        if (t instanceof InvocationTargetException) {
                            cause = ((InvocationTargetException) t).getCause();
                        } else {
                            cause = t;
                        }
                        // エラー発生時のインターセプター
                        if (needsInterceptor) {
                            InterceptorContext.getInstance().setError(cause);
                            interceptor.invokeThrowException();
                        }
                        throw cause;
                    }

                    // トランザクション終了前、メソッド実行後のインターセプター
                    if (needsInterceptor) {
                        InterceptorContext.getInstance().setInvokeResult(result);
                        interceptor.invokeBeforeEndTransaction();
                    }

                    // トランザクションの終了
                    if (needsTransaction) {
                        TransactionManager.commitTransaction(needsCommitRollback);
                    }
                } catch (Throwable t) {
                    try {
                        // トランザクションのロールバック
                        if (needsTransaction) {
                            TransactionManager.rollbackTransaction(needsCommitRollback);
                        }
                    } catch (Throwable t2) {
                        LOG.error(t2.getMessage(), t2);
                    }
                    throw t;
                }

                // トランザクション終了後、メソッド終了後のインターセプター
                if (needsInterceptor) {
                    interceptor.invokeAfterEndTransaction();
                }
            } finally {
                InterceptorContext.finalizeContext();
            }
            return result;
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(TransactionBarrierFactory.class.getName());
}
