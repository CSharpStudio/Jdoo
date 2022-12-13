$.component("JMenu", {
    appmenu: 'ul[role=appmenu]',
    setData: function (data) {
        let me = this;
        me.data = data;
        $.each(me.data.root, function (i, id) {
            let m = me.data[id];
            if (m) {
                let click = m.click ? ` onclick="${m.click}"` : '';
                let state = m.state ? `<span class="right badge badge-danger">${m.state}</span>` : '';
                let icon = m.icon ? `<img src="${m.icon}" alt=" " class="img-app nav-icon mr-3">` : `<i class="far fa-circle nav-icon"></i>`;
                let html = `<li class="nav-item" data="${id}">
                                <a href="${m.url || '#'}" class="${m.cls || ''} nav-link"${click}>${icon}<p>${m.name + state}</p></a>
                            </li>`;
                let nav = $(html);
                nav.data = m;
                let findLink = function (lnk) {
                    let subid = lnk.sub[0];
                    if (subid) {
                        lnk = me.data[subid];
                        if (lnk.url) {
                            return subid;
                        }
                        if (lnk.sub) {
                            return findLink(lnk);
                        }
                    }
                    return null;
                }
                nav.on('click', function () {
                    window.location.hash = 'app=' + id;
                    me.showAppMenu(nav.data);
                    if (!m.click && !m.url) {
                        let link = findLink(m);
                        if (link) {
                            webapp.ws.openTabSidebar($('[data=' + link + ']'));
                        }
                    }
                });
                me.dom.append(nav);
            }
        });
        let hash = jdoo.web.getParams(window.location.hash.substring(1));
        if (hash.app) {
            let m = me.data[hash.app];
            if (m) {
                me.showAppMenu(m);
            }
        }
    },
    showAppMenu: function (data) {
        let me = this, appmenu = $(me.appmenu);
        appmenu.empty();
        appmenu.append(me.getMainMenuTpl(data));
        if (data.sub) {
            $.each(data.sub, function (i, id) {
                let m = me.data[id];
                if (m && (!m.sub || m.sub.length > 0)) {
                    let click = m.click ? 'onclick="' + m.click + '"' : "";
                    if (m.sub) {
                        let html = `<li class="nav-item dropdown">
                                        <a data="${id}" href="${m.url || '#'}" class="nav-link dropdown-toggle ${m.cls || ''}" tabindex="0" data-toggle="dropdown" data-submenu ${click}>
                                            ${m.name}
                                        </a>
                                        <div class="dropdown-menu">${me.getSubMenuTpl(m.sub)}</div>
                                    </li>`;
                        appmenu.append(html);
                    } else {
                        let html = `<li class="nav-item"><a data="${id}" href="${m.url || '#'}" class="${m.cls || ''} nav-link"${click}><p>${m.name}</p></a></li>`;
                        appmenu.append(html);
                    }
                }
            });
        }
        $('[data-submenu]').submenupicker();
    },
    getMainMenuTpl: function (m) {
        let click = m.click ? 'onclick="' + m.click + '"' : "";
        return `<li class="nav-item"><a href="${m.url || '#'}" class="${m.cls || ''} nav-link pt-0 pl-1" ${click}><p style="font-size:1.3rem">${m.name}</p></a></li>`;
    },
    getSubMenuTpl: function (sub) {
        let me = this, tpl = '';
        $.each(sub, function (i, id) {
            let m = me.data[id];
            if (m) {
                let click = m.click ? 'onclick="' + m.click + '"' : "";
                if (m.sub) {
                    tpl += `<div class="dropdown dropright dropdown-submenu">
                                <a data="${id}" href="${m.url || '#'}" class="dropdown-item dropdown-toggle ${m.cls || ''}" tabindex="0" data-toggle="dropdown" data-submenu ${click}>${m.name}</a>
                            <div class="dropdown-menu">${me.getSubMenuTpl(m.sub)}</div></div>`;
                } else {
                    tpl += `<a data="${id}" href="${m.url || '#'}" class="${m.cls || ''} dropdown-item" ${click}>${m.name}</a>`;
                }
            }
        });
        return tpl;
    }
});