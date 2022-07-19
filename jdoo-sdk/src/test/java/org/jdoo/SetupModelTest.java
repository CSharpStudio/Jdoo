package org.jdoo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jdoo.models.TestResUser;
import org.jdoo.core.MetaModel;
import org.jdoo.core.ModelBuilder;
import org.jdoo.core.Registry;
import org.jdoo.data.Cursor;
import org.jdoo.data.TestCursor;
import org.jdoo.models.A;
import org.jdoo.models.A1;
import org.jdoo.models.B;
import org.jdoo.models.C;
import org.jdoo.models.D;
import org.jdoo.models.E;

import org.junit.jupiter.api.Test;

public class SetupModelTest { 
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
        MetaModel c = reg.get("test.a");
        assertFalse(c.getField("code").isReadonly());
        cr.close();
    }

    @Test
    public void testModelExtendInherit(){
        //A -> readonly=false
        //A1 -> readonly=true
        Registry reg = new Registry(null);
        ModelBuilder builder  = new ModelBuilder();
        builder.buildBaseModel(reg);
        builder.buildModel(reg, TestResUser.class, "");
        builder.buildModel(reg, A.class, "");
        builder.buildModel(reg, A1.class, "");
        Cursor cr = TestCursor.open();
        reg.setupModels(cr);
        MetaModel c = reg.get("test.a");
        assertTrue(c.getField("code").isReadonly());
        cr.close();
    }

    @Test
    public void testModelInheritThenExtend(){
        //A -> readonly=false
        //C inherit A
        //A1 -> readonly=true
        Registry reg = new Registry(null);
        ModelBuilder builder  = new ModelBuilder();
        builder.buildBaseModel(reg);
        builder.buildModel(reg, TestResUser.class, "");
        builder.buildModel(reg, A.class, "");
        builder.buildModel(reg, C.class, "");
        builder.buildModel(reg, A1.class, "");
        Cursor cr = TestCursor.open();
        reg.setupModels(cr);
        MetaModel c = reg.get("test.c");
        assertTrue(c.getField("code").isReadonly());
        cr.close();
    }

    @Test
    public void testModelInherits(){
        //A -> readonly=false
        //B -> readonly=true
        //D inherit A,B
        Registry reg = new Registry(null);
        ModelBuilder builder  = new ModelBuilder();
        builder.buildBaseModel(reg);
        builder.buildModel(reg, TestResUser.class, "");
        builder.buildModel(reg, A.class, "");
        builder.buildModel(reg, B.class, "");
        builder.buildModel(reg, D.class, "");
        Cursor cr = TestCursor.open();
        reg.setupModels(cr);
        MetaModel c = reg.get("test.d");
        assertTrue(c.getField("code").isReadonly());
        cr.close();
    }
    

    @Test
    public void testModelInheritMulti(){
        //System.out.println(Long.toString(0, 36));
        //A -> readonly=false
        //C inherit A
        //D inherit C,A
        Registry reg = new Registry(null);
        ModelBuilder builder  = new ModelBuilder();
        builder.buildBaseModel(reg);
        builder.buildModel(reg, TestResUser.class, "");
        builder.buildModel(reg, A.class, "");
        builder.buildModel(reg, C.class, "");
        builder.buildModel(reg, E.class, "");
        Cursor cr = TestCursor.open();
        reg.setupModels(cr);
        MetaModel c = reg.get("test.e");
        assertFalse(c.getField("code").isReadonly());
        cr.close();
    }
}
