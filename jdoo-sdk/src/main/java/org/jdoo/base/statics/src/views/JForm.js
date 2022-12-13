$.component("JForm", {
    ajax: jdoo.emptyFn,
    cols: 4,
    getAsideTpl: function () {
        return `<aside class="left-aside border-right"><div class="m-1">
                    <div class="input-group input-group-sm">
                        <div class="input-group-prepend">
                            <button class="btn btn-default btn-sm tree-expand"><i class="fas fa-chevron-down"></i></button>
                            <button class="btn btn-default btn-sm tree-collapse"><i class="fas fa-chevron-up"></i></button>
                        </div>
                        <input type="text" class="form-control tree-keyword"/>
                        <div class="input-group-append">
                            <div data-btn="view" class="btn btn-default tree-lookup">
                                <i class="fa fa-search"></i>
                            </div>
                        </div>
                    </div>
                </div><div id="treeview_${jdoo.nextId()}" class="treeview"></div></aside>`;
    },
    requiredValid: function (editor) {
        let required = editor.dom.attr('required') || editor.field.required;
        if (eval(required)) {
            let val = editor.getValue();
            if (val === '' || val == null || val == undefined) {
                return '不能为空'.t();
            }
        }
    },
    init: function () {
        let me = this, dom = me.dom;
        me._fields = [];
        me.sel = [];
        me.editors = {};
        if (me.arch) {
            let arch = jdoo.utils.parseXML(me.arch), form = arch.find('form'), toManyArchs = {};
            if (form.length > 0) {
                let tbar = form.children('toolbar');
                me.tbarArch = tbar.prop('outerHTML');
                tbar.remove();
                me.isTree = eval(form.attr('tree'));
                if (me.isTree) {
                    me.cols -= 1;
                }
                if (me.cols < 1) {
                    me.cols = 1;
                }
                me._initFields(form, toManyArchs);
                me._initTabs(form);
                me._initGroup(form);
                form.addClass('grid');
                let html = '<div class="container-fluid form-body"><div class="card-body row"><div class="col-md-12">' + arch.children().prop('outerHTML') + '</div></div></div>';
                if (me.isTree) {
                    html = me.getAsideTpl() + html;
                    dom.css("display", "flex");
                }
                dom.html(html).find('form').css('grid-template-columns', 'repeat(' + me.cols + ', 1fr)')
                    .on('click', '.field-group .group-expender', function () {
                        let btn = $(this), body = btn.parents('.field-group').find('.group-body');
                        if (btn.hasClass('collapsed')) {
                            body.show();
                            btn.removeClass('collapsed');
                            btn.find('[role=button]').removeClass('fa-chevron-down').addClass('fa-chevron-up');
                        } else {
                            body.hide();
                            btn.addClass('collapsed');
                            btn.find('[role=button]').removeClass('fa-chevron-up').addClass('fa-chevron-down');
                        }
                    });
                me._initTreeView(form);
                dom.find('[data-field]').each(function () {
                    let el = $(this),
                        fname = el.attr('data-field'),
                        field = me.fields[fname],
                        editor = el.attr('editor') || field.type,
                        cfg = {
                            field: field,
                            model: me.model,
                            module: me.module,
                            owner: me,
                            dom: el
                        },
                        ctl = jdoo.editors[editor];
                    if (!ctl) {
                        throw new Error('找不到编辑器:' + editor);
                    }
                    if (field.type === 'many2many' || field.type === 'one2many') {
                        cfg.arch = toManyArchs[fname];
                    }
                    let edt = new ctl(cfg);
                    if (edt.onValueChange) {
                        edt.onValueChange(function (e, ed) {
                            ed.dirty = true;
                            me.dirty = true;
                            me._updateEditorState();
                            me.valid(ed.field.name);
                        });
                    }
                    me.editors[fname] = edt;
                });
                dom.find('[readonly]').each(function () {
                    let e = $(this),
                        fname = e.attr('form-field'),
                        expr = this.attributes['readonly'].value;
                    if (fname) {
                        me.getEditor(fname).setReadonly(eval(expr));
                    }
                });
                dom.find('[t-reset]').each(function () {
                    let e = $(this),
                        fname = e.attr('form-field'),
                        reset = e.attr('t-reset');
                    if (fname) {
                        $.each(reset.split(","), function () {
                            let edt = me.editors[this];
                            if (edt.onValueChange) {
                                edt.onValueChange(function () {
                                    me.getEditor(fname).setValue();
                                });
                            }
                        });
                    }
                });
                dom.find('[data-toggle=tooltip]').tooltip();
            }
        }
        me.onSelected(me.selected);
        if (me.data) {
            me.setData(me.data);
        }
    },
    _initTreeView: function (form) {
        let me = this, dom = me.dom;
        if (me.isTree) {
            let sortField = form.attr('sort_field'),
                allowSort = sortField != undefined && sortField != '';
            me.treeview = dom.find('.treeview').JTree({
                model: me.model,
                module: me.module,
                fields: me.fields,
                presentField: form.attr('present_field') || 'present',
                parentField: form.attr('parent_field') || 'parent_id',
                sortField: sortField,
                config: function (setting) {
                    setting.edit.drag.prev = allowSort;
                    setting.edit.drag.next = allowSort;
                    setting.callback.onDrop = function (event, treeId, treeNodes, targetNode, moveType) {
                        console.log(arguments)
                        if (moveType !== 'inner' && moveType !== 'next' && moveType !== 'prev') {
                            return;
                        }
                        let ids = [], vals = {}, pId = me.treeview.parentField, id = me.treeview.idField, sort = me.treeview.sortField;
                        $.each(treeNodes, function () {
                            ids.push(this[id]);
                        });
                        if (moveType === 'inner') {
                            vals[pId] = targetNode ? targetNode[id] : null;
                        } else if (moveType === 'next') {
                            vals[pId] = targetNode ? targetNode[pId] : null;
                            vals[sort] = targetNode ? targetNode[sort] + 1 : 0;
                        } else if (moveType === 'prev') {
                            vals[pId] = targetNode ? targetNode[pId] : null;
                            vals[sort] = targetNode ? targetNode[sort] - 1 : 0;
                        }
                        jdoo.rpc({
                            model: me.model,
                            module: me.module,
                            method: "update",
                            args: {
                                ids: ids,
                                values: vals
                            },
                            onsuccess: function (r) {
                                jdoo.msg.show('保存成功'.t());
                                me.load();
                            }
                        });
                    }
                },
                ajax: function (tree, callback) {
                    let kw = dom.find('.tree-keyword').val();
                    jdoo.rpc({
                        model: me.model,
                        module: me.module,
                        method: "presentSearch",
                        args: {
                            keyword: kw,
                            offset: 0,
                            limit: 0,
                            order: '',
                            fields: tree.getFields()
                        },
                        onsuccess: function (r) {
                            callback(r.data);
                        }
                    });
                },
                onselected: function (e, tree, selected) {
                    dom.triggerHandler('treeSelected', [me.treeview, selected]);
                    if (selected.length > 0) {
                        view.urlHash.id = selected[0].id;
                    } else {
                        delete view.urlHash.id;
                    }
                    view.changeView();
                    me.ajax(me, function (r) { me.setData(r.data) });
                }
            });
            me.treeview.load();
            dom.on('click', '.tree-expand', function () {
                me.treeview.expandAll();
            }).on('click', '.tree-collapse', function () {
                me.treeview.collapseAll();
            }).on('click', '.tree-lookup', function () {
                me.treeview.load();
            });
        }
        me.onTreeSelected(me.treeSelected);
    },
    onTreeSelected: function (handler) {
        this.dom.on('treeSelected', handler);
    },
    _initFields: function (form, toManyArchs) {
        let me = this;
        form.find('field').each(function () {
            let el = $(this);
            if (el.parents('field').length > 0) {
                return;
            }
            let name = el.attr('name'),
                label = el.attr('label'),
                nolabel = el.attr('nolabel'),
                help = el.attr('help'),
                field = me.fields[name],
                colspan = Math.min(el.attr('colspan') || 1, me.cols),
                rowspan = el.attr('rowspan') || 1,
                css = 'form-group col-12',
                attrs = "",
                html = '<div form-field="' + name + '" ';
            if (!field) {
                throw new Error('模型' + me.model + '找不到字段' + name);
            }
            if (field.deny) {
                el.remove();
            } else {
                $.each(this.attributes, function (i, attr) {
                    if (attr.name === 'class') {
                        css += ' ' + attr.value;
                    } else {
                        let v = encodeURI(attr.value);
                        if (['name', 'editor'].indexOf(attr.name) == -1) {
                            html += attr.name + '="' + v + '" ';
                        }
                        if (['t-readonly', 't-visible', 'style', 't-reset'].indexOf(attr.name) == -1) {
                            attrs += attr.name + '="' + v + '" ';
                        }
                    }
                });
                me._fields.push(name);
                if (field.type === 'many2many' || field.type === 'one2many') {
                    toManyArchs[name] = el.html();
                }
                if (el.attr('readonly') == undefined && field.readonly) {
                    html += 'readonly="1" ';
                }
                if (help == undefined) {
                    help = field.help;
                }
                if (help) {
                    attrs += ' data-toggle="tooltip" data-original-title="' + help + '"';
                }
                html += ' style="grid-column:span ' + colspan + ';grid-row:span ' + rowspan + '"';
                html += ' class="' + css + '">';
                if (!eval(nolabel)) {
                    if (!label) {
                        label = field.label || field.name;
                    }
                    label = label.t();
                    html += '<label>' + label + ' </label>';
                    let required = eval(el.attr('required'));
                    if (required == undefined) {
                        required = field.required;
                    }
                    if (required) {
                        html += '<span class="text-danger"> *</span>';
                    }
                }
                html += '<div data-field="' + name + '"  ' + attrs + '> </div>';
                html += '<span class="invalid-feedback"></span></div>';
                el.replaceWith(html);
            }
        });
    },
    _initGroup: function (form) {
        let me = this;
        form.find('group').each(function () {
            let group = $(this), css = 'field-group', attrs = '', cols = eval(group.attr("colspan") || me.cols), styles = 'grid-column:span ' + cols + ';';
            $.each(this.attributes, function (i, attr) {
                if (attr.name === 'class') {
                    css += ' ' + attr.value;
                } else if (attr.name === 'style') {
                    styles += ' ' + attr.value;
                } else {
                    let v = encodeURI(attr.value);
                    attrs += attr.name + '="' + v + '" ';
                }
            });
            let label = group.attr('label');
            if (label) {
                label = label.t();
            }
            let expander = eval(group.attr('collapsable')) ? '<div class="group-expender"><span role="button" class="fa fa-chevron-up"></span></div>' : '';
            let html = `<div class="${css}" style="${styles}" ${attrs}>
                            <div class="group-header border-bottom">${label + expander}</div>
                            <div class="group-body"><div class="group-content grid" style="grid-template-columns:repeat(' + cols + ', 1fr)">${group.prop('innerHTML')}</div></div>
                        </div>`;
            group.replaceWith(html);
        });
    },
    _initTabs: function (form) {
        let me = this;
        form.find('tabs').each(function () {
            let tabs = $(this), nav = '', content = '', tabAttrs = '', tabStyle = 'grid-column:span ' + me.cols + ';';
            $.each(this.attributes, function (i, attr) {
                if (attr.name === 'style') {
                    tabStyle += ' ' + attr.value;
                } else {
                    let v = encodeURI(attr.value);
                    tabAttrs += attr.name + '="' + v + '" ';
                }
            });
            tabs.children('tab').each(function (i) {
                let tab = $(this);
                if (tab.children().length > 0) {
                    let label = tab.attr('label'), id = 'tab-' + jdoo.nextId(), active = nav ? '' : ' active', show = nav ? '' : ' show', attrs = '', css = '';
                    if (label) {
                        label = label.t();
                    }
                    $.each(this.attributes, function (i, attr) {
                        if (attr.name === 'class') {
                            css += ' ' + attr.value;
                        } else {
                            let v = encodeURI(attr.value);
                            if (attr.name != 'id') {
                                attrs += attr.name + '="' + v + '" ';
                            }
                        }
                    });
                    nav += `<li class="nav-item" ${attrs}>
                                <a class="nav-link${active + css}" id="${id}-tab" data-toggle="pill" href="#${id}" role="tab" aria-controls="${id}" aria-selected="true">${label}</a>
                            </li>`;

                    content += `<div class="tab-pane fade${show + active}" ${attrs} id="${id}" role="tabpanel" aria-labelledby="${id}-tab">
                                    <div class="grid mt-3" style="grid-template-columns:repeat(' + me.cols + ', 1fr)">${tab.prop('innerHTML')}</div>
                                </div>`;
                }
            });
            if (nav) {
                let html = `<div style="${tabStyle}" ${tabAttrs}>
                                <ul class="nav nav-tabs col-12" role="tablist">${nav}</ul>
                                <div class="tab-content col-12">${content}</div>
                            </div>`;
                tabs.replaceWith(html);
            } else {
                tabs.remove();
            }
        });
    },
    _updateEditorState: function () {
        let me = this, toUpdate = me.dom.find('[t-visible],[t-readonly]');
        if (toUpdate.length > 0) {
            let data = me.getData();
            data.id = data.id || me.dataId;
            toUpdate.each(function () {
                let e = $(this), visible = e.attr('t-visible'), readonly = e.attr('t-readonly');
                if (visible) {
                    data.__test_visible = new Function("return " + decodeURI(visible));
                    if (data.__test_visible()) {
                        e.show();
                    } else {
                        e.hide();
                    }
                }
                if (readonly) {
                    data.__test_readonly = new Function("return " + decodeURI(readonly));
                    let fname = e.attr('form-field'), edt = me.getEditor(fname);
                    if (edt) {
                        edt.setReadonly(data.__test_readonly());
                    }
                }
            });
        }
    },
    onSelected: function (handler) {
        this.dom.on('selected', handler);
    },
    getFields: function () {
        return this._fields;
    },
    getSelected: function () {
        return this.sel;
    },
    setData: function (data) {
        let me = this;
        $.each(me.getFields(), function (i, fname) {
            me.getEditor(fname).setValue(data[fname]);
        });
        if (data.id) {
            me.sel = [data.id];
        } else {
            me.sel = [];
        }
        me.dataId = data.id || '';
        me._updateEditorState();
        me.clearInvalid();
        me.dom.triggerHandler("selected", [me, me.sel]);
    },
    setInvalid: function (field, error) {
        let me = this;
        me.dom.find('[form-field=' + field + '] .invalid-feedback').html(error).show();
        me.dom.find('[form-field=' + field + '] .form-control').addClass('is-invalid');
    },
    valid: function (field) {
        let me = this, v = true,
            _valid = function (fname) {
                let editor = me.getEditor(fname), error = [], display = me.dom.find('[form-field=' + fname + ']').css('display'),
                    addError = function (err) {
                        if (err) {
                            error.push(err);
                        }
                    };
                if (display == 'none') {
                    return;
                }
                addError(me.requiredValid(editor));
                if (editor.valid) {
                    addError(editor.valid());
                }
                if (error.length > 0) {
                    me.setInvalid(fname, error.join(";"));
                    v = false;
                }
            };
        me.clearInvalid(field);
        if (field) {
            _valid(field);
        } else {
            $.each(me.getFields(), function (i, fname) {
                _valid(fname);
            });
        }
        return v;
    },
    clearInvalid: function (field) {
        let me = this;
        if (field) {
            me.dom.find('[form-field=' + field + '] .invalid-feedback').empty().hide();
            me.dom.find('[form-field=' + field + '] .form-control').removeClass('is-invalid');
        } else {
            me.dom.find('.invalid-feedback').empty().hide();
            me.dom.find('.form-control').removeClass('is-invalid');
        }
    },
    getData: function () {
        let me = this, data = {};
        $.each(me.getFields(), function (i, fname) {
            data[fname] = me.getEditor(fname).getValue();
        });
        return data;
    },
    getDirtyData: function () {
        let me = this, data = {};
        $.each(me.getFields(), function (i, fname) {
            let edt = me.getEditor(fname);
            if (edt.dirty) {
                if (edt.getRawValue) {
                    data[fname] = edt.getRawValue();
                } else {
                    data[fname] = edt.getValue();
                }
            }
        });
        return data;
    },
    clean: function () {
        let me = this;
        me.dirty = false;
        $.each(me.getFields(), function (i, fname) {
            me.getEditor(fname).dirty = false;
        });
    },
    getRawData: function () {
        let me = this, data = {};
        $.each(me.getFields(), function (i, fname) {
            let e = me.getEditor(fname);
            if (e.getRawValue) {
                data[fname] = e.getRawValue();
            } else {
                data[fname] = e.getValue();
            }
        });
        return data;
    },
    getEditor: function (name) {
        let e = this.editors[name];
        if (!e) {
            throw new Error("找不到name=[" + name + "]的editor");
        }
        return e;
    },
    createChild: function () {
        let me = this;
        if (me.treeview) {
            let sel = me.treeview.ztree.getSelectedNodes()[0];
            if (sel) {
                delete view.urlHash.id;
                let data = {};
                $.each(me.getFields(), function () {
                    let field = me.fields[this];
                    if (field.defaultValue) {
                        data[this] = field.defaultValue;
                    }
                });
                data[me.treeview.parentField] = [sel.id, sel[me.treeview.presentField]];
                me.setData(data);
            }
        }
    },
    create: function (values) {
        let me = this, data = {};
        $.each(me.getFields(), function () {
            let field = me.fields[this];
            data[this] = field.defaultValue;
        });
        $.extend(true, data, values);
        me.setData(data);
    },
    load: function () {
        let me = this;
        if (me.treeview) {
            me.treeview.load();
        }
        me.ajax(me, function (r) {
            me.setData(r.data);
            me.clean();
        });
    }
});