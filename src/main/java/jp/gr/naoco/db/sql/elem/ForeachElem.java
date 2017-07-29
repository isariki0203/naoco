package jp.gr.naoco.db.sql.elem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForeachElem implements SqlElem {
    private String key_;

    private SqlElem next_ = EoqElem.INSTANCE;

    private SqlElem body_ = new DummyElem();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public ForeachElem(String key) {
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
        Object obj = variableMap.get(key_);
        if ((null != obj) && (obj instanceof Iterable)) {
            Iterable<? extends Object> ite = (Iterable<? extends Object>) obj;
            for (Object elem : ite) {
                Map<String, Object> newMap = new HashMap<String, Object>();
                if (elem instanceof Map) {
                    newMap.putAll((Map<? extends String, ? extends Object>) elem);
                }
                body_.appendSqlBuilder(builder, newMap, parameterList);
            }
        }
        if (!isLast()) {
            next_.appendSqlBuilder(builder, variableMap, parameterList);
        }
    }

    public SqlElem getBody() {
        return body_;
    }

    public void setBody(SqlElem body) {
        body_.setNext(body);
    }
}
