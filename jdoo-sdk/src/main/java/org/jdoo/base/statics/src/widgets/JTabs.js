
$.component("JTabs", {
    scrollOffset: 40,
    contextMenu: false,
    getTpl: function () {
        return `<div class="tab-panel">
                    <div class="tab-header">
                        <span role="button" style="display:none" class="nav-link bg-light border-bottom" data-widget="tab-scrollleft">
                            <span class="fas fa-angle-double-left"></span>
                        </span>
                        <ul class="nav nav-tabs overflow-hidden" role="tablist"></ul>
                        <span role="button" style="display:none" class="nav-link bg-light border-bottom" data-widget="tab-scrollright">
                            <span class="fas fa-angle-double-right"></span>
                        </span>
                    </div>
                    <div class="tab-content"></div>
                </div>`;
    },
    init: function () {
        let me = this;
        let mousedown = false;
        let mousedownInterval = null;
        me.dom.html(me.getTpl())
            .on('tabActived', me.tabActived)
            .on('tabCreated', me.tabCreated)
            .on('tabRemoved', me.tabRemoved)
            .on('click', '.nav-item-close', function () {
                me.removeTab($(this).parent().attr('data'));
            }).on('click', '[role=tab]', function () {
                let tab = $(this);
                me.dom.triggerHandler("tabActived", [me, tab]);
            }).on('mousedown', '[data-widget=tab-scrollleft]', function (e) {
                e.preventDefault();
                clearInterval(mousedownInterval);
                let scrollOffset = -me.scrollOffset;
                mousedown = true;
                me._navScroll(scrollOffset);
                mousedownInterval = setInterval(function () {
                    me._navScroll(scrollOffset);
                }, 250);
            }).on('mousedown', '[data-widget=tab-scrollright]', function (e) {
                e.preventDefault();
                clearInterval(mousedownInterval);
                let scrollOffset = me.scrollOffset;
                mousedown = true;
                me._navScroll(scrollOffset);
                mousedownInterval = setInterval(function () {
                    me._navScroll(scrollOffset);
                }, 250);
            }).on('mouseup', function () {
                if (mousedown) {
                    mousedown = false;
                    clearInterval(mousedownInterval);
                    mousedownInterval = null;
                }
            });
        $(window).on('resize', function () {
            me.updateScroll();
        });
        me.updateScroll();
    },
    _navScroll: function (offset) {
        let me = this, leftPos = me.dom.find('[role=tablist]').scrollLeft();
        me.dom.find('[role=tablist]').animate({
            scrollLeft: leftPos + offset
        }, 250, 'linear');
    },
    updateScroll: function () {
        let me = this, tabs = me.dom.find('[role=tablist]'), w = 0;
        tabs.children('li').each(function () {
            w += $(this).width();
        });
        if (w > tabs.width()) {
            me.dom.find('[data-widget=tab-scrollleft],[data-widget=tab-scrollright]').show();
        } else {
            me.dom.find('[data-widget=tab-scrollleft],[data-widget=tab-scrollright]').hide();
        }
    },
    openTab: function (id, opt) {
        let me = this, tabId = 'tab-' + id, tabContentId = 'tabcontent-' + id, tab = me.dom.find('#' + tabId);
        if (tab.length > 0) {
            if (!tab.hasClass('active')) {
                me.dom.find('[role=tab]').removeClass('active');
                me.dom.find('[role=tabpanel]').removeClass('show active');
                tab.addClass('active');
                let tabPanel = me.dom.find('#' + tabContentId);
                tabPanel.addClass('show active');
                me.dom.triggerHandler("tabActived", [me, tab]);
            }
        } else {
            me.dom.find('[role=tab]').removeClass('active');
            me.dom.find('[role=tabpanel]').removeClass('show active');
            let nav = '<li data="' + id + '" class="nav-item">';
            if (opt.closable) {
                nav += '<a role="button" class="nav-item-close"><i class="fas fa-times"></i></a>'
            }
            nav += '<a class="nav-link active" id="' + tabId + '" data-toggle="pill" href="#' + tabContentId + '" role="tab" aria-controls="' + tabContentId + '" aria-selected="true">' + opt.title + '</a></li>';
            me.dom.find('[role=tablist]').append(nav);

            me.dom.find('.tab-content').append('<div class="tab-pane fade show active" id="' + tabContentId + '" role="tabpanel" aria-labelledby="' + tabId + '"></div>');
            if (opt.init) {
                opt.init(me.dom.find('#' + tabContentId));
            }
            tab = me.dom.find('#' + tabId)
            me.dom.triggerHandler("tabCreated", [me, tab]);
            me.dom.triggerHandler("tabActived", [me, tab]);
            if (me.contextMenu) {
                me.initTabMenu(id);
            }
            me.updateScroll();
        }
    },
    updateTab: function (id, title) {
        this.dom.find('#tab-' + id).html(title);
        this.updateScroll();
    },
    removeTab: function (id) {
        let me = this;
        me.dom.find('[data=' + id + ']').remove();
        me.dom.find('#tabcontent-' + id).remove();
        me.dom.triggerHandler("tabRemoved", [me, id]);
        let tab = me.dom.find('[role=tab].active');
        if (tab.length == 0) {
            tab = me.dom.find('[role=tab]:first').addClass('active');
            me.dom.find('#' + tab.attr('aria-controls')).addClass('show active');
            me.dom.triggerHandler("tabActived", [me, tab]);
        }
        me.updateScroll();
    },
    removeOtherTab: function (id) {
        let me = this;
        me.dom.find('li:not([data=' + id + '])').remove();
        me.dom.find('.tab-pane:not(#tabcontent-' + id + ')').remove();
        me.dom.triggerHandler("tabRemoved", [me]);
        let tab = me.dom.find('#tab-' + id);
        if (!tab.hasClass('active')) {
            tab.addClass('active');
            me.dom.find('#' + tab.attr('aria-controls')).addClass('show active');
            me.dom.triggerHandler("tabActived", [me, tab]);
        }
        me.updateScroll();
    },
    onTabActived: function (handler) {
        this.dom.on('tabActived', handler);
    },
    onTabCreated: function (handler) {
        this.dom.on('tabCreated', handler);
    },
    onTabRemoved: function (handler) {
        this.dom.on('tabRemoved', handler);
    },
    enableTab: function (id, enabled) {
        //TODO
    },
    initTabMenu: function (id) {
        let me = this;
        me.dom.find('[data=' + id + ']').contextMenu({
            width: 110, // width
            itemHeight: 30, // 菜单项height
            autoHide: true,
            bgColor: "#fff", // 背景颜色
            color: "#0000", // 字体颜色
            fontSize: 12, // 字体大小
            hoverColor: "#ffff", // hover字体颜色
            hoverBgColor: "#32c5d2", // hover背景颜色
            menu: [
                { // 菜单项
                    text: "关闭当前".t(),
                    callback: function () {
                        me.removeTab(id);
                    }
                },
                {
                    text: "关闭其他".t(),
                    callback: function () {
                        me.removeOtherTab(id);
                    }
                }
            ]
        });
    },
});