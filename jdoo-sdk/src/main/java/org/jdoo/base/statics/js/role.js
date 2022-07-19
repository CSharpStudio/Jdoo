function editPermission() {
    var getRoleId = function(){
        return page.getSelected()[0];;
    }
    var globaleId = 0;
    var getId = function () {
        return globaleId++;
    };
    var modal = $("#modal-permission");
    if (modal.length == 0) {
        var html = '<div class="modal fade" id="modal-permission">\
                        <div class="modal-dialog modal-xl">\
                            <div class="modal-content">\
                            <div class="modal-header">\
                                <h4 class="modal-title">'+ '角色'.t() + '<span role="role-name"></span>' + '权限'.t() + '</h4>\
                                <button type="button" class="close" data-dismiss="modal" aria-label="Close">\
                                <span aria-hidden="true">&times;</span>\
                                </button>\
                            </div>\
                            <div class="modal-body">\
                                <p>'+ '加载中'.t() + '</p>\
                            </div>\
                            <div class="modal-footer justify-content-between">\
                                <button type="button" class="btn btn-default" data-dismiss="modal">'+ '关闭'.t() + '</button>\
                                <button type="button" role="btn-save" class="btn btn-primary">'+ '保存'.t() + '</button>\
                            </div>\
                            </div>\
                        </div>\
                    </div>';
        $(document.body).append(html);
        modal = $("#modal-permission");
        modal.find('button[role=btn-save]').on('click', function () {
            var btn = $(this);
            btn.attr('disabled', true);
            btn.html('保存中'.t());
            var permission = [];
            modal.find('input[type=checkbox]').each(function () {
                var input = $(this);
                if (input.is(":checked")) {
                    permission.push(input.attr('data-id'));
                }
            });
            $.jrpc({
                model: "rbac.role",
                method: "savePermission",
                args: { ids: [getRoleId()], permissions: permission },
                onsuccess: function (r) {
                    Msg.showMsg('保存成功'.t());
                    btn.attr('disabled', false);
                    btn.html('保存'.t());
                }
            });
        });
    } else {
        modal.find('.modal-body').html('<p>' + '加载中'.t() + '</p>');
    }
    modal.modal({ backdrop: false });
    $.jrpc({
        model: "rbac.role",
        method: "getPresent",
        args: { ids: [getRoleId()] },
        onsuccess: function (r) {
            modal.find('[role=role-name]').html('[' + r.data[0][1] + ']');
        }
    });
    $.jrpc({
        model: "rbac.permission",
        method: "loadModelPermission",
        args: {},
        onsuccess: function (r) {
            var html = '';
            $.each(r.data, function () {
                var appId = 'app_' + getId();
                html += '<div class="card card-primary card-outline"><a class="d-block w-100" data-toggle="collapse" href="#'
                    + appId + '" aria-expanded="true"><div class="card-header">' + this.app + '</div></a><div id="' + appId + '" class="collapse show">';
                $.each(this.models, function () {
                    html += '<div class="card-body"><div class="per-menu">' + this.menu + '(' + this.model + ')</div><div class="row ml-5">';
                    $.each(this.permissions, function () {
                        var id = 'per_' + getId();
                        var checked = this.role_ids.indexOf(getRoleId()) > -1 ? ' checked="checked"' : '';
                        html += '<div class="form-check m-2"><input ' + checked + ' data-id="' + this.id + '" type="checkbox" class="form-check-input" id="'
                            + id + '"/><label for="' + id + '" class="form-check-label">' + this.name.t() + '(' + this.auth + ')</label></div>';
                    });
                    html += "</div></div>";
                });
                html += "</div></div>";
            });
            modal.find('.modal-body').html(html);
        }
    });
}

