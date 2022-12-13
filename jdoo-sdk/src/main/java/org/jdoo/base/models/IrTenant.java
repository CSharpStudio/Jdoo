package org.jdoo.base.models;

import java.util.Arrays;

import org.jdoo.*;
import org.jdoo.data.Cursor;
import org.jdoo.exceptions.ValidationException;
import org.jdoo.tenants.Tenant;
import org.jdoo.tenants.TenantService;

/**
 * 租户
 *
 * @author
 */
@Model.Meta(name = "ir.tenant", label = "租户")
public class IrTenant extends Model {
    static Field name = Field.Char().label("名称");
    static Field tenant = Field.Char().label("租户");
    static Field config = Field.Char().label("配置");

    @Model.ServiceMethod(doc = "重置租户")
    public void reset(Records rec) {
        Tenant tenant = rec.getEnv().getRegistry().getTenant();
        Tenant newTenant = new Tenant(tenant.getKey(), tenant.getName(), tenant.getProperties());
        TenantService.register(newTenant);
    }

    public String getConfig(Records rec, String tenant) {
        Cursor cr = rec.getEnv().getCursor();
        cr.execute("SELECT config FROM ir_tenant WHERE tenant=%s", Arrays.asList(tenant));
        Object[] row = cr.fetchOne();
        if (cr.getRowCount() > 0) {
            return (String) row[0];
        }
        throw new ValidationException(rec.l10n("租户[%s]不存在", tenant));
    }
}
