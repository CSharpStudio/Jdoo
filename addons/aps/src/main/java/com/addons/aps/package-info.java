@org.jdoo.Manifest(name = "aps", depends = "mom", category = "Manufacture", summary = "高级计划排程", models = {
        Item.class,
        PlanTask.class
}, data = {
        "views/plan_task_views.xml",
        "views/item_views.xml"
})
package com.addons.aps;

import com.addons.aps.models.*;
