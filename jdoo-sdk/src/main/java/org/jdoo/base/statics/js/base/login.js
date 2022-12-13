if (top.window.location != window.location) {
    top.window.location.reload();
}
$(function () {
    $.validator.setDefaults({
        submitHandler: function () {
            $("[name=errorMessage]").html('');
            $("[name=loginSubmit]").attr("disabled", true);
            var remember = $('[name=remember]').is(":checked");
            jdoo.rpc({
                url: jdoo.web.getTenantPath() + "/rpc/login",
                data: {
                    id: jdoo.utils.randomId(),
                    jsonrpc: "2.0",
                    method: "login",
                    params: {
                        login: $('[name=account]').val(),
                        password: window.btoa(unescape(encodeURIComponent($('[name=password]').val()))),
                        remember: remember
                    }
                },
                success: function (rs) {
                    $("[name=loginSubmit]").attr("disabled", false);
                    if (rs.error) {
                        console.log(rs.error);
                        $("[name=errorMessage]").html(rs.error.message);
                    } else {
                        var r = rs.result, opt = remember ? { expires: 7 } : {};
                        jdoo.web.cookie('ctx_token', r.token, opt);
                        jdoo.web.cookie('ctx_user', r.id, opt);
                        jdoo.web.cookie('ctx_tz', r.tz, opt);
                        jdoo.web.cookie('ctx_lang', r.lang, opt);
                        jdoo.web.cookie('ctx_company', r.company, opt);
                        //TODO界面显示公司
                        jdoo.web.cookie('user_companies', r.companies, opt);
                        var url = jdoo.web.getUrlParam('url') || jdoo.web.getTenantPath();
                        window.location.href = url + window.location.hash;
                    }
                },
                error: function (rs) {
                    $("[name=loginSubmit]").attr("disabled", false);
                    console.log(rs);
                    alert('发生错误：' + rs);
                }
            });
        }
    });
    $('#loginForm').validate({
        rules: {
            account: {
                required: true
            },
            password: {
                required: true
            }
        },
        messages: {
            account: {
                required: "请输入账号/手机号/邮箱".t()
            },
            password: {
                required: "请输入密码".t()
            }
        },
        errorElement: 'span',
        errorPlacement: function (error, element) {
            error.addClass('invalid-feedback');
            element.closest('.form-group').append(error);
        },
        highlight: function (element, errorClass, validClass) {
            $(element).addClass('is-invalid');
        },
        unhighlight: function (element, errorClass, validClass) {
            $(element).removeClass('is-invalid');
        }
    });
});