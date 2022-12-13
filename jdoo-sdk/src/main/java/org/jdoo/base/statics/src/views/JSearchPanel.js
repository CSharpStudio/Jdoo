$.component("JSearchPanel", {
    limit: 1000,
    presentField: 'present',
    parentField: 'parent_id',
    idField: 'id',
    config: function (setting) { },
    getTpl: function () {
        return `<div class="search-panel">
                    <label class="ml-2 mt-2 mb-0">${this.label}</label>
                    <div id="search_panel_${jdoo.nextId()}" class="ztree"></div>
                </div>`
    },
    init: function () {
        let me = this;
        me.sel = [];
        me._fields = [];
        me._fields.push(me.presentField);
        if (me.arch) {
            let arch = jdoo.utils.parseXML(me.arch).children('searchpanel'),
                el = arch.find('field:first-child'),
                name = el.attr('name'),
                field = me.fields[name];
            if (field.type !== 'many2one') {
                throw new Error('searchpanel not support：' + field.type);
            }
            me.label = el.attr('label') || field.label;
            me.field = field;
            me.isTree = Boolean(eval(arch.attr("tree")));
            me.select = eval(el.attr('select')) || [];
            if (me.isTree) {
                me._fields.push(me.parentField);
            }
            me.dom.html(me.getTpl());
            me.ztreeSetting = {
                view: {
                    showIcon: false,
                    selectedMulti: false
                },
                data: {
                    simpleData: {
                        enable: true,
                        pIdKey: "parent_id"
                    },
                    key: {
                        name: "present"
                    },
                },
                callback: {
                    onClick: function (e, id, node) {
                        me.sel = me.ztree.getSelectedNodes();
                        me.dom.triggerHandler("selected", [me, me.sel]);
                    }
                }
            };
            me.ztreeSetting.view.showLine = me.isTree;
            me.config(me.ztreeSetting);
        }
        me.onSelected(me.selected);
    },
    load: function () {
        let me = this;
        me.ajax(me, function (data) {
            me.ztree = $.fn.zTree.init(me.dom.find('.ztree'), me.ztreeSetting, data);
            me.ztree.expandAll(true);
        });
    },
    onSelected: function (handler) {
        this.dom.on("selected", handler);
    },
    getSelect: function () {
        return this.select;
    },
    getCriteria: function () {
        let me = this;
        let sel = me.sel[0];
        if (sel && sel.id != 'all') {
            //TODO 处理树形数据递归
            return [[me.field.name, '=', sel.id]];
        }
        return [];
    },
    getFields: function () {
        return this._fields;
    },
});