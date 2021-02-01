

@jdoo.modules.Import({ 
    Base.class })

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
package jdoo.addons.web;

import jdoo.addons.web.models.*;