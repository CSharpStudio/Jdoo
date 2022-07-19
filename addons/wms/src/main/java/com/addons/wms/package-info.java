@org.jdoo.Manifest(name = "wms", category = "Manufacture", summary = "仓库管理", models = {
        Item.class,
        ReceiveSendControl.class,
        Rohs.class,
        Storage.class
}, data = {
        "data/rohs_data.xml",
        "data/storage_data.xml",
        "views/storage_views.xml",
        "views/item_views.xml"
})
package com.addons.wms;

import com.addons.wms.models.*;
