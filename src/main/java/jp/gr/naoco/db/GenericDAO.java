package jp.gr.naoco.db;

import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.gr.naoco.core.NaocoCoreFacade;
import jp.gr.naoco.core.log.LaolLogger;
import jp.gr.naoco.core.transaction.TransactionManager;
import jp.gr.naoco.db.entity.AbstractEntity;
import jp.gr.naoco.db.entity.FixedQueryRenderer;
import jp.gr.naoco.db.entity.ORMapper;
import jp.gr.naoco.db.exception.QueryRenderingException;
import jp.gr.naoco.db.exception.QueryTemplateException;
import jp.gr.naoco.db.sql.DatabaseBridge;
import jp.gr.naoco.db.sql.TemplateAnalyzer;
import jp.gr.naoco.db.sql.TemplateReader;
import jp.gr.naoco.db.sql.elem.SqlElem;

/**
 * テーブル非依存の全テーブル共通のオブジェクト永続化処理を定義。
 * <p>
 * 本クラスの各メソッドをしようするには、NaocoCoreFacadeのstartTransactionかtakeoverTransactionを呼び出して、
 * DB接続のトランザクション開始が必要となる。
 * </p>
 */
public class GenericDAO {
    private static final HashMap<Class<?>, ParameterSetter> SETTER_MAP = new HashMap<Class<?>, ParameterSetter>();
    static {
        SETTER_MAP.put(String.class, new ParameterSetter() {
            @Override
            public void setObject(Object parameter, PreparedStatement statement, int parameterIndex)
                    throws SQLException {
                statement.setString(parameterIndex, (String) parameter);
            }
        });
        SETTER_MAP.put(Integer.class, new ParameterSetter() {
            @Override
            public void setObject(Object parameter, PreparedStatement statement, int parameterIndex)
                    throws SQLException {
                statement.setInt(parameterIndex, (int) parameter);
            }
        });
        SETTER_MAP.put(Long.class, new ParameterSetter() {
            @Override
            public void setObject(Object parameter, PreparedStatement statement, int parameterIndex)
                    throws SQLException {
                statement.setLong(parameterIndex, (long) parameter);
            }
        });
        SETTER_MAP.put(BigDecimal.class, new ParameterSetter() {
            @Override
            public void setObject(Object parameter, PreparedStatement statement, int parameterIndex)
                    throws SQLException {
                statement.setBigDecimal(parameterIndex, (BigDecimal) parameter);
            }
        });
        SETTER_MAP.put(Short.class, new ParameterSetter() {
            @Override
            public void setObject(Object parameter, PreparedStatement statement, int parameterIndex)
                    throws SQLException {
                statement.setShort(parameterIndex, (short) parameter);
            }
        });
        SETTER_MAP.put(Float.class, new ParameterSetter() {
            @Override
            public void setObject(Object parameter, PreparedStatement statement, int parameterIndex)
                    throws SQLException {
                statement.setFloat(parameterIndex, (float) parameter);
            }
        });
        SETTER_MAP.put(Double.class, new ParameterSetter() {
            @Override
            public void setObject(Object parameter, PreparedStatement statement, int parameterIndex)
                    throws SQLException {
                statement.setDouble(parameterIndex, (double) parameter);
            }
        });
        SETTER_MAP.put(java.sql.Date.class, new ParameterSetter() {
            @Override
            public void setObject(Object parameter, PreparedStatement statement, int parameterIndex)
                    throws SQLException {
                statement.setDate(parameterIndex, (java.sql.Date) parameter);
            }
        });
        SETTER_MAP.put(java.sql.Time.class, new ParameterSetter() {
            @Override
            public void setObject(Object parameter, PreparedStatement statement, int parameterIndex)
                    throws SQLException {
                statement.setTime(parameterIndex, (java.sql.Time) parameter);
            }
        });
        SETTER_MAP.put(java.sql.Timestamp.class, new ParameterSetter() {
            @Override
            public void setObject(Object parameter, PreparedStatement statement, int parameterIndex)
                    throws SQLException {
                statement.setTimestamp(parameterIndex, (java.sql.Timestamp) parameter);
            }
        });
        SETTER_MAP.put(java.util.Date.class, new ParameterSetter() {
            @Override
            public void setObject(Object parameter, PreparedStatement statement, int parameterIndex)
                    throws SQLException {
                statement.setTimestamp(parameterIndex, new java.sql.Timestamp(((java.util.Date) parameter).getTime()));
            }
        });
        SETTER_MAP.put(byte[].class, new ParameterSetter() {
            @Override
            public void setObject(Object parameter, PreparedStatement statement, int parameterIndex)
                    throws SQLException {
                statement.setBinaryStream(parameterIndex, new java.io.ByteArrayInputStream((byte[]) parameter));
            }
        });
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private GenericDAO() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * エンティティで指定したIDに対応する、DB上のレコードを更新する。
     * <p>
     * 指定するエンティティについては、&#64;Idで主キー指定したクラス変数に対応するsetterメソッドをすべて呼出して、<br/>
     * 更新対象レコードが1件となるように取得条件を指定する必要がある。
     * </p>
     * <p>
     * &#64;Columnを付与したプロパティのsetterを呼び出したカラムのみ更新対象とする。
     * </p>
     *
     * @param entity
     *            更新結果を格納したエンティティ
     * @return 更新したレコード数
     * @throws SQLException
     * @throws QueryRenderingException
     *             &#64;Id を付与したクラス変数に対応するsetterで、呼び出されていないものが存在する場合。
     */
    public static <T extends AbstractEntity> int update(T entity) throws SQLException {
        ArrayList<Object> parameterList = new ArrayList<Object>();
        String sql = FixedQueryRenderer.renderUpdate(entity, parameterList);

        LOG.sql(sql);
        PreparedStatement st = null;
        try {
            st = NaocoCoreFacade.prepareStatement(sql);
            setParameter(st, parameterList, entity.getClass());
            LOG.sql(forLog(parameterList));
            return st.executeUpdate();
        } finally {
            if ((null != st) && !st.isClosed()) {
                st.close();
            }
        }
    }

    /**
     * エンティティで指定した内容で、DBにレコードを追加する。
     * <p>
     * 指定するエンティティについては、&#64;Idで主キー指定したクラス変数に対応するsetterメソッドをすべて呼出す必要がある。
     * </p>
     *
     * @param entity
     *            追加する内容を格納したエンティティ
     * @return 更新したレコード数
     * @throws SQLException
     * @throws QueryRenderingException
     *             &#64;Id を付与したクラス変数に対応するsetterで、呼び出されていないものが存在する場合。
     */
    public static <T extends AbstractEntity> int insert(T entity) throws SQLException {
        ArrayList<Object> parameterList = new ArrayList<Object>();
        String sql = FixedQueryRenderer.renderInsert(entity, parameterList, false);

        LOG.sql(sql);
        PreparedStatement st = null;
        try {
            st = NaocoCoreFacade.prepareStatement(sql);
            setParameter(st, parameterList, entity.getClass());
            LOG.sql(forLog(parameterList));
            return st.executeUpdate();
        } finally {
            if ((null != st) && !st.isClosed()) {
                st.close();
            }
        }
    }

    /**
     * エンティティで指定した内容で、DBにレコードを一括で追加する。
     * <p>
     * 指定するエンティティについては、@Idで主キー指定したクラス変数に対応するsetterメソッドをすべて呼出す必要がある。
     * </p>
     * <p>
     * 一括追加対象となる情報をリストとしてヒープ領域に持ちきれない場合は、{@linke
     * GenericDAO#startInsertBatch(AbstractEntity)}を 利用して、適宜、バッチ対象のエンティティを追加する方式とすること。
     * </p>
     *
     * @param entity
     *            追加する内容を格納したエンティティのリスト
     * @return 更新したレコード数の配列
     * @throws SQLException
     * @throws QueryRenderingException
     *             &#64;Id を付与したクラス変数に対応するsetterで、呼び出されていないものが存在する場合。
     */
    public static <T extends AbstractEntity> int[] insertBatch(List<T> entityList) throws SQLException {
        if (entityList.isEmpty()) {
            return new int[] {};
        }
        BatchInsert batch = new BatchInsert(entityList.get(0));
        for (AbstractEntity entity : entityList) {
            batch.addBatch(entity);
        }
        return batch.executeBatch();
    }

    /**
     * バッチ追加を開始する。
     * <p>
     * 指定したエンティティのテーブルについて、バッチ追加をするためのオブジェクトを取得する。
     * </p>
     * <p>
     * 追加内容については、 本メソッドの返却オブジェクトの {@link BatchInsert#addBatch(AbstractEntity)}を呼び出して 追加内容を格納したエンティティをそれぞれ指定する。
     * <p>
     * すべての追加が完了したら、 本メソッドの返却オブジェクトの {@link BatchInsert#executeBatch()}を呼び出して バッチ追加を実行し、DBに追加内容を反映する。
     * </p>
     *
     * <pre>
     * Usage:
     *    BatchInsert batch = CommonDao.startInsertBatch(entity[0]);
     *    batch.addBatch(entity[0]);
     *    batch.addBatch(entity[1]);
     *    batch.addBatch(entity[2]);
     *    batch.executeBatch();
     * </pre>
     *
     * @param entity
     *            バッチ追加の一件目のエンティティ（本メソッド内ではバッチに追加をしないので、このエンティティについても別途 addBatch で追加する必要がある）
     * @param entity
     *            バッチ追加の一件目のエンティティ（本メソッド内ではバッチに追加をしないので、このエンティティについても別途 addBatch で追加する必要がある）
     */
    public static <T extends AbstractEntity> BatchInsert startInsertBatch(T entity) throws SQLException {
        BatchInsert batch = new BatchInsert(entity);
        return batch;
    }

    /**
     * エンティティで指定したIDに対応する、DB上のレコードを削除する。
     * <p>
     * 指定するエンティティについては、@Idで主キー指定したクラス変数に対応するsetterメソッドをすべて呼出して、 削除結果が1件となるように取得条件を指定する必要がある。
     * </p>
     *
     * @param entity
     *            削除条件となるIDカラムの設定をしたエンティティ
     * @return 更新したレコード数
     * @throws SQLException
     * @throws QueryRenderingException
     *             指定したエンティティに&#64;Idを付与したクラス変数が存在しない場合。
     *             あるいは、&#64;Idを付与したクラス変数に対応するsetterで、呼び出されていないものが存在する場合。
     */
    public static <T extends AbstractEntity> int delete(T entity) throws SQLException {
        ArrayList<Object> parameterList = new ArrayList<Object>();
        String sql = FixedQueryRenderer.renderDelete(entity, parameterList);

        LOG.sql(sql);
        PreparedStatement st = null;
        try {
            st = NaocoCoreFacade.prepareStatement(sql);
            setParameter(st, parameterList, entity.getClass());
            LOG.sql(forLog(parameterList));
            return st.executeUpdate();
        } finally {
            if ((null != st) && !st.isClosed()) {
                st.close();
            }
        }
    }

    /**
     * エンティティで指定したIDに対応する、DB取得結果エンティティを取得する。
     * <p>
     * 指定するエンティティについては、@Idで主キー指定したクラス変数に対応するsetterメソッドをすべて呼出して、 取得結果が1件となるように取得条件を指定する必要がある。
     * </p>
     * <p>
     * 本メソッドは、find(entity, false)を呼び出すのと同様である。
     * </p>
     *
     * @param entity
     *            取得条件となるIDカラムの設定をしたエンティティ
     * @return DB取得結果のエンティティ 取得結果が0件であった場合はnullを返却
     * @throws SQLException
     * @throws QueryRenderingException
     *             指定したエンティティに&#64;Idを付与したクラス変数が存在しない場合。
     *             あるいは、&#64;Idを付与したクラス変数に対応するsetterで、呼び出されていないものが存在する場合。
     */
    public static <T extends AbstractEntity> T find(T entity) throws SQLException {
        return find(entity, false);
    }

    /**
     * エンティティで指定したIDに対応する、DB取得結果エンティティを取得する。
     * <p>
     * 指定するエンティティについては、@Idで主キー指定したクラス変数に対応するsetterメソッドをすべて呼出して、 取得結果が1件となるように取得条件を指定する必要がある。
     * </p>
     * <p>
     * 取得したレコードについて、ロックを取得したい場合はforUpdateにtrueを指定する。
     * </p>
     *
     * @param entity
     *            取得条件となるIDカラムの設定をしたエンティティ
     * @param forUpdate
     *            クエリにFOR UPDATE 句を付与してロックを取得する場合はtrue。それ以外はfalse
     * @return DB取得結果のエンティティ 取得結果が0件であった場合はnullを返却
     * @throws SQLException
     * @throws QueryRenderingException
     *             指定したエンティティに@Idを付与したクラス変数が存在しない場合。
     *             あるいは、@Idを付与したクラス変数に対応するsetterで、呼び出されていないものが存在する場合。
     */
    public static <T extends AbstractEntity> T find(T entity, boolean forUpdate) throws SQLException {
        ArrayList<Object> parameterList = new ArrayList<Object>();
        String sql = FixedQueryRenderer.renderSelect(entity, parameterList, forUpdate);
        LOG.sql(sql);
        PreparedStatement st = NaocoCoreFacade.prepareStatement(sql);
        setParameter(st, parameterList, entity.getClass());
        LOG.sql(forLog(parameterList));
        ResultSet rs = null;
        rs = st.executeQuery();
        T result = (T) ORMapper.mappingById(rs, entity.getClass());
        return result;
    }

    /**
     * エンティティクラスに対応するテーブルを全件取得するResultSetのIteratorを生成する。
     * <p>
     * 指定したエンティティクラスに対応するレコードを、条件なしで全件取得する。 並び順はエンティティの定義内で@Idアノテーションが付与されたクラス変数順昇順で指定する。（@Idが存在しない場合は並び順指定なし）
     * </p>
     *
     * @param entityClass
     *            取得対象テーブルに対応するエンティティのクラス
     * @return 結果のエンティティを取得するためのIterator
     * @throws SQLException
     */
    public static <T extends AbstractEntity> Iterator<T> findAll(Class<T> entityClass) throws SQLException {
        return findAll(entityClass, 0);
    }

    /**
     * エンティティクラスに対応するテーブルを全件取得するResultSetのIteratorを生成する。
     * <p>
     * 指定したエンティティクラスに対応するレコードを、条件なしで全件取得する。 並び順はエンティティの定義内で@Idアノテーションが付与されたクラス変数順昇順で指定する。（@Idが存在しない場合は並び順指定なし）
     * </p>
     *
     * @param entityClass
     *            取得対象テーブルに対応するエンティティのクラス
     * @param fetchSize
     *            Statementに設定するフェッチサイズ
     * @return 結果のエンティティを取得するためのIterator
     * @throws SQLException
     */
    public static <T extends AbstractEntity> Iterator<T> findAll(Class<T> entityClass, int fetchSize)
            throws SQLException {
        String sql = FixedQueryRenderer.renderSelectAll(entityClass);
        PreparedStatement st = NaocoCoreFacade.prepareStatement(sql);
        if (0 < fetchSize) {
            st.setFetchSize(fetchSize);
        }
        LOG.sql(sql);
        ResultSet rs = st.executeQuery();
        return ORMapper.getResultsetIterator(rs, entityClass);
    }

    /**
     * SQLテンプレートファイルから動的に可変部分を設定して、SQLを実行した結果を取得する。
     * <p>
     * 詳細を書くこと！
     * </p>
     *
     * @param templateSql
     *            SQLテンプレートファイルのパス（クラスパスが通っているフォルダからの相対パス）
     * @param variableMap
     *            可変部分の値を設定したマップ
     * @param entityClass
     *            結果を格納するエンティティのクラス
     * @return 結果を格納したエンティティのリスト
     * @throws SQLException
     */
    public static <T extends AbstractEntity> List<T> executeSelect(String templateSql, Map<String, Object> variableMap,
            Class<T> entityClass) throws SQLException {
        Iterator<String> templateLines = TemplateReader.readTemplate(templateSql);
        SqlElem firstElem = TemplateAnalyzer.analyze(templateSql, templateLines);
        ArrayList<Object> parameterList = new ArrayList<Object>(variableMap.size());
        String sql = createSql(firstElem, variableMap, parameterList);
        LOG.sql(sql);
        PreparedStatement st = NaocoCoreFacade.prepareStatement(sql);
        setParameter(st, parameterList, null);
        LOG.sql(forLog(parameterList));

        ResultSet rs = null;
        rs = st.executeQuery();
        List<T> result = ORMapper.mapping(rs, entityClass);
        return result;
    }

    /**
     * SQLテンプレートファイルから動的に可変部分を設定して、SQLを実行した結果を取得する。
     * <p>
     * 取得結果件数がヒープ領域に収まりきらないほど大量であることが予想される場合、 取得結果のResultSetをIteratorでラップしたオブジェクトを取得する。
     * </p>
     * <p>
     * 詳細を書くこと！
     * </p>
     *
     * @param templateSql
     *            SQLテンプレートファイルのパス（クラスパスが通っているフォルダからの相対パス）
     * @param variableMap
     *            可変部分の値を設定したマップ
     * @param entityClass
     *            結果を格納するエンティティのクラス
     * @return 結果を格納したエンティティのリスト
     * @throws SQLException
     */
    public static <T extends AbstractEntity> Iterator<T> executeSelectSequential(String templateSql,
            Map<String, Object> variableMap, Class<T> entityClass) throws SQLException {
        return executeSelectSequential(templateSql, variableMap, entityClass, -1);
    }

    /**
     * SQLテンプレートファイルから動的に可変部分を設定して、SQLを実行した結果を取得する。
     * <p>
     * 取得結果件数がヒープ領域に収まりきらないほど大量であることが予想される場合、 取得結果のResultSetをIteratorでラップしたオブジェクトを取得する。
     * </p>
     * <p>
     * 詳細を書くこと！
     * </p>
     *
     * @param templateSql
     *            SQLテンプレートファイルのパス（クラスパスが通っているフォルダからの相対パス）
     * @param variableMap
     *            可変部分の値を設定したマップ
     * @param entityClass
     *            結果を格納するエンティティのクラス
     * @param fetchSize Statementに設定するフェッチサイズ
     * @return 結果を格納したエンティティのリスト
     * @throws SQLException
     */
    public static <T extends AbstractEntity> Iterator<T> executeSelectSequential(String templateSql,
            Map<String, Object> variableMap, Class<T> entityClass, int fetchSize) throws SQLException {
        Iterator<String> templateLines = TemplateReader.readTemplate(templateSql);
        SqlElem firstElem = TemplateAnalyzer.analyze(templateSql, templateLines);
        ArrayList<Object> parameterList = new ArrayList<Object>(variableMap.size());
        String sql = createSql(firstElem, variableMap, parameterList);
        LOG.sql(sql);
        PreparedStatement st = NaocoCoreFacade.prepareStatement(sql);
        if (0 < fetchSize) {
            st.setFetchSize(fetchSize);
        }
        setParameter(st, parameterList, null);
        LOG.sql(forLog(parameterList));

        ResultSet rs = st.executeQuery();
        Iterator<T> result = ORMapper.getResultsetIterator(rs, entityClass);
        return result;
    }

    /**
     * SQLテンプレートファイルから動的に可変部分を設定して、SQL（SELECT COUNT ...) を実行した結果を取得する。
     * <p>
     * 詳細を書くこと！
     * </p>
     *
     * @param templateSql
     *            SQLテンプレートファイルのパス（クラスパスが通っているフォルダからの相対パス）
     * @param variableMap
     *            可変部分の値を設定したマップ
     * @param entityClass
     *            結果を格納するエンティティのクラス
     * @return 結果を格納したエンティティのリスト
     * @throws SQLException
     */
    public static long executeSelectCount(String templateSql, Map<String, Object> variableMap) throws SQLException {
        Iterator<String> templateLines = TemplateReader.readTemplate(templateSql);
        SqlElem firstElem = TemplateAnalyzer.analyze(templateSql, templateLines);
        ArrayList<Object> parameterList = new ArrayList<Object>(variableMap.size());
        String sql = createSql(firstElem, variableMap, parameterList);
        LOG.sql(sql);
        PreparedStatement st = NaocoCoreFacade.prepareStatement(sql);
        setParameter(st, parameterList, null);
        LOG.sql(forLog(parameterList));

        ResultSet rs = null;
        try {
            rs = st.executeQuery();
            if (!rs.next()) {
                throw new QueryTemplateException("call 'executeSelectCount' method. but, ResultSet has no records"
                        + "(maybe, sql is not SELECT COUNT(*) ... sentence.:" + templateSql);
            }
            return rs.getLong(1);
        } finally {
            if (null != rs) {
                rs.close();
            }
            if (null != st) {
                st.close();
            }
        }
    }

    /**
     * SQLテンプレートファイルから動的に可変部分を設定して、更新系SQLを実行する。
     * <p>
     * 詳細を書くこと！
     * </p>
     *
     * @param templateSql
     *            SQLテンプレートファイルのパス（クラスパスが通っているフォルダからの相対パス）
     * @param variableMap
     *            可変部分の値を設定したマップ
     * @return 結果を格納したエンティティのリスト
     * @throws SQLException
     */
    public static int executeUpdate(String templateSql, Map<String, Object> variableMap) throws SQLException {
        Iterator<String> templateLines = TemplateReader.readTemplate(templateSql);
        SqlElem firstElem = TemplateAnalyzer.analyze(templateSql, templateLines);
        ArrayList<Object> parameterList = new ArrayList<Object>(variableMap.size());
        String sql = createSql(firstElem, variableMap, parameterList);
        LOG.sql(sql);
        PreparedStatement st = null;
        try {
            st = NaocoCoreFacade.prepareStatement(sql);
            setParameter(st, parameterList, null);
            LOG.sql(forLog(parameterList));

            return st.executeUpdate();
        } finally {
            if ((null != st) && !st.isClosed()) {
                st.close();
            }
        }

    }

    /**
     * SQLテンプレートファイルから動的に可変部分を設定して、更新系SQLを実行する。
     * <p>
     * 同一のSQLの場合、内部で同一のPreparedStatementを実行します。
     * </p>
     * <p>
     * 通常のexecuteUpdateと異なり、本メソッドでは内部で保持しているPreparedStatementにaddBatchを実行するのみで、executeBatchとcloseは、
     * commitまで実行しません。rollback時はPreparedStatementのcloseのみ実行し、executeBatchは実行しません。
     * </p>
     *
     * @param templateSql
     *            SQLテンプレートファイルのパス（クラスパスが通っているフォルダからの相対パス）
     * @param variableMap
     *            可変部分の値を設定したマップ
     * @throws SQLException
     */
    public static void executeUpdateByStored(String templateSql, Map<String, Object> variableMap) throws SQLException {
        Iterator<String> templateLines = TemplateReader.readTemplate(templateSql);
        SqlElem firstElem = TemplateAnalyzer.analyze(templateSql, templateLines);
        ArrayList<Object> parameterList = new ArrayList<Object>(variableMap.size());
        String sql = createSql(firstElem, variableMap, parameterList);
        LOG.sql(sql);
        PreparedStatement st = null;

        st = TransactionManager.storePreparedStatement(sql);
        setParameter(st, parameterList, null);
        LOG.sql(forLog(parameterList));

        st.addBatch();
    }

    /**
     * シーケンスの次の値を取得する
     *
     * @param sequenceName
     *            シーケンスの名称
     * @return シーケンスから取得した値
     * @throws SQLException
     */
    public static String nextvalSequence(String sequenceName) throws SQLException {
        return DatabaseBridge.sequence(sequenceName);
    }

    // ///////////////////////

    private static void setParameter(PreparedStatement statement, List<Object> parameterList,
            Class<? extends AbstractEntity> entityClass) throws SQLException {
        Iterator<Object> parameters = parameterList.iterator();
        for (int i = 1; parameters.hasNext(); i++) {
            Object parameter = parameters.next();
            if (null == parameter) {
                DatabaseBridge.setNull(statement, i);
                continue;
            }
            ParameterSetter setter = SETTER_MAP.get(parameter.getClass());
            if (null == setter) {
                if (null != entityClass) {
                    throw new QueryRenderingException("entity(" + entityClass.getName()
                            + ") field has not allowed type:" + parameter.getClass().getName());
                } else {
                    throw new QueryRenderingException(
                            "variable map has not allowed type:" + parameter.getClass().getName());
                }
            }
            setter.setObject(parameter, statement, i);
        }
    }

    private static String forLog(List<Object> parameterList) {
        if (!LOG.requiredDebugLevel()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(" [");
        for (Object obj : parameterList) {
            if (null == obj) {
                builder.append("null, ");
            } else {
                builder.append("'").append(obj.toString()).append("', ");
            }
        }
        if (!parameterList.isEmpty()) {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append("]");
        return builder.toString();
    }

    private static String createSql(SqlElem firstElem, Map<String, Object> variableMap, List<Object> parameterList) {
        StringBuilder builder = new StringBuilder();
        firstElem.appendSqlBuilder(builder, variableMap, parameterList);
        String sql = builder.toString();
        return sql.replaceAll("\\s+", " ") // 空白文字列はスペース1文字に置換
                .replaceAll("\\s*,\\s*", ", ") // カンマは後ろスペース1文字に置換
                .replaceAll("\\(\\s*", "(") // 括弧の始まり後の空白を削除
                .replaceAll("\\s*\\)", ")") // 括弧の終わり前の空白を削除
                .replaceAll("\\s$", ""). // 末尾の空白は削除
                replaceAll(";$", ""); // 末尾のセミコロンは削除
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    private static interface ParameterSetter {
        public void setObject(Object parameter, PreparedStatement statement, int parameterIndex) throws SQLException;
    }

    public static class BatchInsert {
        private final PreparedStatement st_;

        private final String sql_;

        private int count_ = 0;

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Constructor

        private BatchInsert(AbstractEntity firstEntity) throws SQLException {
            sql_ = FixedQueryRenderer.renderInsert(firstEntity, null, true);
            LOG.sql("start batch reserve");
            LOG.sql(sql_);
            st_ = NaocoCoreFacade.prepareStatement(sql_);
        }

        // /////////////////////////////////////////////////////////////////////////////////////////
        // Method

        public void addBatch(AbstractEntity entity) throws SQLException {
            try {
                List<Object> parameterList = FixedQueryRenderer.createInsertBatchParameterList(entity);
                setParameter(st_, parameterList, entity.getClass());
                if (LOG.requiredDebugLevel()) {
                    LOG.sql("(batch reserve)" + sql_ + forLog(parameterList));
                }
                st_.addBatch();
                count_++;
            } catch (Throwable t) {
                if ((null != st_) && !st_.isClosed()) {
                    st_.close();
                }
                throw t;
            }
        }

        public int[] executeBatch() throws SQLException {
            try {
                if (LOG.requiredDebugLevel()) {
                    LOG.sql("end batch reserve [count=" + count_ + "]");
                }
                if (0 < count_) {
                    try {
                        return st_.executeBatch();
                    } catch (BatchUpdateException e) {
                        LOG.warn(e.getMessage(), e);
                        throw e;
                    }
                } else {
                    return new int[] {};
                }
            } finally {
                if ((null != st_) && !st_.isClosed()) {
                    st_.close();
                }
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(GenericDAO.class.getName());
}
