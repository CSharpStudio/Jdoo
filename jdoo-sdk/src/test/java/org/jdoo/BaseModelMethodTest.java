package org.jdoo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdoo.core.Environment;
import org.jdoo.core.ModelBuilder;
import org.jdoo.core.Registry;
import org.jdoo.data.Cursor;
import org.jdoo.data.TestCursor;
import org.jdoo.models.M2o_M;
import org.jdoo.models.M2o_O;
import org.jdoo.models.TestResUser;
import org.jdoo.models.TestModel;

import org.junit.jupiter.api.Test;

public class BaseModelMethodTest {
    void buildTestModel(Cursor cr, Registry reg) {
        ModelBuilder builder = new ModelBuilder();
        builder.buildBaseModel(reg);
        builder.buildModel(reg, TestResUser.class, "");
        builder.buildModel(reg, TestModel.class, "");
        reg.setupModels(cr);
        //reg.initModels(cr, reg.getModels().keySet(), true);
    }

    @Test
    public void testCreateModel() {

        Registry reg = new Registry(null);
        Cursor cr = TestCursor.open();
        buildTestModel(cr, reg);

        Environment env = new Environment(reg, cr, "", new HashMap<>());
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Name");
        values.put("description", "Description");
        values.put("inherit", "Inherit");
        values.put("table", "Table");
        values.put("order", "Order");
        Records rec = env.get("test.model").create(values);

        assertEquals("Name", rec.get("name"));
        assertEquals("Description", rec.get("description"));
        assertEquals("Inherit", rec.get("inherit"));
        assertEquals("Table", rec.get("table"));
        assertEquals("Order", rec.get("order"));

        List<Map<String, Object>> val_list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> v = new HashMap<>();
            v.put("id", "id" + i);
            v.put("name", "Name" + i);
            v.put("description", "Description" + i);
            v.put("inherit", "Inherit" + i);
            v.put("table", "Table" + i);
            v.put("order", "Order" + i);
            val_list.add(v);
        }
        Records batch = env.get("test.model").createBatch(val_list);
        assertEquals(5, batch.size());

        cr.close();
    }

    @Test
    public void testReadModel() {

        Registry reg = new Registry(null);
        Cursor cr = TestCursor.open();
        buildTestModel(cr, reg);

        Environment env = new Environment(reg, cr, "", new HashMap<>());
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Name");
        values.put("description", "Description");
        values.put("inherit", "Inherit");
        values.put("table", "Table");
        values.put("order", "Order");
        Records saved = env.get("test.model").create(values);

        env.getCache().invalidate();
        Records rec = env.get("test.model").browse(saved.getId());
        TestModel tm = rec.as(TestModel.class);
        String name = tm.get_name();
        System.out.println(name);

        String type_name = tm.get_type_name();
        System.out.println(type_name);

        cr.close();
    }

    @Test
    public void testSearchModel() {
        Registry reg = new Registry(null);
        Cursor cr = TestCursor.open();
        buildTestModel(cr, reg);

        Environment env = new Environment(reg, cr, "", new HashMap<>());
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Name");
        values.put("description", "Description");
        values.put("inherit", "Inherit");
        values.put("table", "Table");
        values.put("order", "Order");
        Records saved = env.get("test.model").create(values);

        env.getCache().invalidate();

        Records search = saved.find(Criteria.equal("name", "Name"), 0, 0, "order desc");

        System.out.println(search.getId());

        cr.close();
    }

    @Test
    public void testM2oSearchModel() {

        Registry reg = new Registry(null);
        Cursor cr = TestCursor.open();
        ModelBuilder builder = new ModelBuilder();
        builder.buildBaseModel(reg);
        builder.buildModel(reg, TestResUser.class, "");
        builder.buildModel(reg, M2o_M.class, "");
        builder.buildModel(reg, M2o_O.class, "");
        reg.setupModels(cr);
        //reg.initModels(cr, reg.getModels().keySet(), true);

        Environment env = new Environment(reg, cr, "", new HashMap<>());
        List<String> new_ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> one_values = new HashMap<>();
            one_values.put("name", "OneName" + i);
            Records one = env.get("test.m2o_o").create(one_values);
            Map<String, Object> many_values = new HashMap<>();
            many_values.put("name", "ManyName" + i);
            many_values.put("one_id", one.getId());
            Records many = env.get("test.m2o_m").create(many_values);
            new_ids.add(many.getId());
        }

        Records search = env.get("test.m2o_m").find(
                Criteria.like("one_id.name", "OneName").or(Criteria.equal("one_id", "01lt7x8lwt4ht")), 0, 0,
                "");

        for (Records r : search)
            System.out.println(r.getId());

        cr.close();
    }

    @Test
    public void testUpdateModel() {
        Registry reg = new Registry(null);
        Cursor cr = TestCursor.open();
        buildTestModel(cr, reg);

        Environment env = new Environment(reg, cr, "", new HashMap<>());
        List<Map<String, Object>> val_list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> v = new HashMap<>();
            v.put("id", "id" + i);
            v.put("name", "Name" + i);
            v.put("description", "Description" + i);
            v.put("inherit", "Inherit" + i);
            v.put("table", "Table" + i);
            v.put("order", "Order" + i);
            val_list.add(v);
        }
        Records batch = env.get("test.model").createBatch(val_list);
        for (TestModel m : batch.of(TestModel.class)) {
            m.set_name(m.get_name() + " - NEW");
            m.set_type("char");
        }
        System.out.println(batch.getEnv().getToUpdate().getModels());
        batch.flush();

        batch.update(new HashMap<String, Object>() {
            {
                put("name", "Name NEW");
                put("table", "Tabel New");
            }
        });
        System.out.println(batch.getEnv().getToUpdate().getModels());
        batch.flush();

        batch.load(new HashMap<String, Object>() {
            {
                put("name", "Name NEW load");
                put("table", "Tabel New");
            }
        });
        System.out.println(batch.getEnv().getToUpdate().getModels());

        batch.flush();

        System.out.println(batch.getEnv().getToUpdate().getModels());

        cr.close();
    }
}
