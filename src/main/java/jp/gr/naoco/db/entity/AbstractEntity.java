package jp.gr.naoco.db.entity;

import java.util.HashSet;

public abstract class AbstractEntity {
    protected HashSet<String> setFieldNameSet_ = new HashSet<String>();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    public AbstractEntity() {
        // nothing to do
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * 指定したカラム名のsetterメソッドが呼び出されているかを判別する
     * 
     * @param propertyName カラム名
     * @return 指定したカラム名について、setterの呼出しがされている場合はtrue、未呼出しの場合はfalse
     */
    public boolean isSetField(String propertyName) {
        return setFieldNameSet_.contains(propertyName.toUpperCase());
    }
}
