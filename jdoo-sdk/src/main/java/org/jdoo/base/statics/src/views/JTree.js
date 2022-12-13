$.component("JTree", {
    presentField: 'present',
    parentField: 'parent_id',
    idField: 'id',
    sortField: null,
    config: function (setting) { },
    ajax: function (tree, callback) { },
    init: function () {
        let me = this;
        me.ztreeSetting = {
            view: {
                showIcon: false,
                selectedMulti: false
            },
            edit: {
                enable: true,
                drag: {
                },
                showRemoveBtn: false,
                showRenameBtn: false
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
                    let selected = me.ztree.getSelectedNodes();
                    me.dom.triggerHandler('selected', [me, selected]);
                }
            }
        };
        me.config(me.ztreeSetting);
        me.fields = [];
        me.fields.push(me.parentField);
        me.fields.push(me.presentField);
        if (me.sortField) {
            me.fields.push(me.sortField);
        }
        me.dom.addClass("ztree");
        me.onSelected(me.onselected);
    },
    onSelected: function (handler) {
        this.dom.on('selected', handler);
    },
    load: function () {
        let me = this;
        me.ajax(me, function (data) {
            me.ztree = $.fn.zTree.init(me.dom, me.ztreeSetting, data);
            me.ztree.expandAll(true);
            let urlHash = jdoo.web.getParams(window.location.hash.substring(1)),
                id = urlHash.id,
                node = me.ztree.getNodeByParam("id", id);
            if (node) {
                me.ztree.selectNode(node);
            }
        });
    },
    getFields: function () {
        return this.fields;
    },
    expandSelected: function () {
        let me = this;
        if (me.ztree) {
            let nodes = me.ztree.getSelectedNodes();
            if (nodes.length > 0) {
                me.ztree.expandNode(nodes[0], true, true, true);
            }
        }
    },
    expandAll: function () {
        let me = this;
        if (me.ztree) {
            me.ztree.expandAll(true);
        }
    },
    collapseSelected: function () {
        let me = this;
        if (me.ztree) {
            let nodes = me.ztree.getSelectedNodes();
            if (nodes.length > 0) {
                me.ztree.expandNode(nodes[0], false, true, true);
            }
        }
    },
    collapseAll: function () {
        let me = this;
        if (me.ztree) {
            me.ztree.expandAll(false);
        }
    }
});