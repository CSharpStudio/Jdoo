jdoo.component("JIconLib", {
    selector: '[data-widget=icon-lib]',
    lib: '/res/org/jdoo/base/statics/plugins/fontawesome-free/css/all.css',
    getTpl: function () {
        return `<div class="modal fade" id="modal-icon">
                    <div class="modal-dialog modal-xl">
                        <div class="modal-content">
                        <div class="modal-header">
                            <h4 class="modal-title">${'图标库'.t()}</h4>
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                        <div class="modal-body">
                        </div>
                        <div class="modal-footer justify-content-between">
                            <button type="button" class="btn btn-default" data-dismiss="modal">${'关闭'.t()}</button>
                        </div>
                        </div>
                    </div>
                </div>`
    },
    init: function () {
        let me = this;
        $(document).on("click", me.selector, function () {
            me.show();
        });
    },
    show: function () {
        let me = this;
        $("#modal-icon").remove();
        $(document.body).append(me.getTpl());
        let modal = $("#modal-icon");
        modal.modal({ backdrop: false });
        $.ajax({
            url: me.lib,
            type: "GET",
            dataType: "text",
            success: function (rs) {
                let html = '<div style="grid-template-columns: repeat(6, 1fr);display: grid;">';
                $.each(rs.match(/fa-[\w\-]+:before/gi), function () {
                    let n = this.replace(':before', '');
                    html += '<div class="mr-2"><i class="fa ' + n + ' mr-1"></i>' + n + '</div>';
                });
                html += '</div>';
                modal.find(".modal-body").html(html);
            },
            error: function (rs) {
                console.log(rs);
            }
        });
    }
});