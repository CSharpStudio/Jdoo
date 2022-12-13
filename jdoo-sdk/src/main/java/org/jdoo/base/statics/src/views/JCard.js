$.component("JCard", {
    ajax: jdoo.emptyFn,
    init: function () {
        let me = this, dom = me.dom;
        me._fields = [];
        me.sel = [];
        if (me.arch) {
            let arch = jdoo.utils.parseXML(me.arch), card = arch.children('card');
            if (card.length > 0) {
                me.tbarArch = card.children('toolbar').prop('outerHTML');
                me.limit = eval(card.attr('limit') || 16);
                me.pager.limit = me.limit;
                card.children('field').each(function () {
                    let el = $(this),
                        name = el.attr('name'),
                        field = me.fields[name];
                    if (!field) {
                        throw new Error('模型' + me.model + '找不到字段' + name);
                    }
                    if (!field.deny) {
                        me._fields.push(name);
                    }
                });
                let tpl = card.children('template').html();
                me.tpl = juicer(tpl);
            }
        }
        dom.addClass('row mt-3 card-view');
        me.load();
        me.onSelected(me.selected);
    },
    onSelected: function (handler) {
        this.dom.on('selected', handler);
    },
    load: function () {
        let me = this, dom = me.dom;
        dom.html(`<div class="col-12" style="text-align:center;">${'数据加载中'.t()}</div>`);
        me.ajax(me, function (e) {
            if (e.data.length > 0) {
                dom.empty();
                $.each(e.data, function (i, d) {
                    for (let k in this) {
                        let field = me.fields[k];
                        if (field.type === 'selection') {
                            let v = this[k];
                            this[k] = [v, field.options[v]];
                        }
                    }
                    let html = me.tpl.render(this);
                    dom.append(`<div class="col-3" style="min-width: 18rem;"><div class="card" data="${this['id']}">${html}</div></div>`);
                });
            } else {
                dom.html(`<div class="col-12" style="text-align:center;">${'没有数据'.t()}</div>`);
            }
            dom.find('.card').on('click', function () {
                let card = $(this);
                dom.find('.card').removeClass('selected');
                card.addClass('selected');
                me.sel = [card.attr('data')];
                dom.triggerHandler('selected', [me, me.sel]);
            })
            if (typeof me.dblClick === 'function') {
                dom.find('.card').on('dblclick', function () {
                    let card = $(this);
                    let id = card.attr('data');
                    me.dblClick(me, id);
                });
            };
            dom.triggerHandler('loaded', [me, e.data]);
            me.sel = [];
            dom.triggerHandler('selected', [me, me.sel]);
        });
    },
    getSelected: function () {
        return this.sel;
    },
    getFields: function () {
        return this._fields;
    }
});