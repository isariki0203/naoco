package jp.gr.naoco.external.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import jp.gr.naoco.core.factory.AbstractInstanceFactory;
import jp.gr.naoco.core.factory.InstanceFactory;

/**
 * Google Guiceを利用したインジェクション実行クラス
 * <p>
 * 指定したクラス、あるいはインスタンスの中で、{@see com.google.inject.Inject}
 * が付与されたフィールド、あるいはsetterメソッドに対して、 InstanceFactory派生クラスで取得したインスタンスのインジェクションを行う。
 * </p>
 * <h3>実行時に必要な外部ライブラリ</h3>
 * 本クラスの実行には、Struts2の実行で必要な各ライブラリのほか、GoogleGuiceの以下のライブラリが必要となる。
 * <ul>
 * <li>aopalliance.jar</li>
 * <li>javax.inject.jar</li>
 * <li>guice-3.0.jar</li>
 * 
 * @author naoco0917
 */
public class GuiceLogicInjector implements LogicInjectorBridge {

    private final Map<String, List<Class<?>>> CALLED_CLASS_NAME_MAP = new ConcurrentHashMap<String, List<Class<?>>>();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public GuiceLogicInjector() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public <T> T newInstance(Class<T> clazz) throws Exception {
        Injector injector = createInjector(clazz);
        return injector.getInstance(clazz);
    }

    @Override
    public <T> void inject(T instance) throws Exception {
        Injector injector = createInjector(instance.getClass());
        injector.injectMembers(instance);
    }

    // ///////////////////////

    private <T> Injector createInjector(Class<T> clazz) throws Exception {
        // 初呼出しのクラスについては、インジェクション対象の型を一度ロードして、staticフィールドの処理を呼び出す。
        List<Class<?>> list = CALLED_CLASS_NAME_MAP.get(clazz.getName());
        if (null == list) {
            list = new ArrayList<Class<?>>();

            // インジェクション対象のクラス変数について、Class#forNameでstatic宣言処理を呼出し
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (null != field.getAnnotation(Inject.class)) {
                    Class.forName(field.getType().getName());
                    list.add(field.getType());
                }
            }
            // インジェクション対象のメソッドの第一引数について、Class#forNameでstatic宣言処理を呼出し
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (null != method.getAnnotation(Inject.class)) {
                    Class<?>[] types = method.getParameterTypes();
                    if (0 < types.length) {
                        Class.forName(types[0].getName());
                        list.add(types[0]);
                    }
                }
            }
            // 同時に複数スレッドで同一のインターフェースに対して、上記処理が走った場合、
            // キャッシュに格納されるリストは後勝ちとなるが、リストの内容はどちらも変わらないはずなので、
            // マップの追加処理以外では、特に処理の同期はとらない。
            CALLED_CLASS_NAME_MAP.put(clazz.getName(), list);
        }

        final List<Class<?>> interfaceList = list;
        // Injector を生成
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                for (Class<?> interfaceClass : interfaceList) {
                    InstanceFactory<?> factory = AbstractInstanceFactory.getFactory(interfaceClass.getName());
                    bind((Class<Object>) interfaceClass).toInstance(factory.getInsatnce());
                }
            }
        });
        return injector;
    }

}
