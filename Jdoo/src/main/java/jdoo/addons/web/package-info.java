

@jdoo.modules.Import({ 
    Base.class })

@jdoo.modules.Manifest(
    name = "web", 
    version = "1.0", 
    category = "Hidden", 
    description = "Odoo Web core module.\n===================================================\n\nThis module provides the core of the Odoo Web Client.", 
    depends = {"base"}, 
    data = {}, 
    test = {}, 
    auto_install = true,
    bootstrap = true)
package jdoo.addons.web;

import jdoo.addons.web.models.*;