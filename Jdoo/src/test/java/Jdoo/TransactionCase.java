package jdoo;

import org.junit.Before;
import org.junit.After;

import jdoo.apis.Environment;
import jdoo.models.RecordSet;
import jdoo.modules.Loader;
import jdoo.modules.Registry;
import jdoo.util.Kvalues;
import jdoo.data.Database;

public class TransactionCase {
    protected Environment env;

    protected RecordSet env(String model) {
        return env.get(model);
    }

    @Before
    public void setUp() {
        Registry registry = Loader.getRegistry("test");
        Database db = new Database("config/dbcp.properties");
        env = Environment.create(registry, db.cursor(), "uid", new Kvalues(), false);
        // env.cr().setAutoCommit(true);
    }

    @After
    public void tearDown() {
        // env.cr().commit();
    }
}
