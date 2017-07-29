package jp.gr.naoco.db.sql.elem;

import java.util.List;
import java.util.Map;

public class DummyElem implements SqlElem {
    private SqlElem next_ = EoqElem.INSTANCE;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    protected DummyElem() {
        // nothing to do
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
        next_.appendSqlBuilder(builder, variableMap, parameterList);
    }

    @Override
    public String toString() {
        return (this.getClass().getSimpleName());
    }
}
