jdoo.editor('one2many', {
    limit: 1000,
    buttonsTpl: {
        "add": `<button name="add" type="button" class="btn btn-success mr-1" click="target.addValue()">${'添加'.t()}</button>`,
        "edit": `<button name="edit" type="button" t-enable="id" class="btn btn-info mr-1" click="target.editValue()">${'编辑'.t()}</button>`,
        "remove": `<button name="remove" type="button" t-enable="ids" class="btn btn-danger" click="target.removeValue()">${'删除'.t()}</button>`
    },
    getTpl: function () {
        return `<div role="tbar"></div><div class="grid-sm o2m-grid" id="o2m_${this.name + '-' + jdoo.nextId()}">${'加载中'.t()}</div>`
    },
    init: function () {
        let me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        me.delete = [];
        me.create = [];
        me.update = [];
        dom.html(me.getTpl());
        me.initToolbar();
    },
    initToolbar: function () {
        let me = this;
        if (me.grid && !me.dom.hasClass('readonly')) {
            me.toolbar = me.dom.find('[role=tbar]').JToolbar({
                arch: me.grid.tbarArch || '<toolbar/>',
                auths: "@all",
                buttons: "default",
                buttonsTpl: me.buttonsTpl,
                defaultButtons: 'add|edit|remove',
                target: me
            });
        }
    },
    updateGrid: function () {
        let me = this;
        if (me._fields) {
            let el = me.dom.children('.o2m-grid');
            el.html('');
            me.grid = el.JGrid({
                model: me.field.comodel,
                module: me.module,
                arch: me.arch,
                fields: me._fields,
                selected: function (e, grid, sel) {
                    let selected = [];
                    $.each(sel, function (i, id) {
                        $.each(me.data, function () {
                            if (this.id === id) {
                                selected.push(this);
                            }
                        });
                    });
                    if (me.toolbar) {
                        me.toolbar.update(selected);
                    }
                },
                rowDblClick: function (e, grid, id) {
                    if (!me.dom.hasClass('readonly')) {
                        me.editValue();
                    }
                },
                saveEdit(grid, id, data, callback) {
                    me.saveEdit(id, data, callback);
                },
                loadEdit(grid, id, callback) {
                    if (id && id.startsWith('new')) {
                        for (let i = 0; i < me.data.length; i++) {
                            let d = me.data[i];
                            if (d.id === id) {
                                callback({ data: d });
                                break;
                            }
                        }
                    } else {
                        jdoo.rpc({
                            model: grid.model,
                            module: grid.module,
                            method: "read",
                            args: {
                                ids: [id],
                                fields: grid.editForm.getFields()
                            },
                            context: {
                                usePresent: true
                            },
                            onsuccess: function (r) {
                                callback({ data: r.data[0] });
                            }
                        });
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
                                    offset: 0,
                                    limit: me.limit,
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
            me.grid.onEditValueChange(function () {
                me.dom.triggerHandler("valueChange", [me]);
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
                    me._fields = r.data.fields;
                    me.updateGrid();
                }
            });
        }
    },
    removeDataById(data, id) {
        for (let i = 0; i < data.length; i++) {
            let d = data[i];
            if (d.id === id) {
                data.splice(i, 1);
                break;
            }
        }
    },
    saveEdit: function (id, data, callback) {
        let me = this;
        if (id) {
            data.id = id;
            for (let i = 0; i < me.data.length; i++) {
                let d = me.data[i];
                if (d.id === id) {
                    $.extend(d, data);
                }
            }
            if (id.startsWith('new')) {
                me.removeDataById(me.create, id);
                me.create.push(data);
            } else {
                me.removeDataById(me.update, id);
                me.update.push(data);
            }
        } else {
            data.id = 'new-' + jdoo.nextId();
            me.create.push(data);
            me.data.push(data);
        }
        callback(true);
    },
    addValue: function () {
        let me = this;
        me.grid.addData();
    },
    editValue: function () {
        let me = this;
        let id = me.grid.getSelected()[0];
        me.grid.editData(id);
    },
    removeValue: function () {
        let me = this;
        $.each(me.grid.getSelected(), function () {
            let id = this;
            if (!id.startsWith('new')) {
                me.delete.push(id);
            }
            for (let i = 0; i < me.data.length; i++) {
                let d = me.data[i];
                if (d.id === id) {
                    me.data.splice(i, 1);
                    break;
                }
            }
        });
        me.grid.table.draw();
        me.grid.dom.triggerHandler("editValueChange", [me.grid, me.grid.editForm]);
    },
    onValueChange: function (handler) {
        this.dom.on("valueChange", handler);
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
            let values = {};
            $.extend(values, me.create[i]);
            delete values.id;
            v.push([0, 0, values]);
        }
        for (let i = 0; i < me.update.length; i++) {
            let values = {};
            $.extend(values, me.update[i]);
            let id = values.id;
            delete values.id;
            v.push([1, id, values]);
        }
        for (let i = 0; i < me.delete.length; i++) {
            v.push([2, me.delete[i], 0]);
        }
        return v;
    },
    setValue: function (v) {
        let me = this;
        me.values = v || [];
        delete me.data;
        me.delete = [];
        me.create = [];
        me.update = [];
        me.updateGrid();
    }
});