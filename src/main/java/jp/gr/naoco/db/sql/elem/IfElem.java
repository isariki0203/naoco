package jp.gr.naoco.db.sql.elem;

import java.util.List;
import java.util.Map;

public class IfElem implements SqlElem {
    private String key_;

    private SqlElem next_ = EoqElem.INSTANCE;

    private SqlElem if_ = new DummyElem();

    private SqlElem else_ = new DummyElem();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public IfElem(String key) {
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
        Object value = variableMap.get(key_);
        if ((null == value) || !(value instanceof Boolean)) {
            next_.appendSqlBuilder(builder, variableMap, parameterList);
            return;
        }
        boolean result = ((Boolean) value).booleanValue();
        if (result && (null != if_)) {
            if_.appendSqlBuilder(builder, variableMap, parameterList);
        } else if (null != else_) {
            else_.appendSqlBuilder(builder, variableMap, parameterList);
        }
        next_.appendSqlBuilder(builder, variableMap, parameterList);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName() + " key:" + key_ + "\n");
        builder.append("if:\n");
        SqlElem elem = if_;
        while (!(elem instanceof EoqElem)) {
            builder.append("  " + elem.toString() + "\n");
            elem = elem.getNext();
        }
        builder.append("else:\n");
        elem = else_;
        while (!(elem instanceof EoqElem)) {
            builder.append("  " + elem.toString() + "\n");
            elem = elem.getNext();
        }
        return builder.toString();
    }

    public SqlElem getIf() {
        return if_;
    }

    public void setIf(SqlElem ifelem) {
        if_.setNext(ifelem);
    }

    public SqlElem getElse() {
        return else_;
    }

    public void setElse(SqlElem elseelem) {
        else_.setNext(elseelem);
    }
}
