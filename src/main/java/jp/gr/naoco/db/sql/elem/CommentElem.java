package jp.gr.naoco.db.sql.elem;

import java.util.List;
import java.util.Map;

import jp.gr.naoco.db.sql.DatabaseBridge;

public class CommentElem implements SqlElem {
    private SqlElem next_ = EoqElem.INSTANCE;

    private String comment_;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public CommentElem(String comment) {
        comment_ = comment;
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
        builder.append(DatabaseBridge.getHintComment(comment_));
        next_.appendSqlBuilder(builder, variableMap, parameterList);
    }

    @Override
    public String toString() {
        return (this.getClass().getSimpleName() + " comment:" + comment_);
    }

    public String getComment() {
        return comment_;
    }
}
