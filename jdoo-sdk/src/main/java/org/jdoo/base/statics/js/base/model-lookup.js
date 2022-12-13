//@ sourceURL=model-lookup.js
jdoo.editor('model_lookup', {
    limit: 10,
    getTpl: function () {
        return `<div class="model-lookup" style="position:relative">
                    <input type="text" class="form-control lookup"/>
                    <div class="container-fluid dropdown-lookup search-dropdown">
                        <div class="lookup-body"></div>
                        <div class="card-footer">
                            <button type="button" data-btn="clear" class="btn btn-sm btn-default">${'清空'.t()}</button>
                            <div class="btn-group float-right">
                                <button type="button" data-btn="prev" class="btn btn-sm btn-default">
                                    <i class="fa fa-angle-left"></i>
                                </button>
                                <button type="button" data-btn="next" class="btn btn-sm btn-default">
                                    <i class="fa fa-angle-right"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>`;
    },

    init: function () {
        let me = this;
        me.offset = 0;
        me.keyword = '';
        me.dom.append(me.getTpl())
            .on('click', '[data-btn=clear]', function (e) {
                me.offset = 0;
                me.keyword = '';
                me.setValue();
                me.dom.find("input").triggerHandler("change");
            })
            .on('click', '[data-btn=next]', function (e) {
                me.offset += me.limit;
                me.lookup();
            })
            .on('click', '[data-btn=prev]', function (e) {
                me.offset -= me.limit;
                if (me.offset < 0) {
                    me.offset = 0;
                }
                me.lookup();
            })
            .on('click', '.lookup', function (e) {
                if ($(this).attr('readonly')) {
                    return;
                }
                me.showDropdown();
                me.lookup();
                e.preventDefault();
                e.stopPropagation();
            })
            .on('click', '.dropdown-lookup', function (e) {
                me.dropclick = true;
            });
        $(document).on('click', function () {
            if (me.dropclick) {
                me.dropclick = false;
            } else {
                me.hideDropdown();
            }
        });
        let timer;
        me.dom.find('input').keyup(function () {
            let input = $(this);
            if (input.attr('readonly')) {
                return;
            }
            if (!me.open) {
                me.showDropdown();
            }
            clearTimeout(timer);
            timer = setTimeout(function () {
                me.offset = 0;
                me.keyword = input.val();
                me.lookup();
            }, 500);
        });
    },
    onValueChange: function (handler) {
        let me = this;
        me.dom.find('input').on('change', function (e) {
            handler(e, me);
        });
    },
    lookup: function () {
        let me = this,
            el = me.dom.find('input'),
            filter = me.dom.attr('search') || null,
            body = me.dom.find('.lookup-body');
        body.html('<div class="m-2">' + '加载中'.t() + '</div>');
        let criteria = ['|', ["model", "like", me.keyword], ["name", "like", me.keyword]];
        if (filter) {
            filter = decodeURI(filter);
            let data = me.owner.getRawData();
            data.__filter = new Function("return " + filter);
            filter = data.__filter();
            $.each(filter, function () {
                criteria.push(this);
            });
        }
        jdoo.rpc({
            model: 'ir.model',
            module: 'base',
            method: "search",
            args: {
                fields: ['model', 'name'],
                limit: me.limit,
                offset: me.offset,
                criteria: criteria,
                nextTest: true
            },
            onsuccess: function (r) {
                if (r.data.values[0]) {
                    let html = `<div class="select2-container select2-container--default select2-container--open row"><ul class="select2-results__options col-12">`;
                    $.each(r.data.values, function () {
                        let sel = this.model === el.attr('data-value') ? ' select2-results__option--highlighted" "aria-selected"="true' : '';
                        html += '<li class="select2-results__option' + sel + '" data-value="' + this.model + '">' + this.name + '(' + this.model + ')</li>';
                    });
                    html += '</ul></div>';
                    body.html(html);
                    body.find('.select2-results__option').hover(function () {
                        body.find('.select2-results__option').removeClass('select2-results__option--highlighted').removeAttr('aria-selected');
                        $(this).addClass('select2-results__option--highlighted').attr('aria-selected', 'true');
                    }, function () {
                    }).on('click', function () {
                        let item = $(this);
                        me.offset = 0;
                        me.keyword = '';
                        me.setValue(item.attr('data-value'));
                        me.dom.find("input").triggerHandler("change");
                        me.hideDropdown();
                    });
                } else {
                    body.html('<div class="m-2">' + '没有数据'.t() + '</div>');
                }
                let nextBtn = me.dom.find('[data-btn=next]');
                if (r.data.hasNext) {
                    nextBtn.attr('disabled', false);
                } else {
                    nextBtn.attr('disabled', true);
                }
            }
        });
    },
    showDropdown: function () {
        this.dom.find('.dropdown-lookup').show().addClass('show');
        this.open = true;
    },
    hideDropdown: function () {
        this.dom.find('.dropdown-lookup').hide().removeClass('show');
        this.open = false;
    },
    setReadonly: function (v) {
        if (v) {
            this.dom.find('input').attr('readonly', true);
        } else {
            this.dom.find('input').removeAttr('readonly');
        }
    },
    getValue: function () {
        return this.dom.find('input').val();
    },
    getRawValue: function () {
        let me = this, el = me.dom.find('input');
        if (val) {
            return val;
        }
        return null;
    },
    setValue: function (v) {
        let me = this,
            el = me.dom.find('input');
        if (v) {
            el.val(v);
        } else {
            el.val('');
        }
    }
});
jdoo.searchEditor('model_lookup', {
    extends: "editors.model_lookup",
    getCriteria: function () {
        let val = this.getRawValue();
        if (val) {
            return [[this.name, '=', val]];
        }
        return [];
    },
    getText: function () {
        return this.getValue();
    },
});
