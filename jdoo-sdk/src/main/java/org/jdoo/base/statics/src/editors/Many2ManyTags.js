jdoo.editor('many2many_tags', {
    limit: 10, //显示条数
    getTpl: function () {
        return `<div class="form-control m2m-tag" id="${this.name + '-' + jdoo.nextId()}">
                    <div class="m2m-tag-container">
                        <ul class="m2m-tag-selection-body">
                            <li class="m2m-tag-input">
                                <input type="text" class="lookup m2m-tag-field"/>
                            </li>
                        </ul>
                    </div>
                    <div class="container-fluid dropdown-lookup search-dropdown">
                        <div class="lookup-body"></div>
                        <div class="card-footer">
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
        let me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        me.offset = 0;
        me.delete = [];
        me.create = [];
        me.keyword = '';
        dom.html(me.getTpl())
            .on('click', '[data-btn=next]', function (e) {
                me.offset += me.limit;
                me.lookup();
            }).on('click', '[data-btn=prev]', function (e) {
                me.offset -= me.limit;
                if (me.offset < 0) {
                    me.offset = 0;
                }
                me.lookup();
            }).on('click', '.m2m-tag-container', function (e) {
                if (me.dom.attr('readonly')) {
                    return;
                }
                me.showDropdown();
                me.lookup();
                e.preventDefault();
                e.stopPropagation();
            }).on('click', '.dropdown-lookup', function (e) {
                me.dropclick = true;
            }).on('click', '.m2m-tag-choice-remove', function (e) {
                $(this).closest('li').remove();
                let id = $(this).closest('li').data().value;
                if (me.create.indexOf(id) > -1) {
                    me.create.remove(id);
                } else {
                    me.delete.push(id);
                }
                dom.triggerHandler('valuechange', [me]);
                e.preventDefault();
                e.stopPropagation();
            });
        $(document).on('click', function () {
            if (me.dropclick) {
                me.dropclick = false;
            } else {
                me.hideDropdown();
            }
        });
        //通过查询条件过滤数据
        let timer;
        dom.find('input').keyup(function () {
            let i = $(this);
            if (!me.open) {
                me.showDropdown();
            }
            clearTimeout(timer);
            timer = setTimeout(function () {
                me.offset = 0;
                me.keyword = i.val();
                me.lookup();
            }, 500);
        });
    },
    onValueChange: function (handler) {
        this.dom.on('valuechange', handler);
    },
    lookup: function () {
        let me = this, body = me.dom.find('.lookup-body');
        body.html('<div class="m-2">' + '加载中'.t() + '</div>');
        let criteria = [["present", "like", me.keyword]];
        jdoo.rpc({
            model: me.model,
            module: me.module,
            method: "searchRelated",
            args: {
                relatedField: me.field.name,
                options: {
                    limit: me.limit,
                    offset: me.offset,
                    criteria: criteria,
                    nextTest: true,
                    activeTest: true
                }
            },
            onsuccess: function (r) {
                if (r.data.values[0]) {
                    let html = `<div class="select2-container select2-container--default select2-container--open row"><ul class="select2-results__options col-12">`;
                    $.each(r.data.values, function () {
                        let sel = '';
                        if (me.getSelected().indexOf(this.id) != -1) {
                            sel = ' m2m-tag-selected" "aria-selected"="true';
                        }
                        html += `<li class="select2-results__option${sel}" data-value="${this.id}">${this.present}</li>`;
                    });
                    html += '</ul></div>';
                    body.html(html);
                    body.find('.select2-results__option').hover(function () {
                        body.find('.select2-results__option').removeClass('select2-results__option--highlighted').removeAttr('aria-selected');
                        $(this).addClass('select2-results__option--highlighted').attr('aria-selected', 'true');
                    }).on('click', function () {
                        let item = $(this), txt = item.html(), value = item.data().value, list = [{ "present": txt, "id": value }]
                        if (me.getSelected().indexOf(value) == -1) {
                            me.addValue(list, "create");
                        }
                        me.hideDropdown();
                    });
                } else {
                    body.html(`<div class="m-2">${'没有数据'.t()}</div>`);
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
    renderData: function () {
        let me = this;
        jdoo.rpc({
            model: me.model,
            module: me.module,
            method: "searchRelated",
            args: {
                relatedField: me.field.name,
                options: {
                    criteria: [['id', 'in', me.values || []]],
                    offset: 0,
                    limit: me.limit,
                }
            },
            onsuccess: function (r) {
                let values = r.data.values;
                me.addValue(values, "unchanged");
            }
        });
    },
    addValue: function (list, type) {
        let me = this, html = "", r = me.dom.attr('readonly');
        $.each(list, function () {
            if (type == "create") {
                me.create.push(this.id);
            }
            html += '<li class="m2m-tag-choice"  data-value="'
                + this.id + '">  <span class="m2m-tag-choice-remove" ' +
                (r ? 'style="display:none"' : '')
                + ' role="presentation">×</span>' + this.present + ' </li>';
        });
        me.dom.find(".lookup").closest('li').before(html);
        if (type !== 'unchanged') {
            me.dom.triggerHandler('valuechange', [me]);
        }
    },
    showDropdown: function () {
        let me = this;
        me.dom.find('.dropdown-lookup').show();
        me.dom.find('.m2m-tag-field').css("min-width", "80px").focus();
        me.open = true;
    },
    hideDropdown: function () {
        let me = this;
        me.dom.find('.dropdown-lookup').hide();
        me.dom.find('.m2m-tag-field').val('').css("min-width", "0");
        me.offset = 0;
        me.keyword = '';
        me.open = false;
    },
    setReadonly: function (v) {
        let me = this;
        if (v) {
            me.dom.find('.m2m-tag-choice-remove,.m2m-tag-field').hide();
            me.dom.attr('readonly', true);
        } else {
            me.dom.find('.m2m-tag-choice-remove,.m2m-tag-field').show();
            me.dom.removeAttr('readonly');
        }
    },
    //获取选中的值
    getSelected: function () {
        let me = this, valData = [];
        me.dom.find("li.m2m-tag-choice").map(function (e) {
            valData.push($(this).attr('data-value'));
        })
        return valData;
    },
    getValue: function () {
        let me = this, v = [];
        for (let i = 0; i < me.create.length; i++) {
            v.push([4, me.create[i], 0]);
        }
        for (let i = 0; i < me.delete.length; i++) {
            v.push([3, me.delete[i], 0]);
        }
        return v;
    },
    setValue: function (v) {
        let me = this;
        me.values = v || [];
        me.delete = [];
        me.create = [];
        me.dom.find("li.m2m-tag-choice").map(function (e) {
            $(this).closest('li').remove();
        })
        me.renderData();
    }
});