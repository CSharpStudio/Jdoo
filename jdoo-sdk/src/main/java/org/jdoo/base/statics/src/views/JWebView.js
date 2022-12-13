$.component('JWebView', {
    icon: { 'grid': 'fa-list-ul', 'card': 'fa-th-large' },
    getViewTpl: function () {
        let me = this, viewContents = '', getViewType = function () {
            let viewSwitch = '';
            if (me.viewtypes.length > 1) {
                viewSwitch += '<div class="btn-group btn-group-toggle" data-toggle="buttons">';
                $.each(me.viewtypes, function (i, viewtype) {
                    let active = me.urlHash.viewtype === viewtype || i === 0;
                    viewSwitch += `<label role="radio-view-type" data="${viewtype}" class="btn btn-sm btn-secondary${active ? ' active' : ''}">
                                        <input type="radio" name="options" autocomplete="off"${active ? ' checked="checked"' : ''}/>
                                        <i class="fa ${me.icon[viewtype]}"></i>
                                    </label>`;
                });
                viewSwitch += '</div>';
            }
            return viewSwitch;
        };
        $.each(me.viewtypes, function (i, v) {
            viewContents += '<div role="' + v + '"></div>';
        });
        return `<div class="view-panel">
                    <div class="header">
                        <div class="content-header">
                            <div class="container-fluid">
                                <div role="search"></div>
                            </div>
                            <div class="btn-row">
                                <div role="toolbar" class="toolbar"></div>
                                <div class="btn-toolbar float-right toolbar-right">
                                    <div role="pager" class="ml-2"></div>
                                    <div role="view-type" class="ml-2">${getViewType()}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="content">
                        <aside role="search-panel" class="left-aside border-right" style="display:none"></aside>
                        <div class="view-content container-fluid">${viewContents}</div>
                    </div>
                </div>`;
    },
    getFormTpl: function () {
        return `<div class="form-panel">
                    <div class="content-header">
                        <div class="btn-row">
                            <div role="form-toolbar" class="toolbar"></div>
                            <div class="btn-toolbar">
                                <div role="data-nav" class="ml-2"></div>
                                <div class="back"></div>
                            </div>
                        </div>
                    </div>
                    <div class="content">
                        <div role="form" class="form-content">
                        </div>
                    </div>
                </div>`;
    },
    getTpl: function () {
        let me = this, tpl = '';
        if (me.viewtypes.length > 0) {
            tpl += me.getViewTpl();
        }
        if (me.views.form) {
            tpl += me.getFormTpl();
        }
        if (me.views.custom) {
            tpl += '<div class="custom-panel"></div>';
        }
        return tpl;
    },
    new: function (opt) {
        let me = this;
        jdoo.utils.apply(true, me, opt);
        $.event.trigger("viewLoaded", [me]);
        me.init();
        me.render();
    },
    load: function () {
        let me = this;
        if (me.curView && me.curView.load) {
            me.curView.load();
        }
    },
    render: function () {
        let me = this;
        me.urlHash = jdoo.web.getParams(window.location.hash.substring(1));
        me.viewtypes = me.urlHash.view.split(',');
        me.viewkey = me.urlHash.key;
        me.viewtypes.remove('form');
        me.viewtypes.remove('custom');
        me.dom.html(me.getTpl())
            .find('[role=radio-view-type]').on('click', function (i) {
                let viewtype = $(this).attr('data');
                if (viewtype != me.viewtype) {//click触发两次
                    me.changeView(viewtype);
                }
            });
        if (me.viewtypes.length > 0) {
            if (!me.search) {
                let resizeContent = function () {
                    let h = me.dom.find('.view-panel .header').height();
                    me.dom.find('.view-panel .content').css('height', 'calc(100% - ' + h + 'px)');
                };
                me.search = me.dom.find('[role=search]').JSearch({
                    model: me.model,
                    module: me.module,
                    arch: me.views.search.arch,
                    fields: me.fields,
                    submitting: function (e, search) {
                        resizeContent();
                        me.pager.reset();
                        me.load();
                    }
                });
                resizeContent();
                $(window).on('resize', function () {
                    resizeContent();
                });
            }
            if (!me.pager) {
                me.pager = me.dom.find('[role=pager]').JPager({
                    limitChange: function (e, pager) {
                        if (me.curView) {
                            me.curView.limit = pager.limit;
                        }
                    },
                    pageChange: function (e, pager) {
                        me.load();
                    },
                    counting: function (e, pager) {
                        jdoo.rpc({
                            model: me.model,
                            module: me.module,
                            method: "count",
                            args: {
                                criteria: me.search.getCriteria()
                            },
                            onsuccess: function (r) {
                                me.pager.update({
                                    total: r.data
                                });
                            }
                        });
                    }
                });
            }
        }
        me.data = {};
        me.changeView();
        me.dom.trigger("render", [me]);
    },
    changeView: function (viewtype) {
        let me = this;
        let m = viewtype || me.urlHash.viewtype;
        if (!m && me.urlHash.view) {
            m = me.urlHash.view.split(',')[0];
        }
        if (me.viewtype !== m) {
            if (m === 'grid') {
                me.showGridView();
            } else if (m === 'card') {
                me.showCardView();
            } else if (m === 'form') {
                me.showForm();
            } else if (m === 'custom') {
                me.showCustom();
            }
            me.viewtype = m;
            me.urlHash.viewtype = me.viewtype;
            me.dom.trigger("viewChanged", [me.viewtype, me]);
        }
        me.updateHash();
    },
    updateHash: function () {
        let me = this;
        window.location.hash = $.param(me.urlHash);
        let p = jdoo.web.getParams(top.window.location.hash.substring(1));
        if (p.u) {
            p.u = window.location.pathname + '#' + $.param($.extend($.param(unescape(p.u)), me.urlHash));
        }
        top.window.location.hash = $.param(p);
    },
    showGridView: function () {
        let me = this;
        if (!me.grid) {
            me.grid = me.dom.find('[role=grid]').JGrid({
                model: me.model,
                module: me.module,
                arch: me.views.grid.arch,
                fields: me.fields,
                search: me.search,
                pager: me.pager,
                rowDblClick: function (e, grid, id) {
                    me.urlHash.id = id;
                    me.changeView('form');
                },
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
                    jdoo.rpc({
                        model: me.model,
                        module: me.module,
                        method: "search",
                        args: {
                            criteria: me.search.getCriteria(),
                            nextTest: true,
                            offset: me.pager.getOffest(),
                            limit: me.pager.getLimit(),
                            fields: grid.getFields(),
                            order: grid.getSort()
                        },
                        context: {
                            usePresent: true
                        },
                        onsuccess: function (r) {
                            if (r.data.values.length > 0) {
                                let len = me.pager.getOffest() + r.data.values.length;
                                if (r.data.hasNext === false) {
                                    me.pager.update({
                                        to: len,
                                        next: false,
                                        total: len
                                    });
                                } else {
                                    me.pager.update({
                                        to: len,
                                        next: true
                                    });
                                }
                            } else {
                                me.pager.hide();
                            }
                            callback({
                                data: r.data.values
                            });
                            me.data = {};
                            $.each(r.data.values, function (i, v) {
                                me.data[v['id']] = v;
                            });
                        }
                    });
                }
            });
            me.dom.trigger("viewCreated", ['grid', me]);
        } else {
            me.pager.limit = me.grid.limit;
            me.grid.load();
        }
        me.toolbar = me.dom.find('[role=toolbar]').JToolbar({
            arch: me.grid.tbarArch,
            auths: me.auths,
            defaultButtons: 'create|edit|delete|export',
            target: me
        });
        me.curView = me.grid;
        me.showView('grid');
    },
    showCardView: function () {
        let me = this;
        if (!me.card) {
            me.card = me.dom.find('[role=card]').JCard({
                model: me.model,
                module: me.module,
                arch: me.views.card.arch,
                fields: me.fields,
                search: me.search,
                pager: me.pager,
                dblClick: function (card, id) {
                    me.urlHash.id = id;
                    me.changeView('form');
                },
                selected: function (e, card, sel) {
                    let selected = [];
                    $.each(sel, function (i, id) {
                        selected.push(me.data[id]);
                    });
                    if (me.toolbar) {
                        me.toolbar.update(selected);
                    }
                },
                ajax: function (card, callback) {
                    jdoo.rpc({
                        model: me.model,
                        module: me.module,
                        method: "search",
                        args: {
                            criteria: me.search.getCriteria(),
                            nextTest: true,
                            offset: me.pager.getOffest(),
                            limit: me.pager.getLimit(),
                            fields: card.getFields()
                        },
                        context: {
                            usePresent: true
                        },
                        onsuccess: function (r) {
                            if (r.data.values.length > 0) {
                                let len = me.pager.getOffest() + r.data.values.length;
                                if (r.data.hasNext === false) {
                                    me.pager.update({
                                        to: len,
                                        next: false,
                                        total: len
                                    });
                                } else {
                                    me.pager.update({
                                        to: len,
                                        next: true
                                    });
                                }
                            } else {
                                me.pager.hide();
                            }
                            callback({
                                data: r.data.values
                            });
                            me.data = {};
                            $.each(r.data.values, function (i, v) {
                                me.data[v['id']] = v;
                            });
                        }
                    });
                }
            });
            me.dom.trigger("viewCreated", ['card', me]);
        } else {
            me.pager.limit = me.card.limit;
            me.card.load();
        }
        me.toolbar = me.dom.find('[role=toolbar]').JToolbar({
            arch: me.card.tbarArch,
            auths: me.auths,
            defaultButtons: 'create|edit|delete|export',
            target: me
        });
        me.curView = me.card;
        me.showView('card');
    },
    showView: function (name) {
        let me = this;
        me.dom.find('.form-panel,.custom-panel').hide();
        me.dom.find('.view-panel').show();
        $.each(me.viewtypes, function (i, v) {
            me.dom.find('[role=' + v + ']').hide();
        });
        me.dom.find('[role=' + name + ']').show();
    },
    showCustom: function () {
        let me = this;
        if (!me.custom) {
            me.custom = me.dom.find('.custom-panel').JCustom({
                model: me.model,
                module: me.module,
                arch: me.views.custom.arch,
                fields: me.fields,
            });
            me.dom.trigger("viewCreated", ['custom', me]);
        }
        me.curView = me.custom;
        me.dom.find('.custom-panel').show();
        me.dom.find('.view-panel,.form-panel').hide();
    },
    showForm: function () {
        let me = this,
            back = me.viewtype;
        if (!me.form) {
            me.form = me.dom.find('[role=form]').JForm({
                arch: me.views.form.arch,
                model: me.model,
                module: me.module,
                fields: me.fields,
                treeSelected: function (e, tree, sel) {
                    if (me.toolbar) {
                        me.toolbar.update(sel);
                    }
                },
                selected: function (e, form, sel) {
                    if (me.toolbar) {
                        let selected = sel.length > 0 ? [form.getData()] : [];
                        me.toolbar.update(selected);
                    }
                },
                ajax: function (form, callback) {
                    if (me.urlHash.id) {
                        jdoo.rpc({
                            model: me.model,
                            module: me.module,
                            method: "read",
                            args: {
                                ids: [me.urlHash.id],
                                fields: me.form.getFields()
                            },
                            context: {
                                usePresent: true
                            },
                            onsuccess: function (r) {
                                callback({ data: r.data[0] });
                            }
                        });
                    } else {
                        callback({ data: {} });
                    }
                },
            });
            if (me.viewtypes.length > 0) {
                me.dom.find('.form-panel .back').append(`<button role="form-close" class="btn" type="button" >${'返回'.t()}</button>`);
                me.dom.find('[role=form-close]').on('click', function () {
                    me.changeView(back || me.urlHash.view.split(',')[0]);
                });
            }
            me.dom.trigger("viewCreated", ['form', me]);
        }
        me.toolbar = me.dom.find('[role=form-toolbar]').JToolbar({
            arch: me.form.tbarArch,
            auths: me.auths,
            defaultButtons: me.form.isTree ? 'create|createChild|save|delete' : 'create|save',
            target: me
        });
        me.curView = me.form;
        me.dom.find('.form-panel').show();
        me.dom.find('.view-panel,.custom-panel').hide();
        me.form.load();
    },
    getSelected: function () {
        return this.curView.getSelected();
    },
    save: function () {
        let me = this;
        if (me.curView != null) {
            if (!me.curView.valid()) return;
            me.busy(true);
            if (me.urlHash.id) {
                jdoo.rpc({
                    model: me.model,
                    module: me.module,
                    method: "update",
                    args: {
                        ids: [me.urlHash.id],
                        values: me.curView.getDirtyData(),
                    },
                    onerror: function (e) {
                        me.busy(false);
                        jdoo.msg.error(e);
                    },
                    onsuccess: function (r) {
                        me.busy(false);
                        jdoo.msg.show('保存成功'.t());
                        me.load();
                    }
                });
            } else {
                jdoo.rpc({
                    model: me.model,
                    module: me.module,
                    method: "create",
                    args: me.curView.getData(),
                    onerror: function (e) {
                        me.busy(false);
                        jdoo.msg.error(e);
                    },
                    onsuccess: function (r) {
                        me.urlHash.id = r.data;
                        me.busy(false);
                        jdoo.msg.show('保存成功'.t());
                        window.location.hash = $.param(me.urlHash);
                        me.load();
                    }
                });
            }
        }
    },
    busy: function (busy) {
        if (busy) {
            $(document.body).append(`<div id="pageWaitingModal" class="modal" data-keyboard="false" data-backdrop="static" data-role="dialog" aria-labelledby="pageWaitingModalLabel" aria-hidden="true">
                                        <div id="loading" class="loading">${'加载中,请稍等'.t()}</div>
                                    </div>`);
            $('#pageWaitingModal').modal('show');
        } else {
            $('#pageWaitingModal').modal('hide').remove();
        }
    },
    delete: function () {
        let me = this;
        jdoo.rpc({
            model: me.model,
            module: me.module,
            method: 'delete',
            args: {
                ids: me.getSelected()
            },
            onsuccess: function (r) {
                jdoo.msg.show('删除成功'.t());
                delete me.urlHash.id;
                me.load();
            }
        });
    },
    edit: function () {
        let me = this;
        me.urlHash.id = me.getSelected()[0];
        me.changeView('form');
    },
    create: function () {
        let me = this;
        delete me.urlHash.id;
        me.changeView('form');
        me.form.create();
    },
    copy: function () {
        let me = this, ids = me.getSelected();
        me.busy(true);
        delete me.urlHash.id;
        me.changeView('form');
        jdoo.rpc({
            model: me.model,
            module: me.module,
            method: "copy",
            args: {
                ids: ids
            },
            onerror: function (e) {
                me.busy(false);
                jdoo.msg.error(e);
            },
            onsuccess: function (r) {
                me.urlHash.id = r.data[0];
                me.busy(false);
                jdoo.msg.show('保存成功'.t());
                window.location.hash = $.param(me.urlHash);
                me.form.load();
            }
        });
    },
    postSvc: function (svc, ids) {
        let me = this;
        if (!ids) {
            ids = me.getSelected();
        }
        me.busy(true);
        jdoo.rpc({
            model: me.model,
            module: me.module,
            method: svc,
            args: {
                ids: ids
            },
            onerror: function (e) {
                me.busy(false);
                jdoo.msg.error(e);
            },
            onsuccess: function (r) {
                me.busy(false);
                let d = r.data || {};
                if (d.message) {
                    jdoo.msg.show(d.message);
                }
                if (d.action === 'js') {
                    eval(d.script);
                } else if (d.action === 'reload') {
                    me.load();
                } else if (d.action === 'service') {
                    //TODO
                } else if (d.action === 'dialog') {
                    //TODO
                } else if (d.action === 'view') {
                    //TODO
                }
            }
        });
    }
});



window.loadWebView = function () {
    let me = this, ps = jdoo.web.getParams(window.location.hash.substring(1));
    jdoo.rpc({
        model: "ir.ui.view",
        method: "loadView",
        args: {
            model: ps.model,
            type: ps.view,
            key: ps.key
        },
        onsuccess: function (r) {
            if (r.data.resource) {
                $("head").append(r.data.resource);
            }
            window.view = $('body').JWebView(r.data);
        }
    });
}
$(function () {
    loadWebView();
    if (window.history && window.history.pushState) {
        $(window).on('popstate', function () {
            if (window.view) {
                window.view.urlHash = jdoo.web.getParams(window.location.hash.substring(1));
                window.view.changeView();
            }
        });
    }
    $('html,body').css("height", "100%");
    $(document).on('click', function () {
        top.window.$('.dropdown-menu').removeClass('show');
    });
});