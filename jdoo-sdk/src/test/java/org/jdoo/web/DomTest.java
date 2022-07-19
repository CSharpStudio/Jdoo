package org.jdoo.web;

import org.jdoo.base.models.IrUiView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

public class DomTest {
    Elements getData(Document doc) {
        for (Element el : doc.body().children()) {
            if ("data".equals(el.tagName())) {
                return el.children();
            }
        }
        return doc.body().children();
    }

    @Test
    public void TestXPath() {
        //#region
        String primary = "<form string=\"Point of Sale Orders\" create=\"0\">                                                                                                                  "+
        "    <header>                                                                                                                                                         "+
        "        <button name=\"%(action_pos_payment)d\" string=\"Payment\" class=\"oe_highlight\" type=\"action\" states=\"draft\" />                                        "+
        "        <button name=\"action_pos_order_invoice\" string=\"Invoice\" type=\"object\"                                                                                 "+
        "                attrs=\"{'invisible': ['|', ('invoice_group', '=', False), ('state','!=','paid')]}\"/>                                                               "+
        "        <button name=\"refund\" string=\"Return Products\" type=\"object\"                                                                                           "+
        "            attrs=\"{'invisible':[('state','=','draft')]}\"/>                                                                                                        "+
        "        <field name=\"state\" widget=\"statusbar\" statusbar_visible=\"draft,paid,done\" />                                                                          "+
        "    </header>                                                                                                                                                        "+
        "    <sheet>                                                                                                                                                          "+
        "    <field name=\"failed_pickings\" invisible=\"1\"/>                                                                                                                "+
        "    <div class=\"oe_button_box\" name=\"button_box\">                                                                                                                "+
        "        <button name=\"action_stock_picking\"                                                                                                                        "+
        "            type=\"object\"                                                                                                                                          "+
        "            class=\"oe_stat_button\"                                                                                                                                 "+
        "            icon=\"fa-truck\"                                                                                                                                        "+
        "            attrs=\"{'invisible':[('picking_count', '=', 0)]}\">                                                                                                     "+
        "            <field name=\"picking_count\" widget=\"statinfo\" string=\"Pickings\" attrs=\"{'invisible': [('failed_pickings', '!=', False)]}\"/>                      "+
        "            <field name=\"picking_count\" widget=\"statinfo\" string=\"Pickings\" class=\"text-danger\" attrs=\"{'invisible': [('failed_pickings', '=', False)]}\"/> "+
        "        </button>                                                                                                                                                    "+
        "        <button name=\"action_view_invoice\"                                                                                                                         "+
        "            string=\"Invoice\"                                                                                                                                       "+
        "            type=\"object\"                                                                                                                                          "+
        "            class=\"oe_stat_button\"                                                                                                                                 "+
        "            icon=\"fa-pencil-square-o\"                                                                                                                              "+
        "            attrs=\"{'invisible':[('state','!=','invoiced')]}\">                                                                                                     "+
        "        </button>                                                                                                                                                    "+
        "    </div>                                                                                                                                                           "+
        "    <group col=\"4\" colspan=\"4\" name=\"order_fields\">                                                                                                            "+
        "        <field name=\"name\"/>                                                                                                                                       "+
        "        <field name=\"date_order\"/>                                                                                                                                 "+
        "        <field name=\"session_id\" />                                                                                                                                "+
        "        <field string=\"User\" name=\"user_id\"/>                                                                                                                    "+
        "        <field name=\"partner_id\" context=\"{'res_partner_search_mode': 'customer'}\" attrs=\"{'readonly': [('state','=','invoiced')]}\"/>                          "+
        "        <field name=\"fiscal_position_id\" options=\"{'no_create': True}\"/>                                                                                         "+
        "        <field name=\"invoice_group\" invisible=\"1\"/>                                                                                                              "+
        "    </group>                                                                                                                                                         "+
        "    <notebook colspan=\"4\">                                                                                                                                         "+
        "        <page string=\"Products\" name=\"products\">                                                                                                                 "+
        "            <field name=\"lines\" colspan=\"4\" nolabel=\"1\">                                                                                                       "+
        "                <tree string=\"Order lines\" editable=\"bottom\">                                                                                                    "+
        "                    <field name=\"full_product_name\"/>                                                                                                              "+
        "                    <field name=\"pack_lot_ids\" widget=\"many2many_tags\" groups=\"stock.group_production_lot\"/>                                                   "+
        "                    <field name=\"qty\"/>                                                                                                                            "+
        "                    <field name=\"product_uom_id\" string=\"UoM\" groups=\"uom.group_uom\"/>                                                                         "+
        "                    <field name=\"price_unit\" widget=\"monetary\"/>                                                                                                 "+
        "                    <field name=\"discount\" string=\"Disc.%\" widget=\"monetary\"/>                                                                                 "+
        "                    <field name=\"tax_ids_after_fiscal_position\" widget=\"many2many_tags\" string=\"Taxes\"/>                                                       "+
        "                    <field name=\"tax_ids\" widget=\"many2many_tags\" invisible=\"1\"/>                                                                              "+
        "                    <field name=\"price_subtotal\" widget=\"monetary\" force_save=\"1\"/>                                                                            "+
        "                    <field name=\"price_subtotal_incl\" widget=\"monetary\" force_save=\"1\"/>                                                                       "+
        "                    <field name=\"currency_id\" invisible=\"1\"/>                                                                                                    "+
        "                </tree>                                                                                                                                              "+
        "                <form string=\"Order lines\">                                                                                                                        "+
        "                    <group col=\"4\">                                                                                                                                "+
        "                        <field name=\"full_product_name\"/>                                                                                                          "+
        "                        <field name=\"qty\"/>                                                                                                                        "+
        "                        <field name=\"discount\" widget=\"monetary\"/>                                                                                               "+
        "                        <field name=\"price_unit\" widget=\"monetary\"/>                                                                                             "+
        "                        <field name=\"price_subtotal\" invisible=\"1\" widget=\"monetary\" force_save=\"1\"/>                                                        "+
        "                        <field name=\"price_subtotal_incl\" invisible=\"1\" widget=\"monetary\" force_save=\"1\"/>                                                   "+
        "                        <field name=\"tax_ids_after_fiscal_position\" widget=\"many2many_tags\" string=\"Taxes\"/>                                                   "+
        "                        <field name=\"tax_ids\" widget=\"many2many_tags\" invisible=\"1\"/>                                                                          "+
        "                        <field name=\"pack_lot_ids\" widget=\"many2many_tags\" groups=\"stock.group_production_lot\"/>                                               "+
        "                        <field name=\"notice\"/>                                                                                                                     "+
        "                        <field name=\"currency_id\" invisible=\"1\"/>                                                                                                "+
        "                    </group>                                                                                                                                         "+
        "                </form>                                                                                                                                              "+
        "            </field>                                                                                                                                                 "+
        "            <group class=\"oe_subtotal_footer oe_right\" colspan=\"2\" name=\"order_total\">                                                                         "+
        "                <field name=\"amount_tax\"                                                                                                                           "+
        "                       force_save=\"1\"                                                                                                                              "+
        "                       widget=\"monetary\"/>                                                                                                                         "+
        "                <div class=\"oe_subtotal_footer_separator oe_inline\">                                                                                               "+
        "                    <label for=\"amount_total\" />                                                                                                                   "+
        "                    <button name=\"button_dummy\"                                                                                                                    "+
        "                        states=\"draft\" string=\"(update)\" class=\"oe_edit_only oe_link\"/>                                                                        "+
        "                </div>                                                                                                                                               "+
        "                <field name=\"amount_total\"                                                                                                                         "+
        "                       force_save=\"1\"                                                                                                                              "+
        "                       nolabel=\"1\"                                                                                                                                 "+
        "                       class=\"oe_subtotal_footer_separator\"                                                                                                        "+
        "                       widget=\"monetary\"/>                                                                                                                         "+
        "                <field name=\"amount_paid\"                                                                                                                          "+
        "                    string=\"Total Paid (with rounding)\"                                                                                                            "+
        "                    class=\"oe_subtotal_footer_separator\"                                                                                                           "+
        "                    widget=\"monetary\"                                                                                                                              "+
        "                    attrs=\"{'invisible': [('amount_paid','=', 'amount_total')]}\"/>                                                                                 "+
        "                <field name=\"currency_id\" invisible=\"1\"/>                                                                                                        "+
        "            </group>                                                                                                                                                 "+
        "            <div class=\"oe_clear\"/>                                                                                                                                "+
        "        </page>                                                                                                                                                      "+
        "        <page string=\"Payments\" name=\"payments\">                                                                                                                 "+
        "            <field name=\"payment_ids\" colspan=\"4\" nolabel=\"1\">                                                                                                 "+
        "                <tree string=\"Payments\">                                                                                                                           "+
        "                    <field name=\"currency_id\" invisible=\"1\" />                                                                                                   "+
        "                    <field name=\"payment_date\"/>                                                                                                                   "+
        "                    <field name=\"payment_method_id\"/>                                                                                                              "+
        "                    <field name=\"amount\"/>                                                                                                                         "+
        "                </tree>                                                                                                                                              "+
        "            </field>                                                                                                                                                 "+
        "        </page>                                                                                                                                                      "+
        "        <page name=\"extra\" string=\"Extra Info\">                                                                                                                  "+
        "            <group >                                                                                                                                                 "+
        "                <group                                                                                                                                               "+
        "                    string=\"Accounting\"                                                                                                                            "+
        "                    groups=\"account.group_account_manager\"                                                                                                         "+
        "                    attrs=\"{'invisible':['|', ('session_move_id','=', False), ('state', '=', 'invoiced')]}\"                                                        "+
        "                >                                                                                                                                                    "+
        "                    <field name=\"session_move_id\" readonly=\"1\" />                                                                                                "+
        "                </group>                                                                                                                                             "+
        "                <group string=\"Other Information\">                                                                                                                 "+
        "                    <field name=\"pos_reference\"/>                                                                                                                  "+
        "                    <field name=\"company_id\" groups=\"base.group_multi_company\"/>                                                                                 "+
        "                    <field name=\"pricelist_id\" groups=\"product.group_product_pricelist\"/>                                                                        "+
        "                </group>                                                                                                                                             "+
        "            </group>                                                                                                                                                 "+
        "        </page>                                                                                                                                                      "+
        "        <page string=\"Notes\" name=\"notes\">                                                                                                                       "+
        "            <field name=\"note\"/>                                                                                                                                   "+
        "        </page>                                                                                                                                                      "+
        "    </notebook>                                                                                                                                                      "+
        "</sheet>                                                                                                                                                             "+
        "</form>              ";
        //#endregion                                                                                                                                                
        String extension = "<xpath expr=\"field[name='amount']\" position=\"before\">           "+
        "  <field name=\"mercury_prefixed_card_number\" string=\"Card Number\"/>"+
        "  <field name=\"mercury_card_brand\"/>                                 "+
        "  <field name=\"mercury_card_owner_name\"/>                            "+
        "</xpath>                                                               ";
        Document doc = Jsoup.parse(primary);
        Elements base = doc.body().children();

        Document ext = Jsoup.parse(extension);
        Elements data = getData(ext);
        IrUiView.combined(base, data);

        System.out.println(doc.body().html());
    }
}
