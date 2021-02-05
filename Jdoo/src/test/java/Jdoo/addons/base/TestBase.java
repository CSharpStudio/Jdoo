package jdoo.addons.base;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import jdoo.TransactionCase;
import jdoo.models.RecordSet;
import jdoo.util.Dict;

public class TestBase extends TransactionCase {
    @Test
    public void test_20_res_partner_address_sync() {
        RecordSet res_partner = env("res.partner");
        RecordSet ghoststep = res_partner.create(
                new Dict().set("name", "GhostStep").set("is_company", true).set("street", "Main Street, 10")
                        .set("phone", "123456789").set("email", "info@ghoststep.com").set("vat", "BE0477472701")
                        .set("type", "contact"));
        Object id = res_partner.name_create("Denis Bladesmith <denis.bladesmith@ghoststep.com>").first();
        RecordSet p1 = res_partner.browse(id);
        assertEquals("Default type must be \"contact\"", "contact", p1.get("type"));
        String p1phone = "123456789#34";
        p1.write(new Dict().set("phone", p1phone).set("parent_id", ghoststep.id()));
        p1.flush();
        assertEquals("Address fields must be synced", ghoststep.get("street"), p1.get("street"));
        assertEquals("Phone should be preserved after address sync", p1phone, p1.get("phone"));
        assertEquals("Type should be preserved after address sync", "contact", p1.get("type"));
        assertEquals("Email should be preserved after sync", "denis.bladesmith@ghoststep.com", p1.get("email"));
    }
}
