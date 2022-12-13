$.component("JCompany", {
    init: function () {
        let me = this, companies = (jdoo.web.cookie('ctx_company_ids') || '').split(',');
        jdoo.rpc({
            model: "rbac.user",
            method: "getUserCompanies",
            args: {},
            onsuccess: function (r) {
                me.data = r.data;
                $.each(me.data, function () {
                    this.check = companies.indexOf(this.id) > -1;
                });
                me.initCompany();
            }
        });
        me.dom.next().on('click', '[role=button]', function () {
            let cid = $(this).attr('data-id');
            jdoo.rpc({
                model: "rbac.user",
                method: "updateUserCompany",
                args: { companyId: cid },
                onsuccess: function (r) {
                    $.each(me.data, function () {
                        let c = this;
                        c.main = c.id === cid;
                        if (c.main) {
                            c.check = true;
                        }
                    });
                    me.initCompany();
                }
            });
        });
        me.dom.next().on('change', '[role=select-company]', function () {
            let ckb = $(this), cid = ckb.attr('data-id');
            $.each(me.data, function () {
                let c = this;
                if (c.id == cid) {
                    c.check = ckb.is(':checked');
                }
            });
            me.updateCookies();
        });
    },
    updateCookies: function () {
        let me = this, companies = [];
        $.each(me.data, function () {
            let c = this;
            if (c.main) {
                companies.splice(0, 0, c.id);
            } else if (c.check) {
                companies.push(c.id);
            }
        });
        jdoo.web.cookie('ctx_company_ids', companies.join())
    },
    initCompany: function () {
        let me = this, html = '';
        if (me.data.length > 1) {
            $.each(me.data, function () {
                let c = this;
                if (c.main) {
                    me.dom.html(c.present).attr('data-id', c.id);
                } else {
                    html += `<div class="dropdown-item d-flex p-0">
                        <div class="border-right border-info">
                            <span class="btn p-2">
                                <input type="checkbox" ${(c.check ? 'checked' : '')} data-id="${c.id}" role="select-company"/>
                            </span>
                        </div>
                        <div role="button" data-id="${c.id}" class="d-flex flex-grow-1 ml-2 align-items-center">${c.present}</div></div>`;
                }
            });
            me.dom.next().html(html);
            me.dom.show();
        }
        me.updateCookies();
    }
});