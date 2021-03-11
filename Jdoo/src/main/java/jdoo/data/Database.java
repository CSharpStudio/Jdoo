package jdoo.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import jdoo.exceptions.DataException;

public class Database {
    static ConcurrentHashMap<String, Database> databases = new ConcurrentHashMap<String, Database>();

    public static Database get(String tenant) {
        if (!databases.containsKey(tenant)) {
            synchronized (Database.class) {
                if (!databases.containsKey(tenant)) {
                    // db shoud be get from tenant
                    Database db = new Database(tenant, "config/dbcp.properties");
                    databases.put(tenant, db);
                    return db;
                }
            }
        }
        return databases.get(tenant);
    }

    String tenant;
    DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public String getTenant() {
        return tenant;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataException("get connection error", e);
        }
    }

    public Database(String tenant, DataSource ds) {
        this.tenant = tenant;
        dataSource = ds;
    }

    public Database(String tenant, Properties properties) {
        this.tenant = tenant;
        try {
            dataSource = BasicDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            throw new DataException("create datasource error", e);
        }
    }

    public Database(String tenant, String file) {
        this.tenant = tenant;
        try {
            try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(file)) {
                if (is == null) {
                    throw new FileNotFoundException(file);
                }
                Properties properties = new Properties();
                properties.load(is);
                dataSource = BasicDataSourceFactory.createDataSource(properties);
            }
        } catch (Exception e) {
            throw new DataException("create datasource error", e);
        }
    }

    public Cursor cursor() {
        return new Cursor(tenant, dataSource);
    }
}