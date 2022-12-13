//@ sourceURL=module.js
jdoo.component("base.Module", {
    getMask: function (label) {
        return `<div class="modal top-mask" style="display:block;align-items: center;background-color: #e5e5e5a8;display: flex;flex-direction: column;height: 100vh;justify-content: center;"><div style="width: 180px; height: 60px;line-height: 60px;background-color: #17a2b8;color: white;border-radius: 5px;text-align: center;">${label}<div><div>`;
    },
    init: function () {
        let me = this;
        me.view.dom.on('click', '[role=install]', function () {
            var ids = me.view.getSelected();
            me.run({ ids: ids, label: '应用安装中', method: 'install' });
        }).on('click', '[role=uninstall]', function () {
            var ids = me.view.getSelected();
            me.run({ ids: ids, label: '应用卸载中', method: 'uninstall' });
        });
    },
    run: function (opt) {
        var me = this;
        $(top.window.document.body).append(me.getMask(opt.label.t()));
        jdoo.rpc({
            model: me.view.model,
            module: me.view.module,
            method: opt.method,
            args: {
                ids: opt.ids
            },
            onerror: function (e) {
                $(top.window.document.body).find('.top-mask').remove();
                jdoo.msg.error(e);
            },
            onsuccess: function (r) {
                top.window.location.reload();
            }
        });
    }
});

$(document).on('viewLoaded', function (e, view) {
    jdoo.create("base.Module", { view: view });
});