package org.jdoo.utils;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.jdoo.Records;
import org.jdoo.core.Environment;
import org.jdoo.exceptions.ValueException;
import org.jdoo.util.KvMap;
import org.jdoo.util.Tuple;
import org.jdoo.utils.ImportUtils.ErrorHandle;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ImportUtils {
    public interface ErrorHandle {
        void handle(String message, Exception ex);
    }

    public static void importXml(InputStream input, String module, Environment env, ErrorHandle onError) {
        new XmlImport(input, module, env, onError).load();
    }
}

class XmlImport {
    Document doc;
    ErrorHandle onError;
    Environment env;
    String module;

    public XmlImport(InputStream input, String module, Environment env, ErrorHandle onError) {
        this.module = module;
        this.env = env;
        this.onError = onError;
        XmlReader reader = new XmlReader();
        reader.setDocumentFactory(new XmlDocumentFactory());
        try {
            doc = reader.read(input);
        } catch (Exception e) {
            e.printStackTrace();
            onError("读取xml失败", e);
        }
    }

    public void load() {
        Element root = doc.getRootElement();
        for (Element el : root.elements()) {
            try {
                if ("record".equals(el.getName())) {
                    loadRecord(el);
                } else if ("menu".equals(el.getName())) {
                    loadMenu(el);
                } else if ("view".equals(el.getName())) {
                    loadView(el);
                } else if ("web".equals(el.getName())) {
                    loadWeb(el);
                } else if ("action".equals(el.getName())) {
                    loadAction(el);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                onError(el, ex);
            }
        }
    }

    void loadAction(Element el) {
    }

    void loadWeb(Element el) {
        String id = getAttribute(el, "id");
        if (!id.contains(".")) {
            id = module + "." + id;
        }
        String key = id;
        String inherit_id = getAttributeOr(el, "inherit_id", null);
        String mode = "primary";
        if (StringUtils.isNotEmpty(inherit_id)) {
            if (!inherit_id.contains(".")) {
                inherit_id = module + "." + inherit_id;
            }
            Records inherit = env.getRef(inherit_id);
            key = (String) inherit.get("key");
            inherit_id = inherit.getId();
            mode = "extension";
        }
        String name = getAttribute(el, "name");
        String priority = getAttributeOr(el, "priority", "16");
        String arch = "";
        for (Element e : el.elements()) {
            arch += e.asXML();
        }

        Records rec = env.findRef(id);
        if (rec == null) {
            rec = env.get("ir.ui.view");
        } else if (!rec.getMeta().getName().equals("ir.ui.view")) {
            throw new ValueException();
        }
        KvMap values = new KvMap()
                .set("key", key)
                .set("name", name)
                .set("arch", arch)
                .set("mode", mode)
                .set("inherit_id", inherit_id)
                .set("priority", Integer.valueOf(priority))
                .set("type", "web");

        if (rec.any()) {
            rec.update(values);
        } else {
            rec = rec.create(values);
            env.get("ir.model.data").create(new KvMap()
                    .set("name", id.split("\\.", 2)[1])
                    .set("module", module)
                    .set("model", "ir.ui.view")
                    .set("res_id", rec.getId()));
        }
    }

    void loadView(Element el) {
        String id = getAttribute(el, "id");
        String name = getAttribute(el, "name");
        String model = getAttribute(el, "model");
        if (!id.contains(".")) {
            id = module + "." + id;
        }
        String inherit_id = getAttributeOr(el, "inherit_id", null);
        String mode = "primary";
        if (StringUtils.isNotEmpty(inherit_id)) {
            if (!inherit_id.contains(".")) {
                inherit_id = module + "." + inherit_id;
            }
            Records inherit = env.getRef(inherit_id);
            inherit_id = inherit.getId();
            mode = "extension";
        }
        String priority = getAttributeOr(el, "priority", "16");
        String type = getAttributeOr(el, "type", null);
        if (StringUtils.isEmpty(type)) {
            type = el.elements().get(0).getName();
        }
        String arch = "";
        for (Element e : el.elements()) {
            arch += e.asXML();
        }

        Records rec = env.findRef(id);
        if (rec == null) {
            rec = env.get("ir.ui.view");
        } else if (!rec.getMeta().getName().equals("ir.ui.view")) {
            throw new ValueException();
        }
        KvMap values = new KvMap()
                .set("model", model)
                .set("name", name)
                .set("arch", arch)
                .set("mode", mode)
                .set("inherit_id", inherit_id)
                .set("priority", Integer.valueOf(priority))
                .set("type", type);

        if (rec.any()) {
            rec.update(values);
        } else {
            rec = rec.create(values);
            env.get("ir.model.data").create(new KvMap()
                    .set("name", id.split("\\.", 2)[1])
                    .set("module", module)
                    .set("model", "ir.ui.view")
                    .set("res_id", rec.getId()));
        }
    }

    void loadMenu(Element el) {
        String id = getAttribute(el, "id");
        if (!id.contains(".")) {
            id = module + "." + id;
        }
        String parent = getAttributeOr(el, "parent", null);
        if (StringUtils.isNotEmpty(parent)) {
            if (!parent.contains(".")) {
                parent = module + "." + parent;
            }
            Records inherit = env.getRef(parent);
            parent = inherit.getId();
        }
        String name = getAttribute(el, "name");
        String seq = getAttributeOr(el, "seq", "0");
        String url = getAttributeOr(el, "url", null);
        String model = getAttributeOr(el, "model", null);
        String view = getAttributeOr(el, "view", null);
        String icon = getAttributeOr(el, "icon", null);
        String click = getAttributeOr(el, "click", null);
        String css = getAttributeOr(el, "css", null);
        // TODO action
        Records rec = env.findRef(id);
        if (rec == null) {
            rec = env.get("ir.ui.menu");
        } else if (!rec.getMeta().getName().equals("ir.ui.menu")) {
            throw new ValueException();
        }
        KvMap values = new KvMap()
                .set("name", name)
                .set("url", url)
                .set("model", model)
                .set("parent_id", parent)
                .set("icon", icon)
                .set("click", click)
                .set("css", css)
                .set("sequence", Integer.valueOf(seq))
                .set("view", view);

        if (rec.any()) {
            rec.update(values);
        } else {
            rec = rec.create(values);
            env.get("ir.model.data").create(new KvMap()
                    .set("name", id.split("\\.", 2)[1])
                    .set("module", module)
                    .set("model", "ir.ui.menu")
                    .set("res_id", rec.getId()));
        }
    }

    String getAttribute(Element el, String name) {
        Attribute attr = el.attribute(name);
        if (attr == null) {
            throw new ValueException(String.format("未指定%s属性", name));
        }
        return attr.getText();
    }

    String getAttributeOr(Element el, String name, String defaultValue) {
        Attribute attr = el.attribute(name);
        if (attr == null) {
            return defaultValue;
        }
        return attr.getText();
    }

    void loadRecord(Element el) {
        String key = getAttribute(el, "id");
        String model = getAttribute(el, "model");
        if (!key.contains(".")) {
            key = module + "." + key;
        }
        Records rec = env.findRef(key);
        if (rec == null) {
            rec = env.get(model);
        } else if (!rec.getMeta().getName().equals(model)) {
            throw new ValueException();
        }
        KvMap values = new KvMap();
        for (Element field : el.elements("field")) {
            try {
                Tuple<String, Object> tuple = getField(field);
                values.put(tuple.getItem1(), tuple.getItem2());
            } catch (Exception ex) {
                ex.printStackTrace();
                onError(field, ex);
            }
        }
        if (rec.any()) {
            rec.update(values);
        } else {
            rec = rec.create(values);
            env.get("ir.model.data").create(new KvMap()
                    .set("name", key.split("\\.", 2)[1])
                    .set("module", module)
                    .set("model", model)
                    .set("res_id", rec.getId()));
        }
    }

    Pattern refPattern = Pattern.compile("ref\\((?<ref>\\S+)\\)");

    Object evalValue(String eval) {
        ObjectMapper map = new ObjectMapper();
        try {
            JsonNode node = map.readTree(eval);
            if (node.isBoolean()) {
                return node.asBoolean();
            } else if (node.isDouble()) {
                return node.asDouble();
            } else if (node.isInt()) {
                return node.asInt();
            } else if (node.isNull()) {
                return null;
            } else if (node.isTextual()) {
                return node.asText();
            } else if (node.isArray()) {
                return map.readValue(eval, List.class);
            }
            return map.readValue(eval, Map.class);
        } catch (Exception e) {
            throw new ValueException(String.format("eval的值[%s]解析失败", eval));
        }
    }

    Tuple<String, Object> getField(Element field) {
        String name = getAttribute(field, "name");
        String eval = getAttributeOr(field, "eval", null);
        if (eval != null) {
            Matcher m = refPattern.matcher(eval);
            while (m.find()) {
                String ref = m.group();
                String refVal = m.group("ref");
                Records refRec = env.getRef(refVal);
                eval = eval.replace(ref, "\"" + refRec.getId() + "\"");
            }
            Object val = evalValue(eval);
            return new Tuple<>(name, val);
        }
        String ref = getAttributeOr(field, "ref", null);
        if (ref != null) {
            Records refRec = env.getRef(ref);
            return new Tuple<>(name, refRec.getId());
        }
        return new Tuple<>(name, field.getText());
    }

    public void onError(String message, Exception ex) {
        if (onError != null) {
            onError.handle(message, ex);
        }
    }

    public void onError(Element el, Exception ex) {
        if (onError != null) {
            onError.handle(String.format("解析第[%s]行元素[%s]发生错误：", ((XmlElement) el).getLineNumber(), el.getName())
                    + ThrowableUtils.getCause(ex).getMessage(), ex);
        }
    }

    // #region xml linenumber

    static class XmlReader extends SAXReader {

        @Override
        protected SAXContentHandler createContentHandler(XMLReader reader) {
            return new XmlContentHandler(getDocumentFactory(),
                    getDispatchHandler());
        }

        @Override
        public void setDocumentFactory(DocumentFactory documentFactory) {
            super.setDocumentFactory(documentFactory);
        }

    }

    static class XmlContentHandler extends SAXContentHandler {

        private Locator locator;

        // this is already in SAXContentHandler, but private
        private DocumentFactory documentFactory;

        public XmlContentHandler(DocumentFactory documentFactory,
                ElementHandler elementHandler) {
            super(documentFactory, elementHandler);
            this.documentFactory = documentFactory;
        }

        @Override
        public void setDocumentLocator(Locator documentLocator) {
            super.setDocumentLocator(documentLocator);
            this.locator = documentLocator;
            if (documentFactory instanceof XmlDocumentFactory) {
                ((XmlDocumentFactory) documentFactory).setLocator(documentLocator);
            }

        }

        public Locator getLocator() {
            return locator;
        }
    }

    static class XmlDocumentFactory extends DocumentFactory {

        private Locator locator;

        public XmlDocumentFactory() {
            super();
        }

        public void setLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public Element createElement(QName qname) {
            XmlElement element = new XmlElement(qname);
            if (locator != null)
                element.setLineNumber(locator.getLineNumber());
            return element;
        }

    }

    /**
     * An Element that is aware of it location (line number in) in the source
     * document
     */
    static class XmlElement extends DefaultElement {

        private int lineNumber = -1;

        public XmlElement(QName qname) {
            super(qname);
        }

        public XmlElement(QName qname, int attributeCount) {
            super(qname, attributeCount);

        }

        public XmlElement(String name, Namespace namespace) {
            super(name, namespace);

        }

        public XmlElement(String name) {
            super(name);

        }

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }
    }

    // #endregion
}
