package jp.gr.naoco.external.inject;

import jp.gr.naoco.core.annotation.BeforeStartTransaction;
import jp.gr.naoco.core.annotation.Interceptor;
import jp.gr.naoco.core.transaction.InterceptorContext;

import org.apache.log4j.Logger;

@Interceptor(name = "naoco.logic_intercept")
public class LogicInjectionInterceptor {

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public LogicInjectionInterceptor() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @BeforeStartTransaction
    public void inject() {
        InterceptorContext context = InterceptorContext.getInstance();
        Object instance = context.getInvokeInstance();
        try {
            LogicInjectorBridge.FACTORY.getInsatnce().inject(instance);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }

    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final Logger LOG = Logger.getLogger(LogicInjectionInterceptor.class);
}
