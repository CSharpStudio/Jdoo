package org.jdoo.data;

import java.util.Properties;

import org.jdoo.exceptions.ModelException;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class DatabaseTest {

    @Test
    public void connection() {
        Properties properties;
        try {
            properties = PropertiesLoaderUtils.loadAllProperties("dbcp.properties");
        } catch (Exception e) {
            throw new ModelException("load properties error", e);
        }
        Database db = new Database(properties);
        Cursor cr = db.openCursor();
        cr.close();
    }

    // @Test
    // public void testParams() {
    //     Properties properties;
    //     try {
    //         properties = PropertiesLoaderUtils.loadAllProperties("dbcp.properties");
    //     } catch (Exception e) {
    //         throw new ModelException("load properties error", e);
    //     }
    //     Database db = new Database(properties);
    //     Cursor cr = db.openCursor();
    //     try {
    //         PreparedStatement statement = cr.connection.prepareStatement("SELECT * from test_model where id in (?)");
    //         statement.setObject(1, 1);
    //         boolean res = statement.execute();
    //         if (res) {
    //             ResultSet resultSet = statement.getResultSet();
    //         } else {
    //         }
    //     } catch (Exception e) {
    //         throw new DataException("执行SQL失败:", e);
    //     }
    // }
}
