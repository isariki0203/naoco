package jp.gr.naoco.db.sql.postgresql;

import java.util.HashSet;

public class AvaliableParameterTypes {
    public static final HashSet<Class<?>> TYPE_SET = new HashSet<Class<?>>();
    static {
        TYPE_SET.add(java.math.BigDecimal.class);
        TYPE_SET.add(java.sql.Blob.class);
        TYPE_SET.add(java.sql.Clob.class);
        TYPE_SET.add(java.sql.Date.class);
        TYPE_SET.add(java.util.Date.class);
        TYPE_SET.add(java.lang.Double.class);
        TYPE_SET.add(java.lang.Float.class);
        TYPE_SET.add(java.lang.Integer.class);
        TYPE_SET.add(java.lang.Long.class);
        TYPE_SET.add(java.lang.Short.class);
        TYPE_SET.add(java.lang.String.class);
        TYPE_SET.add(java.sql.Time.class);
        TYPE_SET.add(java.sql.Timestamp.class);
    };
}
