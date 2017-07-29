package jp.gr.naoco.alacarte;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JavaBeans オブジェクトのディープコピー処理を定義
 * <p>
 * JavaBeansオブジェクトをディープコピーするmapメソッドを定義する。
 * </p>
 * <p>
 * ディープコピーとは、プリミティブ型の値とそれに近しいクラスのオブジェクトを除き、JavaBeansの要素オブジェクト内の各要素も同様にコピーすることを示す。
 * </p>
 * <p>
 * また、配列とjava.util.Collection派生クラスのオブジェクト、java.util.Map派生クラスの要素も、新たなインスタンスを生成してコピー結果を追加したものを設定する。
 * </p>
 * <p>
 * 集合（java.util.Collection、java.util.Map、配列）をディープコピーする最大の要素数は1024とし、それ以上の要素を持つ集合については、参照コピーとする。
 * </p>
 */
public final class BeanMapper {
    private static final List<Class<?>> EXCEPT_CLASS = new ArrayList<Class<?>>();
    static {
        EXCEPT_CLASS.add(Class.class);
        EXCEPT_CLASS.add(Enum.class);
        EXCEPT_CLASS.add(Package.class);
        EXCEPT_CLASS.add(Throwable.class);
        EXCEPT_CLASS.add(Boolean.class);
        EXCEPT_CLASS.add(Byte.class);
        EXCEPT_CLASS.add(Character.class);
        EXCEPT_CLASS.add(Double.class);
        EXCEPT_CLASS.add(Float.class);
        EXCEPT_CLASS.add(Integer.class);
        EXCEPT_CLASS.add(Long.class);
        EXCEPT_CLASS.add(Number.class);
        EXCEPT_CLASS.add(Short.class);
        EXCEPT_CLASS.add(String.class);
        EXCEPT_CLASS.add(BigDecimal.class);
        EXCEPT_CLASS.add(BigInteger.class);
        EXCEPT_CLASS.add(java.util.Date.class);
    }

    private static final Set<Method> EXCEPT_METHOD = new HashSet<Method>();
    static {
        try {
            EXCEPT_METHOD.add(Object.class.getMethod("getClass"));
        } catch (Exception e) {
            // nothing to do
        }
    }

    private static final List<Class<?>> EXCEPT_DEEPCOPY_CLASS = new ArrayList<Class<?>>();
    static {
        EXCEPT_DEEPCOPY_CLASS.add(String.class);
        EXCEPT_DEEPCOPY_CLASS.add(java.util.Date.class);
        EXCEPT_DEEPCOPY_CLASS.add(Long.class);
        EXCEPT_DEEPCOPY_CLASS.add(BigDecimal.class);
        EXCEPT_DEEPCOPY_CLASS.add(BigInteger.class);
        EXCEPT_DEEPCOPY_CLASS.add(Boolean.class);
        EXCEPT_DEEPCOPY_CLASS.add(Byte.class);
        EXCEPT_DEEPCOPY_CLASS.add(Character.class);
        EXCEPT_DEEPCOPY_CLASS.add(Double.class);
        EXCEPT_DEEPCOPY_CLASS.add(Float.class);
        EXCEPT_DEEPCOPY_CLASS.add(Integer.class);
        EXCEPT_DEEPCOPY_CLASS.add(Number.class);
        EXCEPT_DEEPCOPY_CLASS.add(Short.class);
        EXCEPT_DEEPCOPY_CLASS.add(java.sql.Array.class);
        EXCEPT_DEEPCOPY_CLASS.add(java.sql.Blob.class);
        EXCEPT_DEEPCOPY_CLASS.add(java.sql.Clob.class);
        EXCEPT_DEEPCOPY_CLASS.add(java.sql.NClob.class);
    }

    private static final int MAX_DEEPCOPY_SIZE = 1024;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private BeanMapper() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * sourceについて、targetClassで指定した新しいインスタンスを生成し、各項目をコピーした結果を返却する。
     *
     * @param source コピー元オブジェクト
     * @param targetClass コピー先クラス
     * @return targetClassの新たに生成したオブジェクトにsource内の各項目をコピーした結果。<br>
     *         何らかの理由でインスタンスの生成に失敗した場合はnullを返却する。
     */
    public static <T, S> T map(S source, Class<T> targetClass) {
        if (Collection.class.isAssignableFrom(targetClass)) {
            try {
                return (T) copyCollection((Collection<?>) source);
            } catch (ClassCastException e) {
                return null;
            }
        } else if (Map.class.isAssignableFrom(targetClass)) {
            try {
                return (T) copyMap((Map<?, ?>) source);
            } catch (ClassCastException e) {
                return null;
            }
        } else if (targetClass.isArray()) {
            try {
                return (T) copyArray(targetClass, source);
            } catch (ClassCastException e) {
                return null;
            }
        } else if (isExceptClass(source.getClass()) || isExceptClass(targetClass)) {
            try {
                return (T) source;
            } catch (ClassCastException e) {
                return null;
            }
        }

        try {
            T target = targetClass.newInstance();
            return map(source, target);
        } catch (IllegalAccessException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        }
    }

