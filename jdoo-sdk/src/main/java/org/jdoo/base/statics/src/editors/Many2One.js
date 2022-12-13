jdoo.editor('many2one', {
    limit: 10,
    link: true,
    getTpl: function () {
        return `<div class="input-group" id="${this.name + '-' + jdoo.nextId()}">
                    <input type="text" class="form-control lookup"/>
                    <div class="container-fluid dropdown-lookup search-dropdown">
                        <div class="lookup-body"></div>
                        <div class="card-footer">
                            <button type="button" data-btn="clear" class="btn btn-sm btn-default">${'清空'.t()}</button>
                            <div class="btn-group float-right">
                                <button type="button" data-btn="prev" class="btn btn-sm btn-default">
                                    <i class="fa fa-angle-left"></i>
                                </button>
                                <button type="button" data-btn="next" class="btn btn-sm btn-default">
                                    <i class="fa fa-angle-right"></i>
                                </button>
                            </div>
                        </div>
                    </div>`+ (this.link ? `<div class="input-group-append">
                    <button type="button" data-btn="link" class="btn btn-default">
                        <i class="fa fa-external-link-alt"></i>
                    </button>
                </div></div>` : '</div>');
    },
    init: function () {
        let me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        me.link = eval(dom.attr('link') || me.link);
        me.offset = 0;
        me.keyword = '';
        dom.html(me.getTpl())
            .on('click', '.lookup-create', function () {
                me.showLinkModel({ present: dom.find('input').val() });
            }).on('click', '[data-btn=link]', function (e) {
                let id = dom.find('input').attr('data-value');
                me.showLinkModel({ id: id });
            }).on('click', '.dropdown-lookup', function (e) {
                me.dropclick = true;
            }).on('click', '.lookup', function (e) {
                if ($(this).attr('readonly')) {
                    return;
                }
                me.showDropdown();
                me.lookup();
                e.preventDefault();
                e.stopPropagation();
            }).on('click', '[data-btn=clear]', function (e) {
                me.offset = 0;
                me.keyword = '';
                me.setValue();
                dom.find("input").triggerHandler("change");
            }).on('click', '[data-btn=next]', function (e) {
                me.offset += me.limit;
                me.lookup();
            }).on('click', '[data-btn=prev]', function (e) {
                me.offset -= me.limit;
                if (me.offset < 0) {
                    me.offset = 0;
                }
                me.lookup();
            });;
        $(document).on('click', function () {
            if (me.dropclick) {
                me.dropclick = false;
            } else {
                me.hideDropdown();
            }
        });
        let timer;
        dom.find('input').keyup(function () {
            let input = $(this);
            if (input.attr('readonly')) {
                return;
            }
            if (!me.open) {
                me.showDropdown();
            }
            clearTimeout(timer);
            timer = setTimeout(function () {
                me.offset = 0;
                me.keyword = input.val();
                me.lookup();
            }, 500);
        });
    },
    onValueChange: function (handler) {
        let me = this;
        me.dom.find('input').on('change', function (e) {
            handler(e, me);
        });
    },
    setReadonly: function (v) {
        if (v) {
            this.dom.find('input').attr('readonly', true);
        } else {
            this.dom.find('input').removeAttr('readonly');
        }
    },
    getValue: function () {
        let me = this, el = me.dom.find('input'), val = el.attr('data-value');
        if (val) {
            return [val, el.attr('data-text')];
        }
        return null;
    },
    getRawValue: function () {
        let me = this, el = me.dom.find('input'), val = el.attr('data-value');
        if (val) {
            return val;
        }
        return null;
    },
    setValue: function (v) {
        let me = this,
            link = me.dom.find('[data-btn=link]'),
            el = me.dom.find('input');
        if (v && v[0]) {
            el.val(v[1]).attr('data-value', v[0]).attr('data-text', v[1]);
            link.attr('disabled', false);
        } else {
            el.val('').attr('data-value', '').attr('data-text', '');
            link.attr('disabled', true);
        }
    },
    showLinkModel: function (rec) {
        let me = this, comodel = me.field.comodel, label = me.dom.attr('label');
        if (!label) {
            label = me.field.label || me.field.name;
        }
        jdoo.showDialog({
            submitText: '保存'.t(),
            title: '打开'.t() + ' ' + label.t(),
            init: function () {
                let dialog = this;
                jdoo.rpc({
                    model: "ir.ui.view",
                    method: "loadView",
                    args: {
                        model: comodel,
                        type: "form"
                    },
                    onsuccess: function (r) {
                        let d = r.data;
                        if (d.auths !== '@all' && d.indexOf('update') == -1 && d.indexOf('create') == -1) {
                            dialog.dom.find('button[role=btn-save]').hide();
                        }
                        dialog.form = dialog.body.JForm({
                            model: comodel,
                            module: me.module,
                            fields: d.fields,
                            arch: d.views.form.arch,
                            ajax: function (form, callback) {
                                jdoo.rpc({
                                    model: me.model,
                                    module: me.module,
                                    method: "searchRelated",
                                    args: {
                                        relatedField: me.field.name,
                                        options: {
                                            limit: 1,
                                            offset: 0,
                                            criteria: [['id', '=', dialog.form.dataId]],
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
                        });
                        if (rec.id) {
                            dialog.form.dataId = rec.id;
                            dialog.form.load();
                        }
                        else {
                            let data = { active: true };
                            data[d.present[0]] = rec.present;
                            dialog.form.create(data);
                        }
                    }
                });
            },
            submit: function () {
                let dialog = this;
                if (!dialog.form.valid()) {
                    return;
                }
                dialog.busy(true);
                let id = dialog.form.dataId, data = dialog.form.getData(), method, args;
                if (id) {
                    method = 'update';
                    args = {
                        ids: [id],
                        values: data,
                    };
                } else {
                    method = 'create';
                    args = data;
                }
                jdoo.rpc({
                    model: me.field.comodel,
                    module: me.module,
                    method: method,
                    args: args,
                    dialog: dialog,
                    onsuccess: function (r) {
                        if (!id) {
                            dialog.form.dataId = r.data;
                        }
                        dialog.form.load();
                    }
                });
            }
        });
    },
    lookup: function () {
        let me = this,
            el = me.dom.find('input'),
            filter = me.dom.attr('search') || null,
            body = me.dom.find('.lookup-body');
        body.html('<div class="m-2">' + '加载中'.t() + '</div>');
        let criteria = [["present", "like", me.keyword]];
        if (filter) {
            filter = decodeURI(filter);
            let data = me.owner.getRawData();
            data.__filter = new Function("return " + filter);
            filter = data.__filter();
            $.each(filter, function () {
                criteria.push(this);
            });
        }
        jdoo.rpc({
            model: me.model,
            module: me.module,
            method: "searchRelated",
            args: {
                relatedField: me.field.name,
                options: {
                    limit: me.limit,
                    offset: me.offset,
                    criteria: criteria,
                    nextTest: true,
                    activeTest: true
                }
            },
            onsuccess: function (r) {
                if (r.data.values[0]) {
                    let html = '<div class="select2-container select2-container--default select2-container--open row"><ul class="select2-results__options col-12">';
                    $.each(r.data.values, function () {
                        let sel = this.id === el.attr('data-value') ? ' select2-results__option--highlighted" "aria-selected"="true' : '';
                        html += '<li class="select2-results__option' + sel + '" data-value="' + this.id + '">' + this.present + '</li>';
                    });
                    html += '</ul></div>';
                    body.html(html);
                    body.find('.select2-results__option').hover(function () {
                        body.find('.select2-results__option').removeClass('select2-results__option--highlighted').removeAttr('aria-selected');
                        $(this).addClass('select2-results__option--highlighted').attr('aria-selected', 'true');
                    }, function () {
                    }).on('click', function () {
                        let item = $(this), txt = item.html();
                        me.offset = 0;
                        me.keyword = '';
                        me.setValue([item.attr('data-value'), txt]);
                        me.dom.find("input").triggerHandler("change");
                        me.hideDropdown();
                    });
                } else {
                    let nodata = function () {
                        let html = '<div class="m-2 lookup-nodata">' + '没有数据'.t() + '</div>';
                        if (me.canCreate) {
                            html += '<a class="m-2 lookup-create"><i class="mr-2">' + '创建'.t() + '</i>"' + el.val() + '"</a>';
                        }
                        body.html(html);
                    }
                    if (me.canCreate == undefined) {
                        jdoo.rpc({
                            model: 'rbac.security',
                            method: "canCreate",
                            args: {
                                model: me.field.comodel
                            },
                            onsuccess: function (r) {
                                me.canCreate = r.data;
                                nodata();
                            }
                        });
                    } else {
                        nodata();
                    }
                }
                let nextBtn = me.dom.find('[data-btn=next]');
                if (r.data.hasNext) {
                    nextBtn.attr('disabled', false);
                } else {
                    nextBtn.attr('disabled', true);
                }
            }
        });
    },
    showDropdown: function () {
        let me = this, el = me.dom.find('.dropdown-lookup');
        el.show().addClass('show');
        me.open = true;
    },
    hideDropdown: function () {
        let me = this, el = me.dom.find('.dropdown-lookup');
        el.hide().removeClass('show');
        let input = me.dom.find('input'), v = input.val();
        if (!v) {
            me.setValue();
            me.dom.find("input").triggerHandler("change");
        } else if (v != input.attr('data-text')) {
            input.val(input.attr('data-text'));
        }
        me.offset = 0;
        me.keyword = '';
        me.open = false;
    },
});

jdoo.searchEditor('many2one', {
    extends: "editors.many2one",
    getCriteria: function () {
        var val = this.getRawValue();
        if (val) {
            return [[this.name, '=', val]];
        }
        return [];
    },
    getText: function () {
        return this.dom.find('input').val();
    },
});