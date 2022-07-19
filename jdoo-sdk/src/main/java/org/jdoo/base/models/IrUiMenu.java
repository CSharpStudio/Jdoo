package org.jdoo.base.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.jdoo.*;
import org.jdoo.core.Constants;
import org.jdoo.fields.RelationalField.DeleteMode;
import org.jdoo.util.KvMap;
import org.jdoo.utils.StringUtils;

@Model.Meta(name = "ir.ui.menu", description = "菜单", order = "sequence, id")
public class IrUiMenu extends Model {
    static Field name = Field.Char().label("菜单").required().translate();
    static Field active = Field.Boolean().defaultValue(Default.value(true));
    static Field sequence = Field.Integer().defaultValue(Default.value(0));
    static Field child_ids = Field.One2many("ir.ui.menu", "parent_id").label("子菜单");
    static Field parent_id = Field.Many2one("ir.ui.menu").label("父菜单").index().ondelete(DeleteMode.Restrict);
    static Field icon = Field.Char().label("图标");
    static Field url = Field.Char().label("地址");
    static Field model = Field.Char().label("模型");
    static Field view = Field.Char().label("视图");
    static Field click = Field.Char().label("点击事件");
    static Field css = Field.Char().label("样式");

    @SuppressWarnings("unchecked")
    @Model.ServiceMethod(records = false, doc = "加载菜单", auth = Constants.ANONYMOUS)
    public Map<String, Object> loadMenu(Records rec) {
        // TODO 按用户权限加载
        KvMap menus = new KvMap();
        List<String> root = new ArrayList<>();
        boolean isAdmin = rec.getEnv().isAdmin();
        List<String> authModels = (List<String>) rec.getEnv().get("rbac.permission").call("loadAuthModels");
        rec = rec.find(new Criteria());
        for (Records r : rec) {
            KvMap menu = new KvMap();
            // TODO action
            String url = (String) r.get("url");
            if (StringUtils.isEmpty(url)) {
                String model = (String) r.get("model");
                if (StringUtils.isNotEmpty(model)) {
                    if (!isAdmin && !authModels.contains(model)) {
                        continue;
                    }
                    url = "{tenant}/view#menu=" + r.getId() + "&model=" + model + "&view="
                            + ((String) r.get("view"));
                }
            }
            if (StringUtils.isNotEmpty(url)) {
                url = url.replace("{tenant}", rec.getEnv().getRegistry().getTenant().getKey());
                menu.put("url", url);
            }
            putMenu(menu, r, "name");
            putMenu(menu, r, "icon");
            putMenu(menu, r, "click");
            putMenu(menu, r, "css");
            Records sub = (Records) r.get("child_ids");
            if (sub.any()) {
                menu.put("sub", sub.getIds());
            }
            Records parent = (Records) r.get("parent_id");
            if (!parent.any()) {
                root.add(r.getId());
            }
            menus.put(r.getId(), menu);
            menus.put("root", root);
        }
        return menus;
    }

    void putMenu(KvMap menu, Records rec, String field) {
        Object value = rec.get(field);
        if (value instanceof String) {
            if (StringUtils.isNotEmpty((String) value)) {
                menu.put(field, value);
            }
        }
    }
}
