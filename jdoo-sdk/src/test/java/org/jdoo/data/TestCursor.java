package org.jdoo.data;

import java.sql.Connection;
import java.util.Properties;

import org.jdoo.exceptions.ModelException;

import org.springframework.core.io.support.PropertiesLoaderUtils;

public class TestCursor extends Cursor {

    public static TestCursor open() {
        Properties properties;
        try {
            properties = PropertiesLoaderUtils.loadAllProperties("dbcp.properties");
        } catch (Exception e) {
            throw new ModelException("load properties error", e);
        }
        Database db = new Database(properties);
        Connection conn = db.getConnection();
        TestCursor cr = new TestCursor(conn, Database.getSqlDialect(db.getDriver()));
        cr.setAutoCommit(false);
        return cr;
    }

    public TestCursor(Connection conn, SqlDialect dialect) {
        super(conn, dialect);
    }
}
