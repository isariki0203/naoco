package jp.gr.naoco.db.sql.mysql;

import java.sql.SQLException;

import jp.gr.naoco.core.log.LaolLogger;

public class SelectSequence {
    public static String nextval(String sequenceName) throws SQLException {
        throw new UnsupportedOperationException("MySQL is not supported sequence.");
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(SelectSequence.class.getName());
}
