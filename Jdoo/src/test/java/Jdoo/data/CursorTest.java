package jdoo.data;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jdoo.tools.Tuple;

public class CursorTest {
    Database db;
    String dbName;

    @Before
    public void setUp() {
        dbName = "z_db_" + UUID.randomUUID().toString().replace("-", "");
        createDb(dbName);
        try {
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl("jdbc:postgresql://192.168.175.123:5432/" + dbName);
            ds.setUsername("postgres");
            ds.setPassword("sie123456");
            db = new Database(ds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        dropDb(dbName);
    }

    String Uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    void dropDb(String db) {
        try {
            Class.forName("org.postgresql.Driver");
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl("jdbc:postgresql://192.168.175.123:5432/db_test");
            ds.setUsername("postgres");
            ds.setPassword("sie123456");
            Connection connection = ds.getConnection();
            PreparedStatement statement = connection.prepareStatement("drop database " + db);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void createDb(String db) {
        try {
            Class.forName("org.postgresql.Driver");
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl("jdbc:postgresql://192.168.175.123:5432/db_test");
            ds.setUsername("postgres");
            ds.setPassword("sie123456");
            Connection connection = ds.getConnection();
            PreparedStatement statement = connection.prepareStatement("create database " + db);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void executeTest() {
        try (Cursor cr = db.cursor()) {
            cr.execute("CREATE TABLE res_users (							          \n"
                    + "    id varchar NOT NULL,                                        \n"
                    + "    active boolean default True,                               \n"
                    + "    login varchar(64) NOT NULL UNIQUE,                         \n"
                    + "    password varchar default null,                             \n"
                    + "    -- No FK references below, will be added later by ORM      \n"
                    + "    -- (when the destination rows exist)                       \n"
                    + "    company_id integer, -- references res_company,             \n"
                    + "    partner_id integer, -- references res_partner,             \n"
                    + "    create_date timestamp without time zone,                   \n"
                    + "    primary key(id)                                            \n" + ");");

            cr.execute("insert into res_users(id, login, create_date) values(%s,%s,%s)",
                    new Tuple<>("1", "1", new Date()));
            cr.execute("insert into res_users(id, login, create_date) values(%s,%s,%s)",
                    new Tuple<>("2", "2", new Date()));
            cr.execute("insert into res_users(id, login, create_date) values(%s,%s,%s)",
                    new Tuple<>("3", "3", new Date()));
            cr.execute("insert into res_users(id, login, create_date) values(%s,%s,%s)",
                    new Tuple<>("4", "4", new Date()));
            cr.commit();
            cr.execute("select * from res_users where id = %s", new Tuple<>("1"));
            assertTrue(cr.rowcount > 0);

            cr.execute("select * from res_users where id in %s", new Tuple<>(new Tuple<>("1", "2", "3", "4")));
            Tuple<?> one = cr.fetchone();
            assertTrue(one.size() > 0);
            List<Tuple<?>> many = cr.fetchmany(2);
            assertTrue(many.size() > 0);
            List<Tuple<?>> all = cr.fetchall();
            assertTrue(all.size() > 0);
        }
    }
}
