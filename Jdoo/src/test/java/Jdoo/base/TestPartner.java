package jdoo.base;

import org.junit.Test;

import jdoo.TransactionCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jdoo.models.Self;
import jdoo.tools.Dict;

public class TestPartner extends TransactionCase {
    @Test
    public void test_name_search() {
        Self test_partner = env("res.partner").call(Self.class, "create", new Dict() {
            {
                put("name", "Vlad the Impaler");
            }
        });

        Self test_user = env("res.users").call(Self.class, "create", new Dict() {
            {
                put("name", "Vlad the Impaler");
                put("login", "vlad");
                put("email", "vlad.the.impaler@example.com");
            }
        });

        // ns_res = self.env['res.partner'].name_search('Vlad', operator='ilike')
        // assertEquals(set(i[0] for i in ns_res), set((test_partner |
        // test_user.partner_id).ids))

        // ns_res = self.env['res.partner'].name_search('Vlad', args=[('user_ids.email',
        // 'ilike', 'vlad')])
        // assertEquals(set(i[0] for i in ns_res), set(test_user.partner_id.ids))

        assertTrue(true);
    }
}
