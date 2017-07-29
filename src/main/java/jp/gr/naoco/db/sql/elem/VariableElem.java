package jp.gr.naoco.db.sql.elem;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import jp.gr.naoco.db.sql.DatabaseBridge;

public class VariableElem implements SqlElem {
    private SqlElem next_ = EoqElem.INSTANCE;

    private String key_;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public VariableElem(String key) {
        key_ = key;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public void setNext(SqlElem next) {
        next_ = next;
    }

    @Override
    public SqlElem getNext() {
        return next_;
    }

    @Override
    public boolean isLast() {
        return (next_ instanceof EoqElem);
    }

    @Override
    public void appendSqlBuilder(StringBuilder builder, Map<String, Object> variableMap, List<Object> parameterList) {
        Object variable = variableMap.get(key_);
        if (null == variable) {
            parameterList.add(null);
            builder.append("? ");
        } else if (variable instanceof Iterable) {
            Iterable<?> ite = (Iterable<?>) variable;
            StringBuilder tmp = new StringBuilder();
            builder.append("(");
            for (Object e : ite) {
                parameterList.add(e);
                tmp.append("?,");
            }
            String tmpStr = tmp.substring(0, tmp.length() - 1);
            if (0 < tmpStr.length()) {
                builder.append(tmpStr).append(") ");
            } else {
                builder.append("() ");
            }
        } else {
            HashSet<Class<?>> typeSet = DatabaseBridge.getAvaliableParameterTypes();
            if (typeSet.contains(variable.getClass())) {
                parameterList.add(variable);
                builder.append("? ");
            } else {
                throw new UnsupportedOperationException(
                        "PreparedStatement is not supported type:" + variable.getClass().getName() + ", key=" + key_);
            }
        }

        next_.appendSqlBuilder(builder, variableMap, parameterList);
    }

    @Override
    public String toString() {
        return (this.getClass().getSimpleName() + " key:" + key_);
    }
}
