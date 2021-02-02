@jdoo.modules.Import({ 
    Base.class,
    Unknown.class,
    IrModel.class,
    IrModelAccess.class, 
    IrDefault.class, 
    PartnerCategory.class, 
    Partner.class, 
    ResBank.class,
    ResUsers.class })
@jdoo.modules.Manifest(
    name = "base", 
    version = "1.3", 
    category = "Hidden", 
    description = "The kernel of Odoo, needed for all installation.\n===================================================", 
    depends = {}, 
    data = {}, 
    test = {}, 
    installable = true, 
    auto_install = true)
package jdoo.addons.base;

import jdoo.addons.base.models.*;