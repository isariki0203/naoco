package jp.gr.naoco.db.sql;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;

import jp.gr.naoco.core.NaocoCoreFacade;

public class DatabaseBridge {
    private static HashMap<String, DBElem> MAP = new HashMap<String, DBElem>();
    static {
        // Oracle
        MAP.put("Oracle JDBC driver", new DBElem() {
            @Override
            public HashSet<Class<?>> getAvaliableParameterTypes() {
                return jp.gr.naoco.db.sql.oracle.AvaliableParameterTypes.TYPE_SET;
            }

            @Override
            public String getHintComment(String comment) {
                if (comment.startsWith("+")) {
                    return "/*" + comment.trim() + " */ ";
                }
                return "";
            }

            @Override
            public void setNull(PreparedStatement statement, int index) throws SQLException {
                statement.setNull(index, Types.NULL);
            }

            @Override
            public String nextSequence(String sequenceName) throws SQLException {
                return jp.gr.naoco.db.sql.oracle.SelectSequence.nextval(sequenceName);
            }
        });
        // Postgresql
        MAP.put("PostgreSQL JDBC Driver", new DBElem() {
            @Override
            public HashSet<Class<?>> getAvaliableParameterTypes() {
                return jp.gr.naoco.db.sql.postgresql.AvaliableParameterTypes.TYPE_SET;
            }

            @Override
            public void setNull(PreparedStatement statement, int index) throws SQLException {
                statement.setNull(index, Types.NULL);
            }

            @Override
            public String nextSequence(String sequenceName) throws SQLException {
                return jp.gr.naoco.db.sql.postgresql.SelectSequence.nextval(sequenceName);
            }
        });
        // MySQL
        MAP.put("MySQL-AB JDBC Driver", new DBElem() {
            @Override
            public HashSet<Class<?>> getAvaliableParameterTypes() {
                return jp.gr.naoco.db.sql.mysql.AvaliableParameterTypes.TYPE_SET;
            }

            @Override
            public void setNull(PreparedStatement statement, int index) throws SQLException {
                statement.setNull(index, Types.NULL);
            }

            @Override
            public String nextSequence(String sequenceName) throws SQLException {
                return jp.gr.naoco.db.sql.mysql.SelectSequence.nextval(sequenceName);
            }
        });
        // HiRDB（対象外）
        MAP.put("JP.co.Hitachi.soft.HiRDB.JDBC.HiRDBDriver", new DBElem());
        // SQL Server（対象外）
        MAP.put("com.microsoft.sqlserver.jdbc.SQLServerDriver", new DBElem());
        // Firebird（対象外）
        MAP.put("org.firebirdsql.jdbc.FBDriver", new DBElem());
        // DB2（対象外）
        MAP.put("com.ibm.db2.jcc.DB2Driver", new DBElem());
        // Symfoware（対象外）
        MAP.put("com.fujitsu.symfoware.jdbc.SYMDriver", new DBElem());
        // HSQLDB
        MAP.put("HSQL Database Engine Driver", new DBElem() {
            @Override
            public HashSet<Class<?>> getAvaliableParameterTypes() {
                return jp.gr.naoco.db.sql.hsqldb.AvaliableParameterTypes.TYPE_SET;
            }

            @Override
            public void setNull(PreparedStatement statement, int index) throws SQLException {
                statement.setNull(index, Types.NULL);
            }

            @Override
            public String nextSequence(String sequenceName) throws SQLException {
                return jp.gr.naoco.db.sql.hsqldb.SelectSequence.nextval(sequenceName);
            }
        });
        // Derby（対象外）
        MAP.put("org.apache.derby.jdbc.EmbeddedDriver", new DBElem());
        // H2（対象外）
        MAP.put("org.h2.Driver", new DBElem());
        // Sybase（対象外）
        MAP.put("com.sybase.jdbc3.jdbc.SybDriver", new DBElem());
        // SQLite
        MAP.put("SQLiteJDBC", new DBElem() {
            @Override
            public HashSet<Class<?>> getAvaliableParameterTypes() {
                return jp.gr.naoco.db.sql.sqlite.AvaliableParameterTypes.TYPE_SET;
            }

            @Override
            public void setNull(PreparedStatement statement, int index) throws SQLException {
                statement.setNull(index, Types.NULL);
            }

            @Override
            public String nextSequence(String sequenceName) throws SQLException {
                return jp.gr.naoco.db.sql.sqlite.SelectSequence.nextval(sequenceName);
            }
        });
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    public static HashSet<Class<?>> getAvaliableParameterTypes() {
        String driverName = NaocoCoreFacade.getDriverName();
        DBElem elem = MAP.get(driverName);
        if (null == elem) {
            throw new IllegalArgumentException("Not configured driver name in DatabaseBridge:" + driverName);
        }
        HashSet<Class<?>> result = elem.getAvaliableParameterTypes();
        if (null == result) {
            throw new UnsupportedOperationException("Not supported db type:" + driverName);
        }
        return result;
    }

    public static String getHintComment(String comment) {
        String driverName = NaocoCoreFacade.getDriverName();
        DBElem elem = MAP.get(driverName);
        if (null == elem) {
            throw new IllegalArgumentException("Not configured driver name in DatabaseBridge:" + driverName);
        }
        return elem.getHintComment(comment);
    }

    public static void setNull(PreparedStatement statement, int index) throws SQLException {
        String driverName = NaocoCoreFacade.getDriverName();
        DBElem elem = MAP.get(driverName);
        if (null == elem) {
            throw new IllegalArgumentException("Not configured driver name in DatabaseBridge:" + driverName);
        }
        elem.setNull(statement, index);
    }

    public static String sequence(String sequenceName) throws SQLException {
        String driverName = NaocoCoreFacade.getDriverName();
        DBElem elem = MAP.get(driverName);
        return elem.nextSequence(sequenceName);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    private static class DBElem {
        public HashSet<Class<?>> getAvaliableParameterTypes() {
            return null;
        }

        public String getHintComment(String comment) {
            return "";
        }

        public void setNull(PreparedStatement statement, int index) throws SQLException {
            ParameterMetaData meta = statement.getParameterMetaData();
            int sqlType = meta.getParameterType(index);
            statement.setNull(index, sqlType);
        }

        public String nextSequence(String sequenceName) throws SQLException {
            throw new UnsupportedOperationException();
        }
    }
}
