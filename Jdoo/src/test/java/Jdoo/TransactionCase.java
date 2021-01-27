package jdoo;

import org.junit.Before;

import jdoo.apis.Environment;
import jdoo.base.__init__;
import jdoo.data.Cursor;
import jdoo.models.Self;
import jdoo.modules.Registry;
import jdoo.tools.Dict;
import jdoo.data.Database;

public class TransactionCase {    
    protected Environment env;
    protected Self env(String model){
        return env.get(model);
    }
    @Before
    public void setUp(){
        Registry registry = new Registry("test");
        __init__.init(registry);
        Cursor cr = new Database("config/dbcp.properties").cursor();
        env = Environment.create(registry, cr, "uid", new Dict(), false);
    }
}
