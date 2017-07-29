package jp.gr.naoco.external.inject;

import jp.gr.naoco.core.factory.InstanceFactory;
import jp.gr.naoco.core.factory.SingletoneInstanceFactory;

/**
 * インジェクターのインターフェース
 * <p>
 * 未指定の場合は GuiceLogicInjector インスタンスをFACTORYから取得する。
 * </p>
 * 
 * @author naoco0917
 */
public interface LogicInjectorBridge {
    public <T> T newInstance(Class<T> clazz) throws Exception;

    public <T> void inject(T instance) throws Exception;

    public static final InstanceFactory<LogicInjectorBridge> FACTORY = new SingletoneInstanceFactory<LogicInjectorBridge>(
            LogicInjectorBridge.class, GuiceLogicInjector.class);
}
