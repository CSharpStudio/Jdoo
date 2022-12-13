jdoo.editor('many2many', {
    buttonsTpl: {
        "add": `<button name="add" type="button" class="btn btn-success mr-1" click="target.addValue()">${'添加'.t()}</button>`,
        "remove": `<button name="remove" type="button" t-enable="ids" class="btn btn-danger" click="target.removeValue()">${'删除'.t()}</button>`
    },
    lookupTpl: `<div class="modal fade" id="modal-m2m">
                    <div class="modal-dialog modal-xl">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title">${'选择'.t()}<span class="comodel-name"></span></h4>
                                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                                </button>
                            </div>
                            <div class="modal-body">
                                <div class="m2m-pager"></div>
                                <div class="grid-sm m2m-cogrid"></div>
                            </div>
                            <div class="modal-footer justify-content-between">
                                <button type="button" class="btn btn-default" data-dismiss="modal">${'关闭'.t()}</button>
                                <button type="button" role="btn-save" class="btn btn-primary">${'确定'.t()}</button>
                            </div>
                        </div>
                    </div>
                </div>`,
    getTpl: function () {
        return `<div role="tbar"></div><div class="grid-sm m2m-grid" id="m2m_${this.name + '-' + jdoo.nextId()}">${'加载中'.t()}</div>`;
    },
    init: function () {
        let me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        me.delete = [];
        me.create = [];
        dom.html(me.getTpl());
    },
    updateGrid: function () {
        let me = this;
        if (me.fields) {
            let el = me.dom.children('.m2m-grid');
            el.html('');
            me.grid = el.JGrid({
                model: me.field.comodel,
                module: me.module,
                arch: me.arch,
                fields: me.fields,
                selected: function (e, grid, sel) {
                    let selected = [];
                    $.each(sel, function (i, id) {
                        selected.push(me.data[id]);
                    });
                    if (me.toolbar) {
                        me.toolbar.update(selected);
                    }
                },
                ajax: function (grid, callback, data, settings) {
                    if (me.data) {
                        callback({
                            data: me.data
                        });
                    } else {
                        jdoo.rpc({
                            model: me.model,
                            module: me.module,
                            method: "searchRelated",
                            args: {
                                relatedField: me.field.name,
                                options: {
                                    criteria: [['id', 'in', me.values || []]],
                                    nextTest: true,
                                    offset: 0,
                                    limit: me.field.limit,
                                    fields: grid.getFields(),
                                    order: grid.getSort()
                                }
                            },
                            context: {
                                usePresent: true
                            },
                            onsuccess: function (r) {
                                me.data = r.data.values;
                                callback({
                                    data: r.data.values
                                });
                            }
                        });
                    }
                }
            });
            me.initToolbar();
        } else {
            jdoo.rpc({
                model: 'ir.ui.view',
                method: "loadFields",
                args: {
                    model: me.field.comodel
                },
                onsuccess: function (r) {
                    me.fields = r.data.fields;
                    me.updateGrid();
                }
            });
        }
    },
    initToolbar: function () {
        let me = this;
        if (me.grid && !me.dom.hasClass('readonly')) {
            me.toolbar = me.dom.find('[role=tbar]').JToolbar({
                arch: me.grid.tbarArch || '<toolbar/>',
                auths: "@all",
                buttons: "default",
                buttonsTpl: me.buttonsTpl,
                defaultButtons: 'add|remove',
                target: me
            });
        }
    },
    addValue: function () {
        let me = this;
        $("#modal-m2m").remove();
        $(document.body).append(me.lookupTpl);
        let modal = $("#modal-m2m");
        modal.find('[role=btn-save]').on('click', function () {
            let selected = cogrid.getSelected();
            for (let i = 0; i < selected.length; i++) {
                let id = selected[i], row = cogrid.data[id];
                if (me.delete.indexOf(id) > -1) {
                    me.delete.remove(id);
                } else {
                    me.create.push(id);
                }
                me.data.push(row);
            }
            me.grid.table.draw();
            modal.modal('hide');
            me.dom.triggerHandler('valueChange', [me]);
        });
        modal.modal({ backdrop: false });
        modal.find('.m2m-pager').empty();
        modal.find('.m2m-cogrid').empty();
        let pager = modal.find('.m2m-pager').JPager({
            pageChange: function (e, pager) {
                cogrid.load();
            },
            counting: function (e, pager) {
                jdoo.rpc({
                    model: me.model,
                    module: me.module,
                    method: "count",
                    args: {
                        criteria: [] //TODO查询功能
                    },
                    onsuccess: function (r) {
                        pager.update({
                            total: r.data
                        });
                    }
                });
            }
        });
        let cogrid = modal.find('.m2m-cogrid').JGrid({
            model: me.field.comodel,
            module: me.module,
            arch: me.arch,
            fields: me.fields,
            selected: function (e, grid, sel) {
            },
            rowDblClick: function (e, grid, id) {
                let row = grid.data[id];
                if (me.delete.indexOf(id) > -1) {
                    me.delete.remove(id);
                } else {
                    me.create.push(id);
                }
                me.data.push(row);
                me.grid.table.draw();
                modal.modal('hide');
                me.dom.triggerHandler('valueChange', [me]);
            },
            ajax: function (grid, callback, data, settings) {
                jdoo.rpc({
                    model: me.model,
                    module: me.module,
                    method: "searchRelated",
                    args: {
                        relatedField: me.field.name,
                        options: {
                            criteria: [['id', 'not in', me.values]],
                            nextTest: true,
                            offset: pager.getOffest(),
                            limit: pager.getLimit(),
                            fields: grid.getFields(),
                            order: grid.getSort(),
                            activeTest: true
                        }
                    },
                    context: {
                        active_test: true,
                        usePresent: true
                    },
                    onsuccess: function (r) {
                        if (r.data.values.length > 0) {
                            let len = pager.getOffest() + r.data.values.length;
                            if (r.data.hasNext === false) {
                                pager.update({
                                    to: len,
                                    next: false,
                                    total: len
                                });
                            } else {
                                pager.update({
                                    to: len,
                                    next: true
                                });
                            }
                        } else {
                            pager.hide();
                        }
                        callback({
                            data: r.data.values
                        });
                        cogrid.data = {};
                        $.each(r.data.values, function (i, v) {
                            cogrid.data[v['id']] = v;
                        });
                    }
                });
            }
        });
    },
    removeValue: function () {
        let me = this, id = me.grid.table.row().id();
        if (me.create.indexOf(id) > -1) {
            me.create.remove(id);
        } else {
            me.delete.push(id);
        }
        for (let i = 0; i < me.data.length; i++) {
            if (me.data[i].id === id) {
                me.data.splice(i, 1);
                break;
            }
        }
        me.grid.table.draw();
        me.dom.triggerHandler('valueChange', [me]);
    },
    onValueChange: function (handler) {
        this.dom.on('valueChange', handler);
    },
    setReadonly: function (v) {
        let me = this;
        if (v) {
            me.dom.find('[role=tbar]').empty();
            me.dom.addClass('readonly');
        } else {
            me.dom.removeClass('readonly');
            me.initToolbar();
        }
    },
    getValue: function () {
        let me = this, v = [];
        for (let i = 0; i < me.create.length; i++) {
            v.push([4, me.create[i], 0]);
        }
        for (let i = 0; i < me.delete.length; i++) {
            v.push([3, me.delete[i], 0]);
        }
        return v;
    },
    setValue: function (v) {
        let me = this;
        me.values = v || [];
        delete me.data;
        me.delete = [];
        me.create = [];
        me.updateGrid();
    }
});