package jdoo;

import org.junit.Before;

import jdoo.apis.Environment;
import jdoo.base.__init__;
import jdoo.data.Cursor;
import jdoo.models.Self;
import jdoo.modules.Registry;
import jdoo.tools.Dict;

public class TransactionCase {    
    protected Environment env;
    protected Self env(String model){
        return env.get(model);
    }
    @Before
    public void setUp(){
        Registry registry = new Registry();
        __init__.init(registry);
        Cursor cr = new Cursor();
        env = Environment.create("key", registry, cr, "uid", new Dict(), false);
    }
}
