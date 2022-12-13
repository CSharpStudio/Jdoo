
$.component("ModuleView", {
    ztreeSetting: {
        view: {
            selectedMulti: false
        },
        data: {
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
        return `<div class="h-100 module-view">
                <div class="module-head">
                    <label>应用</label>
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
                <div class="module-panel">
                    <div id="modulelist" class="module-list ztree"></div>
                </div>
            </div>
            <div class="h-100 module-extender" style="display:none" role="button">
                <span class="fa fa-angle-right"></span>
            </div>`;
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
                    if (node && node.type === 'add_diagram') {
                        me.editDiagram(node, true);
                    } else if (node && node.type === 'diagram') {
                        me.designer.openDiagram(node);
                    }
                },
                beforeEditName: function (tid, node) {
                    if (node.type === 'diagram') {
                        me.editDiagram(node);
                    } else if (node.type === 'app') {
                        me.editModule(node);
                    } else if (node.type === 'model') {
                        me.editModel(node);
                    }
                    return false;
                },
                beforeRemove: function (tid, node) {
                    if (node.type === 'diagram') {
                        me.deleteDiagram(node);
                    } else if (node.type === 'app') {
                        me.deleteModule(node);
                    } else if (node.type === 'model') {
                        me.deleteModel(node);
                    }
                    return false;
                },
                beforeExpand: function (tid, node) {
                    if (node.type === 'models' && !node.children) {
                        return me.loadModels(node);
                    }
                    else if (node.type === 'diagrams' && !node.children) {
                        return me.loadDiagrams(node);
                    }
                    return true;
                },
                beforeDrag: function (tid, node) {
                    return node[0] && node[0].type === 'model';
                },
                onDrop: function (e, tid, nodes) {
                    if (me.designer.diagram && $(e.target).hasClass('model-canvas')) {
                        let zoom = 100 / me.designer.diagram.zoom;
                        me.designer.diagram.linkModel({ id: nodes[0].id, x: e.offsetX * zoom, y: e.offsetY * zoom });
                    }
                }
            }
        });
        me.dom.on('click', '.btn-collapse', function () {
            me.dom.animate({
                width: 8
            }, 250, 'linear', function () {
                me.dom.find('.module-view').hide();
                me.dom.find('.module-extender').show();
            });
        }).on('click', '.module-extender', function () {
            $(this).hide();
            me.dom.find('.module-view').show();
            me.dom.animate({
                width: 240
            }, 250, 'linear');
        }).on('click', '.btn-collespe', function () {
            if (me.appTree) {
                me.appTree.expandAll(false);
            }
        }).on('click', '.btn-add', function () {
            me.editModule({});
        }).on('click', '.btn-lookup', function () {
            me.loadModules();
        }).on('click', '.btn-refresh', function () {
            let tid = $(this).attr('data-n'), node = me.appTree.getNodeByTId(tid);
            if (node.type === 'models') {
                me.loadModels(node);
            } else if (node.type === 'diagrams') {
                me.loadDiagrams(node);
            }
        });
        me.dom.find('.input-keyword').on('keydown', function (e) {
            if (e.keyCode === 13) {
                me.loadModules();
            }
        });
        me.loadModules();
    },
    loadModules: function () {
        let me = this, kw = me.dom.find('.input-keyword').val(), criteria = [];
        if (kw) {
            criteria.push(['present', 'like', kw]);
        }
        jdoo.rpc({
            model: me.model,
            module: me.module,
            method: "searchRelated",
            args: {
                relatedField: "module_id",
                options: {
                    limit: 10000,
                    offset: 0,
                    criteria: criteria,
                    nextTest: true,
                    activeTest: true,
                    fields: ['present']
                }
            },
            context: {
                usePresent: true
            },
            onsuccess: function (r) {
                let data = r.data.values;
                $.each(data, function () {
                    this.iconSkin = 'app';
                    this.type = 'app';
                    this.children = [
                        { present: "模型图", isParent: true, type: 'diagrams', module_id: this.id, iconSkin: 'folder' },
                        { present: "模型", isParent: true, type: 'models', id: "models-" + this.id, module_id: this.id, iconSkin: 'folder' },
                    ];
                });
                me.appTree = $.fn.zTree.init(me.dom.find('.module-list'), me.ztreeSetting, data);
            }
        });
    },
    loadModels: function (node) {
        let me = this;
        me.appTree.removeChildNodes(node);
        if (node.module_id && !node.loading) {
            node.loading = true;
            jdoo.rpc({
                model: me.model,
                module: me.module,
                method: "loadModels",
                args: {
                    moduleId: node.module_id,
                    fields: ['present', 'module_id']
                },
                context: {
                    usePresent: true
                },
                onerror: function (e) {
                    jdoo.msg.error(e);
                    node.loading = false;
                },
                onsuccess: function (r) {
                    let data = r.data;
                    $.each(data, function () {
                        this.type = 'model';
                        this.iconSkin = 'model';
                    });
                    me.appTree.addNodes(node, data);
                    node.loading = false;
                }
            });
            return false;
        }
        return true;
    },
    loadDiagrams: function (node) {
        let me = this;
        me.appTree.removeChildNodes(node);
        if (node.module_id && !node.loading) {
            node.loading = true;
            jdoo.rpc({
                model: me.model,
                module: me.module,
                method: "search",
                args: {
                    criteria: [['module_id', '=', node.module_id]],
                    offset: 0,
                    limit: 10000,
                    fields: ['present', 'module_id', 'width', 'height']
                },
                onerror: function (e) {
                    jdoo.msg.error(e);
                    node.loading = false;
                },
                onsuccess: function (r) {
                    let data = r.data.values;
                    $.each(data, function () {
                        this.type = 'diagram';
                        this.iconSkin = 'diagram';
                    });
                    data.splice(0, 0, { present: '<新建>', type: 'add_diagram', iconSkin: 'add-diagram' });
                    me.appTree.addNodes(node, data);
                    node.loading = false;
                }
            });
        }
        return false;
    },
    showDiagramDialog: function (view, param) {
        let me = this;
        jdoo.showDialog({
            css: '',
            title: '模型图'.t(),
            submitText: '保存'.t(),
            init: function () {
                let dialog = this;
                dialog.form = dialog.body.JForm({
                    cols: 1,
                    model: me.model,
                    module: me.module,
                    fields: view.fields,
                    arch: view.views.form.arch,
                    ajax: function (form, callback) {
                        if (param.id) {
                            jdoo.rpc({
                                model: me.model,
                                module: me.module,
                                method: "read",
                                args: {
                                    ids: [param.id],
                                    fields: dialog.form.getFields()
                                },
                                context: {
                                    usePresent: true
                                },
                                onsuccess: function (r) {
                                    callback({ data: r.data[0] });
                                }
                            });
                        }
                    }
                });
                if (param.id) {
                    dialog.form.load();
                } else {
                    dialog.form.create();
                }
            },
            submit: function () {
                let dialog = this;
                if (dialog.form.valid()) {
                    dialog.busy(true);
                    let data = dialog.form.getData();
                    data.module_id = param.diagramsNode.module_id;
                    if (param.id) {
                        jdoo.rpc({
                            model: me.model,
                            module: me.module,
                            method: "update",
                            args: {
                                ids: [param.id],
                                values: data,
                            },
                            dialog: dialog,
                            onsuccess: function (r) {
                                me.ws.updateTab(param.id, data.name);
                                me.loadDiagrams(param.diagramsNode);
                                let d = me.designer.diagrams[param.id];
                                if (d) {
                                    d.updateSize({ width: data.width, height: data.height });
                                }
                                dialog.close();
                            }
                        });
                    } else {
                        jdoo.rpc({
                            model: me.model,
                            module: me.module,
                            method: "create",
                            args: data,
                            dialog: dialog,
                            onsuccess: function (r) {
                                me.loadDiagrams(param.diagramsNode);
                                me.designer.openDiagram({ id: r.data, module_id: data.module_id, present: data.name, width: data.width, height: data.height });
                                dialog.close();
                            }
                        });
                    }
                }
            }
        });

    },
    deleteDiagram: function (node) {
        let me = this;
        jdoo.msg.confirm({
            content: "确定删除模型图:".t() + node.present + "?",
            submit: function () {
                jdoo.rpc({
                    model: me.model,
                    module: me.module,
                    method: "delete",
                    args: {
                        ids: [node.id]
                    },
                    onsuccess: function (r) {
                        me.loadDiagrams(node.getParentNode());
                        jdoo.msg.show('删除成功'.t());
                    }
                });
            }
        });
    },
    editDiagram: function (node, create) {
        let me = this, param = { diagramsNode: node.getParentNode() };
        if (!create) {
            param.id = node.id;
        }
        if (!me.diagramView) {
            jdoo.rpc({
                model: "ir.ui.view",
                method: "loadView",
                args: {
                    model: 'modeling.diagram',
                    type: "form"
                },
                onsuccess: function (r) {
                    me.diagramView = r.data;
                    me.showDiagramDialog(me.diagramView, param);
                }
            })
        } else {
            me.showDiagramDialog(me.diagramView, param);
        }
    },
    deleteModule: function (node) {
        let me = this;
        jdoo.msg.confirm({
            content: "确定删除应用:".t() + node.present + "?",
            submit: function () {
                jdoo.rpc({
                    model: me.model,
                    module: me.module,
                    method: "deleteModule",
                    args: {
                        moduleId: node.id
                    },
                    onsuccess: function (r) {
                        me.loadModules();
                        jdoo.msg.show('删除成功'.t());
                    }
                });
            }
        });
    },
    showModuleDialog: function (view, param) {
        let me = this;
        jdoo.showDialog({
            title: "应用".t(),
            submitText: '保存'.t(),
            init: function () {
                let dialog = this;
                dialog.form = dialog.body.JForm({
                    fields: view.fields,
                    model: view.model,
                    module: view.module,
                    arch: view.views.form.arch,
                    ajax: function (form, callback) {
                        if (param.id) {
                            jdoo.rpc({
                                model: me.model,
                                module: me.module,
                                method: "searchRelated",
                                args: {
                                    relatedField: "module_id",
                                    options: {
                                        criteria: [["id", "=", param.id]],
                                        nextTest: false,
                                        fields: dialog.form.getFields()
                                    }
                                },
                                context: {
                                    usePresent: true
                                },
                                onsuccess: function (r) {
                                    callback({ data: r.data.values[0] });
                                }
                            });
                        }
                    },
                });
                if (param.id) {
                    dialog.form.load();
                } else {
                    dialog.form.create();
                }
            },
            submit: function () {
                let dialog = this;
                if (dialog.form.valid()) {
                    dialog.busy(true);
                    let data = dialog.form.getData();
                    if (param.id) {
                        data.id = param.id;
                        jdoo.rpc({
                            model: me.model,
                            module: me.module,
                            method: "updateModule",
                            args: {
                                values: data,
                            },
                            dialog: dialog,
                            onsuccess: function (r) {
                                me.loadModules();
                                dialog.close();
                            }
                        });
                    } else {
                        jdoo.rpc({
                            model: me.model,
                            module: me.module,
                            method: "addModule",
                            args: { values: data },
                            dialog: dialog,
                            onsuccess: function (r) {
                                me.loadModules();
                                dialog.close();
                            }
                        });
                    }
                }
            }
        });
    },
    editModule: function (node) {
        let me = this;
        if (!me.moduleView) {
            jdoo.rpc({
                model: "ir.ui.view",
                method: "loadView",
                args: {
                    model: 'ir.module',
                    type: "form"
                },
                onsuccess: function (r) {
                    me.moduleView = r.data;
                    me.showModuleDialog(me.moduleView, node);
                }
            })
        } else {
            me.showModuleDialog(me.moduleView, node);
        }
    },
    deleteModel: function (node) {
        let me = this;
        jdoo.msg.confirm({
            content: "确定删除模型:".t() + node.present + "?",
            submit: function () {
                jdoo.rpc({
                    model: me.model,
                    module: me.module,
                    method: "deleteModel",
                    args: {
                        modelId: node.id
                    },
                    onsuccess: function (r) {
                        me.loadModels(node.getParentNode());
                        for (let d in me.designer.diagrams) {
                            me.designer.diagrams[d].removeModel(node.id);
                        }
                        jdoo.msg.showMsg('删除成功'.t());
                    }
                });
            }
        });
    },
    editModel: function (node) {
        let me = this;
    }
});