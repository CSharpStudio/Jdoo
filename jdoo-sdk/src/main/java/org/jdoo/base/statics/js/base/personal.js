//@ sourceURL=personal.js
jdoo.component('base.Personal', {
    init: function () {
        let me = this, view = me.view;
        view.dom.on('viewCreated', function (e, name) {
            if (name === 'form') {
                f = me.view.form;
                f.ajax = function (callback) {
                    jdoo.rpc({
                        model: 'rbac.user',
                        module: 'base',
                        method: "getPersonal",
                        args: {
                            fields: f.getFields()
                        },
                        context: {
                            usePresent: true
                        },
                        onsuccess: function (r) {
                            callback({ data: r.data });
                        }
                    });
                }
                f.load();
            }
        });
        me.view.dom.on('click', '.btn-save', function () {
            me.save();
        }).on('click', '.btn-update-pwd', function () {
            me.showDialog();
        });
    },
    save: function () {
        let me = this, view = me.view, form = me.view.form;
        if (form.valid()) {
            let data = form.getData();
            view.busy(true);
            jdoo.rpc({
                model: 'rbac.user',
                method: "updatePersonal",
                args: {
                    values: data,
                },
                onerror: function (e) {
                    view.busy(false);
                    jdoo.msg.error(e);
                },
                onsuccess: function (r) {
                    view.busy(false);
                    jdoo.msg.show('保存成功'.t());
                    form.load();
                }
            });
        }
    },
    showDialog: function () {
        let me = this;
        jdoo.showDialog({
            title: '修改密码'.t(),
            css: '',
            init: function () {
                let dialog = this;
                dialog.form = dialog.body.JForm({
                    cols: 1,
                    fields: {
                        old_pwd: { name: 'old_pwd', type: 'password', label: '原密码', required: true },
                        new_pwd: { name: 'new_pwd', type: 'password', label: '新密码', required: true },
                        cfm_pwd: { name: 'cfm_pwd', type: 'password', label: '确认密码', required: true }
                    },
                    arch: '<form><field name="old_pwd"/><field name="new_pwd"/><field name="cfm_pwd"/></form>'
                });
            },
            submit: function () {
                let dialog = this;
                me.updatePwd(dialog);
            }
        });
    },
    updatePwd: function (dialog) {
        let me = this, form = dialog.form;
        if (form.valid()) {
            let d = form.getData();
            if (d.old_pwd === d.new_pwd) {
                form.setInvalid('new_pwd', '新密码不能跟原密码相同'.t());
                return;
            }
            if (d.cfm_pwd != d.new_pwd) {
                form.setInvalid('cfm_pwd', '确认密码与新密码不一致'.t());
                return;
            }
            me.view.mask();
            jdoo.rpc({
                model: 'rbac.user',
                method: "changePassword",
                args: {
                    oldPassword: window.btoa(unescape(encodeURIComponent(d.old_pwd))),
                    newPassword: window.btoa(unescape(encodeURIComponent(d.new_pwd)))
                },
                onerror: function (e) {
                    me.view.unmask();
                    jdoo.msg.error(e);
                },
                onsuccess: function (r) {
                    me.view.unmask();
                    jdoo.msg.show('保存成功'.t());
                    dialog.close();
                }
            });
        }
    }
});
$(document).on('viewLoaded', function (e, view) {
    jdoo.create('base.Personal', { view: view });
});