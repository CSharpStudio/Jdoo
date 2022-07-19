$(window).ready(function () {
    $("[name=login_submit]").click(function () {
        var url = '/' + tenant + '/rpc/login',
            login = $("[name=login_account]").val(),
            password = $("[name=login_password]").val();
        if (!login) {
            $("[name=login_token]").html("账号不能为空");
            return;
        }
        if (!password) {
            $("[name=login_token]").html("密码不能为空");
            return;
        }
        $("[name=login_token]").html("加载中");
        $.ajax(
            {
                type: "POST",
                dataType: "json",
                data: JSON.stringify({
                    id: "login",
                    jsonrpc: "2.0",
                    method: "login",
                    params: {
                        login: login,
                        password: window.btoa(unescape(encodeURIComponent(password))),
                        remember: false
                    }
                }),
                contentType: "application/json",
                url: url,
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    $("[name=login_token]").html(XMLHttpRequest.status + ':' + XMLHttpRequest.responseText);
                },
                success: function (data) {
                    if (data.error) {
                        $("[name=login_token]").html(data.error.message);
                        console.log(data);
                    } else {
                        var token = data.result.token;
                        document.cookie = "jtoken=" + token;
                        $("[name=login_token]").html(token);
                    }
                }
            });
    });
    $("[name=collapse_all]").click(function () {
        $(".opblock-model").each(function (i, e) {
            var tag = $(e);
            var arrow = tag.find('.arrow');
            if (tag.hasClass('is-open')) {
                tag.removeClass('is-open');
                tag.next().hide();
                arrow.html('\u25e2');
            }
        });
    });
    $(".opblock-model").each(function (i, e) {
        var tag = $(e);
        tag.click(function () {
            var arrow = tag.find('.arrow');
            if (tag.hasClass('is-open')) {
                tag.removeClass('is-open');
                tag.next().hide();
                arrow.html('\u25e2');
            } else {
                tag.addClass('is-open');
                tag.next().show();
                arrow.html('\u25e5');
            }
        });
    });
    $(".opblock-summary").each(function (i, e) {
        var tag = $(e);
        tag.click(function () {
            var arrow = tag.find('.arrow');
            if (tag.hasClass('is-open')) {
                tag.removeClass('is-open');
                tag.next().hide();
                arrow.html('\u25e2');
            } else {
                tag.addClass('is-open');
                tag.next().show();
                arrow.html('\u25e5');
            }
        });
    });
    $(".try-out-btn").each(function (i, e) {
        var btn = $(e);
        btn.click(function () {
            var body = btn.parents('.opblock-body');
            var code = body.find('.highlight-code');
            var tryCode = body.find('.try-code');
            if (btn.hasClass('cancel')) {
                btn.removeClass('cancel');
                btn.html('\u8bd5\u4e00\u8bd5');
                code.show();
                tryCode.hide();
            } else {
                btn.addClass('cancel');
                btn.html('\u53d6\u6d88');
                code.hide();
                var req = localStorage.getItem(btn.attr("id"));
                if (req) {
                    var body = btn.parents('.opblock-body');
                    body.find('.req-box').val(req);
                }
                tryCode.show();
            }
        });
    });
    $(".execute").each(function (i, e) {
        var btn = $(e);
        btn.click(function () {
            var body = btn.parents('.opblock-body');
            body.find('.res-box').val('\u0020\u52a0\u8f7d\u4e2d...');
            var req = body.find('.req-box').val();
            var url = '/' + tenant + '/rpc/service';
            $.ajax(
                {
                    type: "POST",
                    dataType: "json",
                    data: req,
                    contentType: "application/json",
                    url: url,
                    error: function (XMLHttpRequest, textStatus, errorThrown) {
                        body.find('.res-box').val(XMLHttpRequest.status + ':' + XMLHttpRequest.responseText);
                    },
                    success: function (data) {
                        var tryBtn = body.find(".try-out-btn");
                        console.log(tryBtn.attr("id"));
                        localStorage.setItem(tryBtn.attr("id"), req);
                        body.find('.res-box').val(JSON.stringify(data, null, 4));
                    }
                });
        });
    });
});