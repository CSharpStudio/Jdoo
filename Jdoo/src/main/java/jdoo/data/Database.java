package jdoo.data;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import jdoo.exceptions.DataException;

public class Database {
    DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataException("get connection error", e);
        }
    }

    public Database(DataSource ds) {
        dataSource = ds;
    }

    public Database(Properties properties) {
        try {
            dataSource = BasicDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            throw new DataException("create datasource error", e);
        }
    }

    public Database(String file) {
        try {
            try (FileInputStream is = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(is);
                dataSource = BasicDataSourceFactory.createDataSource(properties);
            }
        } catch (Exception e) {
            throw new DataException("create datasource error", e);
        }
    }

    public Cursor cursor(){
        return new Cursor(dataSource);
    }
}