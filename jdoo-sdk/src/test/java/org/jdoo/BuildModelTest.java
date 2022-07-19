package org.jdoo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jdoo.core.MetaModel;
import org.jdoo.core.ModelBuilder;
import org.jdoo.core.Registry;
import org.jdoo.models.A;
import org.jdoo.models.A1;
import org.jdoo.models.B;

import org.junit.jupiter.api.Test;

public class BuildModelTest {
    @Test
    public void testBuildModel() {
        Registry reg = new Registry(null);
        ModelBuilder builder = new ModelBuilder();
        builder.buildBaseModel(reg);
        builder.buildModel(reg, A.class, "");
        builder.buildModel(reg, B.class, "");
        builder.buildModel(reg, A1.class, "");

        MetaModel a = reg.get("test.a");
        assertEquals("test.a", a.getName());
        MetaModel b = reg.get("test.b");
        assertEquals("test.b", b.getName());
    }
}