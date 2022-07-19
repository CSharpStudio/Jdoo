@org.jdoo.Manifest(name = "base", category = "base", models = {
        Unknown.class,
        IrModuleCategory.class,
        IrModuleDependency.class,
        IrModule.class,
        IrModel.class,
        IrModelData.class,
        IrField.class,
        IrHttp.class,
        IrUiView.class,
        IrUiMenu.class,
        User.class,
        UserLog.class,
        Role.class,
        Permission.class,
        Security.class,
        Token.class,
        Settings.class
}, data = {
        "data/rbac_data.xml",
        "views/web.xml",
        "views/base_menus.xml",
        "views/rbac_views.xml",
        "views/ir_module_views.xml",
        "views/res_config_settings.xml",
        "views/ir_ui_menu_views.xml",
        "views/ir_ui_view_views.xml",
        "views/ir_model_views.xml"
}, autoInstall = true)
package org.jdoo.base;

import org.jdoo.base.models.*;
