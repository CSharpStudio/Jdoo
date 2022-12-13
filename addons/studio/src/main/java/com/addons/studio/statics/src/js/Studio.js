
$.component("Studio", {
    getTpl: function () {
        return `<aside class="left-box">
                </aside>
                <div class="main-box">
                </div>`;
    },
    init: function () {
        let me = this;
        me.dom.append(me.getTpl());
        let ws = me.dom.find('.main-box').JTabs({
            onTabActived: function (e, tabs, tab) {
            }
        });
        me.dom.find('.left-box').MenuView({ ws: ws });
    }
});

$(function () {
    window.apps = $('.studio-designer').Studio({});
});