package org.jdoo.base.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdoo.*;
import org.jdoo.core.Constants;
import org.jdoo.data.Cursor;
import org.jdoo.exceptions.ValidationException;
import org.jdoo.util.KvMap;
import org.jdoo.utils.StringUtils;

@Model.Meta(name = "ir.ui.menu", label = "菜单", order = "sequence, id")
public class IrUiMenu extends Model {
    static Field name = Field.Char().label("菜单").required().translate();
    static Field active = Field.Boolean().defaultValue(true).label("是否生效");
    static Field sequence = Field.Integer().defaultValue(16).label("显示顺序");
    static Field child_ids = Field.One2many("ir.ui.menu", "parent_id").label("子菜单");
    static Field parent_id = Field.Many2one("ir.ui.menu").label("父菜单").index().ondelete(DeleteMode.Restrict);
    static Field icon = Field.Char().label("图标");
    static Field url = Field.Char().label("地址");
    static Field model = Field.Char().label("模型");
    static Field view = Field.Char().label("视图");
    static Field click = Field.Char().label("点击事件");
    static Field css = Field.Char().label("样式");
    static Field role_ids = Field.Many2many("rbac.role", "rbac_role_menu", "menu", "role");

    /**
     * 按用户权限加载菜单
     * 
     * @param rec
     * @return
     */
    @SuppressWarnings("unchecked")
    @Model.ServiceMethod(records = false, doc = "加载菜单", auth = Constants.ANONYMOUS)
    public Map<String, Object> loadMenu(Records rec) {
        KvMap menus = new KvMap();
        List<String> root = new ArrayList<>();
        boolean isAdmin = rec.getEnv().isAdmin();
        List<String> authModels = (List<String>) rec.getEnv().get("rbac.permission").call("loadAllowReadModels");
        List<String> authMenus = loadAuthMenus(rec);
        rec = rec.find(Criteria.equal("active", true));
        for (Records r : rec) {
            Records parent = (Records) r.get("parent_id");
            if (!parent.any()) {
                addMenu(menus, r, isAdmin, authModels, authMenus);
                String id = r.getId();
                if (menus.containsKey(id)) {
                    root.add(r.getId());
                }
            }
        }
        menus.put("root", root);
        return menus;
    }

    /**
     * 加载已授权的菜单id列表
     * 
     * @param rec
     * @return
     */
    public List<String> loadAuthMenus(Records rec) {
        Cursor cr = rec.getEnv().getCursor();
        String sql = "SELECT DISTINCT m.menu FROM rbac_role_menu m"
                + " JOIN rbac_role_user u on m.role=u.role"
                + " JOIN rbac_role r on m.role=r.id"
                + " WHERE u." + cr.quote("user") + "=%s AND r.active=%s";
        cr.execute(sql, Arrays.asList(rec.getEnv().getUserId(), true));
        return cr.fetchAll().stream().map(o -> (String) o[0]).collect(Collectors.toList());
    }

    @Model.Constrains({ "model", "url" })
    public void checkModelAndUrl(Records rec) {
        for (Records r : rec) {
            String model = (String) r.get("model");
            String url = (String) r.get("url");
            if (StringUtils.isNotEmpty(model) && StringUtils.isNotEmpty(url)) {
                throw new ValidationException(rec.l10n("模型与地址不能同时赋值"));
            }
        }
    }

    KvMap toMap(Records rec, List<String> fields) {
        KvMap menu = new KvMap();
        for (String field : fields) {
            Object value = rec.get(field);
            if (value instanceof String) {
                if (StringUtils.isNotEmpty((String) value)) {
                    menu.put(field, value);
                }
            }
        }
        return menu;
    }

    @SuppressWarnings("unchecked")
    boolean addMenu(KvMap menus, Records rec, boolean isAdmin, List<String> authModels, List<String> authMenus) {
        boolean result = false;
        String url = (String) rec.get("url");
        String click = (String) rec.get("click");
        String model = (String) rec.get("model");
        if (StringUtils.isNotEmpty(model)) {
            if (isAdmin || authModels.contains(model)) {
                String[] v = ((String) rec.get("view")).split("\\|");
                url = "/{tenant}/view#menu=" + rec.getId() + "&model=" + model + "&view=" + v[0];
                if (v.length > 1) {
                    url += "&key=" + v[1];
                }
                result = true;
            }
        } else if (StringUtils.isNotEmpty(url) || StringUtils.isNotEmpty(click)) {
            if (isAdmin || authMenus.contains(rec.getId())) {
                result = true;
            }
        }

        KvMap menu = toMap(rec, Arrays.asList("name", "icon", "click", "css"));
        if (StringUtils.isNotEmpty(url)) {
            url = url.replace("{tenant}", rec.getEnv().getRegistry().getTenant().getKey());
            menu.put("url", url);
        }

        Records child = rec.getRel("child_ids").filter(m -> Boolean.TRUE.equals(m.get("active")));
        for (Records c : child) {
            if (addMenu(menus, c, isAdmin, authModels, authMenus)) {
                List<String> sub = (List<String>) menu.get("sub");
                if (sub == null) {
                    sub = new ArrayList<String>();
                    menu.put("sub", sub);
                }
                sub.add(c.getId());
                result = true;
            }
        }
        if (result) {
            menus.put(rec.getId(), menu);
        }
        return result;
    }
}
