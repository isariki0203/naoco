package jp.gr.naoco.db.sql.elem;

import java.util.List;
import java.util.Map;

public interface SqlElem {
    public void setNext(SqlElem next);

    public SqlElem getNext();

    public boolean isLast();

    public void appendSqlBuilder(StringBuilder builder, Map<String, Object> variableMap, List<Object> parameterList);
}
