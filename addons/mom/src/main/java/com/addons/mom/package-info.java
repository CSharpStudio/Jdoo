@org.jdoo.Manifest(name = "mom", category = "Manufacture", label = "制造", models = {
        Item.class,
        WorkOrder.class
}, data = {
        "views/item_views.xml",
        "views/work_order_views.xml",
})
package com.addons.mom;

import com.addons.mom.models.*;
