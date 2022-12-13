(function (factory) {
    "use strict";
    factory(jQuery, window, document);
}(function ($, window, document, undefined) {
    "use strict";
    
    var NAME = 'JFrame';
    var SELECTOR_DATA_TOGGLE_CLOSE = '[data-widget="jframe-close"]';
    var SELECTOR_DATA_TOGGLE_SCROLL_LEFT = '[data-widget="jframe-scrollleft"]';
    var SELECTOR_DATA_TOGGLE_SCROLL_RIGHT = '[data-widget="jframe-scrollright"]';
    var SELECTOR_DATA_TOGGLE_FULLSCREEN = '[data-widget="jframe-fullscreen"]';
    var SELECTOR_CONTENT_WRAPPER = '.content-wrapper';
    var SELECTOR_CONTENT_IFRAME = SELECTOR_CONTENT_WRAPPER + " iframe";
    var SELECTOR_TAB_NAV = SELECTOR_CONTENT_WRAPPER + ".jframe-mode .nav";
    var SELECTOR_TAB_NAVBAR_NAV = SELECTOR_CONTENT_WRAPPER + ".jframe-mode .navbar-nav";
    var SELECTOR_TAB_NAVBAR_NAV_ITEM = SELECTOR_TAB_NAVBAR_NAV + " .nav-item";
    var SELECTOR_TAB_NAVBAR_NAV_LINK = SELECTOR_TAB_NAVBAR_NAV + " .nav-link";
    var SELECTOR_TAB_CONTENT = SELECTOR_CONTENT_WRAPPER + ".jframe-mode .tab-content";
    var SELECTOR_TAB_EMPTY = SELECTOR_TAB_CONTENT + " .tab-empty";
    var SELECTOR_TAB_LOADING = SELECTOR_TAB_CONTENT + " .tab-loading";
    var SELECTOR_TAB_PANE = SELECTOR_TAB_CONTENT + " .tab-pane";
    var SELECTOR_SIDEBAR_MENU_ITEM = '.main-sidebar .nav-item > a.nav-link';
    var SELECTOR_SIDEBAR_SEARCH_ITEM = '.sidebar-search-results .list-group-item';
    var SELECTOR_HEADER_MENU_ITEM = '.main-header .nav-item a.nav-link';
    var SELECTOR_HEADER_DROPDOWN_ITEM = '.main-header a.dropdown-item';
    var CLASS_NAME_IFRAME_MODE$1 = 'jframe-mode';
    var CLASS_NAME_FULLSCREEN_MODE = 'jframe-mode-fullscreen';
    var Default = {
        onTabClick: function onTabClick(item) {
            return item;
        },
        onTabChanged: function onTabChanged(item) {
            return item;
        },
        onTabCreated: function onTabCreated(item) {
            return item;
        },
        autoIframeMode: true,
        autoItemActive: true,
        autoShowNewTab: true,
        autoDarkMode: false,
        allowDuplicates: false,
        allowReload: true,
        loadingScreen: true,
        useNavbarItems: true,
        scrollOffset: 40,
        scrollBehaviorSwap: false,
        iconMaximize: 'fa-expand',
        iconMinimize: 'fa-compress'
    };

    var JFrame = function () {
        function JFrame(element, config) {
            this._config = config;
            this._element = element;

            this._init();
        } // Public


        var _proto = JFrame.prototype;

        _proto.onTabClick = function onTabClick(item) {
            this._config.onTabClick(item);
        };

        _proto.onTabChanged = function onTabChanged(item) {
            this._config.onTabChanged(item);
        };

        _proto.onTabCreated = function onTabCreated(item) {
            this._config.onTabCreated(item);
        };

        _proto.createTab = function createTab(title, link, uniqueName, autoOpen) {
            var _this = this;

            var tabId = "panel-" + uniqueName;
            var navId = "tab-" + uniqueName;

            if (this._config.allowDuplicates) {
                tabId += "-" + Math.floor(Math.random() * 1000);
                navId += "-" + Math.floor(Math.random() * 1000);
            }

            var newNavItem = "<li class=\"nav-item\" role=\"presentation\"><a href=\"#\" class=\"btn-jframe-close\" data-widget=\"jframe-close\" data-type=\"only-this\"><i class=\"fas fa-times\"></i></a><a class=\"nav-link\" data-toggle=\"row\" id=\"" + navId + "\" href=\"#" + tabId + "\" role=\"tab\" aria-controls=\"" + tabId + "\" aria-selected=\"false\">" + title + "</a></li>";
            $(SELECTOR_TAB_NAVBAR_NAV).append(unescape(escape(newNavItem)));
            var newTabItem = "<div class=\"tab-pane fade\" id=\"" + tabId + "\" role=\"tabpanel\" aria-labelledby=\"" + navId + "\"><iframe src=\"" + link + "\"></iframe></div>";
            $(SELECTOR_TAB_CONTENT).append(unescape(escape(newTabItem)));

            if (autoOpen) {
                if (this._config.loadingScreen) {
                    var $loadingScreen = $(SELECTOR_TAB_LOADING);
                    $loadingScreen.fadeIn();
                    $(tabId + " iframe").ready(function () {
                        if (typeof _this._config.loadingScreen === 'number') {
                            _this.switchTab("#" + navId);

                            setTimeout(function () {
                                $loadingScreen.fadeOut();
                            }, _this._config.loadingScreen);
                        } else {
                            _this.switchTab("#" + navId);

                            $loadingScreen.fadeOut();
                        }
                    });
                } else {
                    this.switchTab("#" + navId);
                }
            }

            this.onTabCreated($("#" + navId));
        };

        _proto.openTabSidebar = function openTabSidebar(item, autoOpen) {
            if (autoOpen === void 0) {
                autoOpen = this._config.autoShowNewTab;
            }

            var $item = $(item).clone();

            if ($item.attr('href') === undefined) {
                $item = $(item).parents('a').clone();
            }

            $item.find('.right, .search-path').remove();
            var title = $item.find('p').text();

            if (title === '') {
                title = $item.text();
            }

            var link = $item.attr('href');

            if (link === '#' || link === '' || link === undefined) {
                return;
            }

            var hash = link.split('#')[1];
            var uniqueName = jdoo.web.getParams(hash).menu;

            if (!uniqueName) {
                uniqueName = unescape(link).replace('./', '').replace(/["#&',./:=?[\]]/gi, '-').replace(/(--)/gi, '');
            }

            var navId = "tab-" + uniqueName;

            if (!this._config.allowDuplicates && $("#" + navId).length > 0) {
                return this.switchTab("#" + navId, this._config.allowReload);
            }

            if (!this._config.allowDuplicates && $("#" + navId).length === 0 || this._config.allowDuplicates) {
                this.createTab(title, link, uniqueName, autoOpen);
            }
        };

        _proto.switchTab = function switchTab(item, reload) {
            var _this2 = this;

            if (reload === void 0) {
                reload = false;
            }

            var $item = $(item);
            var tabId = $item.attr('href');
            $(SELECTOR_TAB_EMPTY).hide();

            if (reload) {
                var $loadingScreen = $(SELECTOR_TAB_LOADING);

                if (this._config.loadingScreen) {
                    $loadingScreen.show(0, function () {
                        $(tabId + " iframe").attr('src', $(tabId + " iframe").attr('src')).ready(function () {
                            if (_this2._config.loadingScreen) {
                                if (typeof _this2._config.loadingScreen === 'number') {
                                    setTimeout(function () {
                                        $loadingScreen.fadeOut();
                                    }, _this2._config.loadingScreen);
                                } else {
                                    $loadingScreen.fadeOut();
                                }
                            }
                        });
                    });
                } else {
                    $(tabId + " iframe").attr('src', $(tabId + " iframe").attr('src'));
                }
            }

            $(SELECTOR_TAB_NAVBAR_NAV + " .active").tab('dispose').removeClass('active');

            this._fixHeight();

            $item.tab('show');
            $item.parents('li').addClass('active');
            this.onTabChanged($item);

            if (this._config.autoItemActive) {
                this._setItemActive($(tabId + " iframe").attr('src'));
            }
        };

        _proto.removeActiveTab = function removeActiveTab(type, element) {
            if (type == 'all') {
                $(SELECTOR_TAB_NAVBAR_NAV_ITEM).remove();
                $(SELECTOR_TAB_PANE).remove();
                $(SELECTOR_TAB_EMPTY).show();
                this.onTabChanged(null);
            } else if (type == 'all-other') {
                $(SELECTOR_TAB_NAVBAR_NAV_ITEM + ":not(.active)").remove();
                $(SELECTOR_TAB_PANE + ":not(.active)").remove();
            } else if (type == 'only-this') {
                var $navClose = $(element);
                var $navItem = $navClose.parent('.nav-item');
                var $navItemParent = $navItem.parent();
                var navItemIndex = $navItem.index();
                var tabId = $navClose.siblings('.nav-link').attr('aria-controls');
                $navItem.remove();
                $("#" + tabId).remove();

                if ($(SELECTOR_TAB_CONTENT).children().length == $(SELECTOR_TAB_EMPTY + ", " + SELECTOR_TAB_LOADING).length) {
                    $(SELECTOR_TAB_EMPTY).show();
                    this.onTabChanged(null);
                } else {
                    var prevNavItemIndex = navItemIndex - 1;
                    this.switchTab($navItemParent.children().eq(prevNavItemIndex).find('a.nav-link'));
                }
            } else {
                var _$navItem = $(SELECTOR_TAB_NAVBAR_NAV_ITEM + ".active");

                var _$navItemParent = _$navItem.parent();

                var _navItemIndex = _$navItem.index();

                _$navItem.remove();

                $(SELECTOR_TAB_PANE + ".active").remove();

                if ($(SELECTOR_TAB_CONTENT).children().length == $(SELECTOR_TAB_EMPTY + ", " + SELECTOR_TAB_LOADING).length) {
                    $(SELECTOR_TAB_EMPTY).show();
                    this.onTabChanged(null);
                } else {
                    var _prevNavItemIndex = _navItemIndex - 1;

                    this.switchTab(_$navItemParent.children().eq(_prevNavItemIndex).find('a.nav-link'));
                }
            }
        };

        _proto.toggleFullscreen = function toggleFullscreen() {
            if ($('body').hasClass(CLASS_NAME_FULLSCREEN_MODE)) {
                $(SELECTOR_DATA_TOGGLE_FULLSCREEN + " i").removeClass(this._config.iconMinimize).addClass(this._config.iconMaximize);
                $('body').removeClass(CLASS_NAME_FULLSCREEN_MODE);
                $(SELECTOR_TAB_EMPTY + ", " + SELECTOR_TAB_LOADING).height('100%');
                $(SELECTOR_CONTENT_WRAPPER).height('100%');
                $(SELECTOR_CONTENT_IFRAME).height('100%');
            } else {
                $(SELECTOR_DATA_TOGGLE_FULLSCREEN + " i").removeClass(this._config.iconMaximize).addClass(this._config.iconMinimize);
                $('body').addClass(CLASS_NAME_FULLSCREEN_MODE);
            }

            $(window).trigger('resize');

            this._fixHeight(true);
        } // Private
            ;

        _proto._init = function _init() {
            var usingDefTab = $(SELECTOR_TAB_CONTENT).children().length > 2;

            this._setupListeners();

            this._fixHeight(true);

            if (usingDefTab) {
                var $el = $("" + SELECTOR_TAB_PANE).first(); // eslint-disable-next-line no-console

                console.log($el);
                var uniqueName = $el.attr('id').replace('panel-', '');
                var navId = "#tab-" + uniqueName;
                this.switchTab(navId, true);
            }
        };

        _proto._initFrameElement = function _initFrameElement() {
            if (window.frameElement && this._config.autoIframeMode) {
                var $body = $('body');
                $body.addClass(CLASS_NAME_IFRAME_MODE$1);

                if (this._config.autoDarkMode) {
                    $body.addClass('dark-mode');
                }
            }
        };

        _proto._navScroll = function _navScroll(offset) {
            var leftPos = $(SELECTOR_TAB_NAVBAR_NAV).scrollLeft();
            $(SELECTOR_TAB_NAVBAR_NAV).animate({
                scrollLeft: leftPos + offset
            }, 250, 'linear');
        };

        _proto._setupListeners = function _setupListeners() {
            var _this3 = this;

            $(window).on('resize', function () {
                setTimeout(function () {
                    _this3._fixHeight();
                }, 1);
            });

            if ($(SELECTOR_CONTENT_WRAPPER).hasClass(CLASS_NAME_IFRAME_MODE$1)) {
                $(document).on('click', SELECTOR_SIDEBAR_MENU_ITEM + ", " + SELECTOR_SIDEBAR_SEARCH_ITEM, function (e) {
                    e.preventDefault();

                    _this3.openTabSidebar(e.target);
                });

                if (this._config.useNavbarItems) {
                    $(document).on('click', SELECTOR_HEADER_MENU_ITEM + ", " + SELECTOR_HEADER_DROPDOWN_ITEM, function (e) {
                        e.preventDefault();

                        _this3.openTabSidebar(e.target);
                    });
                }
            }
            $(document).on('click', SELECTOR_TAB_NAVBAR_NAV_LINK, function (e) {
                e.preventDefault();

                _this3.onTabClick(e.target);

                _this3.switchTab(e.target);
            });
            $(document).on('click', SELECTOR_DATA_TOGGLE_CLOSE, function (e) {
                e.preventDefault();
                var target = e.target;

                if (target.nodeName == 'I') {
                    target = e.target.offsetParent;
                }

                _this3.removeActiveTab(target.attributes['data-type'] ? target.attributes['data-type'].nodeValue : null, target);
            });
            $(document).on('click', SELECTOR_DATA_TOGGLE_FULLSCREEN, function (e) {
                e.preventDefault();

                _this3.toggleFullscreen();
            });
            var mousedown = false;
            var mousedownInterval = null;
            $(document).on('mousedown', SELECTOR_DATA_TOGGLE_SCROLL_LEFT, function (e) {
                e.preventDefault();
                clearInterval(mousedownInterval);
                var scrollOffset = _this3._config.scrollOffset;

                if (!_this3._config.scrollBehaviorSwap) {
                    scrollOffset = -scrollOffset;
                }

                mousedown = true;

                _this3._navScroll(scrollOffset);

                mousedownInterval = setInterval(function () {
                    _this3._navScroll(scrollOffset);
                }, 250);
            });
            $(document).on('mousedown', SELECTOR_DATA_TOGGLE_SCROLL_RIGHT, function (e) {
                e.preventDefault();
                clearInterval(mousedownInterval);
                var scrollOffset = _this3._config.scrollOffset;

                if (_this3._config.scrollBehaviorSwap) {
                    scrollOffset = -scrollOffset;
                }

                mousedown = true;

                _this3._navScroll(scrollOffset);

                mousedownInterval = setInterval(function () {
                    _this3._navScroll(scrollOffset);
                }, 250);
            });
            $(document).on('mouseup', function () {
                if (mousedown) {
                    mousedown = false;
                    clearInterval(mousedownInterval);
                    mousedownInterval = null;
                }
            });
        };

        _proto._setItemActive = function _setItemActive(href) {
            $(SELECTOR_SIDEBAR_MENU_ITEM + ", " + SELECTOR_HEADER_DROPDOWN_ITEM).removeClass('active');
            $(SELECTOR_HEADER_MENU_ITEM).parent().removeClass('active');
            var $headerMenuItem = $(SELECTOR_HEADER_MENU_ITEM + "[href$=\"" + href + "\"]");
            var $headerDropdownItem = $(SELECTOR_HEADER_DROPDOWN_ITEM + "[href$=\"" + href + "\"]");
            var $sidebarMenuItem = $(SELECTOR_SIDEBAR_MENU_ITEM + "[href$=\"" + href + "\"]");
            $headerMenuItem.each(function (i, e) {
                $(e).parent().addClass('active');
            });
            $headerDropdownItem.each(function (i, e) {
                $(e).addClass('active');
            });
            $sidebarMenuItem.each(function (i, e) {
                $(e).addClass('active');
                $(e).parents('.nav-treeview').prevAll('.nav-link').addClass('active');
            });
        };

        _proto._fixHeight = function _fixHeight(tabEmpty) {
            if (tabEmpty === void 0) {
                tabEmpty = false;
            }

            if ($('body').hasClass(CLASS_NAME_FULLSCREEN_MODE)) {
                var windowHeight = $(window).height();
                var navbarHeight = $(SELECTOR_TAB_NAV).outerHeight();
                $(SELECTOR_TAB_EMPTY + ", " + SELECTOR_TAB_LOADING + ", " + SELECTOR_CONTENT_IFRAME).height(windowHeight - navbarHeight);
                $(SELECTOR_CONTENT_WRAPPER).height(windowHeight);
            } else {
                var contentWrapperHeight = parseFloat($(SELECTOR_CONTENT_WRAPPER).css('height'));

                var _navbarHeight = $(SELECTOR_TAB_NAV).outerHeight();

                if (tabEmpty == true) {
                    setTimeout(function () {
                        $(SELECTOR_TAB_EMPTY + ", " + SELECTOR_TAB_LOADING).height(contentWrapperHeight - _navbarHeight);
                    }, 50);
                } else {
                    $(SELECTOR_CONTENT_IFRAME).height(contentWrapperHeight - _navbarHeight);
                }
            }
        }
        return JFrame;
    }();

    $.fn[NAME] = function (config) {
        var _options = $.extend({}, Default, config);
        var plugin = new JFrame($(this), _options);
        return plugin;
    }
}));
localStorage.setItem('AdminLTE:IFrame:Options', "{}");//fix iframe issue