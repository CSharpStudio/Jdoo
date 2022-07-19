package org.jdoo.base.models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jdoo.*;
import org.jdoo.data.Cursor;

/**
 * 角色
 * 
 * @author lrz
 */
@Model.Meta(name = "rbac.role", description = "角色")
public class Role extends Model {
    static Field name = Field.Char().label("名称");
    static Field user_ids = Field.Many2many("rbac.user", "rbac_role_user", "role", "user");
    static Field permission_ids = Field.Many2many("rbac.permission", "rbac_role_permission", "role", "permission");
    static Field is_admin = Field.Boolean().label("是否管理员").readonly();

    @ServiceMethod(label = "保存权限")
    public void savePermission(Records rec, List<String> permissions) {
        Records p = rec.getEnv().get("rbac.permission", permissions);
        rec.set("permission_ids", p);
    }

    // public Records getModelAuthorization(Records rec, String uid, String model) {
    //     Cursor cr = rec.getEnv().getCursor();
    //     String sql = "SELECT DISTINCT rp.permission FROM rbac_role_permission rp"
    //             + " JOIN rbac_role_user ru on rp.role=ru.role"
    //             + " WHERE ru.user=%s";
    //     cr.execute(sql, Arrays.asList(uid));
    //     List<Object[]> rows = cr.fetchAll();
    //     List<String> ids = rows.stream().map(row -> (String) row[0]).collect(Collectors.toList());
    //     return rec.getEnv().get("rbac.permission", ids);
    // }
}
