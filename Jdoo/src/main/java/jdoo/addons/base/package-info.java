@jdoo.modules.Import({ //
        Base.class, //
        Unknown.class, //
        IrModel.class, //
        IrModelFields.class, //
        IrModelSelection.class, //
        IrModelConstraint.class, //
        IrModelRelation.class, //
        IrModelAccess.class, //
        IrModelData.class, //
        WizardModelMenu.class, //
        IrSequence.class, //
        IrSequenceDateRange.class, //
        IrUiMenu.class, //
        ViewCustom.class, //
        View.class, //
        // todo ir_actions ir_actions_report
        IrAttachment.class, //
        // todo ir_cron ir_filters
        IrDefault.class, //
        IrTranslation.class, //
        // todo ir_exports
        IrRule.class, //
        // todo ir_config_parameter ir_autovacuum ir_mail_server
        IrFieldsConverter.class, //
        // todo ir_qweb ir_qweb_fields ir_http ir_logging
        Property.class, //
        // todo ir_module ir_demo ir_demo_failure report_layout report_paperformat
        // image_mixin

        Country.class, //
        CountryGroup.class, //
        CountryState.class, //
        Lang.class, //
        PartnerCategory.class, //
        PartnerTitle.class, //
        Partner.class, //
        ResPartnerIndustry.class, //
        ResBank.class, //
        ResPartnerBank.class, //
        // todo res_config
        Currency.class, //
        CurrencyRate.class, //
        Company.class, //
        Groups.class, //
        ResUsersLog.class, //
        ResUsers.class, //
        DecimalPrecision.class,//
        //todo DecimalPrecisionFloat
})
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