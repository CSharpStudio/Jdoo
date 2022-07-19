
(function (factory) {
    "use strict";
    factory(jQuery, window, document);
}(function ($, window, document, undefined) {
    "use strict";
    String.prototype.t = function () {
        return this;//todo
    }
    Array.prototype.remove = function (e) {
        var idx = this.indexOf(e);
        if (idx > -1) {
            this.splice(idx, 1);
        }
    }
    $('t').each(function (i, e) {
        $(e).replaceWith($(e).html().t());
    });
    $.jutil = {
        parseXML: function (xmlStr) {
            if (typeof ($.browser) == "undefined") {
                if (!!navigator.userAgent.match(/Trident\/7\./)) {// IE11
                    var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
                    xmlDoc.async = "false";
                    xmlDoc.loadXML(xmlStr);
                } else {
                    var parser = new DOMParser();
                    xmlDoc = parser.parseFromString(xmlStr, "text/xml");
                }
            } else {
                if ($.browser.msie) {// IE
                    xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
                    xmlDoc.async = "false";
                    xmlDoc.loadXML(xmlStr);
                } else {// Other
                    var parser = new DOMParser();
                    xmlDoc = parser.parseFromString(xmlStr, "text/xml");
                }
            }
            return $(xmlDoc);
        },
        getUrlParam: function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]); return null;
        },
        getParams: function (search) {
            var o = {}, re = /([^&=]+)=([^&]*)/g, m;
            while (m = re.exec(search)) {
                o[decodeURIComponent(m[1])] = decodeURIComponent(m[2]);
            }
            return o;
        },
        newId: function () {
            var guid = "";
            for (var i = 1; i <= 16; i++) {
                var n = Math.floor(Math.random() * 16.0).toString(16);
                guid += n;
            }
            return guid;
        },
        getTenantPath: function () {
            var parts = window.location.pathname.substring(1).split("/");
            return "/" + parts[0];
        },
        cookie: function (name, value, options) {
            if (typeof value != 'undefined') {
                options = options || {};
                if (value === null) {
                    value = '';
                    options = $.extend({}, options);
                    options.expires = -1;
                }
                var expires = '';
                if (options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
                    var date;
                    if (typeof options.expires == 'number') {
                        date = new Date();
                        date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
                    } else {
                        date = options.expires;
                    }
                    expires = '; expires=' + date.toUTCString();
                }
                var path = options.path ? '; path=' + (options.path) : '';
                var domain = options.domain ? '; domain=' + (options.domain) : '';
                var secure = options.secure ? '; secure' : '';
                document.cookie = [name, '=', encodeURIComponent(value), expires, path, domain, secure].join('');
            } else {
                var cookieValue = null;
                if (document.cookie && document.cookie != '') {
                    var cookies = document.cookie.split(';');
                    for (var i = 0; i < cookies.length; i++) {
                        var cookie = jQuery.trim(cookies[i]);
                        if (cookie.substring(0, name.length + 1) == (name + '=')) {
                            cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                            break;
                        }
                    }
                }
                return cookieValue;
            }
        }
    };
    //#region jrpc
    $.jrpc = function (opt) {
        var options = {},
            defaults = {
                url: $.jutil.getTenantPath() + "/rpc/service",
                type: "POST",
                dataType: "json",
                onsuccess: function (data) { },
                onerror: function (err) {
                    Msg.showErr(err);
                },
                success: function (rs) {
                    if (rs.error) {
                        options.onerror(rs.error);
                        if (rs.error.code === 7100) {//未授权，刷新转跳到登录
                            top.window.location.reload();
                        }
                    } else {
                        var result = rs.result;
                        if (result.context && result.context.token) {
                            $.jutil.cookie("jtoken", result.context.token);
                        }
                        options.onsuccess(result);
                    }
                },
                error: function (rs) {
                    console.log(rs);
                }
            };
        var context = $.extend({
            "uid": "",
            "token": $.jutil.cookie("jtoken"),
            "lang": "zh_CN",
        }, opt.context);
        delete opt.context;
        var args = $.extend({}, opt.args);
        delete opt.args;
        var method = opt.method;
        delete opt.method;
        var model = opt.model;
        delete opt.model;
        options = $.extend({
            data: {
                "id": $.jutil.newId(),
                "jsonrpc": "2.0",
                "method": method,
                "params": {
                    "args": args,
                    "context": context,
                    "model": model
                }
            }
        }, defaults, opt);
        options.data = JSON.stringify(options.data);
        $.ajax(options);
    }
    //#endregion

    window.Msg = {};
    Msg.showMsg = function (msg, cls) {
        $(document).Toasts('create', {
            class: cls || 'bg-success',
            position: 'bottomRight',
            autohide: true,
            delay: 2000,
            body: '<div style="min-width:200px">' + msg + '</div>'
        });
    };
    Msg.showErr = function (err) {
        if (err.code === 1000) {
            $(document).Toasts('create', {
                class: 'bg-danger',
                title: '验证不通过'.t(),
                autohide: true,
                delay: 5000,
                body: '<div style="min-width:200px">' + err.message + '</div>'
            });
        } else {
            console.log(err);
            //其它类型错误
            $(document).Toasts('create', {
                class: 'bg-danger',
                title: '发生错误'.t(),
                body: '<div style="min-width:200px">' + err.message + '</div><div>' + (err.data || {}).debug + '</div>'
            });
        }
    };
}));