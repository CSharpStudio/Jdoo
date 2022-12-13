$.component("JToolbar", {
    defaultButtons: 'create|edit|delete',
    defaultButtonCss: 'btn-info',
    buttonsTpl: {
        'create': `<button name="create" class="btn-success" t-click="target.create()">${'创建'.t()}</button>`,
        'createChild': `<button name="createChild" auth="create" class="btn-success" t-enable="id" t-click="target.curView.createChild()">${'创建子'.t()}</button>`,
        'copy': `<button name="copy" ref="create" auth="create" t-enable="id" class="btn-success" t-click="target.copy()">${'复制'.t()}</button>`,
        'edit': `<button name="edit" auth="update" class="btn-info" t-enable="id" t-click="target.edit()">${'编辑'.t()}</button>`,
        'delete': `<button name="delete" ref="edit" t-enable="ids" class="btn-danger" t-click="target.delete()" confirm="${'确定删除?'.t()}">${'删除'.t()}</button>`,
        'save': `<button name="save" auth="create|update" class="btn-info" t-click="target.save()">${'保存'.t()}</button>`,
        'saveAndNew': '',
        'import': '',
        'export': ''
    },
    init: function () {
        let me = this, dom = me.dom, tbar = jdoo.utils.parseXML(me.arch).find('toolbar');
        dom.empty();
        if (tbar.length == 0) return;
        tbar.prepend(me._getDefaultButtons(tbar.attr('buttons') || me.defaultButtons));
        tbar.find('button').each(function () {
            let btn = $(this),
                name = btn.attr('name') || btn.attr('service') || '' + jdoo.nextId(),
                auth = btn.attr('auth') || name || '',
                cls = btn.attr('class'),
                allow = me.auths === "@all";
            btn.attr('auth', auth);
            if (!allow) {
                $.each(auth.split('|'), function () {
                    if (me.auths.indexOf(this) > -1) {
                        allow = true;
                        return true;
                    }
                });
            }
            if (allow) {
                if (!btn.hasClass('btn')) {
                    btn.addClass('btn');
                }
                if (!cls || cls.indexOf('btn-') == -1) {
                    btn.addClass(me.defaultButtonCss);
                }
                let label = btn.attr('label') || '';
                btn.append(label.t());
                btn.attr('name', 'btn_' + name);
                if (!btn.attr('type')) {
                    btn.attr('type', 'button');
                }
                btn.replaceWith('<div name="btn_' + name + '_group" class="btn-group mr-1">' + btn.prop("outerHTML") + '</div>');
            } else {
                btn.replaceWith('');
            }
        });
        tbar.find('button[ref]').each(function () {
            let btn = $(this);
            let ref = btn.attr('ref');
            let group = tbar.find('div[name=btn_' + ref + '_group]');
            if (group.length > 0) {
                btn.attr('class', 'dropdown-item');
                let drop = group.find('dropdown-menu');
                if (drop.length > 0) {
                    drop.append(btn.prop("outerHTML"));
                } else {
                    let cls = group.find('button[name=btn_' + ref + ']').attr('class');
                    cls = cls.replace('disabled', '');
                    group.append('<button type="button" class="' + cls + ' dropdown-toggle dropdown-icon" data-toggle="dropdown"> </button>')
                    group.append('<div class="dropdown-menu" role="menu">' + btn.prop("outerHTML") + '</div>');
                }
                tbar.find('div[name=' + btn.attr('name') + '_group]').remove();
            }
        });
        dom.html(tbar.html())
            .off("click")
            .on("click", 'button[service]', function () {
                let btn = $(this);
                let cfm = btn.attr('confirm');
                if (cfm) {
                    jdoo.msg.confirm({
                        content: cfm,
                        submit: function () {
                            view.postSvc(btn.attr('service'));
                        }
                    });
                } else {
                    view.postSvc(btn.attr('service'));
                }
            }).on('click', 'button[click]', function () {
                let btn = $(this);
                let click = btn.attr('t-click');
                let cfm = btn.attr('confirm');
                if (cfm) {
                    jdoo.msg.confirm({
                        content: cfm,
                        submit: function () {
                            new Function('target', click)(me.target);
                        }
                    });
                } else {
                    new Function('target', click)(me.target);
                }
            });
        me.update([]);
    },
    update: function (data) {
        this.dom.find('button[t-enable]').each(function () {
            let btn = $(this), expr = btn.attr('t-enable');
            if (expr === 'id') {
                if (data.length != 1) {
                    btn.attr('disabled', true);
                } else {
                    btn.attr('disabled', false);
                }
            } else if (expr === 'ids') {
                if (data.length > 0) {
                    btn.attr('disabled', false);
                } else {
                    btn.attr('disabled', true);
                }
            } else {
                let active = true;
                if (expr.startsWith("ids:")) {
                    if (data.length == 0) {
                        active = false;
                    } else {
                        expr = expr.substring(4);
                        for (let i = 0; i < data.length; i++) {
                            let d = data[i];
                            d.__test_active = new Function("return " + expr);
                            if (!d.__test_active()) {
                                active = false;
                                break;
                            }
                        }
                    }
                } else {
                    if (expr.startsWith("id:")) {
                        expr = expr.substring(3);
                    }
                    if (data.length !== 1) {
                        active = false;
                    } else {
                        let d = data[0];
                        d.__test_active = new Function("return " + expr);
                        if (!d.__test_active()) {
                            active = false;
                        }
                    }
                }
                if (active) {
                    btn.attr('disabled', false);
                } else {
                    btn.attr('disabled', true);
                }
            }
        });
    },
    _getDefaultButtons: function (btns) {
        let me = this, html = '', addBtn = function (name) {
            let btn = me.buttonsTpl[name];
            if (btn) {
                html += btn;
            }
        }
        $.each(btns.split('|'), function (i, e) {
            if (e === 'default') {
                $.each(me.defaultButtons.split('|'), function (i, e) {
                    addBtn(e);
                });
            }
            addBtn(e);
        });
        return html;
    }
});