    /**
     * sourceについて、resultオブジェクトに各項目をコピーした結果を返却する。
     * ＊
     *
     * @param source コピー元オブジェクト
     * @param result コピー先オブジェクト
     * @return sourceの各項目をコピーしたresultオブジェクト
     */
    public static <R, S> R map(S source, R result) {
        if (isExceptClass(source.getClass()) || isExceptClass(result.getClass())) {
            try {
                return (R) source;
            } catch (ClassCastException e) {
                return result;
            }
        }

        List<Method> methodList = getAllMethod(source.getClass(), null);
        for (Method method : methodList) {
            if (isExceptMethod(method)) {
                continue;
            }
            String propName = isGetterMethod(method);
            if (null == propName) {
                continue;
            }
            Class<?> returnType = method.getReturnType();
            Method setter = getSetterMethod(result.getClass(), propName, returnType);
            if ((null == setter) || isExceptMethod(method)) {
                continue;
            }
            Object value = null;
            try {
                value = method.invoke(source);
                if (null == value) {
                    setter.invoke(result, value);
                } else if (Collection.class.isAssignableFrom(returnType)) {
                    setter.invoke(result, copyCollection((Collection<?>) value));
                } else if (Map.class.isAssignableFrom(returnType)) {
                    setter.invoke(result, copyMap((Map<?, ?>) value));
                } else if (returnType.isArray()) {
                    setter.invoke(result, copyArray(returnType, value));
                } else if (!needsDeepCopy(returnType)) {
                    setter.invoke(result, value);
                } else {
                    setter.invoke(result, map(value, value.getClass()));
                }
            } catch (InvocationTargetException e) {
                continue;
            } catch (IllegalAccessException e) {
                continue;
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        return result;
    }

    // ///////////////////////

    private static boolean isExceptClass(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }
        for (Class<?> except : EXCEPT_CLASS) {
            if (except.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    public static String isGetterMethod(Method method) {
        String name = method.getName();
        String result = null;
        if (method.getReturnType().equals(boolean.class)) {
            if (!name.startsWith("is") || (name.length() < 3)) {
                return null;
            }
            result = name.substring(2);
        } else if (method.getReturnType().equals(Void.class)) {
            return null;
        } else {
            if (!name.startsWith("get") || (name.length() < 4)) {
                return null;
            }
            result = name.substring(3);
        }
        if (result.length() < 2) {
            return result.toLowerCase();
        } else {
            return result.substring(0, 1).toLowerCase() + result.substring(1);
        }
    }

    private static List<Method> getAllMethod(Class<?> clazz, List<Method> methodList) {
        if (null == methodList) {
            methodList = new ArrayList<Method>();
        }
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            try {
                PropertyDescriptor desc = new PropertyDescriptor(field.getName(), clazz);
                Method getter = desc.getReadMethod();
                if (null != getter) {
                    methodList.add(getter);
                }
            } catch (IntrospectionException e) {
                // nothing to do.
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if ((null == superClass) || superClass.equals(Object.class)) {
            return methodList;
        }
        return getAllMethod(superClass, methodList);
    }

    private static Method getSetterMethod(Class<?> targetClass, String name, Class<?> type) {
        try {
            PropertyDescriptor desc = new PropertyDescriptor(name, targetClass);
            return desc.getWriteMethod();
        } catch (IntrospectionException e) {
            return null;
        }
    }

    private static boolean isExceptMethod(Method method) {
        String name = method.toGenericString();
        if (!name.contains("public ")) {
            return true;
        } else if (name.contains("static ")) {
            return true;
        }
        return EXCEPT_METHOD.contains(method);
    }

    private static boolean needsDeepCopy(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return false;
        }
        for (Class<?> except : EXCEPT_DEEPCOPY_CLASS) {
            if (except.isAssignableFrom(clazz)) {
                return false;
            }
        }
        boolean result = (Serializable.class.isAssignableFrom(clazz));
        return result;
    }

    private static Collection<?> copyCollection(Collection<?> source) {
        if (MAX_DEEPCOPY_SIZE < source.size()) {
            return source;
        }
        try {
            Collection<Object> newInstance = source.getClass().newInstance();
            for (Object obj : source) {
                newInstance.add(map(obj, obj.getClass()));
            }
            return newInstance;
        } catch (InstantiationException e) {
            // nothing to do
        } catch (IllegalAccessException e) {
            // nothing to do
        }
        return source;
    }

    private static Map<?, ?> copyMap(Map<?, ?> source) {
        if (MAX_DEEPCOPY_SIZE < source.size()) {
            return source;
        }
        try {
            Map<Object, Object> newInstance = source.getClass().newInstance();
            for (Object key : source.keySet()) {
                Object newKey = map(key, key.getClass());
                Object value = source.get(key);
                Object newValue = map(value, value.getClass());
                newInstance.put(newKey, newValue);
            }
            return newInstance;
        } catch (InstantiationException e) {
            // nothing to do
        } catch (IllegalAccessException e) {
            // nothing to do
        }
        return source;
    }

    private static Object copyArray(Class<?> type, Object source) {
        int length = Array.getLength(source);
        if (MAX_DEEPCOPY_SIZE < length) {
            return source;
        }
        Class<?> componentType = type.getComponentType();
        Object newInstance = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            Object elem = Array.get(source, i);
            Array.set(newInstance, i, map(elem, elem.getClass()));
        }
        return newInstance;
    }
}
