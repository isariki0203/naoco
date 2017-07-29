package jp.gr.naoco.db.entity;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jp.gr.naoco.core.log.LaolLogger;
import jp.gr.naoco.db.entity.annotation.Column;
import jp.gr.naoco.db.entity.annotation.Id;
import jp.gr.naoco.db.entity.annotation.Table;
import jp.gr.naoco.db.exception.QueryRenderingException;

public class FixedQueryRenderer {
    private static Map<Class<?>, NameMethodContainer> CLASS_DEF_MAP = new ConcurrentHashMap<Class<?>, NameMethodContainer>();

    private static Map<Class<?>, String> DELETE_QUERY_MAP = new ConcurrentHashMap<Class<?>, String>();

    private static Map<Class<?>, String> SELECT_QUERY_MAP = new ConcurrentHashMap<Class<?>, String>();

    private static Map<Class<?>, String> ALL_SELECT_QUERY_MAP = new ConcurrentHashMap<Class<?>, String>();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private FixedQueryRenderer() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Method

    public static String renderUpdate(AbstractEntity entity, List<Object> parameterList) {
        NameMethodContainer classDef = getNameMethodContainer(entity);
        // IDカラムが存在しないエンティティの場合はエラー
        if (classDef.idList_.isEmpty()) {
            throw new QueryRenderingException("try update but @Id is not in:" + entity.getClass());
        }
        // IDカラムでsetterが呼ばれていないものがある場合はエラー
        validSetAllId(entity, classDef);
        String tableName = getTableName(entity);

        // UPDATE句の記述
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableName);

        // SET句の記述
        sql.append(" SET ");
        Iterator<NameMethod> elems = classDef.columnList_.iterator();
        int i = 0;
        while (elems.hasNext()) {
            NameMethod elem = elems.next();

            // setterが呼ばれたカラムのみSET対象
            if (entity.isSetField(elem.name_)) {
                sql.append(elem.name_);
                sql.append("=?, ");
                parameterList.add(invokeGetter(elem.method_, entity));
                i++;
            }
        }
        if (i < 1) {
            throw new QueryRenderingException("not set update columns:" + entity.getClass().getName());
        }
        sql.delete(sql.length() - 2, sql.length());

        // WHERE句の記述
        sql.append(" WHERE ");
        for (NameMethod elem : classDef.idList_) {
            sql.append(elem.name_);
            sql.append("=? AND ");
            parameterList.add(invokeGetter(elem.method_, entity));
        }
        sql.delete(sql.length() - 5, sql.length());

