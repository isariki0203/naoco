package jp.gr.naoco.db.sql.oracle;

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
            String sql = new StringBuilder("SELECT ").append(sequenceName).append(".NEXTVAL FROM DUAL").toString();
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

    public static String currval(String sequenceName) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            String sql = new StringBuilder("SELECT ").append(sequenceName).append(".CURRVAL FROM DUAL").toString();
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
