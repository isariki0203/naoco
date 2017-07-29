package jp.gr.naoco.core.transaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jp.gr.naoco.core.annotation.AfterEndTransaction;
import jp.gr.naoco.core.annotation.AfterStartTransaction;
import jp.gr.naoco.core.annotation.BeforeEndTransaction;
import jp.gr.naoco.core.annotation.BeforeStartTransaction;
import jp.gr.naoco.core.annotation.Interceptor;
import jp.gr.naoco.core.annotation.ThrowException;
import jp.gr.naoco.core.conf.Configuration;
import jp.gr.naoco.core.exception.ReflectionException;
import jp.gr.naoco.core.log.LaolLogger;

public class InterceptorInvocator {
    private final LinkedList<InterceptorContainer> interceptorList_ = new LinkedList<InterceptorContainer>();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public InterceptorInvocator(String[] keys) {
        HashSet<String> keySet = new HashSet<String>(Arrays.asList(keys));
        keySet.add("__default__");
        List<Class<?>> list = Configuration.getIinterceptorList();
        for (Class<?> clazz : list) {
            Interceptor annotation = clazz.getAnnotation(Interceptor.class);
            if (keySet.contains(annotation.name())) {
                interceptorList_.add(new InterceptorContainer(clazz, annotation.name()));
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    public void invokeBeforeStartTransaction() {
        for (InterceptorContainer interceptor : interceptorList_) {
            interceptor.invokeBeforeStartTransaction();
        }
    }

    public void invokeAfterStartTransaction() {
        for (InterceptorContainer interceptor : interceptorList_) {
            interceptor.invokeAfterStartTransaction();
        }
    }

    public void invokeBeforeEndTransaction() {
        Iterator<InterceptorContainer> containeres = interceptorList_.descendingIterator();
        while (containeres.hasNext()) {
            InterceptorContainer interceptor = containeres.next();
            interceptor.invokeBeforeEndTransaction();
        }
    }

    public void invokeAfterEndTransaction() {
        Iterator<InterceptorContainer> containeres = interceptorList_.descendingIterator();
        while (containeres.hasNext()) {
            InterceptorContainer interceptor = containeres.next();
            interceptor.invokeAfterEndTransaction();
        }
    }

    public void invokeThrowException() {
        Iterator<InterceptorContainer> containeres = interceptorList_.descendingIterator();
        while (containeres.hasNext()) {
            InterceptorContainer interceptor = containeres.next();
            interceptor.invokeThrowException();
        }
    }

    private boolean equals_(String arg1, String arg2) {
        if ((null == arg1) && (null == arg2)) {
            return true;
        }
        if (null != arg1) {
            return arg1.equals(arg2);
        }
        return false;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes
    private static class InterceptorContainer {
        private Method beforeStartTxMethod_ = null;

        private Method afterStartTxMethod_ = null;

        private Method beforeEndTxMethod_ = null;

        private Method afterEndTxMethod_ = null;

        private Method throwExceptionMethod_ = null;

        private Object instance_ = null;

        private static final Object[] DUMMY_ARGS = new Object[] {};

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor
        public InterceptorContainer(Class<?> clazz, String key) {
            try {
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    // 引数ありのメソッドはインターセプター実行対象外
                    if (0 < method.getParameterTypes().length) {
                        continue;
                    }
                    // トランザクション開始時、コネクション取得前
                    if ((null == beforeStartTxMethod_)
                            && (null != method.getAnnotation(BeforeStartTransaction.class))) {
                        beforeStartTxMethod_ = method;
                    }
                    // トランザクション開始時、コネクション取得後
                    else if ((null == afterStartTxMethod_)
                            && (null != method.getAnnotation(AfterStartTransaction.class))) {
                        afterStartTxMethod_ = method;
                    }
                    // トランザクション開始時、コネクション取得前
                    else if ((null == beforeEndTxMethod_)
                            && (null != method.getAnnotation(BeforeEndTransaction.class))) {
                        beforeEndTxMethod_ = method;
                    }
                    // トランザクション開始時、コネクション取得後
                    else if ((null == afterEndTxMethod_) && (null != method.getAnnotation(AfterEndTransaction.class))) {
                        afterEndTxMethod_ = method;
                    }
                    // 例外発生時
                    else if ((null == throwExceptionMethod_) && (null != method.getAnnotation(ThrowException.class))) {
                        throwExceptionMethod_ = method;
                    }
                }
                instance_ = clazz.newInstance();
            } catch (InstantiationException e) {
                throw new ReflectionException(e);
            } catch (IllegalAccessException e) {
                throw new ReflectionException(e);
            }
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods
        public void invokeBeforeStartTransaction() {
            invoke_(beforeStartTxMethod_);
        }

        public void invokeAfterStartTransaction() {
            invoke_(afterStartTxMethod_);
        }

        public void invokeBeforeEndTransaction() {
            invoke_(beforeEndTxMethod_);
        }

        public void invokeAfterEndTransaction() {
            invoke_(afterEndTxMethod_);
        }

        public void invokeThrowException() {
            invoke_(throwExceptionMethod_);
        }

        private void invoke_(Method method) {
            if (null == method) {
                return;
            }
            try {
                LOG.debug("invoke: " + instance_.getClass().getName() + "#" + beforeStartTxMethod_.getName());
                method.invoke(instance_, DUMMY_ARGS);
            } catch (IllegalAccessException e) {
                throw new ReflectionException(e);
            } catch (IllegalArgumentException e) {
                throw new ReflectionException(e);
            } catch (InvocationTargetException e) {
                throw new ReflectionException(e);
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger
    private static final LaolLogger LOG = new LaolLogger(InterceptorInvocator.class.getName());
}
