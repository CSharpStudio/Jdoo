package com.addons.studio.models;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jdoo.*;

@Model.Meta(name = "studio.designer")
public class Designer extends Model {
    public Designer() {
        isAuto = false;
    }

    @Model.ServiceMethod(label = "查询菜单")
    public List<Map<String, Object>> searchMenu(Records rec, Collection<String> fields, List<Object> criteria) {
        return rec.getEnv().get("ir.ui.menu").search(fields, Criteria.parse(criteria));
    }
}
