$.component("JSearch", {
    getTpl: function () {
        return `<div class="input-group">
                    <div class="input-group-prepend">
                        <button type="button" class="btn btn-default dropdown-toggle" data-btn="dropdown">${'过滤'.t()}</button>
                        <div class="container-fluid dropdown-menu search-dropdown" style="min-width:300px">
                            <div class="search-form"></div>
                            <div class="card-footer">
                                <button data-btn="clear" class="btn btn-outline-secondary" style="margin-right:5px">${'清空'.t()}</button>
                                <button data-btn="reset" class="btn btn-outline-secondary" style="margin-right:5px">${'重置'.t()}</button>
                                <button data-btn="confirm" class="btn btn-info float-right" style="min-width:100px">${'确定'.t()}</button>
                            </div>
                        </div>
                    </div>
                    <div class="jsearch-container">
                        <span class="jsearch-selection">
                            <ul class="jsearch-selection-body"></ul>
                        </span>
                    </div>
                    <div class="input-group-append">
                        <button data-btn="submit" type="submit" class="btn btn-default">
                            <i class="fa fa-search"></i>
                        </button>
                    </div>
                </div>`;
    },
    init: function () {
        let me = this, dom = me.dom;
        me.query = {};
        me.editors = {};
        me._fields = [];
        dom.html(me.getTpl())
            .on('click', '.jsearch-selection', function (e) {
                me.showDropdown();
                e.preventDefault();
                e.stopPropagation();
            }).on('click', '[data-btn=clear]', function () {
                $.each(me._fields, function (i, field) {
                    me.editors[field].setValue('');
                });
                me._updateCriteria();
                el.triggerHandler("submitting", [me]);
            }).on('click', '[data-btn=reset]', function () {
                me.reset();
                dom.triggerHandler("submitting", [me]);
            }).on('click', '[data-btn=confirm]', function () {
                me.confirm();
            }).on('click', '[data-btn=submit]', function () {
                dom.triggerHandler("submitting", [me]);
            }).on('click', '[data-btn=dropdown]', function (e) {
                me.showDropdown();
                e.preventDefault();
                e.stopPropagation();
            }).on('click', '.dropdown-menu', function (e) {
                me.dropclick = true;
            });
        me.dropdown = dom.find('.search-dropdown');
        me.body = dom.find('.jsearch-selection-body');
        $(document).on('click', function () {
            if (me.dropclick) {
                me.dropclick = false;
            } else {
                me.hideDropdown();
            }
        });
        me.initEditors();
        me.onSubmitting(me.submitting);
        me.dropdown.on('keyup', 'input,select', function (e) {
            if (e.keyCode == 13) {
                me.confirm();
            }
        });
        $.event.trigger("searchReady", [me]);
    },
    confirm: function () {
        let me = this;
        me._updateCriteria();
        me.dropdown.removeClass('show');
        me.dom.triggerHandler("submitting", [me]);
        me.hideDropdown();
    },
    onSubmitting: function (handler) {
        this.dom.on("submitting", handler);
    },
    showDropdown: function () {
        let me = this, el = me.dom.find('.dropdown-menu');
        el.show().addClass('show');
        el.find('input:first').focus();
    },
    hideDropdown: function () {
        let me = this, el = me.dom.find('.dropdown-menu');
        el.hide().removeClass('show');
    },
    initEditors: function () {
        let me = this, form = '<div class="row">';
        if (me.arch) {
            let arch = jdoo.utils.parseXML(me.arch).children('search'),
                searchPanel = arch.find('searchpanel');
            if (searchPanel.length > 0) {
                me.panel = $("[role=search-panel]").show().JSearchPanel({
                    arch: searchPanel.prop("outerHTML"),
                    model: me.model,
                    module: me.module,
                    fields: me.fields,
                    selected: function (e, panel, node) {
                        me.dom.triggerHandler("submitting", [me]);
                    },
                    ajax: function (panel, callback) {
                        jdoo.rpc({
                            model: me.model,
                            module: me.module,
                            method: "searchRelated",
                            args: {
                                relatedField: panel.field.name,
                                options: {
                                    criteria: panel.getSelect(),
                                    fields: panel.getFields(),
                                    limit: panel.limit
                                }
                            },
                            onsuccess: function (r) {
                                r.data.values.splice(0, 0, { present: '全部'.t(), id: 'all' });
                                callback(r.data.values);
                            }
                        });
                    }
                });
                me.panel.load();
            }
            searchPanel.remove();
            me.criteria = eval(arch.attr('criteria'))
            let fields = arch.children('field');
            let col = arch.col || (fields.length <= 3 ? 1 : fields.length <= 6 ? 2 : 3);
            me.dropdown.addClass('col-md-' + (col * 4));
            let forms = [];
            for (let i = 0; i < col; i++) {
                forms[i] = '<div class="col-md-' + (12 / col) + ' form-horizontal"><div class="card-body">';
            }
            fields.each(function (i, e) {
                let el = $(e),
                    name = el.attr('name'),
                    label = el.attr('label'),
                    val = el.attr('default'),
                    op = el.attr('op'),
                    criteria = el.attr('criteria'),
                    editor = el.attr('editor'),
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
                    let html = '<div class="form-group"><label>' + label + '</label>'
                        + '<div data-label="' + label + '"'
                        + (val ? ' data-default="' + val + '"' : '')
                        + (op ? ' data-op="' + op + '"' : '')
                        + (editor ? ' data-editor="' + editor + '"' : '')
                        + (criteria ? ' data-criteria="' + encodeURI(criteria) + '"' : '')
                        + ' data-field="' + name + '"></div></div>';
                    forms[i % col] += html;
                }
            });
            for (let i = 0; i < col; i++) {
                form += forms[i] + '</div></div>';
            }
        }
        form += '</div>';
        me.dropdown.prepend(form);
        me.dropdown.find('[data-field]').each(function () {
            let el = $(this),
                fname = el.attr('data-field'),
                field = me.fields[fname],
                editor = el.attr('data-editor') || field.type,
                ctl = jdoo.searchEditors[editor];
            if (!ctl) {
                throw new Error('找不到编辑器:' + editor);
            }
            me.editors[fname] = new ctl({
                dom: el,
                field: field,
                model: me.model,
                module: me.module,
                allowNull: true,
                op: el.attr('data-op'),
                criteria: el.attr('data-criteria'),
                label: el.attr('data-label')
            });
        });
        me.reset();
    },
    _updateCriteria: function () {
        let me = this;
        me.query = {};
        me.body.empty();
        $.each(me._fields, function (i, field) {
            let editor = me.editors[field], criteria = editor.getCriteria();
            if (editor.criteria) {
                let val = editor.getValue();
                if (editor.getRawVal) {
                    val = editor.getRawValue();
                }
                if (val) {
                    let expr = decodeURI(editor.criteria),
                        f = new Function("value", "return " + expr + ";");
                    criteria = f(val);
                }
            }
            if (criteria.length > 0) {
                me.add(field, editor.label, editor.getText(), criteria);
            }
        });
    },
    reset: function () {
        let me = this;
        $.each(me._fields, function (i, field) {
            me.editors[field].setValue('');
        });
        me.dropdown.find('[data-default]').each(function () {
            let e = $(this),
                val = e.attr('data-default'),
                fname = e.attr('data-field'),
                editor = me.editors[fname];
            editor.setValue(val);
        });
        me._updateCriteria();
    },
    add: function (field, label, text, expr) {
        let me = this;
        me.query[field] = expr;
        let svg = `<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512"><path d="M256 48C141.31 48 48 141.31 48 256s93.31 208 208 208s208-93.31 208-208S370.69 48 256 48zm75.31 260.69a16 16 0 1 1-22.62 22.62L256 278.63l-52.69 52.68a16 16 0 0 1-22.62-22.62L233.37 256l-52.68-52.69a16 16 0 0 1 22.62-22.62L256 233.37l52.69-52.68a16 16 0 0 1 22.62 22.62L278.63 256z" fill="currentColor"></path></svg>`
        let html = `
            <li class="jsearch-choice"  data-field="${field}">
                <p class="filter-name">${label}</p>
                <p class="filter-value"><span>${text}</span><span class="jsearch-choice-remove" role="presentation">${svg}</span></p>
            </li>
        `;
        me.body.append(html);
        let el = me.body.find('[data-field=' + field + ']');
        el.on('click', function (e) { e.stopPropagation(); });
        el.find('.jsearch-choice-remove').on('click', function (e) {
            me.remove(field);
            me.dom.triggerHandler("submitting", [me]);
            e.stopPropagation();
        });
    },
    remove: function (field) {
        let me = this;
        delete me.query[field];
        me.editors[field].setValue('');
        me.body.find('[data-field=' + field + ']').remove();
    },
    setCriteria: function () {

    },
    getCriteria: function () {
        let me = this, criteria = [], vals = Object.values(me.query);
        $.each(vals, function () {
            $.each(this, function () {
                criteria.push(this);
            });
        });
        if (me.criteria) {
            $.each(me.criteria, function () {
                criteria.push(this);
            });
        }
        if (me.panel) {
            $.each(me.panel.getCriteria(), function () {
                criteria.push(this);
            });
        }
        return criteria;
    }
});