package org.jdoo;

import org.jdoo.models.TestResUser;
import org.jdoo.core.ModelBuilder;
import org.jdoo.core.Registry;
import org.jdoo.data.Cursor;
import org.jdoo.data.TestCursor;
import org.jdoo.models.A;

import org.junit.jupiter.api.Test;

public class InitModelTest { 
    @Test
    public void testModelField(){
        //A -> readonly=false
        Registry reg = new Registry(null);
        ModelBuilder builder  = new ModelBuilder();
        builder.buildBaseModel(reg);
        builder.buildModel(reg, TestResUser.class, "");
        builder.buildModel(reg, A.class, "");
        Cursor cr = TestCursor.open();
        reg.setupModels(cr);
        reg.initModels(cr, reg.getModels().keySet());
        //cr.commit();
    }
}
