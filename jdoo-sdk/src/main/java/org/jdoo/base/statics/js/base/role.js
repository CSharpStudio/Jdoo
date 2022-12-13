//@ sourceURL=role.js
jdoo.component('base.Role', {
    globaleId: 0,
    getId: function () {
        return this.globaleId++;
    },
    init: function () {
        let me = this;
        me.view.dom.on('click', '[role=set-admin]', function () {
            me.setAdmin(true);
        }).on('click', '[role=revoke-admin]', function () {
            me.setAdmin(false);
        }).on('click', '[role=edit-permission]', function () {
            me.editPermission();
        });
    },
    setAdmin: function (isAdmin) {
        let me = this;
        jdoo.rpc({
            model: "rbac.role",
            method: "setAdmin",
            args: { ids: me.view.getSelected(), isAdmin: isAdmin },
            onsuccess: function (r) {
                jdoo.msg.show('保存成功'.t());
                me.view.load();
            }
        });
    },
    editPermission: function () {
        let me = this, roleId = me.view.getSelected()[0];
        if (!roleId) return;
        jdoo.showDialog({
            title: '角色'.t() + '<span role="role-name"></span>' + '权限'.t(),
            init: function () {
                let dialog = this;
                jdoo.rpc({
                    model: "rbac.role",
                    method: "getPresent",
                    args: { ids: [roleId] },
                    onsuccess: function (r) {
                        dialog.dom.find('[role=role-name]').html('[' + r.data[0][1] + ']');
                    }
                });
                me.loadPermission(dialog, roleId);
            },
            submit: function () {
                let dialog = this;
                dialog.busy(true);
                var permission = [], menus = [];
                dialog.body.find('.check-per').each(function () {
                    var input = $(this);
                    if (input.is(":checked")) {
                        permission.push(input.attr('data-id'));
                    }
                });
                dialog.body.find('.check-menu').each(function () {
                    var input = $(this);
                    if (input.is(":checked")) {
                        menus.push(input.attr('data-id'));
                    }
                });
                jdoo.rpc({
                    model: "rbac.role",
                    method: "savePermission",
                    args: { ids: me.view.getSelected(), permissions: permission, menus: menus },
                    dialog: dialog
                });
            }
        });
    },
    loadPermission: function (dialog, roleId) {
        let me = this, body = dialog.body;
        jdoo.rpc({
            model: "rbac.role",
            method: "loadPermissionList",
            args: {},
            onsuccess: function (r) {
                var html = `<div class="mb-3">
                                <input type="checkbox" id="showCode" class="mr-1"/><label for="showCode">显示编码</label>
                                <button class="btn btn-default btn-sm btn-expand float-right"><i class="fas fa-chevron-down"></i></button>
                                <button class="btn btn-default btn-sm btn-collapse float-right mr-1"><i class="fas fa-chevron-up"></i></button>
                            </div>`,
                    addModel = function (m, selAll) {
                        html += '<div class="per-menu"><i class="fa fa-desktop mr-1"></i>' + m.menu + '<span class="per-code" style="display:none">(' + m.model + ')</span>';
                        if (selAll) {
                            html += '<div class="btn-group ml-2"><a class="btn btn-default btn-sm btn-sel-all">全选</a><a class="btn btn-default btn-sm btn-unsel-all">全不选</a></div>';
                        }
                        html += '</div><div class="per-service row ml-5 mb-2">';

                        $.each(m.services, function () {
                            var p = this, id = 'per_' + me.getId(), checked = p.role_ids.indexOf(roleId) > -1 ? ' checked="checked"' : '';
                            html += '<div class="form-check mr-4"><input' + checked + ' data-id="' + p.id + '" type="checkbox" class="form-check-input check-per" id="'
                                + id + '"/><label for="' + id + '" class="form-check-label">' + p.name.t() + '<span class="per-code" style="display:none">(' + p.auth + ')</span></label></div>';
                        });
                        html += "</div>";
                        if (m.fields[0]) {
                            html += '<div class="per-field"><div class="per-menu"><i class="fa fa-tasks mr-1"></i>' + '字段权限'.t() + '</div><div class="row ml-5 mb-2">';
                            $.each(m.fields, function () {
                                var p = this, id = 'per_' + me.getId(), checked = p.role_ids.indexOf(roleId) > -1 ? ' checked="checked"' : '';
                                html += '<div class="form-check mr-4"><input' + checked + ' data-id="' + p.id + '" type="checkbox" class="form-check-input check-per" id="'
                                    + id + '"/><label for="' + id + '" class="form-check-label">' + p.name.t() + '<span class="per-code" style="display:none">(' + p.auth + ')</span></label></div>';
                            });
                            html += "</div></div>";
                        }
                        if (m.related) {
                            html += '<div class="per-related">';
                            $.each(m.related, function () {
                                addModel(this);
                            });
                            html += "</div>";
                        }
                    };
                $.each(r.data, function () {
                    var a = this, appId = 'app_' + me.getId();
                    html += '<div class="card card-primary card-outline"><a class="d-block w-100" data-toggle="collapse" href="#'
                        + appId + '" aria-expanded="true"><div class="card-header">' + a.app + '</div></a><div id="' + appId + '" class="collapse show mb-4">';
                    $.each(a.models, function () {
                        html += '<div class="module-permission">';
                        addModel(this, true);
                        html += "</div>";
                    });
                    if (a.menus) {
                        var getMenu = function (i) {
                            var m = a.menus[i], t = '<div class="ml-3 mt-1">';
                            if (m.role_ids) {
                                var id = 'menu_' + me.getId(), checked = m.role_ids.indexOf(roleId) > -1 ? ' checked="checked"' : '';
                                t += '<input' + checked + ' data-id="' + i + '" class="form-check-input check-menu" type="checkbox" id="' + id + '"/><label for="' + id + '" class="form-check-label">' + m.name.t() + '</label>';
                            } else {
                                t += m.name.t();
                            }
                            if (m.sub) {
                                $.each(m.sub, function () {
                                    t += getMenu(this);
                                });
                            }
                            t += '</div>'
                            return t;
                        }
                        html += '<div class="post"></div>';
                        $.each(a.menus.sub, function () {
                            html += getMenu(this);
                        });
                    }
                    html += "</div></div>";
                });
                body.html(html)
                    .on('click', '.btn-expand', function () {
                        body.find('[data-toggle=collapse]').removeClass('collasped');
                        body.find('.collapse').addClass('show');
                    })
                    .on('click', '.btn-collapse', function () {
                        body.find('[data-toggle=collapse]').addClass('collasped');
                        body.find('.collapse').removeClass('show');
                    }).on('change', '#showCode', function () {
                        var me = $(this);
                        if (me.is(":checked")) {
                            body.find('.per-code').show();
                        } else {
                            body.find('.per-code').hide();
                        }
                    }).on('click', '.btn-sel-all', function () {
                        var btn = $(this);
                        btn.parents('.module-permission').find('[type=checkbox]').prop('checked', true)
                    }).on('click', '.btn-unsel-all', function () {
                        var btn = $(this);
                        btn.parents('.module-permission').find('[type=checkbox]').prop('checked', false)
                    });
            }
        });
    }
});
$(document).on('viewLoaded', function (e, view) {
    jdoo.create("base.Role", { view: view });
});