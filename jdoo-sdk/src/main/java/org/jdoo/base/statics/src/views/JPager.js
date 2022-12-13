$.component("JPager", {
    pageSize: 10,
    getTpl: function () {
        return `<div class="btn-group">
                    <div class="pager-from input-group input-group-sm" style="padding: 0;"><span>1</span></div>
                    <span style="min-width:.8rem;text-align: center;">-</span>
                    <div class="pager-to input-group input-group-sm" style="padding: 0;"></div>
                    <span style="min-width:1rem;text-align: center;">/</span>
                    <div class="pager-total" style="padding: 0;"><span>?</span></div>
                </div>
                <div class="btn-group ml-2">
                    <button type="button" class="btn btn-sm btn-default pager-prev">
                        <i class="fa fa-angle-left"></i>
                    </button>
                    <button type="button" class="btn btn-sm btn-default pager-next">
                        <i class="fa fa-angle-right"></i>
                    </button>
                </div>`;
    },
    init: function () {
        let me = this, dom = me.dom;
        me.limit = me.pageSize;
        me.from = 1;
        me.to = me.limit;
        dom.html(me.getTpl())
            .on('click', 'div.pager-from', function (e) {
                let el = $(this);
                if (!el.hasClass('edit')) {
                    el.html('<input type="text" class="form-control" style="width:3rem;">');
                    let input = el.find('input');
                    input.val(me.from);
                    input.focus();
                    input.on('blur', function () {
                        let val = parseInt(input.val());
                        if (!isNaN(val) && val > 0) {
                            me.from = val;
                            el.html('<span>' + val + '</span>');
                            if (me.from > me.to) {
                                me.to = me.from;
                                el.find('div.pager-to').html('<span>' + me.to + '</span>');
                            }
                            me.setLimit(me.to - me.from + 1);
                            dom.triggerHandler('pageChange', [me]);
                        } else {
                            el.html('<span>' + me.from + '</span>');
                        }
                        el.removeClass('edit');
                    });
                    el.addClass('edit');
                }
            }).on('click', 'div.pager-to', function (e) {
                let el = $(this);
                if (!el.hasClass('edit')) {
                    el.html('<input type="text" class="form-control" style="width:3rem;">');
                    let input = el.find('input');
                    input.val(me.to);
                    input.focus();
                    input.on('blur', function () {
                        let val = parseInt(input.val());
                        if (!isNaN(val) && val > 0) {
                            me.to = val;
                            el.html('<span>' + val + '</span>');
                            if (me.to < me.from) {
                                me.from = me.to;
                                el.find('div.pager-from').html('<span>' + me.from + '</span>');
                            }
                            me.setLimit(me.to - me.from + 1);
                            dom.triggerHandler('pageChange', [me]);
                        } else {
                            el.html('<span>' + me.to + '</span>');
                        }
                        el.removeClass('edit');
                    });
                    el.addClass('edit');
                }
            }).on('click', 'div.pager-total', function (e) {
                dom.triggerHandler('counting', [me]);
            }).on('click', 'button.pager-prev', function (e) {
                if (me.from > me.limit) {
                    me.from -= me.limit;
                } else {
                    me.from = 1;
                }
                me.to = me.from + me.limit - 1;
                dom.triggerHandler('pageChange', [me]);
            }).on('click', 'button.pager-next', function (e) {
                me.from += me.limit;
                me.to = me.from + me.limit - 1;
                dom.triggerHandler('pageChange', [me]);
            });
        me.onPageChange(me.pageChange);
        me.onCounting(me.counting);
        me.onLimitChange(me.limitChange);
    },
    onLimitChange: function (handler) {
        this.dom.on("limitChange", handler);
    },
    onPageChange: function (handler) {
        this.dom.on("pageChange", handler);
    },
    onCounting: function (handler) {
        this.dom.on("counting", handler);
    },
    setLimit: function (limit) {
        let me = this;
        me.limit = limit;
        me.dom.triggerHandler('limitChange', [me]);
    },
    getLimit: function () {
        return this.limit;
    },
    getOffest: function () {
        return this.from - 1;
    },
    update: function (e) {
        let me = this, dom = me.dom;
        dom.show();
        if (e.from) {
            me.from = e.from;
        }
        if (e.to) {
            me.to = e.to;
        }
        dom.find('div.pager-from').html('<span>' + me.from + '</span>');
        dom.find('div.pager-to').html('<span>' + me.to + '</span>');
        if (e.next === true) {
            dom.find('button.pager-next').attr('disabled', false);
        } else if (e.next === false) {
            dom.find('button.pager-next').attr('disabled', true);
        }
        dom.find('button.pager-prev').attr('disabled', me.from === 1);
        let t = parseInt(e.total);
        if (!isNaN(t)) {
            dom.find('div.pager-total').html('<span>' + t + '</span>');
        }
    },
    reset: function () {
        let me = this, dom = me.dom;
        me.from = 1;
        me.to = me.limit;
        dom.find('div.pager-from').html('<span>' + me.from + '</span>');
        dom.find('div.pager-to').html('<span>' + me.to + '</span>');
        dom.find('div.pager-total').html('<span>?</span>');
        dom.find('button.pager-next').removeClass('disabled');
    },
    hide: function () {
        this.dom.hide();
    }
});