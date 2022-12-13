jdoo.define("JWebApp", {
    new: function () {
        let me = this;
        me.initWorkspace();
        me.initMenu();
        me.initMenuSearch();
        me.initCompany();
        me.initUser();
        $(document).on('click', '[data-widget=user-logout]', function () {
            jdoo.rpc({
                model: "rbac.user",
                method: "logout",
                args: {},
                onsuccess: function (r) {
                    window.location.href = jdoo.web.getTenantPath() + "/login";
                }
            });
        }).on('click', '[data-widget=debug-exit]', function () {
            jdoo.web.cookie("ctx_debug", false);
            window.location.reload();
        });
    },
    initWorkspace: function () {
        let me = this;
        //iframe    
        me.ws = $('[data-widget=jframe]').JFrame({
            onTabClick: function (item) {
                return item
            },
            onTabChanged: function (item) {
                if (item) {
                    let tabId = item.attr('href'),
                        frame = $(tabId + " iframe"),
                        src = frame.attr('src'),
                        app = frame.attr('app');
                    if (!app) {
                        app = jdoo.web.getParams(window.location.hash.substring(1)).app;
                        frame.attr('app', app);
                    }
                    window.location.hash = 'u=' + encodeURIComponent(src.replace(window.location.origin, ''))
                        + '&t=' + encodeURIComponent(item.html())
                        + '&app=' + app;
                    if (me.menu && me.menu.data) {
                        let menus = me.menu.data[app];
                        if (menus) {
                            me.menu.showAppMenu(menus);
                        }
                    }
                } else {
                    window.location.hash = '';
                }
                return item
            },
            onTabCreated: function (item) {
                return item;
            },
            autoIframeMode: true,
            autoItemActive: false,
            autoShowNewTab: true,
            autoDarkMode: false,
            allowDuplicates: false,
            loadingScreen: false,
            useNavbarItems: true,
            allowReload: false
        });
    },
    initMenu: function () {
        this.menu = $('ul[role=menu]').JMenu({});
    },
    initMenuSearch: function () {
        this.menuSearch = $('[data-widget=menu-search]').JMenuSearch({});
    },
    initCompany: function () {
        $('[data-widget=company]').JCompany({});
    },
    initUser: function () {
        let icon = $('.user-icon'), n = icon.html();
        n = ((n || '').trim()[0] || '').toUpperCase();
        icon.html(n);
        $('[data-widget=user-account]').attr('href', jdoo.web.getTenantPath() + "/view#model=rbac.user&view=form&key=personal&viewtype=form");
    },
    load: function () {
        let me = this;
        jdoo.rpc({
            model: "ir.ui.menu",
            method: "loadMenu",
            args: {},
            onsuccess: function (r) {
                me.menu.setData(r.data);
                me.menuSearch.setData(r.data);
            }
        });
    }
});
window.loadWeb = function () {
    jdoo.create('JFullScreen');
    jdoo.create('JIconLib');
    window.webapp = jdoo.create("JWebApp");
    webapp.load();
    var p = jdoo.web.getParams(window.location.hash.substring(1));
    if (p.u) {
        var hash = p.u.split('#')[1];
        var uniqueName = jdoo.web.getParams(hash).menu;
        if (!uniqueName) {
            uniqueName = unescape(p.u).replace('./', '').replace(/["#&',./:=?[\]]/gi, '-').replace(/(--)/gi, '');
        }
        webapp.ws.createTab(p.t, p.u, uniqueName, true);
    }
}
$(loadWeb);