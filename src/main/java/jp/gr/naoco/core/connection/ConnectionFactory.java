package jp.gr.naoco.core.connection;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;

/**
 * DBコネクションを取得する
 */
public interface ConnectionFactory {
    public Connection create(String lookupName) throws SQLException, ClassNotFoundException, NamingException;
}
