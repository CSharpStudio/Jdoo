$.component("JGrid", {
    ajax: function (grid, callback, data, settings) {
        callback({ data: [] });
    },
    saveEdit: jdoo.emptyFn,
    loadEdit: function (grid, id, callback) {
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
    },
    getEditFBarTpl: function () {
        return `<td colspan="500"><div class="grid-edit"></div>
                    <div class="grid-edit-tbar">
                        <button name="cancel" class="btn btn-outline-secondary">${'取消'.t()}</button>
                        <button name="confirm" class="btn btn-info float-right" style="min-width:100px">${'确定'.t()}</button>
                    </div>
                </td>`;
    },
    init: function () {
        let me = this, columnDefs = [], columnIndex = 0, columnOrder = 0;
        me._fields = [];
        if (me.arch) {
            let arch = jdoo.utils.parseXML(me.arch), grid = arch.children('grid');
            if (grid.length > 0) {
                me.limit = eval(grid.attr('limit') || 10);
                me.pager.limit = me.limit;
                me.showRowNum = eval(grid.attr('showRowNum') || '1');
                me.multiSelect = eval(grid.attr('multiSelect') || '1');
                me.checkSelect = eval(grid.attr('checkSelect') || '0');
                me.tbarArch = grid.children('toolbar').prop('outerHTML');
                me.editArch = grid.children('edit').prop('innerHTML') || grid.prop('innerHTML');
                let html = '<table class="table table-bordered table-hover ' + me.className + '"><thead><tr>';
                if (me.checkSelect) {
                    columnDefs.push({
                        searchable: false,
                        orderable: false,
                        data: null,
                        render: function (data, type, row, opt) { return '<input type="checkbox" class="check-select">'; },
                        targets: columnIndex++
                    });
                    html += '<th style="width:1%"></th>';
                    columnOrder++;
                }
                if (me.showRowNum) {
                    columnDefs.push({
                        searchable: false,
                        orderable: false,
                        data: null,
                        render: function (data, type, row, opt) { return opt.row + 1; },
                        targets: columnIndex++
                    });
                    html += '<th style="width:1%">#</th>';
                    columnOrder++;
                }
                grid.children('field').each(function () {
                    let el = $(this),
                        name = el.attr('name'),
                        label = el.attr('label'),
                        css = el.attr('class'),
                        style = el.attr('style'),
                        field = me.fields[name];
                    if (!field) {
                        throw new Error('模型' + me.model + '找不到字段' + name);
                    }
                    if (!field.deny) {
                        if (!label) {
                            label = field.label || field.name;
                        }
                        label = label.t();
                        me._fields.push(name);
                        html += '<th';
                        if (css) {
                            html += ' class="' + css + '"';
                        }
                        if (style) {
                            html += ' style="' + style + '"';
                        }
                        html += '>' + label + '</th>';
                        columnDefs.push({
                            render: new (jdoo.columns[el.attr('editor') || field.type] || jdoo.columns['default'])({ field: field }).render(),
                            data: name,
                            targets: columnIndex++,
                            orderable: field.sortable
                        });
                    }
                });
                html += '</tr></thead></table>';
                grid.replaceWith(html);
            } else {
                let table = arch.children('table');
                table.find('th').each(function () {
                    let el = $(this),
                        name = el.attr('data-data');
                    me._fields.push(name);
                });
            }
            me.dom.html(arch.children().prop('outerHTML'));
        }
        me.table = me.dom.find('table').DataTable({
            paging: false,
            lengthChange: false,
            searching: false,
            ordering: true,
            info: false,
            autoWidth: false,
            responsive: true,
            processing: true,
            serverSide: true,
            rowId: 'id',
            colReorder: true,
            language: {
                processing: "加载中".t(),
                zeroRecords: "没有数据".t()
            },
            ajax: function (data, callback, settings) {
                me.sel = [];
                me.dom.triggerHandler('selected', [me, []]);
                if (me.redraw && me.data) {
                    callback(me.data);
                    me.dom.triggerHandler('loaded', [me, me.data]);
                } else {
                    if (me.table) {
                        me.ajax(me, function (d) {
                            me.data = d;
                            callback(d);
                            me.dom.triggerHandler('loaded', [me, me.data]);
                        }, data, settings);
                    }
                }
            },
            order: [],
            columnDefs: columnDefs
        });
        me.sel = [];
        me.table.on('change', '.check-select', function () {
            let ckb = $(this);
            let id = me.table.row(ckb.parents('tr')).id();
            if (ckb.is(":checked")) {
                if (me.multiSelect) {
                    me.sel.push(id);
                } else {
                    me.sel = [id];
                }
            } else {
                me.sel.remove(id);
            }
            me.dom.triggerHandler('selected', [me, me.sel]);
        });
        me.table.on('click', 'tbody tr', function () {
            let row = $(this);
            if (me.checkSelect || row.hasClass('edit') || row.children('.dataTables_empty').length === 1) {
                return;
            }
            let id = me.table.row(this).id();
            if (me.multiSelect && window.event.ctrlKey) {
                if (row.hasClass('selected')) {
                    row.removeClass('selected');
                    me.sel.remove(id);
                } else {
                    row.addClass('selected');
                    me.sel.push(id);
                }
            } else {
                me.table.$('tr.selected').removeClass('selected');
                row.addClass('selected');
                me.sel = [id];
            }
            me.dom.triggerHandler('selected', [me, me.sel]);
        });
        me.table.on('dblclick', 'tbody tr', function () {
            let row = $(this);
            if (me.checkSelect || row.hasClass('edit') || row.children('.dataTables_empty').length === 1) {
                return;
            }
            let id = me.table.row(this).id();
            me.dom.triggerHandler('rowDblClick', [me, id]);
        });
        me.table.ajax.reload();
        me.onSelected(me.selected);
        me.onRowDblClick(me.rowDblClick);
        me.onEditValueChange(me.editValueChange);
    },
    onSelected: function (handler) {
        this.dom.on('selected', handler);
    },
    onRowDblClick: function (handler) {
        this.dom.on('rowDblClick', handler);
    },
    _redraw: function () {
        let me = this;
        me.redraw = true;
        me.table.draw();
        me.redraw = false;
        me.dom.find('tr.edit').removeClass('edit');
    },
    addData: function () {
        let me = this;
        me._redraw();
        me.dom.find('table tbody').prepend('<tr id="addNew"></tr>');
        let row = me.dom.find('#addNew');
        me._renderEdit(row);
        me.editForm = row.find('.grid-edit').JForm({
            arch: '<form log_access="0">' + me.editArch + '</form>',
            fields: me.fields,
            model: me.model,
            module: me.module
        });
        let values = {};
        $.each(me.editForm.getFields(), function () {
            let field = me.fields[this];
            values[this] = field.defaultValue;
        });
        me.editForm.setData(values);
    },
    _renderEdit: function (row, id) {
        let me = this;
        row.addClass('edit').html(me.getEditFBarTpl());
        row.find('[name=cancel]').on('click', function () {
            me._redraw();
        });
        row.find('[name=confirm]').on('click', function () {
            if (me.editForm.valid()) {
                let btn = $(this);
                btn.attr('disabeld', true);
                me.saveEdit(me, id, me.editForm.getData(), function (success) {
                    if (success) {
                        me.load();
                        me.dom.triggerHandler("editValueChange", [me, me.editForm]);
                    } else {
                        btn.attr('disabeld', false);
                    }
                });
            }
        });
    },
    onEditValueChange: function (handler) {
        this.dom.on('editValueChange', handler);
    },
    editData: function (id) {
        let me = this;
        id = id || me.sel[0];
        if (id) {
            me._redraw();
            let row = me.dom.find('#' + id);
            me._renderEdit(row, id);
            me.editForm = row.find('.grid-edit').JForm({
                arch: '<form log_access="0">' + me.editArch + '</form>',
                fields: me.fields,
                model: me.model,
                module: me.module,
                ajax: function (form, callback) {
                    me.loadEdit(me, id, callback);
                },
            });
            me.editForm.load();
        }
    },
    getTable: function () {
        return this.table;
    },
    getFields: function () {
        return this._fields;
    },
    getSelected: function () {
        return this.sel;
    },
    getSort: function () {
        let me = this, order = '';
        if (me.table) {
            $.each(me.table.order(), function (i, o) {
                let ds = me.table.column(o[0]).dataSrc();
                if (ds) {
                    if (order != '') {
                        order += ',';
                    }
                    order += ds + ' ' + o[1];

                }
            });
        }
        return order;
    },
    load: function () {
        this.table.ajax.reload();
    }
});