package jp.gr.naoco.db.sql.elem;

import java.util.List;
import java.util.Map;

public class EoqElem implements SqlElem {
    public static EoqElem INSTANCE = new EoqElem();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    private EoqElem() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    @Override
    public void setNext(SqlElem next) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SqlElem getNext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendSqlBuilder(StringBuilder builder, Map<String, Object> variableMap, List<Object> parameterList) {
        // nothing to do
    }

    @Override
    public String toString() {
        return (this.getClass().getSimpleName());
    }
}
