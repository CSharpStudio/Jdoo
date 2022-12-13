
$.component("Modeling", {
    model: "modeling.diagram",
    module: "modeling",
    getTpl: function () {
        return `<aside class="left-box">
                </aside>
                <div class="main-box">
                </div>`;
    },
    init: function () {
        let me = this;
        me.dom.html(me.getTpl());
        me.diagrams = {};
        me.ws = me.dom.find('.main-box').JTabs({
            tabActived: function (e, tabs, tab) {
                let id = tab.parent().attr('data');
                me.diagram = me.diagrams[id];
                let hash = jdoo.web.getParams(window.location.hash.substring(1));
                hash.diagram = id;
                window.location.hash = $.param(hash);
                let tophash = jdoo.web.getParams(top.window.location.hash.substring(1));
                if (tophash.u) {
                    tophash.u = window.location.pathname + '#' + $.param($.extend($.param(unescape(tophash.u)), hash));
                    top.window.location.hash = $.param(tophash);
                }
            }
        });
        me.modelEditor = jdoo.create("FormEdit", { model: 'modeling.model', title: '模型'.t() });
        me.fieldEditor = jdoo.create("FormEdit", { model: 'modeling.model.field', title: '字段'.t() });
        me.modules = me.dom.find('.left-box').ModuleView({
            designer: me,
            model: me.model,
            module: me.module
        });
        let urlHash = jdoo.web.getParams(window.location.hash.substring(1));
        if (urlHash.diagram) {
            me.loadDiagram(urlHash.diagram);
        }
    },
    loadDiagram: function (id) {
        let me = this;
        jdoo.rpc({
            model: me.model,
            module: me.module,
            method: "read",
            args: {
                ids: [id],
                fields: ['present', 'module_id', 'width', 'height']
            },
            onsuccess: function (r) {
                me.openDiagram(r.data[0]);
            }
        })
    },
    openDiagram: function (node) {
        let me = this;
        me.ws.openTab(node.id, {
            title: node.present,
            data: node.id,
            closable: true,
            init: function (tab) {
                me.diagrams[node.id] = tab.Diagram({
                    id: node.id,
                    module_id: node.module_id,
                    modelEditor: me.modelEditor,
                    fieldEditor: me.fieldEditor,
                    width: node.width,
                    height: node.height,
                    modelChange: function (e, canvas) {
                        let models = me.modules.appTree.getNodeByParam("id", "models-" + node.module_id, null);
                        me.modules.loadModels(models);
                    }
                });
            },
            onTabRemoved: function (tabs, ids) {
                $.each(ids, function () {
                    delete me.diagrams[this];
                });
            }
        });
    },
});

$(function () {
    $('.model-designer').Modeling({});
});