        return sql.toString();
    }

    public static String renderInsert(AbstractEntity entity, List<Object> parameterList, boolean forBatch) {
        NameMethodContainer classDef = getNameMethodContainer(entity);
        // IDカラムでsetterが呼ばれていないものがある場合はエラー
        if (!forBatch) {
            validSetAllId(entity, classDef);
        }
        String tableName = getTableName(entity);

        // INSERT句の記述
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName);
        sql.append("(");
        StringBuilder values = new StringBuilder(" VALUES (");

        // カラム値の設定
        int i = 0;
        for (NameMethod elem : classDef.idList_) {
            // バッチ向けでない場合は、setterが呼ばれたカラムのみSET対象
            if (forBatch || (!forBatch && entity.isSetField(elem.name_))) {
                sql.append(elem.name_).append(", ");
                values.append("?, ");
                if (!forBatch) {
                    parameterList.add(invokeGetter(elem.method_, entity));
                }
                i++;
            }
        }
        for (NameMethod elem : classDef.columnList_) {
            // バッチ向けでない場合は、setterが呼ばれたカラムのみSET対象
            if (forBatch || (!forBatch && entity.isSetField(elem.name_))) {
                sql.append(elem.name_).append(", ");
                values.append("?, ");
                if (!forBatch) {
                    parameterList.add(invokeGetter(elem.method_, entity));
                }
                i++;
            }
        }
        if (i < 1) {
            throw new QueryRenderingException(
                    "try render insert query.but not set columns:" + entity.getClass().getName());
        }
        sql.delete(sql.length() - 2, sql.length()).append(")");
        values.delete(values.length() - 2, values.length()).append(")");
        sql.append(values.toString());
        return sql.toString();
    }

    public static List<Object> createInsertBatchParameterList(AbstractEntity entity) {
        NameMethodContainer classDef = getNameMethodContainer(entity);
        // IDカラムでsetterが呼ばれていないものがある場合はエラー
        validSetAllId(entity, classDef);
        ArrayList<Object> parameterList = new ArrayList<Object>(classDef.columnList_.size() + classDef.idList_.size());

        // カラム値の設定
        for (NameMethod elem : classDef.idList_) {
            parameterList.add(invokeGetter(elem.method_, entity));
        }
        for (NameMethod elem : classDef.columnList_) {
            parameterList.add(invokeGetter(elem.method_, entity));
        }
        return parameterList;
    }

    public static String renderDelete(AbstractEntity entity, List<Object> parameterList) {
        NameMethodContainer classDef = getNameMethodContainer(entity);
        // IDカラムでsetterが呼ばれていないものがある場合はエラー
        validSetAllId(entity, classDef);

        // クエリを既に作成済の場合は、キャッシュを返却
        String cache = DELETE_QUERY_MAP.get(entity.getClass());
        if (null != cache) {
            for (NameMethod elem : classDef.idList_) {
                parameterList.add(invokeGetter(elem.method_, entity));
            }
            return cache;
        }

        // IDカラムが存在しないエンティティの場合はエラー
        if (classDef.idList_.isEmpty()) {
            throw new QueryRenderingException("try delete but @Id is not in:" + entity.getClass());
        }

        // DELETE句の記述
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(getTableName(entity));

        // WHERE句の記述
        sql.append(" WHERE ");
        for (NameMethod elem : classDef.idList_) {
            sql.append(elem.name_);
            sql.append("=? AND ");
            parameterList.add(invokeGetter(elem.method_, entity));
        }
        sql.delete(sql.length() - 5, sql.length());

        cache = sql.toString();
        DELETE_QUERY_MAP.put(entity.getClass(), cache);
        return cache;
    }

    public static String renderSelect(AbstractEntity entity, List<Object> parameterList, boolean forUpdate) {
        NameMethodContainer classDef = getNameMethodContainer(entity);
        // IDカラムでsetterが呼ばれていないものがある場合はエラー
        validSetAllId(entity, classDef);

        // クエリを既に作成済の場合は、キャッシュを返却
        String cache = SELECT_QUERY_MAP.get(entity.getClass());
        if (null != cache) {
            for (NameMethod elem : classDef.idList_) {
                parameterList.add(invokeGetter(elem.method_, entity));
            }
            return (forUpdate ? (cache + " FOR UPDATE") : cache);
        }

        // IDカラムが存在しないエンティティの場合はエラー
        if (classDef.idList_.isEmpty()) {
            throw new QueryRenderingException("try delete but @Id is not in:" + entity.getClass());
        }

        // SELECT句の記述
        StringBuilder sql = new StringBuilder("SELECT ");
        for (NameMethod elem : classDef.idList_) {
            sql.append(elem.name_).append(", ");
        }
        for (NameMethod elem : classDef.columnList_) {
            sql.append(elem.name_).append(", ");
        }
        sql.delete(sql.length() - 2, sql.length());

        // FROM句の記述
        sql.append(" FROM ").append(getTableName(entity));

        // WHERE句の記述
        sql.append(" WHERE ");
        for (NameMethod elem : classDef.idList_) {
            sql.append(elem.name_);
            sql.append("=? AND ");
            parameterList.add(invokeGetter(elem.method_, entity));
        }
        sql.delete(sql.length() - 5, sql.length());

        cache = sql.toString();
        SELECT_QUERY_MAP.put(entity.getClass(), cache);
        return (forUpdate ? (cache + " FOR UPDATE") : cache);
    }

    public static String renderSelectAll(Class<? extends AbstractEntity> entityClass) {
        NameMethodContainer classDef = getNameMethodContainer(entityClass);

        // クエリを既に作成済の場合は、キャッシュを返却
        String cache = ALL_SELECT_QUERY_MAP.get(entityClass);
        if (null != cache) {
            return cache;
        }

        // SELECT句の記述
        StringBuilder sql = new StringBuilder("SELECT ");
        for (NameMethod elem : classDef.idList_) {
            sql.append(elem.name_).append(", ");
        }
        for (NameMethod elem : classDef.columnList_) {
            sql.append(elem.name_).append(", ");
        }
        sql.delete(sql.length() - 2, sql.length());

        // FROM句の記述
        sql.append(" FROM ").append(getTableName(entityClass));

        // ORDER BY句の記述
        if (!classDef.idList_.isEmpty()) {
            sql.append(" ORDER BY ");
            for (NameMethod elem : classDef.idList_) {
                sql.append(elem.name_);
                sql.append(" ASC, ");
            }
            sql.delete(sql.length() - 2, sql.length());
        }

        cache = sql.toString();
        ALL_SELECT_QUERY_MAP.put(entityClass, cache);
        return cache;
    }

    // ///////////////////////

    protected static NameMethodContainer getNameMethodContainer(AbstractEntity entity) {
        return getNameMethodContainer(entity.getClass());
    }

    protected static NameMethodContainer getNameMethodContainer(Class<? extends AbstractEntity> entityClass) {
        NameMethodContainer result = CLASS_DEF_MAP.get(entityClass);
        if (result == null) {
            List<NameMethod> idList = new ArrayList<NameMethod>();
            List<NameMethod> columnList = new ArrayList<NameMethod>();
            Map<String, Method> setterMap = new HashMap<String, Method>();
            setMethodList(entityClass, idList, columnList, setterMap);
            result = new NameMethodContainer();
            result.idList_ = idList;
            result.columnList_ = columnList;
            result.setterMap_ = setterMap;
            CLASS_DEF_MAP.put(entityClass, result);
        }
        return result;
    }

    private static String getTableName(AbstractEntity entity) {
        return getTableName(entity.getClass());
    }

    private static String getTableName(Class<? extends AbstractEntity> entityClass) {
        Table annotation = entityClass.getAnnotation(Table.class);
        if (null != annotation) {
            String name = annotation.name();
            if ((null != name) && !name.isEmpty()) {
                return name;
            }
        }
        // アノテーションからテーブル名が取得できなかった場合は、クラス名をテーブル名として返却
        return entityClass.getSimpleName();
    }

    private static Object invokeGetter(Method getterMethod, AbstractEntity entity) {
        try {
            return getterMethod.invoke(entity);
        } catch (InvocationTargetException e) {
            throw new QueryRenderingException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new QueryRenderingException(e.getMessage(), e);
        }
    }

    private static void validSetAllId(AbstractEntity entity, NameMethodContainer classDef) {
        List<NameMethod> idList = classDef.idList_;
        for (NameMethod elem : idList) {
            if (!entity.isSetField(elem.name_)) {
                throw new QueryRenderingException("ID column[" + elem.name_ + "] is not set:" + entity.getClass());
            }
        }
    }

    private static void setMethodList(Class<? extends AbstractEntity> entityClass, List<NameMethod> idList,
            List<NameMethod> columnList, Map<String, Method> setterMap) {
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            String name = null;
            Id id = field.getAnnotation(Id.class);
            NameMethod obj = null;
            if (null != id) {
                name = id.name();
                obj = new NameMethod();
                obj.name_ = name;
                idList.add(obj);
            } else {
                Column column = field.getAnnotation(Column.class);
                if (null != column) {
                    name = column.name();
                    obj = new NameMethod();
                    obj.name_ = name;
                    columnList.add(obj);
                }
            }
            if (null == name) {
                continue;
            }

            PropertyDescriptor descriptor = null;
            try {
                descriptor = new PropertyDescriptor(field.getName(), entityClass);
            } catch (IntrospectionException e) {
                throw new QueryRenderingException(
                        "field name=[" + name + "] is annotated with @Column or @Id annotation."
                                + "but violated JavaBeans definition in class:" + entityClass.getName(),
                        e);
            }
            obj.method_ = descriptor.getReadMethod();
            if (null == obj.method_) {
                throw new QueryRenderingException(
                        "field name=[" + name + "] is annotated with @Column or @Id annotation."
                                + "but getter method is not define in class:" + entityClass.getName());
            }
            Method setter = descriptor.getWriteMethod();
            if (null == setter) {
                throw new QueryRenderingException(
                        "field name=[" + name + "] is annotated with @Column or @Id annotation."
                                + "but setter method is not define in class:" + entityClass.getName());
            }
            setterMap.put(name, setter);
        }

        Class<?> superClass = entityClass.getSuperclass();
        if ((null != superClass) && //
                !AbstractEntity.class.getName().equals(superClass.getName()) && //
                AbstractEntity.class.isAssignableFrom(superClass)) {
            setMethodList((Class<? extends AbstractEntity>) superClass, idList, columnList, setterMap);
        }

        if (idList.isEmpty() && columnList.isEmpty()) {
            throw new QueryRenderingException("is not @Id and @Column field:" + entityClass.getName());
        }
    }

    private static String createGetterName(String columnName) {
        StringBuilder builder = new StringBuilder("get");
        builder.append(columnName.substring(0, 1).toUpperCase());
        if (1 < columnName.length()) {
            builder.append(columnName.substring(1, columnName.length()).toLowerCase());
        }
        return builder.toString();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    protected static class NameMethod {
        protected Method method_;

        protected String name_;
    }

    protected static class NameMethodContainer {
        protected List<NameMethod> idList_;

        protected List<NameMethod> columnList_;

        protected Map<String, Method> setterMap_;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(FixedQueryRenderer.class.getName());
}
