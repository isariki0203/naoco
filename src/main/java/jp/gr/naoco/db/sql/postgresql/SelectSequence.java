package jp.gr.naoco.db.sql.postgresql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.gr.naoco.core.NaocoCoreFacade;
import jp.gr.naoco.core.log.LaolLogger;

public class SelectSequence {
    public static String nextval(String sequenceName) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            String sql = new StringBuilder("SELECT NEXTVAL(\'").append(sequenceName).append("\')").toString();
            statement = NaocoCoreFacade.prepareStatement(sql);
            LOG.sql(sql);
            rs = statement.executeQuery();
            rs.next();
            return rs.getString(1);
        } finally {
            if (null != rs) {
                rs.close();
            }
            if (null != statement) {
                statement.close();
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Logger

    private static final LaolLogger LOG = new LaolLogger(SelectSequence.class.getName());
}
