package jdoo.base;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;

import jdoo.TransactionCase;
import jdoo.models.Self;
import jdoo.tools.Dict;
import jdoo.tools.Tuple;

public class TestBase extends TransactionCase {
    @Test
    public void test_20_res_partner_address_sync() {
        Self res_partner = env("res.partner");
        Self ghoststep = res_partner.call(Self.class, "create",
                new Dict().set("name", "GhostStep").set("is_company", true).set("street", "Main Street, 10")
                        .set("phone", "123456789").set("email", "info@ghoststep.com").set("vat", "BE0477472701")
                        .set("type", "contact"));
        String id = res_partner.call(new TypeReference<Tuple<String>>() {
        }, "name_create", "Denis Bladesmith <denis.bladesmith@ghoststep.com>").get(0);
        Self p1 = res_partner.browse(id);
        assertEquals("Default type must be \"contact\"", "contact", p1.get("type"));
        String p1phone = "123456789#34";
        p1.call(Self.class, "write", new Dict().set("phone", p1phone).set("parent_id", ghoststep.id()));
        p1.call("flush", null, null);
        // assertEquals("Address fields must be synced", ghoststep.get("street"), p1.get("street"));
        // assertEquals("Phone should be preserved after address sync", p1phone, p1.get("phone"));
        // assertEquals("Type should be preserved after address sync", "contact", p1.get("type"));
        // assertEquals("Email should be preserved after sync", "denis.bladesmith@ghoststep.com", p1.get("email"));
    }
}
