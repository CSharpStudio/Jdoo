@org.jdoo.Manifest(name = "base", label = "基础模块", category = "基础模块", models = {
        Unknown.class,
        IrModuleCategory.class,
        IrModuleDependency.class,
        IrModule.class,
        IrModel.class,
        IrModelData.class,
        IrModelField.class,
        IrModelConstraint.class,
        IrHttp.class,
        IrRule.class,
        IrUiView.class,
        IrUiMenu.class,
        ResCompany.class,
        ResLanguage.class,
        ResLocalization.class,
        ResConfig.class,
        RbacUser.class,
        RbacUserLog.class,
        RbacRole.class,
        RbacPermission.class,
        RbacSecurity.class,
        RbacToken.class,
        Policy.class,
        IrAttachment.class,
}, data = {
        "data/res_lang.xml",
        "data/rbac_data.xml",
        "views/web.xml",
        "views/base_menus.xml",
        "views/rbac_views.xml",
        "views/res_lang.xml",
        "views/res_company.xml",
        "views/ir_module_views.xml",
        "views/res_config.xml",
        "views/ir_model_views.xml",
        "views/ir_ui_menu_views.xml",
        "views/ir_ui_view_views.xml"
}, autoInstall = true, application = false)
package org.jdoo.base;

import org.jdoo.base.models.*;