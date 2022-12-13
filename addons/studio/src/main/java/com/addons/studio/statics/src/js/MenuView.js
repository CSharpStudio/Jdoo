$.component("MenuView", {
    label: '菜单',
    model: "studio.designer",
    module: "studio",
    ztreeSetting: {
        view: {
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
        edit: {
            drag: {
                isCopy: true,
                isMove: false,
                prev: false,
                next: false,
                inner: false
            },
            enable: true,
            removeTitle: '删除'.t(),
            renameTitle: '重命名'.t(),
            showRemoveBtn: function (tid, node) {
                return node.type === 'diagram' || node.type === 'app' || node.type === 'model';
            },
            showRenameBtn: function (tid, node) {
                return node.type === 'diagram' || node.type === 'app' || node.type === 'model';
            }
        },
    },
    getTpl: function () {
        return `<div class="h-100 menu-view">
                    <div class="menu-head">
                        <label>菜单</label>
                        <span role="button" class="float-right btn-collapse"><i class="fas fa-bars"></i></span>
                    </div>                        
                    <div class="m-1">
                        <div class="input-group input-group-sm">
                            <div class="input-group-prepend">
                                <button class="btn btn-default btn-sm btn-collespe" title="${'收起所有'.t()}">
                                    <span class="fas fa-chevron-up"></span>
                                </button>
                                <button class="btn btn-default btn-sm btn-add" title="${'新增'.t()}">
                                    <span class="fas fa-plus"></span>
                                </button>
                            </div>
                            <input type="text" class="form-control input-keyword"></input>
                            <div class="input-group-append">
                                <div data-btn="view" class="btn btn-default btn-lookup">
                                    <span class="fa fa-search"></span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="menu-panel">
                        <div id="menulist" class="menu-list ztree"></div>
                    </div>
                </div>
                <div class="h-100 menu-extender" style="display:none" role="button">
                    <span class="fa fa-angle-right"></span>
                </div>
                `;
    },
    init: function () {
        let me = this;
        me.dom.append(me.getTpl());
        $.extend(true, me.ztreeSetting, {
            view: {
                addHoverDom: function (tid, node) {
                    if (node.type === 'models' || node.type === 'diagrams') {
                        let aObj = me.dom.find("#" + node.tId + "_a"), btn = aObj.find('.btn-refresh');
                        if (btn.length > 0) {
                            btn.show();
                            return;
                        }
                        aObj.append('<span data-n="' + node.tId + '" title="' + '重新加载'.t() + '" class="ml-1 fas fa-sync btn-refresh" ></span>');
                    }
                },
                removeHoverDom: function (tid, node) {
                    if (node.type === 'models' || node.type === 'diagrams') {
                        me.dom.find("#" + node.tId + "_a .btn-refresh").hide();
                    }
                }
            },
            callback: {
                onClick: function (e, tid, node) {
                    if (node && node.model) {
                        me.editView(node);
                    }
                },
                beforeEditName: function (tid, node) {
                    return false;
                },
                beforeRemove: function (tid, node) {
                    return false;
                },
                beforeExpand: function (tid, node) {
                    return true;
                }
            }
        });
        me.dom.on('click', '.btn-collapse', function () {
            me.dom.animate({
                width: 8
            }, 250, 'linear', function () {
                me.dom.find('.menu-view').hide();
                me.dom.find('.menu-extender').show();
            });
        }).on('click', '.menu-extender', function () {
            $(this).hide();
            me.dom.find('.menu-view').show();
            me.dom.animate({
                width: 240
            }, 250, 'linear');
        });
        me.load();
        //me.editView({ id: 'test', present: '测试' });
    },
    editView: function (node) {
        let me = this;
        me.ws.openTab(node.id, {
            title: node.present,
            closable: true,
            init: function (body) {
                body.ViewDesigner({
                    dataId: node.id,
                });
            }
        });
    },
    load: function () {
        let me = this, kw = me.dom.find('.input-keyword').val(), criteria = [];
        if (kw) {
            criteria.push(['present', 'like', kw]);
        }
        jdoo.rpc({
            model: me.model,
            module: me.module,
            method: "searchMenu",
            args: {
                criteria: criteria,
                fields: ['present', 'parent_id', 'model']
            },
            onsuccess: function (r) {
                let data = r.data;
                me.appTree = $.fn.zTree.init(me.dom.find('.menu-list'), me.ztreeSetting, data);
            }
        });
    }
});