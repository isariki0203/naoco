package jp.gr.naoco.sample.dummy;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;

import jp.gr.naoco.core.connection.ConnectionFactory;

public class DummyConnectionFactory implements ConnectionFactory {
    @Override
    public Connection create(String lookupName) throws SQLException, ClassNotFoundException, NamingException {
        // System.out.println("DummyConnectionFactory#create:" + lookupName);
        // TODO 自動生成されたメソッド・スタブ
        return new DummyConnection(lookupName);
    }
}
