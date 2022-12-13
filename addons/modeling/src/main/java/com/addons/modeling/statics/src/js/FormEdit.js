
jdoo.component("FormEdit", {
    module: 'modeling',
    edit: function (param) {
        let me = this;
        if (!me.view) {
            jdoo.rpc({
                model: "ir.ui.view",
                method: "loadView",
                args: {
                    model: me.model,
                    type: "form"
                },
                onsuccess: function (r) {
                    me.view = r.data;
                    me.showDialog(me.view, param);
                }
            })
        } else {
            me.showDialog(me.view, param);
        }
    },
    showDialog: function (view, param) {
        let me = this;
        jdoo.showDialog({
            title: param.title || me.title,
            init: function () {
                let dialog = this;
                dialog.form = dialog.body.JForm({
                    model: me.model,
                    module: me.module,
                    fields: view.fields,
                    arch: view.views.form.arch,
                    ajax: function (form, callback) {
                        if (param.id) {
                            jdoo.rpc({
                                model: me.model,
                                module: me.module,
                                method: "read",
                                args: {
                                    ids: [param.id],
                                    fields: dialog.form.getFields()
                                },
                                context: {
                                    usePresent: true
                                },
                                onsuccess: function (r) {
                                    me.data = $.extend({}, r.data[0]);
                                    callback({ data: r.data[0] });
                                }
                            });
                        }
                    }
                });
                if (param.id) {
                    dialog.form.load();
                } else {
                    dialog.form.create();
                }
            },
            submit: param.submit
        });
    }
});