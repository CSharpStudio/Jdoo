package org.jdoo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jdoo.models.TestResUser;
import org.jdoo.core.Environment;
import org.jdoo.core.ModelBuilder;
import org.jdoo.core.Registry;
import org.jdoo.data.Cursor;
import org.jdoo.data.TestCursor;
import org.jdoo.models.M2o_M;
import org.jdoo.models.M2o_O;
import org.jdoo.models.TestFieldTypeModel;

import org.junit.jupiter.api.Test;

public class ModelFieldTest {
    @Test
    public void testFieldType() throws UnsupportedEncodingException {
        Registry reg = new Registry(null);
        Cursor cr = TestCursor.open();
        ModelBuilder builder = new ModelBuilder();
        builder.buildBaseModel(reg);
        builder.buildModel(reg, TestResUser.class, "");
        builder.buildModel(reg, TestFieldTypeModel.class, "");
        reg.setupModels(cr);
        //reg.initModels(cr, reg.getModels().keySet(), true);

        Environment env = new Environment(reg, cr, "",new HashMap<>());
        Map<String, Object> values = new HashMap<>();
        values.put("f_binary", "Binary".getBytes());
        values.put("f_boolean", true);
        values.put("f_char", "char");
        values.put("f_date", Date.valueOf("2022-02-17"));
        values.put("f_datetime", Timestamp.valueOf("2022-02-17 12:15:30"));
        values.put("f_float", 1.2);
        values.put("f_html", "html");
        values.put("f_image", "Image".getBytes());
        values.put("f_int", 1);
        values.put("f_selection", "sel");
        values.put("f_text", "text");
        Records rec = env.get("test.field_type").create(values);

        assertEquals("Binary", new String((byte[]) rec.get("f_binary"), "UTF-8"));
        assertEquals(true, rec.get("f_boolean"));
        assertEquals("char", rec.get("f_char"));
        assertEquals(Date.valueOf("2022-02-17"), rec.get("f_date"));
        assertEquals(Timestamp.valueOf("2022-02-17 12:15:30"), rec.get("f_datetime"));
        assertEquals(1.2, rec.get("f_float"));
        assertEquals("html", rec.get("f_html"));
        assertEquals("Image", new String((byte[]) rec.get("f_image"), "UTF-8"));
        assertEquals(1, rec.get("f_int"));
        assertEquals("sel", rec.get("f_selection"));
        assertEquals("text", rec.get("f_text"));

        rec.getEnv().getCache().invalidate();

        assertEquals("Binary", new String((byte[]) rec.get("f_binary"), "UTF-8"));
        assertEquals(true, rec.get("f_boolean"));
        assertEquals("char", rec.get("f_char"));
        assertEquals(Date.valueOf("2022-02-17"), rec.get("f_date"));
        assertEquals(Timestamp.valueOf("2022-02-17 12:15:30"), rec.get("f_datetime"));
        assertEquals(1.2, rec.get("f_float"));
        assertEquals("html", rec.get("f_html"));
        assertEquals("Image", new String((byte[]) rec.get("f_image"), "UTF-8"));
        assertEquals(1, rec.get("f_int"));
        assertEquals("sel", rec.get("f_selection"));
        assertEquals("text", rec.get("f_text"));
    }

    @Test
    public void testM2oField() throws JsonProcessingException {
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
        for (int i = 0; i < 100; i++) {
            Map<String, Object> one_values = new HashMap<>();
            one_values.put("name", "OneName" + i);
            Records one = env.get("test.m2o_o").create(one_values);
            Map<String, Object> many_values = new HashMap<>();
            many_values.put("name", "ManyName" + i);
            many_values.put("one_id", one.getId());
            Records many = env.get("test.m2o_m").create(many_values);
            new_ids.add(many.getId());
        }
        Object readed = env.get("test.m2o_m", new_ids).read(Arrays.asList("name",
                "one_id"));
        System.out.println(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(readed));
        env.getCache().invalidate();
        for (Records r : env.get("test.m2o_m", new_ids)) {
            Records one = (Records) r.get("one_id");
            System.out.println(one.get("name"));
        }
    }
}
