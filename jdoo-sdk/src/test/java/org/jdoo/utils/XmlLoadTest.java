package org.jdoo.utils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;

public class XmlLoadTest {

    String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>                             "
            + "<data>                                                                    "
            + "    <!-- Default user with full access rights for newly created users --> "
            + "    <record id=\"default_user\" model=\"rbac.users\">                      "
            + "        <field name=\"name\">Default User Template</field>                "
            + "        <field name=\"login\">default</field>                             "
            + "        <field name=\"active\" eval=\"False\"/>                           "
            + "    </record>                                                             "
            + "</data>                                                                   ";

    @Test
    public void Test() {
        try {
            Document doc = DocumentHelper.parseText(xml);
            Element root = doc.getRootElement();
            for (Element el : root.elements()) {
                if ("record".equals(el.getName())) {
                    System.out.print(el.getName());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
