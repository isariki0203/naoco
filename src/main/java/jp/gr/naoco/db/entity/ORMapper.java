package jp.gr.naoco.db.entity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jp.gr.naoco.core.exception.ReflectionException;
import jp.gr.naoco.core.log.LaolLogger;
import jp.gr.naoco.db.entity.FixedQueryRenderer.NameMethod;
import jp.gr.naoco.db.entity.FixedQueryRenderer.NameMethodContainer;

public class ORMapper {

    private static final HashMap<Class<?>, ResultSetGetter> TYPE_GETTER_MAP = new HashMap<Class<?>, ResultSetGetter>();
    static {
        TYPE_GETTER_MAP.put(String.class, new ResultSetGetter() {
            @Override
            public Object get(ResultSet rs, int i) throws SQLException {
                return rs.getString(i);
            }
        });
        TYPE_GETTER_MAP.put(Integer.class, new ResultSetGetter() {
            @Override
            public Object get(ResultSet rs, int i) throws SQLException {
                BigDecimal bd = rs.getBigDecimal(i);
                return ((null == bd) ? null : bd.intValue());
            }
        });
        TYPE_GETTER_MAP.put(Long.class, new ResultSetGetter() {
            @Override
            public Object get(ResultSet rs, int i) throws SQLException {
                BigDecimal bd = rs.getBigDecimal(i);
                return ((null == bd) ? null : bd.longValue());
            }
        });
        TYPE_GETTER_MAP.put(java.math.BigDecimal.class, new ResultSetGetter() {
            @Override
            public Object get(ResultSet rs, int i) throws SQLException {
                return rs.getBigDecimal(i);
            }
        });
        TYPE_GETTER_MAP.put(Double.class, new ResultSetGetter() {
            @Override
            public Object get(ResultSet rs, int i) throws SQLException {
                BigDecimal bd = rs.getBigDecimal(i);
                return ((null == bd) ? null : bd.doubleValue());
            }
        });
        TYPE_GETTER_MAP.put(java.util.Date.class, new ResultSetGetter() {
            @Override
            public Object get(ResultSet rs, int i) throws SQLException {
                return rs.getTimestamp(i);
            }
        });
        TYPE_GETTER_MAP.put(java.sql.Date.class, new ResultSetGetter() {
            @Override
            public Object get(ResultSet rs, int i) throws SQLException {
                return rs.getDate(i);
            }
        });
        TYPE_GETTER_MAP.put(java.sql.Timestamp.class, new ResultSetGetter() {
            @Override
            public Object get(ResultSet rs, int i) throws SQLException {
                return rs.getTimestamp(i);
            }
        });
        TYPE_GETTER_MAP.put(java.sql.Time.class, new ResultSetGetter() {
            @Override
            public Object get(ResultSet rs, int i) throws SQLException {
                return rs.getTime(i);
            }
        });
        TYPE_GETTER_MAP.put(byte[].class, new ResultSetGetter() {
            @Override
            public Object get(ResultSet rs, int i) throws SQLException {
                return rs.getBytes(i);
            }
        });
        TYPE_GETTER_MAP.put(Boolean.class, new ResultSetGetter() {
            @Override
            public Object get(ResultSet rs, int i) throws SQLException {
                return rs.getBoolean(i);
            }
        });
        TYPE_GETTER_MAP.put(java.io.Reader.class, new ResultSetGetter() {
            @Override
            public Object get(ResultSet rs, int i) throws SQLException {
                return rs.getCharacterStream(i);
            }
        });
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private ORMapper() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * ORマッピング
     *
     * @param rs
     * @param cls
     * @return
     * @throws SQLException
     */
    public static <T extends AbstractEntity> T mappingById(ResultSet rs, Class<T> cls) throws SQLException {
        List<T> list = mapping(rs, cls);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    /**
     * ORマッピング
     *
     * @param rs
     * @param cls
     * @return
     * @throws SQLException
     */
    public static <T extends AbstractEntity> List<T> mapping(ResultSet rs, Class<T> cls) throws SQLException {
        List<T> list = new ArrayList<T>();
        try {
            while (rs.next()) {
                list.add(mapping1Line(rs, cls));
            }
        } finally {
            closeResultSet(rs);
        }
        return list;
    }

    /**
     * ResultSetから一行ずつフェッチした結果のエンティティを返却するIteratorを生成
     * <p>
     * Iterator#next()メソッドの呼出しの度に、ResultSetから1レコードのみフェッチをしてmappiing1Lineの 結果をnext()メソッドの返却値として取得する。
     * </p>
     * <p>
     * 本メソッドはクエリ発行の結果、大量のレコードを取得する可能性がある場合に、すべての結果をヒープに保持せずに、 一行毎にOutputStreamなどに結果を書き込みたい場合に使用する。
     * </p>
     */
    public static <T extends AbstractEntity> Iterator<T> getResultsetIterator(ResultSet rs, Class<T> cls)
            throws SQLException {
        return new ResultSetIterator<>(rs, cls);
    }

    /**
     * 1行だけマッピング ResultSet#nextは実行済みであること。
     *
     * @param rs
     * @param cls
     * @return
     * @throws SQLException
     */
    public static <T extends AbstractEntity> T mapping1Line(ResultSet rs, Class<T> cls) throws SQLException {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            return mapping1Line(rs, cls, rsmd);
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * 1行だけマッピング ResultSet#nextは実行済みであること。
     *
     * @param rs
     * @param cls
     * @return
     * @throws SQLException
     */
    public static <T extends AbstractEntity> T mapping1Line(ResultSet rs, Class<T> cls, ResultSetMetaData rsmd)
            throws SQLException {
        T entity = null;
        try {
            try {
                entity = cls.newInstance();
            } catch (InstantiationException e) {
                throw new ReflectionException(e);
            } catch (IllegalAccessException e) {
                throw new ReflectionException(e);
            }

            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String colname = rsmd.getColumnLabel(i);
                int coltype = rsmd.getColumnType(i);

                // カラムタイプにより引数を設定。
                // 本処理では引数1つのみ対応
                Class<?>[] argsType = null;
                Object[] args = null;
                switch (coltype) {
                case Types.CHAR:
                case Types.VARCHAR:
                    argsType = new Class[] {String.class };
                    break;

                case Types.BIT:
                case Types.INTEGER:
                case Types.TINYINT:
                case Types.SMALLINT:
                    argsType = new Class[] {Integer.class, java.math.BigDecimal.class };
                    break;

                case Types.BIGINT:
                    argsType = new Class[] {Long.class, java.math.BigDecimal.class };
                    break;

                case Types.FLOAT:
                case Types.REAL:
                case Types.DOUBLE:
                    argsType = new Class[] {Double.class, java.math.BigDecimal.class };
                    break;

                case Types.NUMERIC:
                case Types.DECIMAL:
                    argsType = new Class[] {Long.class, Double.class, Integer.class, java.math.BigDecimal.class };
                    break;

                case Types.DATE:
                case Types.TIMESTAMP:
                    argsType = new Class[] {java.sql.Date.class, java.sql.Timestamp.class, java.sql.Time.class,
                            java.util.Date.class };
                    break;

                case Types.CLOB:
                    argsType = new Class[] {String.class };
                    if (rs.getCharacterStream(i) == null) {
                        args = new Object[] {null };
                        break;
                    }
                    BufferedReader reader = new BufferedReader(rs.getCharacterStream(i));

                    StringBuilder temp = new StringBuilder();
                    try {
                        String line = null;
                        while (null != (line = reader.readLine())) {
                            temp.append(line).append(System.getProperty("line.separator"));
                        }
                        args = new Object[] {temp.toString() };
                    } catch (IOException e) {
                        args = new Object[] {temp.toString() };
                    } finally {
                        try {
                            if (null != reader)
                                reader.close();
                        } catch (IOException e) {
                            // nothing to do
                        }
                    }
                    break;

                case Types.BLOB:
                    argsType = new Class[] {byte[].class };
                    InputStream in = rs.getBinaryStream(i);
                    BufferedInputStream buf = null;
                    ByteArrayOutputStream out = null;
                    try {
                        buf = new BufferedInputStream(in);
                        out = new ByteArrayOutputStream();
                        byte[] bytes = new byte[8192];
                        int len = 0;
                        while (0 <= (len = buf.read(bytes))) {
                            out.write(bytes, 0, len);
                            bytes = new byte[8192];
                        }
                        args = new Object[] {out.toByteArray() };
                    } catch (IOException e) {
                        continue;
                    } finally {
                        try {
                            if (null != buf) {
                                buf.close();
                            }
                            if (null != in) {
                                in.close();
                            }
                            if (null != out) {
                                out.close();
                            }
                        } catch (IOException e) {
                            continue;
                        }
                    }
                    break;

                default:
                    argsType = new Class[] {String.class };
                    break;
                }
                // メソッド生成
                Method method = getSetter(entity, colname);
                if ((null == method) || (0 == method.getParameterTypes().length)) {
                    continue;
                }
                Class<?> fixedArgType = method.getParameterTypes()[0];

                // メソッド実行
                try {
                    if (null != args) {
                        method.invoke(entity, args);
                    } else {
                        Object arg = TYPE_GETTER_MAP.get(fixedArgType).get(rs, i);
                        method.invoke(entity, arg);
                    }
                } catch (IllegalArgumentException e3) {
                    throw new ReflectionException(e3);
                } catch (IllegalAccessException e3) {
                    throw new ReflectionException(e3);
                } catch (InvocationTargetException e3) {
                    throw new ReflectionException(e3);
                }
            }
        } catch (SQLException e) {
            throw e;
        }

        // エンティティ内のsetter呼出し済みSetから、ID以外のものを削除
        NameMethodContainer container = FixedQueryRenderer.getNameMethodContainer(entity);
        for (NameMethod elem : container.columnList_) {
            entity.setFieldNameSet_.remove(elem.name_);
        }

        return entity;
    }

    /**
     * setter風に変換
     *
     * @param name
     * @return
     */
    private static Method getSetter(AbstractEntity entity, String columnName) {
        NameMethodContainer container = FixedQueryRenderer.getNameMethodContainer(entity);
        return container.setterMap_.get(columnName);
    }

    private static void closeResultSet(ResultSet rs) throws SQLException {
        Statement st = ((null == rs) ? null : rs.getStatement());
        try {
            if ((null != rs) && !rs.isClosed()) {
                rs.close();
            }
        } finally {
            if ((null != st) && !st.isClosed()) {
                st.close();
            }
        }

    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    public static class ResultSetIterator<T extends AbstractEntity> implements Iterator<T> {
        public static final long ACCESS_TIMEOUT_INTERVAL_MILLIS = 60 * 60 * 1000;

        private ResultSet resultSet_;

        private Class<T> class_;

        private ResultSetMetaData rsmd_;

        private Boolean hasNext_;

        private long lastAccess_;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        private ResultSetIterator(ResultSet resultSet, Class<T> clazz) throws SQLException {
            resultSet_ = resultSet;
            class_ = clazz;
            rsmd_ = resultSet.getMetaData();
            hasNext_ = null;
            lastAccess_ = System.currentTimeMillis();
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Methods

        @Override
        public boolean hasNext() {
            lastAccess_ = System.currentTimeMillis();
            try {
                if (null == hasNext_) {
                    hasNext_ = resultSet_.next();
                    if (!hasNext_.booleanValue()) {
                        closeResultSet(resultSet_);
                    }
                }
                return hasNext_.booleanValue();
            } catch (Throwable t) {
                try {
                    closeResultSet(resultSet_);
                } catch (SQLException e2) {
                    LOG.error(e2.getMessage(), e2);
                }
                throw new RuntimeException(t);
            }
        }

        @Override
        public T next() {
            lastAccess_ = System.currentTimeMillis();
            try {
                hasNext_ = null;
                if (resultSet_.isClosed()) {
                    return null;
                }
                return mapping1Line(resultSet_, class_, rsmd_);
            } catch (Throwable t) {
                try {
                    closeResultSet(resultSet_);
                } catch (SQLException e2) {
                    LOG.error(e2.getMessage(), e2);
                }
                throw new RuntimeException(t);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean closeTimeout() throws SQLException {
            long current = System.currentTimeMillis();
            if (ACCESS_TIMEOUT_INTERVAL_MILLIS < (current - lastAccess_)) {
                closeResultSet(resultSet_);
                return true;
            }
            return false;
        }
    }

    // ///////////////////////

    private static interface ResultSetGetter {
        public Object get(ResultSet rs, int i) throws SQLException;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(ORMapper.class.getName());

}
