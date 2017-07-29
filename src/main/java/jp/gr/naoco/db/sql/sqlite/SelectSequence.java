package jp.gr.naoco.db.sql.sqlite;

import java.sql.SQLException;

import jp.gr.naoco.core.log.LaolLogger;

public class SelectSequence {
    public static String nextval(String sequenceName) throws SQLException {
        throw new UnsupportedOperationException("SQLite is not support sequence.");
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(SelectSequence.class.getName());
}
