package org.jdoo.data;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;

import org.jdoo.data.mysql.MySqlDialect;
import org.jdoo.data.postgresql.PgSqlDialect;
import org.jdoo.exceptions.DataException;
import org.jdoo.data.oracle.OracleDialect;
//import org.apache.commons.dbcp.BasicDataSourceFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;

/**
 * 数据库配置
 * 
 * @author lrz
 */
public class Database {
    static Map<String, SqlDialect> sqlDialects = new HashMap<>();

    public static void addSqlDialect(String provider, SqlDialect dialect) {
        sqlDialects.put(provider, dialect);
    }

    public static SqlDialect getSqlDialect(String provider) {
        SqlDialect dialect = sqlDialects.get(provider);
        if (dialect == null) {
            throw new DataException(String.format("未注册提供者[%s]的SqlDialect", provider));
        }
        return dialect;
    }

    static {
        sqlDialects.put("oracle.jdbc.OracleDriver", new OracleDialect());
        sqlDialects.put("org.postgresql.Driver", new PgSqlDialect());
        sqlDialects.put("com.mysql.cj.jdbc.Driver", new MySqlDialect());
        sqlDialects.put("com.mysql.jdbc.Driver", new MySqlDialect());        
    }

    DataSource dataSource;
    String driver;

    public String getDriver(){
        return driver;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataException("获取数据库连接失败", e);
        }
    }

    public Database(Properties properties) {
        try {
            driver = properties.getProperty("driverClassName");
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataException("创建DataSource失败", e);
        }
    }

    public Database(String file) {
        try {
            try (FileInputStream is = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(is);
                driver = properties.getProperty("driverClassName");
                dataSource = DruidDataSourceFactory.createDataSource(properties);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataException("创建DataSource失败", e);
        }
    }

    public void close() {
        if (dataSource instanceof DruidDataSource) {
            ((DruidDataSource) dataSource).close();
        }
    }

    public Cursor openCursor() {
        Connection conn = getConnection();
        return new Cursor(conn, getSqlDialect(driver));
    }
}