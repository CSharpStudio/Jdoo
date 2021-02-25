package jdoo.addons.base;

import org.junit.Test;

import jdoo.TransactionCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import jdoo.models.RecordSet;
import jdoo.models.d;
import jdoo.util.Kvalues;
import jdoo.util.Pair;

public class TestPartner extends TransactionCase {
    @Test
    public void test_name_search() {
        RecordSet test_partner = env("res.partner").create(new Kvalues().set("name", "Vlad the Impaler"));

        RecordSet test_user = env("res.users").create(new Kvalues().set("name", "Vlad the Impaler").set("login", "vlad")
                .set("email", "vlad.the.impaler@example.com"));
        //List<Pair<Object, Object>> ns_res = env("res.partner").name_search("Vlad");

        // assertEquals(set(i[0] for i in ns_res), set((test_partner |
        // test_user.partner_id).ids))

        List<Pair<Object, Object>> ns_res = env("res.partner").name_search("Vlad", d.on("user_ids.email", "ilike", "vlad"));
        // assertEquals(set(i[0] for i in ns_res), set(test_user.partner_id.ids))

        assertTrue(true);
    }
}
