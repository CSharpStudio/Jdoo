@jdoo.modules.Import({ //
        Base.class, //
        Unknown.class, //
        IrModel.class, //
        IrModelAccess.class, //
        IrDefault.class, //
        Country.class, //
        CountryGroup.class, //
        CountryState.class, //
        PartnerCategory.class, //
        PartnerTitle.class, //
        Partner.class, //
        ResPartnerIndustry.class, //
        ResBank.class, //
        ResPartnerBank.class, //
        Currency.class, //
        CurrencyRate.class, //
        Company.class, //
        Groups.class, //
        ResUsersLog.class, //
        ResUsers.class })
@jdoo.modules.Manifest(//
        name = "base", //
        version = "1.3", //
        category = "Hidden", //
        description = "The kernel of Odoo, needed for all installation.\n"
                + "===================================================", //
        depends = {}, //
        data = {}, //
        test = {}, //
        installable = true, //
        auto_install = true)
package jdoo.addons.base;

import jdoo.addons.base.models.*;