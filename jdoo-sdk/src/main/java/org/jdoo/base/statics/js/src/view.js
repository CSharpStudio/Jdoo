(function (factory) {
    "use strict";
    factory(jQuery, window, document);
}(function ($, window, document, undefined) {
    "use strict";

    //#region utils
    var globaleId = 0;
    var getId = function () {
        return globaleId++;
    };

    moment.defineLocale('zh-cn', {
        months: '一月_二月_三月_四月_五月_六月_七月_八月_九月_十月_十一月_十二月'.split(
            '_'
        ),
        monthsShort: '1月_2月_3月_4月_5月_6月_7月_8月_9月_10月_11月_12月'.split(
            '_'
        ),
        weekdays: '星期日_星期一_星期二_星期三_星期四_星期五_星期六'.split('_'),
        weekdaysShort: '周日_周一_周二_周三_周四_周五_周六'.split('_'),
        weekdaysMin: '日_一_二_三_四_五_六'.split('_'),
        longDateFormat: {
            LT: 'HH:mm',
            LTS: 'HH:mm:ss',
            L: 'YYYY/MM/DD',
            LL: 'YYYY年M月D日',
            LLL: 'YYYY年M月D日Ah点mm分',
            LLLL: 'YYYY年M月D日ddddAh点mm分',
            l: 'YYYY/M/D',
            ll: 'YYYY年M月D日',
            lll: 'YYYY年M月D日 HH:mm',
            llll: 'YYYY年M月D日dddd HH:mm',
        },
        meridiemParse: /凌晨|早上|上午|中午|下午|晚上/,
        meridiemHour: function (hour, meridiem) {
            if (hour === 12) {
                hour = 0;
            }
            if (meridiem === '凌晨' || meridiem === '早上' || meridiem === '上午') {
                return hour;
            } else if (meridiem === '下午' || meridiem === '晚上') {
                return hour + 12;
            } else {
                // '中午'
                return hour >= 11 ? hour : hour + 12;
            }
        },
        meridiem: function (hour, minute, isLower) {
            var hm = hour * 100 + minute;
            if (hm < 600) {
                return '凌晨';
            } else if (hm < 900) {
                return '早上';
            } else if (hm < 1130) {
                return '上午';
            } else if (hm < 1230) {
                return '中午';
            } else if (hm < 1800) {
                return '下午';
            } else {
                return '晚上';
            }
        },
        calendar: {
            sameDay: '[今天]LT',
            nextDay: '[明天]LT',
            nextWeek: function (now) {
                if (now.week() !== this.week()) {
                    return '[下]dddLT';
                } else {
                    return '[本]dddLT';
                }
            },
            lastDay: '[昨天]LT',
            lastWeek: function (now) {
                if (this.week() !== now.week()) {
                    return '[上]dddLT';
                } else {
                    return '[本]dddLT';
                }
            },
            sameElse: 'L',
        },
        dayOfMonthOrdinalParse: /\d{1,2}(日|月|周)/,
        ordinal: function (number, period) {
            switch (period) {
                case 'd':
                case 'D':
                case 'DDD':
                    return number + '日';
                case 'M':
                    return number + '月';
                case 'w':
                case 'W':
                    return number + '周';
                default:
                    return number;
            }
        },
        relativeTime: {
            future: '%s后',
            past: '%s前',
            s: '几秒',
            ss: '%d 秒',
            m: '1 分钟',
            mm: '%d 分钟',
            h: '1 小时',
            hh: '%d 小时',
            d: '1 天',
            dd: '%d 天',
            w: '1 周',
            ww: '%d 周',
            M: '1 个月',
            MM: '%d 个月',
            y: '1 年',
            yy: '%d 年',
        },
        week: {
            // GB/T 7408-1994《数据元和交换格式·信息交换·日期和时间表示法》与ISO 8601:1988等效
            dow: 1, // Monday is the first day of the week.
            doy: 4, // The week that contains Jan 4th is the first week of the year.
        },
    });
    //#endregion

    //#region columns
    $.columns = {};
    //#region CharColumn
    var CharColumn = function (opt) {
        var me = this;
        me.options = $.extend({}, CharColumn.defaults, opt);
    };
    CharColumn.defaults = {
    };
    CharColumn.prototype = {
        init: function () {

        },
        render: function () {
            var me = this;
            return function (data, type, row) {
                if (data === null) {
                    data = '';
                }
                return '<span data-field="' + me.options.field.name + '">' + data + '</span>';
            }
        }
    };
    $.columns['char'] = function (opt) {
        return new CharColumn(opt);
    };
    //#endregion
    //#region SelectionColumn
    var SelectionColumn = function (opt) {
        var me = this;
        me.options = $.extend({}, SelectionColumn.defaults, opt);
    };
    SelectionColumn.defaults = {
    };
    SelectionColumn.prototype = {
        init: function () {

        },
        render: function () {
            var me = this;
            return function (data, type, row) {
                var v = me.options.field.options[data];
                if (v === undefined) {
                    v = '';
                }
                return '<span data-field="' + me.options.field.name + '">' + v + '</span>';
            }
        }
    };
    $.columns['selection'] = function (opt) {
        return new SelectionColumn(opt);
    };
    //#endregion
    //#region BoolColumn
    var BoolColumn = function (opt) {
        var me = this;
        me.options = $.extend({}, BoolColumn.defaults, opt);
    };
    BoolColumn.defaults = {
    };
    BoolColumn.prototype = {
        init: function () {

        },
        render: function () {
            var me = this;
            return function (data, type, row) {
                if (data) {
                    return '<label class="checked-column"></label>';
                }
                return '<label class="unchecked-column" ></label>';
            }
        }
    };
    $.columns['boolean'] = function (opt) {
        return new BoolColumn(opt);
    };
    //#endregion
    //#region Many2OneColumn
    var Many2OneColumn = function (opt) {
        var me = this;
        me.options = $.extend({}, Many2OneColumn.defaults, opt);
    };
    Many2OneColumn.defaults = {
    };
    Many2OneColumn.prototype = {
        init: function () {

        },
        render: function () {
            var me = this;
            return function (data, type, row) {
                if (data[0]) {
                    return '<span data-field="' + me.options.field.name + '" data-value="' + data[0] + '">' + data[1] + '</span>';
                }
                return '<span data-field="' + me.options.field.name + '" data-value=""></span>';
            }
        }
    };
    $.columns['many2one'] = function (opt) {
        return new Many2OneColumn(opt);
    };
    //#endregion
    //#endregion

    //#region editors
    $.editors = {
    };
    //#region CharEditor
    var CharEditor = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, CharEditor.defaults, opt);
        me.init();
    };
    CharEditor.defaults = {
        attrs: {},
        onchange: function () { }
    };
    CharEditor.prototype = {
        init: function () {
            var me = this,
                id = me.options.field.name + '-' + getId(),
                readonly = me.options.attrs['readonly'] ? 'readonly ' : '';
            me.elem.append('<input type="text" ' + readonly + 'class="form-control" id="' + id + '"/>');
            me.elem.find('input').on('change', function () {
                me.options.onchange(me);
            });
        },
        readonly: function (v) {
            if (v) {
                this.elem.children('input').attr('readonly', true);
            } else {
                this.elem.children('input').removeAttr('readonly');
            }
        },
        getVal: function () {
            return this.elem.children('input').val();
        },
        getText: function () {
            return this.getVal();
        },
        setVal: function (v) {
            this.elem.children('input').val(v);
        }
    };
    $.editors['char'] = function (elem, opt) {
        return new CharEditor(elem, opt);
    };
    //#endregion
    //#region IntEditor
    var IntEditor = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, IntEditor.defaults, opt);
        me.init();
    };
    IntEditor.defaults = {
        attrs: {},
        onchange: function () { }
    };
    IntEditor.prototype = {
        init: function () {
            var me = this,
                id = me.options.field.name + '-' + getId(),
                readonly = me.options.attrs['readonly'] ? 'readonly ' : '';
            me.elem.append('<input type="number" ' + readonly + 'id="' + id + '"/>');
            me._spinner(me.elem.find("input[type='number']"));
            me.elem.find('input').on('change', function () {
                me.options.onchange(me);
            });
        },
        _spinner: function (el) {
            el.inputSpinner({
                buttonsClass: "input-group-text",
                decrementButton: '<i class="fa fa-minus"></i>',
                incrementButton: '<i class="fa fa-plus"></i>'
            });
        },
        readonly: function (v) {
            if (v) {
                this._spinner(this.elem.children('input').attr('readonly', true));
            } else {
                this._spinner(this.elem.children('input').removeAttr('readonly'));
            }
        },
        getVal: function () {
            return this.elem.children('input').val();
        },
        getText: function () {
            return this.getVal();
        },
        setVal: function (v) {
            if (v === null) {
                v = undefined;
            }
            this.elem.children('input').val(v);
        }
    };
    $.editors['integer'] = function (elem, opt) {
        return new IntEditor(elem, opt);
    };
    //#endregion
    //#region BoolEditor
    var BoolEditor = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, BoolEditor.defaults, opt);
        me.init();
    };
    BoolEditor.defaults = {
        attrs: {},
        onchange: function () { }
    };
    BoolEditor.prototype = {
        init: function () {
            var me = this,
                id = me.options.field.name + '-' + getId(),
                readonly = me.options.attrs['readonly'] ? 'disabled ' : '';
            if (me.options.allowNull) {
                var html = '<select class="form-control"' + readonly + '>';
                html += '<option value=""> </option>';
                html += '<option value="true">' + '是'.t() + '</option>';
                html += '<option value="false">' + '否'.t() + '</option>';
                html += "</select>"
                me.elem.append(html);
                me.elem.find('select').on('change', function () {
                    me.options.onchange(me);
                });
            } else {
                me.elem.addClass('custom-switch custom-control');
                me.elem.append('<input type="checkbox" ' + readonly + 'class="custom-control-input" id="' + id + '"/><label for="' + id + '" class="custom-control-label mt-1"></label>');
                me.elem.find('input').on('change', function () {
                    me.options.onchange(me);
                });
            }
        },
        readonly: function (v) {
            var me = this;
            if (me.options.allowNull) {
                if (v) {
                    me.elem.children('select').attr('disabled', true);
                } else {
                    me.elem.children('select').removeAttr('disabled');
                }
            } else {
                if (v) {
                    me.elem.children('input').attr('disabled', true);
                } else {
                    me.elem.children('input').removeAttr('disabled');
                }
            }
        },
        getVal: function () {
            var me = this;
            if (me.options.allowNull) {
                var v = me.elem.children('select').val();
                if (v === 'true') {
                    return true;
                } else if (v === 'false') {
                    return false;
                }
                return null;
            }
            return me.elem.children('input').is(":checked");
        },
        getText: function () {
            var me = this;
            if (me.options.allowNull) {
                var v = me.elem.children('select').val();
                if (v === 'true') {
                    return '是'.t();
                } else if (v === 'false') {
                    return '否'.t();
                }
                return '';
            }
            return me.elem.children('input').is(":checked") ? '是'.t() : '否'.t();
        },
        setVal: function (v) {
            var me = this;
            if (me.options.allowNull) {
                var val = (v === null || v === undefined || v === '') ? '' : (v ? 'true' : 'false');
                return me.elem.children('select').val(val);
            } else {
                me.elem.children('input').prop("checked", v === true);
            }
        }
    };
    $.editors['boolean'] = function (elem, opt) {
        return new BoolEditor(elem, opt);
    };
    //#endregion
    //#region TextEditor
    var TextEditor = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, TextEditor.defaults, opt);
        me.init();
    };
    TextEditor.defaults = {
        attrs: {},
        onchange: function () { }
    };
    TextEditor.prototype = {
        init: function () {
            var me = this,
                id = me.options.field.name + '-' + getId(),
                readonly = me.options.attrs['readonly'] ? 'disabled ' : '';
            me.elem.append('<textarea id="' + id + '" ' + readonly + 'rows="3" type="text" class="form-control"/>');
            me.elem.find('textarea').on('change', function () {
                me.options.onchange(me);
            });
        },
        readonly: function (v) {
            if (v) {
                this.elem.children('textarea').attr('disabled', true);
            } else {
                this.elem.children('textarea').removeAttr('disabled');
            }
        },
        getVal: function () {
            return this.elem.children('textarea').val();
        },
        getText: function () {
            return this.getVal();
        },
        setVal: function (v) {
            this.elem.children('textarea').val(v);
        }
    };
    $.editors['text'] = function (elem, opt) {
        return new TextEditor(elem, opt);
    };
    //#endregion
    //#region FloatEditor
    var FloatEditor = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, FloatEditor.defaults, opt);
        me.init();
    };
    FloatEditor.defaults = {
        attrs: {},
        onchange: function () { }
    };
    FloatEditor.prototype = {
        init: function () {
            var me = this,
                id = me.options.field.name + '-' + getId(),
                readonly = me.options.attrs['readonly'] ? 'readonly ' : '';
            me.elem.append('<input ' + readonly + 'type="number" data-decimals="2" step="0.1" id="' + id + '"/>');
            me._spinner(me.elem.find("input[type='number']"));
            me.elem.find('input').on('change', function () {
                me.options.onchange(me);
            });
        },
        _spinner: function (el) {
            el.inputSpinner({
                buttonsClass: "input-group-text",
                decrementButton: '<i class="fa fa-minus"></i>',
                incrementButton: '<i class="fa fa-plus"></i>'
            });
        },
        readonly: function (v) {
            if (v) {
                this._spinner(this.elem.children('input').attr('readonly', true));
            } else {
                this._spinner(this.elem.children('input').removeAttr('readonly'));
            }
        },
        getVal: function () {
            return this.elem.children('input').val();
        },
        getText: function () {
            return this.getVal();
        },
        setVal: function (v) {
            if (v === null) {
                v = undefined;
            }
            this.elem.children('input').val(v);
        }
    };
    $.editors['float'] = function (q, opt) {
        return new FloatEditor(q, opt);
    }
    //#endregion
    //#region DateEditor
    var DateEditor = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, DateEditor.defaults, opt);
        me.init();
    };
    DateEditor.defaults = {
        format: 'YYYY-MM-DD',
        attrs: {},
        onchange: function () { }
    };
    DateEditor.prototype = {
        init: function () {
            var me = this,
                id = me.options.field.name + '-' + getId(),
                html = '<div class="input-group date" id="' + id + '" data-target-input="nearest">\
                            <input type="text" class="form-control datetimepicker-input" data-target="#' + id + '" />\
                            <div class="input-group-append" data-target="#'+ id + '" data-toggle="datetimepicker">\
                                <div class="input-group-text"><i class="fa fa-calendar"></i></div>\
                            </div>\
                        </div>';
            me.elem.append(html);
            me.elem.find('#' + id).datetimepicker({
                format: me.options.format,
                locale: moment.locale('zh-cn')
            });
            me.elem.find('#' + id).on('change.datetimepicker', function () {
                me.options.onchange(me);
            });
            me.readonly(me.options.attrs['readonly']);
            me.id = id;
        },
        readonly: function (v) {
            if (v) {
                this.elem.find('input').attr('readonly', true);
            } else {
                this.elem.find('input').removeAttr('readonly');
            }
        },
        getVal: function () {
            var me = this,
                text = me.elem.find('input').val(),
                date = me.elem.find('#' + me.id).datetimepicker('viewDate').format(me.options.format);
            if (text) {
                return date;
            }
            return '';
        },
        getText: function () {
            return this.getVal();
        },
        setVal: function (v) {
            var me = this;
            if (v === undefined || v === '') {
                me.elem.find('#' + me.id).datetimepicker('clear');
            } else {
                me.elem.find('#' + me.id).datetimepicker('date', v);
            }
        }
    };
    $.editors['date'] = function (elem, opt) {
        return new DateEditor(elem, opt);
    }
    //#endregion
    //#region DateTimeEditor
    var DateTimeEditor = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, DateTimeEditor.defaults, opt);
        me.init();
    };
    DateTimeEditor.defaults = {
        attrs: {},
        format: 'YYYY-MM-DD HH:mm:ss',
        onchange: function () { }
    };
    DateTimeEditor.prototype = {
        init: function () {
            var me = this,
                id = me.options.field.name + '-' + getId(),
                html = '<div class="input-group date" id="' + id + '" data-target-input="nearest">\
                            <input type="text" class="form-control datetimepicker-input" data-target="#' + id + '" />\
                            <div class="input-group-append" data-target="#'+ id + '" data-toggle="datetimepicker">\
                                <div class="input-group-text"><i class="fa fa-calendar"></i></div>\
                            </div>\
                        </div>';
            me.elem.append(html);
            me.elem.find('#' + id).datetimepicker({
                format: me.options.format,
                icons: { time: 'far fa-clock' },
                locale: moment.locale('zh-cn'),
                language: 'zh-cn'
            });
            me.elem.find('#' + id).on('change.datetimepicker', function () {
                me.options.onchange(me);
            });
            me.readonly(me.options.attrs['readonly']);
            me.id = id;
        },
        readonly: function (v) {
            if (v) {
                this.elem.find('input').attr('readonly', true);
            } else {
                this.elem.find('input').removeAttr('readonly');
            }
        },
        getVal: function () {
            var me = this,
                text = me.elem.find('input').val(),
                date = me.elem.find('#' + me.id).datetimepicker('viewDate').format(me.options.format);
            if (text) {
                return date;
            }
            return '';
        },
        getText: function () {
            return this.getVal();
        },
        setVal: function (v) {
            var me = this;
            if (v === undefined || v === '') {
                me.elem.find('#' + me.id).datetimepicker('clear');
            } else {
                me.elem.find('#' + me.id).datetimepicker('date', v);
            }
        }
    };
    $.editors['datetime'] = function (elem, opt) {
        return new DateTimeEditor(elem, opt);
    }
    //#endregion
    //#region HtmlEditor
    var HtmlEditor = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, HtmlEditor.defaults, opt);
        me.init();
    };
    HtmlEditor.defaults = {
        attrs: {},
        onchange: function () { }
    };
    HtmlEditor.prototype = {
        init: function () {
            var me = this,
                readonly = me.options.attrs['readonly'] ? 'readonly ' : '';
            me.elem.append('<input ' + readonly + 'type="text" class="form-control"/>');
            me.elem.find('input').on('change', function () {
                me.options.onchange(me);
            });
        },
        readonly: function (v) {
            if (v) {
                this.elem.children('input').attr('readonly', true);
            } else {
                this.elem.children('input').removeAttr('readonly');
            }
        },
        getVal: function () {
            return this.elem.children('input').val();
        },
        getText: function () {
            return this.getVal();
        },
        setVal: function (v) {
            this.elem.children('input').val(v);
        }
    };
    $.editors['html'] = function (elem, opt) {
        return new HtmlEditor(elem, opt);
    }
    //#endregion
    //#region SelectionEditor
    var SelectionEditor = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, SelectionEditor.defaults, opt);
        me.init();
    };
    SelectionEditor.defaults = {
        attrs: {}
    };
    SelectionEditor.prototype = {
        init: function () {
            var me = this,
                readonly = me.options.attrs['readonly'] ? 'disabled ' : '',
                html = '<select class="form-control"' + readonly + '>';
            if (me.options.allowNull) {
                html += '<option value=""> </option>';
            }
            for (const key in me.options.field.options) {
                html += '<option value="' + key + '">' + me.options.field.options[key] + '</option>';
            }
            html += "</select>"
            me.elem.append(html);
            me.elem.find('select').on('change', function () {
                me.options.onchange(me);
            });
        },
        readonly: function (v) {
            if (v) {
                this.elem.children('select').attr('disabled', true);
            } else {
                this.elem.children('select').removeAttr('disabled');
            }
        },
        getVal: function () {
            return this.elem.children('select').val();
        },
        getText: function () {
            return this.options.field.options[this.getVal()];
        },
        setVal: function (v) {
            this.elem.children('select').val(v);
        }
    };
    $.editors['selection'] = function (q, opt) {
        return new SelectionEditor(q, opt);
    };
    //#endregion
    //#region Many2OneEditor
    var Many2OneEditor = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, Many2OneEditor.defaults, opt);
        me.init();
    };
    Many2OneEditor.defaults = {
        attrs: {},
        limit: 10,
        onchange: function () { }
    };
    Many2OneEditor.prototype = {
        init: function () {
            var me = this,
                html = '<div class="input-group">\
                            <input type="text" class="form-control lookup"/>\
                            <div class="container-fluid dropdown-lookup search-dropdown">\
                                <div class="lookup-body"></div>\
                                <div class="card-footer">\
                                    <div data-btn="clear" class="btn btn-sm btn-default">' + '清空'.t() + '</div>\
                                    <div class="btn-group float-right">\
                                        <div data-btn="prev" class="btn btn-sm btn-default">\
                                            <i class="fa fa-angle-left"></i>\
                                        </div>\
                                        <div data-btn="next" class="btn btn-sm btn-default">\
                                            <i class="fa fa-angle-right"></i>\
                                        </div>\
                                    </div>\
                                </div>\
                            </div>\
                            <div class="input-group-append">\
                                <div data-btn="view" class="btn btn-default">\
                                    <i class="fa fa-external-link-alt"></i>\
                                </div>\
                            </div>\
                        </div>';
            me.offset = 0;
            me.keyword = '';
            me.elem.append(html);
            me.elem.find('[data-btn=clear]').on('click', function (e) {
                me.offset = 0;
                me.keyword = '';
                me.setVal();
            });
            me.elem.find('[data-btn=next]').on('click', function (e) {
                var btn = $(this);
                if (btn.hasClass('disabled')) {
                    return;
                }
                me.offset += me.limit;
                me.lookup();
            });
            me.elem.find('[data-btn=prev]').on('click', function (e) {
                var btn = $(this);
                if (btn.hasClass('disabled')) {
                    return;
                }
                me.offset -= me.limit;
                if (me.offset < 0) {
                    me.offset = 0;
                }
                me.lookup();
            });
            me.elem.find('.lookup').on('click', function (e) {
                if ($(this).attr('readonly')) {
                    return;
                }
                me.showDropdown();
                me.lookup();
                e.preventDefault();
                e.stopPropagation();
            });
            me.elem.find('.dropdown-lookup').on('click', function (e) {
                me.dropclick = true;
            });
            $(document).on('click', function () {
                if (me.dropclick) {
                    me.dropclick = false;
                } else {
                    me.hideDropdown();
                }
            });
            var timer;
            me.elem.find('input').on('change', function () {
                me.options.onchange(me);
            }).keyup(function () {
                var input = $(this);
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
            me.readonly(me.options.attrs['readonly']);
        },
        lookup: function () {
            var me = this,
                el = me.elem.find('input'),
                body = me.elem.find('.lookup-body');
            body.html('<div class="m-2">' + '加载中'.t() + '</div>');
            $.jrpc({
                model: me.options.model,
                method: "lookup",
                args: {
                    field: me.options.field.name,
                    limit: me.options.limit,
                    offset: me.offset,
                    keyword: me.keyword
                },
                onsuccess: function (r) {
                    if (r.data.values[0]) {
                        var html = '<div class="select2-container select2-container--default select2-container--open row">\
                            <ul class="select2-results__options col-12">';
                        $.each(r.data.values, function () {
                            var sel = this[0] === el.attr('data-value') ? ' select2-results__option--highlighted" "aria-selected"="true' : '';
                            html += '<li class="select2-results__option' + sel + '" data-value="' + this[0] + '">' + this[1] + '</li>';
                        });
                        html += '</ul></div>';
                        body.html(html);
                        body.find('.select2-results__option').hover(function () {
                            body.find('.select2-results__option').removeClass('select2-results__option--highlighted').removeAttr('aria-selected');
                            $(this).addClass('select2-results__option--highlighted').attr('aria-selected', 'true');
                        }, function () {
                        }).on('click', function () {
                            var item = $(this), txt = item.html();
                            me.offset = 0;
                            me.keyword = '';
                            el.attr('data-value', item.attr('data-value')).attr('data-text', txt).val(txt);
                            me.hideDropdown();
                        });
                    } else {
                        body.html('<div class="m-2">' + '没有数据'.t() + '</div>');
                    }
                    var nextBtn = me.elem.find('[data-btn=next]');
                    if (r.data.hasNext) {
                        nextBtn.removeClass('disabled');
                    } else {
                        nextBtn.addClass('disabled');
                    }
                }
            });
        },
        showDropdown: function () {
            var me = this, el = me.elem.find('.dropdown-lookup');
            el.show().addClass('show');
            me.open = true;
        },
        hideDropdown: function () {
            var me = this, el = me.elem.find('.dropdown-lookup');
            el.hide().removeClass('show');
            var input = me.elem.find('input');
            if (input.val() != input.attr('data-text')) {
                input.val(input.attr('data-text'));
                me.offset = 0;
                me.keyword = '';
            }
            me.open = false;
        },
        readonly: function (v) {
            if (v) {
                this.elem.find('input').attr('readonly', true);
            } else {
                this.elem.find('input').removeAttr('readonly');
            }
        },
        getVal: function () {
            var me = this, el = me.elem.find('input'), val = el.attr('data-value');
            if (val) {
                return [val, el.attr('data-text')];
            }
            return null;
        },
        getText: function () {
            return this.elem.find('input').val();
        },
        setVal: function (v) {
            var me = this,
                el = me.elem.find('input');
            if (v && v[0]) {
                el.val(v[1]).attr('data-value', v[0]).attr('data-text', v[1]);
            } else {
                el.val('').attr('data-value', '').attr('data-text', '');
            }
        }
    };
    $.editors['many2one'] = function (elem, opt) {
        return new Many2OneEditor(elem, opt);
    }
    //#endregion
    //#region One2ManyEditor
    var One2ManyEditor = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, One2ManyEditor.defaults, opt);
        me.init();
    };
    One2ManyEditor.defaults = {
        attrs: {}
    };
    One2ManyEditor.prototype = {
        init: function () {
            var me = this;
            me.delete = [];
            me.create = [];
            me.update = [];
            var html = '<div role="tbar"></div>';
            html += '</div><div id="m2m_' + me.options.field.name + '">' + '加载中'.t() + '</div>';
            me.elem.append(html);
            me.readonly(me.options.attrs['readonly']);
        },
        _renderGrid: function () {
            var me = this;
            $.jrpc({
                model: 'ir.ui.view',
                method: "loadFields",
                args: {
                    model: me.options.field.comodel
                },
                onsuccess: function (r) {
                    var el = me.elem.children('#m2m_' + me.options.field.name);
                    el.html('');
                    me.fields = r.data.fields;
                    me.grid = el.JGrid({
                        model: me.options.field.comodel,
                        arch: me.options.attrs.arch,
                        fields: me.fields,
                        onselected: function (grid, sel) {
                            var selected = [];
                            $.each(sel, function (i, id) {
                                selected.push(me.data[id]);
                            });
                            me._updateTbar(selected);
                        },
                        saveEdit(grid, id, data, callback) {
                            me._saveEdit(id, data, callback);
                        },
                        loadEdit(grid, id, callback) {
                            if (id && id.startsWith('new')) {
                                for (var i = 0; i < me.data.length; i++) {
                                    var d = me.data[i];
                                    if (d.id === id) {
                                        callback({ data: d });
                                        break;
                                    }
                                }
                            } else {
                                $.jrpc({
                                    model: grid.options.model,
                                    method: "read",
                                    args: {
                                        ids: [id],
                                        fields: grid.editForm.getFields()
                                    },
                                    onsuccess: function (r) {
                                        callback({ data: r.data[0] });
                                    }
                                });
                            }
                        },
                        load: function (grid, data, callback, settings) {
                            if (me.data) {
                                callback({
                                    data: me.data
                                });
                            } else {
                                $.jrpc({
                                    model: me.options.field.comodel,
                                    method: "search",
                                    args: {
                                        criteria: [['id', 'in', me.values || []]],
                                        nextTest: true,
                                        offset: 0,
                                        limit: me.options.field.limit,
                                        fields: grid.getFields(),
                                        order: grid.getSort()
                                    },
                                    onsuccess: function (r) {
                                        me.data = r.data.values;
                                        callback({
                                            data: r.data.values
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            });
        },
        _removeDataById(data, id) {
            for (var i = 0; i < data.length; i++) {
                var d = data[i];
                if (d.id === id) {
                    data.splice(i, 1);
                    break;
                }
            }
        },
        _saveEdit: function (id, data, callback) {
            var me = this;
            if (id) {
                data.id = id;
                for (var i = 0; i < me.data.length; i++) {
                    var d = me.data[i];
                    if (d.id === id) {
                        $.extend(d, data);
                    }
                }
                if (id.startsWith('new')) {
                    me._removeDataById(me.create, id);
                    me.create.push(data);
                } else {
                    me._removeDataById(me.update, id);
                    me.update.push(data);
                }
            } else {
                data.id = 'new-' + getId();
                me.create.push(data);
                me.data.push(data);
            }
            callback(true);
        },
        _addValue: function (edit) {
            var me = this;
            me.grid.addData();
        },
        _editValue: function (edit) {
            var me = this;
            var id = me.grid.getSelected()[0];
            me.grid.editData(id);
        },
        _removeValue: function (me) {
            var me = this;
            var id = me.grid.getSelected()[0];
            if (!id.startsWith('new')) {
                me.delete.push(id);
            }
            for (var i = 0; i < me.data.length; i++) {
                var d = me.data[i];
                if (d === id) {
                    me.data.splice(i, 1);
                    break;
                }
            }
            me.grid.table.draw();
        },
        _updateTbar: function (sel) {
            var btn = this.elem.find('[name=remove],[name=edit]');
            if (sel && sel.length === 1) {
                btn.removeClass('disabled');
            } else {
                btn.addClass('disabled');
            }
        },
        readonly: function (v) {
            var me = this, tbar = me.elem.find('[role=tbar]');
            if (v) {
                tbar.html('');
            } else {
                //todo toobar
                var html = '<button name="add" type="button" class="btn btn-success mr-1">' + '添加'.t()
                    + '</button><button name="edit" type="button" class="btn btn-info mr-1 disabled">' + '编辑'.t() + '</button>'
                    + '</button><button name="remove" type="button" class="btn btn-danger disabled">' + '删除'.t() + '</button>';
                tbar.append(html);
                tbar.find('[name=add]').on('click', function () {
                    if (!$(this).hasClass('disabled')) {
                        me._addValue(me);
                    }
                });
                tbar.find('[name=edit]').on('click', function () {
                    if (!$(this).hasClass('disabled')) {
                        me._editValue(me);
                    }
                });
                tbar.find('[name=remove]').on('click', function () {
                    if (!$(this).hasClass('disabled')) {
                        me._removeValue(me);
                    }
                });
            }
        },
        getVal: function () {
            var me = this, v = [];
            for (var i = 0; i < me.create.length; i++) {
                var values = {};
                $.extend(values, me.create[i]);
                delete values.id;
                v.push([0, 0, values]);
            }
            for (var i = 0; i < me.update.length; i++) {
                var values = {};
                $.extend(values, me.update[i]);
                var id = values.id;
                delete values.id;
                v.push([1, id, values]);
            }
            for (var i = 0; i < me.delete.length; i++) {
                v.push([2, me.delete[i], 0]);
            }
            return v;
        },
        getText: function () {
            return '';
        },
        setVal: function (v) {
            var me = this;
            me.values = v || [];
            delete me.data;
            me.delete = [];
            me.create = [];
            me.update = [];
            me._renderGrid();
        }
    };
    $.editors['one2many'] = function (elem, opt) {
        return new One2ManyEditor(elem, opt);
    }
    //#endregion
    //#region Many2ManyEditor
    var Many2ManyEditor = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, Many2ManyEditor.defaults, opt);
        me.init();
    };
    Many2ManyEditor.defaults = {
        attrs: {}
    };
    Many2ManyEditor.prototype = {
        init: function () {
            var me = this;
            me.delete = [];
            me.create = [];
            var html = '<div role="tbar"></div>';
            html += '</div><div id="m2m_' + me.options.field.name + '">' + '加载中'.t() + '</div>';
            me.elem.append(html);
            me.readonly(me.options.attrs['readonly']);
        },
        _renderGrid: function () {
            var me = this;
            $.jrpc({
                model: 'ir.ui.view',
                method: "loadFields",
                args: {
                    model: me.options.field.comodel
                },
                onsuccess: function (r) {
                    var el = me.elem.children('#m2m_' + me.options.field.name);
                    el.html('');
                    me.fields = r.data.fields;
                    me.grid = el.JGrid({
                        model: me.options.field.comodel,
                        arch: me.options.attrs.arch,
                        fields: me.fields,
                        onselected: function (grid, sel) {
                            var selected = [];
                            $.each(sel, function (i, id) {
                                selected.push(me.data[id]);
                            });
                            me._updateTbar(selected);
                        },
                        load: function (grid, data, callback, settings) {
                            if (me.data) {
                                callback({
                                    data: me.data
                                });
                            } else {
                                $.jrpc({
                                    model: me.options.field.comodel,
                                    method: "search",
                                    args: {
                                        criteria: [['id', 'in', me.values || []]],
                                        nextTest: true,
                                        offset: 0,
                                        limit: me.options.field.limit,
                                        fields: grid.getFields(),
                                        order: grid.getSort()
                                    },
                                    onsuccess: function (r) {
                                        me.data = r.data.values;
                                        callback({
                                            data: r.data.values
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            });
        },
        _addValue: function (edit) {
            var me = this, modal = $("#modal-m2m"), dialog = {};
            if (modal.length > 0) {
                modal.remove();
            }
            var html = '<div class="modal fade" id="modal-m2m">\
                        <div class="modal-dialog modal-xl">\
                            <div class="modal-content">\
                            <div class="modal-header">\
                                <h4 class="modal-title">'+ '选择'.t() + '<span class="comodel-name"></span></h4>\
                                <button type="button" class="close" data-dismiss="modal" aria-label="Close">\
                                <span aria-hidden="true">&times;</span>\
                                </button>\
                            </div>\
                            <div class="modal-body">\
                                <div class="m2m-pager"></div>\
                                <div class="m2m-cogrid"></div>\
                            </div>\
                            <div class="modal-footer justify-content-between">\
                                <button type="button" class="btn btn-default" data-dismiss="modal">'+ '关闭'.t() + '</button>\
                                <button type="button" role="btn-save" class="btn btn-primary">'+ '确定'.t() + '</button>\
                            </div>\
                            </div>\
                        </div>\
                    </div>';
            $(document.body).append(html);
            modal = $("#modal-m2m");
            modal.find('[role=btn-save]').on('click', function () {
                var selected = cogrid.getSelected();
                for (var i = 0; i < selected.length; i++) {
                    var id = selected[i], row = cogrid.data[id];
                    if (me.delete.indexOf(id) > -1) {
                        me.delete.remove(id);
                    } else {
                        me.create.push(id);
                    }
                    me.data.push(row);
                }
                me.grid.table.draw();
                modal.modal('hide');
            });
            modal.modal({ backdrop: false });
            modal.find('.m2m-pager').empty();
            modal.find('.m2m-cogrid').empty();
            var pager = modal.find('.m2m-pager').JPager({
                onchange: function (p) {
                    cogrid.load();
                },
                oncount: function (p) {
                    $.jrpc({
                        model: me.options.model,
                        method: "count",
                        args: {
                            criteria: [] //TODO查询功能
                        },
                        onsuccess: function (r) {
                            pager.update({
                                total: r.data
                            });
                        }
                    });
                }
            });
            var cogrid = modal.find('.m2m-cogrid').JGrid({
                model: me.options.field.comodel,
                arch: me.options.attrs.arch,
                fields: me.fields,
                onselected: function (grid, sel) {
                },
                load: function (grid, data, callback, settings) {
                    $.jrpc({
                        model: me.options.field.comodel,
                        method: "search",
                        args: {
                            criteria: [['id', 'not in', me.values]],
                            nextTest: true,
                            offset: pager.getOffest(),
                            limit: pager.getLimit(),
                            fields: grid.getFields(),
                            order: grid.getSort()
                        },
                        context: { active_test: true },
                        onsuccess: function (r) {
                            if (r.data.values.length > 0) {
                                var len = pager.getOffest() + r.data.values.length;
                                if (r.data.hasNext === false) {
                                    pager.update({
                                        to: len,
                                        next: false,
                                        total: len
                                    });
                                } else {
                                    pager.update({
                                        to: len,
                                        next: true
                                    });
                                }
                            } else {
                                pager.hide();
                            }
                            callback({
                                data: r.data.values
                            });
                            cogrid.data = {};
                            $.each(r.data.values, function (i, v) {
                                cogrid.data[v['id']] = v;
                            });
                        }
                    });
                }
            });
        },
        _removeValue: function (me) {
            var id = me.grid.table.row().id();
            if (me.create.indexOf(id) > -1) {
                me.create.remove(id);
            } else {
                me.delete.push(id);
            }
            for (var i = 0; i < me.data.length; i++) {
                if (me.data[i].id === id) {
                    me.data.splice(i, 1);
                    break;
                }
            }
            me.grid.table.draw();
        },
        _updateTbar: function (sel) {
            var btn = this.elem.find('[name=remove]');
            if (sel && sel.length === 1) {
                btn.removeClass('disabled');
            } else {
                btn.addClass('disabled');
            }
        },
        readonly: function (v) {
            var me = this, tbar = me.elem.find('[role=tbar]');
            if (v) {
                tbar.html('');
            } else {
                var html = '<button name="add" type="button" class="btn btn-success mr-1">' + '添加'.t()
                    + '</button><button name="remove" type="button" class="btn btn-danger disabled">' + '删除'.t() + '</button>';
                tbar.append(html);
                tbar.find('[name=add]').on('click', function () {
                    if (!$(this).hasClass('disabled')) {
                        me._addValue(me);
                    }
                });
                tbar.find('[name=remove]').on('click', function () {
                    if (!$(this).hasClass('disabled')) {
                        me._removeValue(me);
                    }
                });
            }
        },
        getVal: function () {
            var me = this, v = [];
            for (var i = 0; i < me.create.length; i++) {
                v.push([4, me.create[i], 0]);
            }
            for (var i = 0; i < me.delete.length; i++) {
                v.push([3, me.delete[i], 0]);
            }
            return v;
        },
        getText: function () {
            return '';
        },
        setVal: function (v) {
            var me = this;
            me.values = v || [];
            delete me.data;
            me.delete = [];
            me.create = [];
            me._renderGrid();
        }
    };
    $.editors['many2many'] = function (elem, opt) {
        return new Many2ManyEditor(elem, opt);
    }
    //#endregion
    //#endregion

    //#region search
    var JSearch = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, JSearch.defaults, opt);
        me.init();
    }
    JSearch.defaults = {
        submit: function (search) {
        },
        tpl: '<div class="input-group">\
                <div class="input-group-prepend">\
                    <button type="button" class="btn btn-default dropdown-toggle" data-btn="dropdown">' + '过滤'.t() + '</button>\
                    <div class="container-fluid dropdown-menu search-dropdown" style="min-width:300px">\
                        <div class="search-form"></div>\
                        <div class="card-footer">\
                            <button data-btn="confirm" class="btn btn-primary" style="min-width:100px">' + '确定'.t() + '</button>\
                            <button data-btn="reset" class="btn btn-outline-secondary float-right" style="margin-right:5px">' + '重置'.t() + '</button>\
                            <button data-btn="clear" class="btn btn-outline-secondary float-right" style="margin-right:5px">' + '清空'.t() + '</button>\
                        </div>\
                    </div>\
                </div>\
                <div class="jsearch-container">\
                    <span class="jsearch-selection">\
                        <ul class="jsearch-selection-body"></ul>\
                    </span>\
                </div>\
                <div class="input-group-append">\
                    <button data-btn="submit" type="submit" class="btn btn-default">\
                        <i class="fa fa-search"></i>\
                    </button>\
                </div>\
            </div>'
    }
    JSearch.prototype = {
        init: function () {
            var me = this;
            me.query = {};
            me.editors = {};
            me.fields = [];
            me.elem.append(me.options.tpl);
            me.dropdown = me.elem.find('.search-dropdown');
            me.body = me.elem.find('.jsearch-selection-body');
            me.elem.find('.jsearch-selection').on('click', function (e) {
                me.showDropdown();
                e.preventDefault();
                e.stopPropagation();
            });
            me.elem.find('[data-btn=clear]').on('click', function () {
                $.each(me.fields, function (i, field) {
                    me.editors[field].setVal('');
                });
            });
            me.elem.find('[data-btn=confirm]').on('click', function () {
                me.query = {};
                me.body.empty();
                $.each(me.fields, function (i, field) {
                    var editor = me.editors[field];
                    var val = editor.getVal();
                    if (val != null && val != undefined && val !== '') {
                        var name = editor.options.label + ' = ' + editor.getText();
                        me.add(field, name, [field, '=', val]);
                    }
                });
                me.dropdown.removeClass('show');
                me.options.submit(me);
                me.hideDropdown();
            });
            me.elem.find('[data-btn=submit]').on('click', function () {
                me.options.submit(me);
            });
            me.elem.find('[data-btn=dropdown]').on('click', function (e) {
                me.showDropdown();
                e.preventDefault();
                e.stopPropagation();
            });
            me.elem.find('.dropdown-menu').on('click', function (e) {
                me.dropclick = true;
            });
            $(document).on('click', function () {
                if (me.dropclick) {
                    me.dropclick = false;
                } else {
                    me.hideDropdown();
                }
            });
            me.initDropdown();
        },
        showDropdown: function () {
            var me = this, el = me.elem.find('.dropdown-menu');
            el.show().addClass('show');
        },
        hideDropdown: function () {
            var me = this, el = me.elem.find('.dropdown-menu');
            el.hide().removeClass('show');
        },
        initDropdown: function () {
            var me = this, form = '<div class="row">', fieldAttrs = {};
            if (me.options.arch) {
                var arch = $.jutil.parseXML(me.options.arch).children('search');
                var fields = arch.children('field');
                var col = arch.col || (fields.length <= 6 ? 1 : fields.length <= 12 ? 2 : 3);
                me.dropdown.addClass('col-md-' + (col * 4));
                var forms = [];
                for (var i = 0; i < col; i++) {
                    forms[i] = '<div class="col-md-' + (12 / col) + ' form-horizontal" style="min-width: 300px;"><div class="card-body">';
                }
                fields.each(function (i, e) {
                    var el = $(e),
                        name = el.attr('name'),
                        attrs = {},
                        label = el.attr('label'),
                        field = me.options.fields[name];
                    if (!field) {
                        console.log(name);
                        debugger;
                    }
                    if (!label) {
                        label = field.label || field.name;
                    }
                    label = label.t();
                    $.each(this.attributes, function (i, attr) {
                        attrs[attr.name] = attr.value;
                    });
                    fieldAttrs[name] = attrs;
                    me.fields.push(name);
                    var html = '<div class="form-group"><label>' + label + '</label>'
                        + '<div data-label="' + label + '" data-field="' + name + '"></div></div>';
                    forms[i % col] += html;
                });
                for (var i = 0; i < col; i++) {
                    form += forms[i] + '</div></div>';
                }
            }
            form += '</div>';
            me.dropdown.prepend(form);
            me.dropdown.find('[data-field]').each(function () {
                var e = $(this),
                    fname = e.attr('data-field'),
                    field = me.options.fields[fname],
                    editor = field.editor || field.type;
                me.editors[fname] = $.editors[editor](e, {
                    field: field,
                    model: me.options.model,
                    attrs: fieldAttrs[fname],
                    allowNull: true,
                    label: e.attr('data-label'),
                    onchange: function (e) {
                        me.dirty = true;
                    }
                });
            });
        },
        add: function (field, name, expr) {
            var me = this;
            me.query[field] = expr;
            me.body.append('<li class="jsearch-choice" data-field=' + field + '>' + name + '<span class="jsearch-choice-remove" role="presentation">×</span></li>');
            var el = me.body.find('[data-field=' + field + ']');
            el.on('click', function (e) { e.stopPropagation(); });
            el.find('.jsearch-choice-remove').on('click', function (e) {
                me.remove(field);
                me.options.submit(me);
                e.stopPropagation();
            });
        },
        remove: function (field) {
            var me = this;
            delete me.query[field];
            me.editors[field].setVal('');
            me.body.find('[data-field=' + field + ']').remove();
        },
        getCriteria: function () {
            var me = this;
            return Object.values(me.query);
        }
    }
    $.fn.jSearch = JSearch;
    $.fn.JSearch = function (opt) {
        return new JSearch(this, opt);
    }
    //#endregion

    //#region pager
    var JPager = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, JPager.defaults, opt);
        me.init();
    }
    JPager.defaults = {
        pageSize: 10,
        onchange: function (pager) { },
        oncount: function (pager) { },
        tpl: '<div class="btn-group">\
                <div class="btn pager-from input-group input-group-sm" style="padding: 0;"><span>1</span></div>\
                <span style="min-width:.8rem;text-align: center;">-</span>\
                <div class="btn pager-to input-group input-group-sm" style="padding: 0;"></div>\
                <span style="min-width:1rem;text-align: center;">/</span>\
                <div class="btn pager-total" style="padding: 0;"><span>?</span></div>\
            </div>\
            <div class="btn-group ml-2">\
                <button type="button" class="btn btn-sm btn-default pager-prev">\
                    <i class="fa fa-angle-left"></i>\
                </button>\
                <button type="button" class="btn btn-sm btn-default pager-next">\
                    <i class="fa fa-angle-right"></i>\
                </button>\
            </div>'
    }
    JPager.prototype = {
        init: function () {
            var me = this;
            me.limit = me.options.pageSize;
            me.from = 1;
            me.to = me.limit;
            me.elem.append(me.options.tpl);
            me.elem.find('div.pager-from').on('click', function (e) {
                var el = $(this);
                if (!el.hasClass('edit')) {
                    el.html('<input type="text" class="form-control" style="width:3rem;">');
                    var input = el.find('input');
                    input.val(me.from);
                    input.focus();
                    input.on('blur', function () {
                        var val = parseInt(input.val());
                        if (!isNaN(val) && val > 0) {
                            me.from = val;
                            el.html('<span>' + val + '</span>');
                            if (me.from > me.to) {
                                me.to = me.from;
                                me.elem.find('div.pager-to').html('<span>' + me.to + '</span>');
                            }
                            me.limit = me.to - me.from + 1;
                            me.options.onchange(me);
                        } else {
                            el.html('<span>' + me.from + '</span>');
                        }
                        el.removeClass('edit');
                    });
                    el.addClass('edit');
                }
            });
            me.elem.find('div.pager-to').on('click', function (e) {
                var el = $(this);
                if (!el.hasClass('edit')) {
                    el.html('<input type="text" class="form-control" style="width:3rem;">');
                    var input = el.find('input');
                    input.val(me.to);
                    input.focus();
                    input.on('blur', function () {
                        var val = parseInt(input.val());
                        if (!isNaN(val) && val > 0) {
                            me.to = val;
                            el.html('<span>' + val + '</span>');
                            if (me.to < me.from) {
                                me.from = me.to;
                                me.elem.find('div.pager-from').html('<span>' + me.from + '</span>');
                            }
                            me.limit = me.to - me.from + 1;
                            me.options.onchange(me);
                        } else {
                            el.html('<span>' + me.to + '</span>');
                        }
                        el.removeClass('edit');
                    });
                    el.addClass('edit');
                }
            });
            me.elem.find('div.pager-total').on('click', function (e) {
                me.options.oncount(me);
            });
            me.elem.find('button.pager-prev').on('click', function (e) {
                if ($(this).hasClass('disabled')) return;
                if (me.from > me.limit) {
                    me.from -= me.limit;
                } else {
                    me.from = 1;
                }
                me.to = me.from + me.limit - 1;
                me.options.onchange(me);
            });
            me.elem.find('button.pager-next').on('click', function (e) {
                if ($(this).hasClass('disabled')) return;
                me.from += me.limit;
                me.to = me.from + me.limit - 1;
                me.options.onchange(me);
            });
        },
        getLimit: function () {
            return this.limit;
        },
        getOffest: function () {
            return this.from - 1;
        },
        update: function (e) {
            var me = this;
            this.elem.show();
            if (e.from) {
                me.from = e.from;
            }
            if (e.to) {
                me.to = e.to;
            }
            me.elem.find('div.pager-from').html('<span>' + me.from + '</span>');
            me.elem.find('div.pager-to').html('<span>' + me.to + '</span>');
            if (e.next === true) {
                me.elem.find('button.pager-next').removeClass('disabled');
            } else if (e.next === false) {
                me.elem.find('button.pager-next').addClass('disabled');
            }
            if (me.from === 1) {
                me.elem.find('button.pager-prev').addClass('disabled');
            } else {
                me.elem.find('button.pager-prev').removeClass('disabled');
            }
            var t = parseInt(e.total);
            if (!isNaN(t)) {
                me.elem.find('div.pager-total').html('<span>' + t + '</span>');
            }
        },
        reset: function () {
            var me = this;
            me.from = 1;
            me.to = me.limit;
            me.elem.find('div.pager-from').html('<span>' + me.from + '</span>');
            me.elem.find('div.pager-to').html('<span>' + me.to + '</span>');
            me.elem.find('div.pager-total').html('<span>?</span>');
            me.elem.find('button.pager-next').removeClass('disabled');
        },
        hide: function () {
            this.elem.hide();
        }
    }
    $.fn.jPager = JPager;
    $.fn.JPager = function (opt) {
        return new JPager(this, opt);
    }
    //#endregion

    //#region toolbar    
    var JToolbar = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, JToolbar.defaults, opt);
        me.init();
    }
    JToolbar.defaults = {
        postSvc: function (svc) { console.log(svc) },
        defaultButtons: 'create|copy|edit|delete',
        buttons: {
            'create': '<button name="create" class="btn-success" click="page.create()">' + '创建'.t() + '</button>',
            'copy': '<button name="copy" ref="create" auth="create" active="id" class="btn-success" click="page.copy()">' + '复制'.t() + '</button>',
            'edit': '<button name="edit" auth="update" class="btn-info" active="id" click="page.edit()">' + '编辑'.t() + '</button>',
            'delete': '<button name="delete" ref="edit" active="ids" class="btn-danger" click="page.delete()" confirm="' + '确定删除?'.t() + '">' + '删除'.t() + '</button>',
            'save': '<button name="save" auth="create|update" class="btn-info" click="page.save()">' + '保存'.t() + '</button>',
            'saveAndNew': '',
            'import': '',
            'export': ''
        }
    }
    JToolbar.prototype = {
        init: function () {
            var me = this, tbar = $.jutil.parseXML(me.options.arch).find('toolbar');
            me.elem.empty();
            if (tbar.length == 0) return;
            tbar.prepend(me._getDefaultButtons(tbar.attr('buttons')));
            tbar.find('button').each(function () {
                var btn = $(this),
                    name = btn.attr('name') || btn.attr('service'),
                    auth = btn.attr('auth') || name || '',
                    cls = btn.attr('class'),
                    allow = me.options.auths === "@all";
                if (!allow) {
                    $.each(auth.split('|'), function () {
                        if (me.options.auths.indexOf(this) > -1) {
                            allow = true;
                            return true;
                        }
                    });
                }
                if (allow) {
                    if (!btn.hasClass('btn')) {
                        btn.addClass('btn');
                    }
                    if (!cls || cls.indexOf('btn-') == -1) {
                        btn.addClass('btn-info');
                    }
                    var label = btn.attr('label') || '';
                    btn.append(label.t());
                    btn.attr('name', 'btn_' + name);
                    if (!btn.attr('type')) {
                        btn.attr('type', 'button');
                    }
                    btn.replaceWith('<div name="btn_' + name + '_group" class="btn-group mr-1">' + btn.prop("outerHTML") + '</div>');
                } else {
                    btn.replaceWith('');
                }
            });
            tbar.find('button[ref]').each(function () {
                var btn = $(this);
                var ref = btn.attr('ref');
                var group = tbar.find('div[name=btn_' + ref + '_group]');
                if (group.length > 0) {
                    btn.attr('class', 'dropdown-item');
                    var drop = group.find('dropdown-menu');
                    if (drop.length > 0) {
                        drop.append(btn.prop("outerHTML"));
                    } else {
                        var cls = group.find('button[name=btn_' + ref + ']').attr('class');
                        cls = cls.replace('disabled', '');
                        group.append('<button type="button" class="' + cls + ' dropdown-toggle dropdown-icon" data-toggle="dropdown"> </button>')
                        group.append('<div class="dropdown-menu" role="menu">' + btn.prop("outerHTML") + '</div>');
                    }
                    tbar.find('div[name=' + btn.attr('name') + '_group]').remove();
                }
            });
            me.elem.append(tbar.html());
            me.elem.find('button[service]').on('click', function () {
                var btn = $(this);
                if (btn.hasClass('disabled')) return;
                var cfm = btn.attr('confirm');
                if (!cfm || confirm(cfm)) {
                    page.postSvc(btn.attr('service'));
                }
            });
            me.elem.find('button[click]').on('click', function () {
                var btn = $(this);
                if (btn.hasClass('disabled')) return;
                var click = btn.attr('click');
                var cfm = btn.attr('confirm');
                if (!cfm || confirm(cfm)) {
                    eval(click);
                }
            });
            me.update([]);
        },
        update: function (data) {
            this.elem.find('button[active]').each(function () {
                var btn = $(this);
                if (btn.attr('active') === 'id') {
                    if (data.length != 1) {
                        btn.addClass('disabled');
                    } else {
                        btn.removeClass('disabled');
                    }
                } else if (btn.attr('active') === 'ids') {
                    if (data.length > 0) {
                        btn.removeClass('disabled');
                    } else {
                        btn.addClass('disabled');
                    }
                } else {
                    //todo
                }
            });
        },
        _getDefaultButtons: function (btns) {
            var me = this, html = '';
            if (btns === 'default') {
                btns = me.options.defaultButtons;
            }
            $.each(btns.split('|'), function (i, e) {
                var btn = me.options.buttons[e];
                if (btn) {
                    html += btn;
                }
            });
            return html;
        }
    }
    $.fn.jToolbar = JToolbar;
    $.fn.JToolbar = function (opt) {
        return new JToolbar(this, opt);
    }
    //#endregion

    //#region grid       
    var JGrid = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, JGrid.defaults, opt);
        me.init();
    }
    JGrid.defaults = {
        load: function (grid, data, callback, settings) {
            callback({ data: [] });
        },
        onselected: function (grid, data) { },
        saveEdit: function (grid, id, data, callback) { },
        loadEdit: function (grid, id, callback) {
            $.jrpc({
                model: grid.options.model,
                method: "read",
                args: {
                    ids: [id],
                    fields: grid.editForm.getFields()
                },
                onsuccess: function (r) {
                    callback({ data: r.data[0] });
                }
            });
        }
    }
    JGrid.prototype = {
        init: function () {
            var me = this, columnDefs = [], columnIndex = 0;
            me.fields = [];
            if (me.options.arch) {
                var arch = $.jutil.parseXML(me.options.arch), grid = arch.children('grid');
                if (grid.length > 0) {
                    me.editArch = grid.children('edit').prop('innerHTML');
                    var css = me.options.className;
                    var html = '<table class="table table-bordered table-hover ' + css + '"><thead><tr>';
                    grid.children('field').each(function () {
                        var el = $(this),
                            name = el.attr('name'),
                            label = el.attr('label'),
                            field = me.options.fields[name];
                        if (!field) {
                            console.log(name);
                            debugger;
                        }
                        if (!label) {
                            label = field.label || field.name;
                        }
                        label = label.t();
                        me.fields.push(name);
                        var render = field.type === 'many2one' ? ' data-render="[1]"' : '';
                        html += '<th data-data="' + name + '"' + render + '>' + label + '</th >';
                        columnDefs.push({
                            render: ($.columns[field.type] || $.columns['char'])({ field: field }).render(),
                            targets: columnIndex++
                        });
                    });
                    html += '</tr></thead></table>';
                    grid.replaceWith(html);
                } else {
                    var table = arch.children('table');
                    table.find('th').each(function () {
                        var el = $(this),
                            name = el.attr('data-data');
                        me.fields.push(name);
                    });
                }
                me.elem.append(arch.children().prop('outerHTML'));
            }
            me.table = me.elem.find('table').DataTable({
                paging: false,
                lengthChange: false,
                searching: false,
                ordering: true,
                info: false,
                autoWidth: false,
                responsive: true,
                processing: true,
                serverSide: true,
                rowId: 'id',
                language: {
                    processing: "加载中".t(),
                    zeroRecords: "没有数据".t()
                },
                ajax: function (data, callback, settings) {
                    me.sel = [];
                    me.options.onselected(me, []);
                    if (me.redraw && me.data) {
                        callback(me.data);
                    } else {
                        me.options.load(me, data, function (d) {
                            me.data = d;
                            callback(d);
                        }, settings);
                    }
                },
                columnDefs: columnDefs
            });
            me.sel = [];
            me.table.on('click', 'tbody tr', function () {
                var row = $(this);
                if (row.hasClass('edit') || row.children('.dataTables_empty').length === 1) {
                    return;
                }
                var id = me.table.row(this).id();
                if (window.event.ctrlKey) {
                    if (row.hasClass('selected')) {
                        row.removeClass('selected');
                        me.sel.remove(id);
                    } else {
                        row.addClass('selected');
                        me.sel.push(id);
                    }
                    me.options.onselected(me, me.sel);
                } else {
                    me.table.$('tr.selected').removeClass('selected');
                    row.addClass('selected');
                    me.sel = [id];
                    me.options.onselected(me, me.sel);
                }
            });
        },
        _redraw: function () {
            var me = this;
            me.redraw = true;
            me.table.draw();
            me.redraw = false;
            me.elem.find('tr.edit').removeClass('edit');
        },
        addData: function () {
            var me = this;
            me._redraw();
            me.elem.find('table tbody').prepend('<tr id="addNew"></tr>');
            var row = me.elem.find('#addNew');
            me._renderEdit(row);
            me.editForm = row.find('.grid-edit').JForm({
                arch: '<form log_access="0">' + me.editArch + '</form>',
                fields: me.options.fields,
                model: me.options.model
            });
            var values = {};
            $.each(me.editForm.getFields(), function () {
                var field = me.options.fields[this];
                values[this] = field.defaultValue;
            });
            me.editForm.setData(values);
        },
        _renderEdit: function (row, id) {
            var me = this,
                html = '<td colspan="500"><div class="grid-edit"></div>\
                            <div class="grid-edit-tbar">\
                                <button name="confirm" class="btn btn-primary" style="min-width:100px">' + '确定'.t() + '</button>\
                                <button name="cancel" class="btn btn-outline-secondary float-right">' + '取消'.t() + '</button>\
                            </div>\
                        </td>';
            row.addClass('edit').html(html);
            row.find('[name=cancel]').on('click', function () {
                me._redraw();
            });
            row.find('[name=confirm]').on('click', function () {
                var btn = $(this);
                if (btn.hasClass('disabled')) {
                    return;
                }
                btn.addClass('disabeld');
                me.options.saveEdit(me, id, me.editForm.getData(), function (success) {
                    if (success) {
                        me.load();
                    } else {
                        btn.removeClass('disabeld');
                    }
                });
            });
        },
        editData: function (id) {
            var me = this;
            id = id || me.sel[0];
            if (id) {
                me._redraw();
                var row = me.elem.find('#' + id);
                me._renderEdit(row, id);
                me.editForm = row.find('.grid-edit').JForm({
                    arch: '<form log_access="0">' + me.editArch + '</form>',
                    fields: me.options.fields,
                    model: me.options.model,
                    load: function (callback) {
                        me.options.loadEdit(me, id, callback);
                    },
                });
                me.editForm.load();
            }
        },
        getTable: function () {
            return this.table;
        },
        getFields: function () {
            return this.fields;
        },
        getSelected: function () {
            return this.sel;
        },
        getSort: function () {
            var me = this, order = '';
            if (me.table) {
                $.each(me.table.order(), function (i, o) {
                    if (order != '') {
                        order += ',';
                    }
                    order += me.table.column(o[0]).dataSrc() + ' ' + o[1];
                });
            }
            return order;
        },
        load: function () {
            this.table.ajax.reload();
        }
    }
    $.fn.jGrid = JGrid;
    $.fn.JGrid = function (opt) {
        return new JGrid(this, opt);
    }
    //#endregion

    //#region form
    var JForm = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, JForm.defaults, opt);
        me.init();
    };
    JForm.defaults = {
        load: function (callback) { }
    };
    JForm.prototype = {
        init: function () {
            var me = this;
            me.fields = [];
            me.sel = [];
            me.editors = {};
            if (me.options.arch) {
                var arch = $.jutil.parseXML(me.options.arch);
                arch.find('toolbar').remove();
                var form = arch.find('form'), fieldAttrs = {};
                if (form.length > 0) {
                    form.addClass('card-body row');
                    form.find('tabs').each(function () {
                        var tabs = $(this), nav = '', content = '';
                        tabs.children('tab').each(function (i) {
                            var tab = $(this), label = tab.attr('label'), id = 'tab-' + getId(),
                                active = i === 0 ? ' active' : '', show = i === 0 ? ' show' : '';
                            if (label) {
                                label = label.t();
                            }
                            nav += '<li class="nav-item">\
                                        <a class="nav-link' + active + '" id="nav-' + id + '" data-toggle="pill" href="#' + id +
                                '" role="tab" aria-controls="' + id + '" aria-selected="true">' + label + '</a>\
                                    </li>';

                            content += '<div class="tab-pane fade' + show + active + '" id="' + id + '" role="tabpanel" aria-labelledby="' + id + '-tab"><div class="row mt-3">'
                                + tab.prop('innerHTML') + '</div></div>';
                        });
                        var html = '<ul class="nav nav-tabs col-12" role="tablist">'
                            + nav + '</ul><div class="tab-content col-12">'
                            + content + '</div>';
                        tabs.replaceWith(html);
                    });

                    form.find('field').each(function () {
                        var el = $(this);
                        if (el.parents('field').length > 0) {
                            return;
                        }
                        var name = el.attr('name'),
                            attrs = {}, label = '',
                            nolabel = el.attr('nolabel'),
                            field = me.options.fields[name];
                        if (!field) {
                            console.log(name);
                            debugger;
                        }
                        if (nolabel != undefined) {
                            nolabel = eval(nolabel);
                        }
                        if (!nolabel) {
                            label = el.attr('label');
                            if (!label) {
                                label = field.label || field.name;
                            }
                            label = label.t();
                        }
                        $.each(this.attributes, function (i, attr) {
                            attrs[attr.name] = attr.value;
                        });
                        me.fields.push(name);
                        fieldAttrs[name] = attrs;
                        var colspan = Math.min(el.attr('colspan') || 1, 3);
                        var colcss = 'col-' + (colspan * 4);
                        if (field.type === 'many2many' || field.type === 'one2many') {
                            attrs.arch = el.html();
                        }
                        var html = '<div class="form-group ' + colcss + '">';
                        if (!nolabel) {
                            html += '<label>' + label + ' </label>';
                        }
                        html += '<div data-field="' + name + '"> </div></div>';
                        el.replaceWith(html);
                    });

                    var html = arch.children().prop('outerHTML');
                    var logAccess = form.attr('log_access');
                    if (logAccess == undefined) {
                        logAccess = "1";
                    }
                    logAccess = eval(logAccess);
                    if (logAccess) {
                        html = '<div class="col-md-9">' + html + '</div>';
                    } else {
                        html = '<div class="col-md-12">' + html + '</div>';
                    }
                    me.elem.append(html);
                    if (logAccess && me.options.fields['create_uid']) {
                        var html = '<div class="col-md-3">\
                                <div class="card card-info mt-3">\
                                    <div class="card-header">\
                                        <h3 class="card-title">基本信息</h3>\
                                    </div>\
                                    <div class="card-body">\
                                        <div class="mb-3">\
                                            <h6>创建时间:</h6>\
                                            <span readonly="true" data-field="create_date"></span>\
                                        </div>\
                                        <div class="mb-3">\
                                            <h6>创建人:</h6>\
                                            <span readonly="true" data-field="create_uid"></span>\
                                        </div>\
                                        <div class="mb-3">\
                                            <h6>修改时间:</h6>\
                                            <span readonly="true" data-field="update_date"></span>\
                                        </div>\
                                        <div class="mb-3">\
                                            <h6>修改人:</h6>\
                                            <span readonly="true" data-field="update_uid"></span>\
                                        </div>\
                                    </div>\
                                </div>\
                            </div>';
                        fieldAttrs['create_date'] = { readonly: true };
                        fieldAttrs['create_uid'] = { readonly: true };
                        fieldAttrs['update_date'] = { readonly: true };
                        fieldAttrs['update_uid'] = { readonly: true };
                        me.fields.push('create_date');
                        me.fields.push('create_uid');
                        me.fields.push('update_date');
                        me.fields.push('update_uid');
                        me.elem.append(html);
                    }
                    me.elem.find('[data-field]').each(function () {
                        var e = $(this),
                            fname = e.attr('data-field'),
                            field = me.options.fields[fname],
                            editor = field.editor || field.type;
                        me.editors[fname] = $.editors[editor](e, {
                            field: field,
                            model: me.options.model,
                            attrs: fieldAttrs[fname],
                            onchange: function (e) {
                                me.dirty = true;
                            }
                        });
                    });
                }
            }
        },
        getFields: function () {
            return this.fields;
        },
        getSelected: function () {
            return this.sel;
        },
        setData: function (data) {
            var me = this;
            $.each(me.getFields(), function (i, fname) {
                me.editors[fname].setVal(data[fname]);
            });
        },
        getData: function () {
            var me = this, data = {};
            $.each(me.getFields(), function (i, fname) {
                data[fname] = me.editors[fname].getVal();
            });
            return data;
        },
        load: function () {
            var me = this;
            me.options.load(function (r) { me.setData(r.data) });
        }
    };
    $.fn.jForm = JForm;
    $.fn.JForm = function (opt) {
        return new JForm(this, opt);
    };
    //#endregion

    //#region JCard
    var JCard = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, JCard.defaults, opt);
        me.init();
    };
    JCard.defaults = {
        load: function (card, callback) {
        }
    };
    JCard.prototype = {
        init: function () {
            var me = this;
            me.fields = [];
            if (me.options.arch) {
                var arch = $.jutil.parseXML(me.options.arch);
                var card = arch.children('card');
                if (card.length > 0) {
                    card.children('field').each(function () {
                        var el = $(this),
                            name = el.attr('name');
                        me.fields.push(name);
                    });
                    var tpl = card.children('template').html();
                    me.tpl = tpl;
                }
            }
            me.elem.addClass('row mt-3');
            me.load();
        },
        load: function () {
            var me = this;
            me.elem.empty();
            me.options.load(me, function (e) {
                $.each(e.data, function (i, d) {
                    var html = me.tpl;
                    $.each(me.fields, function (i, f) {
                        html = html.replace('#{' + f + '}', d[f] || '');
                    });
                    me.elem.append('<div class="col-4" style="min-width: 18rem;"><div class="card">' + html + '</div></div>');
                });
            });
        },
        getFields: function () {
            return this.fields;
        }
    };
    $.fn.jCard = JCard;
    $.fn.JCard = function (opt) {
        return new JCard(this, opt);
    };
    //#endregion

    //#region view
    var JView = function (elem, opt) {
        var me = this;
        me.elem = elem;
        me.options = $.extend({}, JView.defaults, opt);
        me.init();
    };
    JView.defaults = {
        icon: { 'grid': 'fa-list-ul', 'card': 'fa-th-large' },
        getTpl: function (view) {
            var tpl = '',
                getViewMode = function (view) {
                    var views = '';
                    if (view.views.length > 1) {
                        views += '<div class="btn-group btn-group-toggle" data-toggle="buttons">';
                        $.each(view.views, function (i, v) {
                            views += '<label role="radio-view-mode" data="' + v + '" class="btn btn-sm btn-secondary' + (i === 0 ? ' active' : '') + '">\
                                <input type="radio" name="options" autocomplete="off"' + (i === 0 ? ' checked="checked"' : '') + '/>\
                                <i class="fa '+ view.options.icon[v] + '"></i>\
                            </label>';
                        });
                        views += '</div>';
                    }
                    return views;
                };
            if (view.views.length > 0) {
                var views = '';
                $.each(view.views, function (i, v) {
                    views += '<div role="' + v + '"></div>';
                });
                tpl += '<div class="view-panel">\
                            <div class="content-header">\
                                <div class="container-fluid">\
                                    <div role="search"></div>\
                                </div>\
                                <div class="btn-row">\
                                    <div role="toolbar" class="toolbar"></div>\
                                    <div class="btn-toolbar float-right toolbar-right">\
                                        <div role="pager" class="ml-2"></div>\
                                        <div role="view-mode" class="ml-2">' + getViewMode(view) + '</div>\
                                    </div>\
                                </div>\
                            </div>\
                            <div class="content">\
                                <div class="container-fluid">' + views + '</div>\
                            </div>\
                        </div>';
            }
            if (view.options.views.form) {
                tpl += '<div class="form-panel">\
                            <div class="content-header">\
                                <div class="btn-row">\
                                    <div role="form-toolbar" class="toolbar"></div>\
                                    <div class="btn-toolbar float-right toolbar-right">\
                                        <div role="data-nav" class="ml-2"></div>\
                                    </div>\
                                </div>\
                            </div>\
                            <div class="content">\
                                <div class="container-fluid">\
                                    <div role="form" class="row"></div>\
                                </div>\
                            </div>\
                        </div>';
            }
            return tpl;
        }
    };
    JView.prototype = {
        init: function () {
            var me = this;
            me.urlHash = $.jutil.getParams(window.location.hash.substring(1));
            me.views = me.urlHash.view.split(',');
            me.views.remove('form');
            if (me.options.resource) {
                $("head").append(me.options.resource);
            }
            me.elem.append(me.options.getTpl(me));
            me.elem.find('[role=radio-view-mode]').on('click', function (i) {
                var mode = $(this).attr('data');
                if (mode != me.mode) {//click触发两次
                    me.changeView(mode);
                }
            });
            if (me.views.length > 0) {
                if (!me.search) {
                    me.search = me.elem.find('[role=search]').JSearch({
                        model: me.options.model,
                        arch: me.options.views.search.arch,
                        fields: me.options.fields,
                        submit: function () {
                            me.pager.reset();
                            me.curView.load();
                        }
                    });
                }
                if (!me.pager) {
                    me.pager = me.elem.find('[role=pager]').JPager({
                        onchange: function (p) {
                            me.curView.load();
                        },
                        oncount: function (p) {
                            $.jrpc({
                                model: me.options.model,
                                method: "count",
                                args: {
                                    criteria: me.search.getCriteria()
                                },
                                onsuccess: function (r) {
                                    me.pager.update({
                                        total: r.data
                                    });
                                }
                            });
                        }
                    });
                }
            }
            me.data = {};
            me.changeView();
        },
        changeView: function (mode) {
            var me = this;
            var m = mode || me.urlHash.mode || me.urlHash.view.split(',')[0];
            if (me.mode !== m) {
                if (m === 'grid') {
                    me.showGridView();
                }
                if (m === 'card') {
                    me.showCardView();
                }
                if (m === 'form') {
                    me.showForm();
                }
                me.mode = m;
                me.urlHash.mode = me.mode;
            }
            window.location.hash = $.param(me.urlHash);
            var p = $.jutil.getParams(top.window.location.hash.substring(1));
            if (p.u) {
                p.u = window.location.pathname + '#' + $.param($.extend($.param(unescape(p.u)), me.urlHash));
            }
            top.window.location.hash = $.param(p);
        },
        showGridView: function () {
            var me = this;
            me.toolbar = me.elem.find('[role=toolbar]').JToolbar({
                arch: me.options.views.grid.arch,
                auths: me.options.auths,
                defaultButtons: 'create|copy|edit|delete|export'
            });
            if (!me.grid) {
                me.grid = me.elem.find('[role=grid]').JGrid({
                    model: me.options.model,
                    arch: me.options.views.grid.arch,
                    fields: me.options.fields,
                    search: me.search,
                    pager: me.pager,
                    onselected: function (grid, sel) {
                        var selected = [];
                        $.each(sel, function (i, id) {
                            selected.push(me.data[id]);
                        });
                        me.toolbar.update(selected);
                    },
                    load: function (grid, data, callback, settings) {
                        $.jrpc({
                            model: me.options.model,
                            method: "search",
                            args: {
                                criteria: me.search.getCriteria(),
                                nextTest: true,
                                offset: me.pager.getOffest(),
                                limit: me.pager.getLimit(),
                                fields: grid.getFields(),
                                order: grid.getSort()
                            },
                            onsuccess: function (r) {
                                if (r.data.values.length > 0) {
                                    var len = me.pager.getOffest() + r.data.values.length;
                                    if (r.data.hasNext === false) {
                                        me.pager.update({
                                            to: len,
                                            next: false,
                                            total: len
                                        });
                                    } else {
                                        me.pager.update({
                                            to: len,
                                            next: true
                                        });
                                    }
                                } else {
                                    me.pager.hide();
                                }
                                callback({
                                    data: r.data.values
                                });
                                me.data = {};
                                $.each(r.data.values, function (i, v) {
                                    me.data[v['id']] = v;
                                });
                            }
                        });
                    }
                });
            } else {
                me.grid.load();
            }
            me.curView = me.grid;
            me.showView('grid');
        },
        showCardView: function () {
            var me = this;
            if (!me.card) {
                me.card = me.elem.find('[role=card]').JCard({
                    model: me.options.model,
                    arch: me.options.views.card.arch,
                    fields: me.options.fields,
                    search: me.search,
                    pager: me.pager,
                    onselected: function (card, sel) {
                        var selected = [];
                        $.each(sel, function (i, id) {
                            selected.push(me.data[id]);
                        });
                        me.toolbar.update(selected);
                    },
                    load: function (card, callback) {
                        $.jrpc({
                            model: me.options.model,
                            method: "search",
                            args: {
                                criteria: me.search.getCriteria(),
                                nextTest: true,
                                offset: me.pager.getOffest(),
                                limit: me.pager.getLimit(),
                                fields: card.getFields()
                            },
                            onsuccess: function (r) {
                                if (r.data.values.length > 0) {
                                    var len = me.pager.getOffest() + r.data.values.length;
                                    if (r.data.hasNext === false) {
                                        me.pager.update({
                                            to: len,
                                            next: false,
                                            total: len
                                        });
                                    } else {
                                        me.pager.update({
                                            to: len,
                                            next: true
                                        });
                                    }
                                } else {
                                    me.pager.hide();
                                }
                                callback({
                                    data: r.data.values
                                });
                                me.data = {};
                                $.each(r.data.values, function (i, v) {
                                    me.data[v['id']] = v;
                                });
                            }
                        });
                    }
                });
            } else {
                me.card.load();
            }
            me.curView = me.card;
            me.showView('card');
        },
        showView: function (name) {
            var me = this;
            me.elem.find('.form-panel').hide();
            me.elem.find('.view-panel').show();
            $.each(me.views, function (i, v) {
                me.elem.find('[role=' + v + ']').hide();
            });
            me.elem.find('[role=' + name + ']').show();
        },
        showForm: function () {
            var me = this,
                back = me.mode;
            if (!me.form) {
                me.form = me.elem.find('[role=form]').JForm({
                    arch: me.options.views.form.arch,
                    model: me.options.model,
                    fields: me.options.fields,
                    load: function (callback) {
                        if (me.urlHash.id) {
                            me.form.sel = [me.urlHash.id];
                            $.jrpc({
                                model: me.options.model,
                                method: "read",
                                args: {
                                    ids: [me.urlHash.id],
                                    fields: me.form.getFields()
                                },
                                onsuccess: function (r) {
                                    callback({ data: r.data[0] });
                                }
                            });
                        } else {
                            callback({ data: {} });
                        }
                    },
                });
                me.toolbar = me.elem.find('[role=form-toolbar]').JToolbar({
                    arch: me.options.views.form.arch,
                    auths: me.options.auths,
                    defaultButtons: 'create|save'
                });
                if (me.views.length > 0) {
                    me.elem.find('.form-panel').append('<button role="form-close" class="btn" type="button" style="position:absolute;top:1px;right:1px">\
                        <i class="fas fa-angle-double-left mr-1"></i>'+ '返回'.t() + '</button>');
                    me.elem.find('[role=form-close]').on('click', function () {
                        me.changeView(back || me.urlHash.view.split(',')[0]);
                    });
                }
            }
            me.curView = me.form;
            me.elem.find('.form-panel').show();
            me.elem.find('.view-panel').hide();
            me.form.load();
        },
        getSelected: function () {
            return this.curView.getSelected();
        },
        save: function () {
            var me = this;
            if (me.curView != null) {
                var data = me.curView.getData();
                me.mask();
                if (me.urlHash.id) {
                    $.jrpc({
                        model: me.options.model,
                        method: "update",
                        args: {
                            ids: [me.urlHash.id],
                            values: data,
                        },
                        onerror: function (e) {
                            me.unmask();
                            Msg.showErr(e);
                        },
                        onsuccess: function (r) {
                            me.unmask();
                            Msg.showMsg('保存成功'.t());
                            me.curView.load();
                        }
                    });
                } else {
                    $.jrpc({
                        model: me.options.model,
                        method: "create",
                        args: data,
                        onerror: function (e) {
                            me.unmask();
                            Msg.showErr(e);
                        },
                        onsuccess: function (r) {
                            me.urlHash.id = r.data;
                            me.unmask();
                            Msg.showMsg('保存成功'.t());
                            window.location.hash = $.param(me.urlHash);
                            me.curView.load();
                        }
                    });
                }
            }
        },
        mask: function () {
            var me = this;
            if (!me.maskmodal) {
                me.maskmodal = $("<div id=\"pageWaitingModal\" class=\"modal fade\" data-keyboard=\"false\" data-backdrop=\"static\" data-role=\"dialog\" " +
                    "aria-labelledby=\"pageWaitingModalLabel\" aria-hidden=\"true\">" +
                    "<div id=\"loading\" class=\"loading\">" + '加载中,请稍等'.t() + "</div> </div>").appendTo("body");
            }
            $('#pageWaitingModal').modal('show');
        },
        unmask: function () {
            var me = this;
            $('#pageWaitingModal').remove();
            me.maskmodal = null;
            $('.modal-backdrop').remove();
        },
        load: function () {

        },
        delete: function () {
            var me = this;
            $.jrpc({
                model: me.options.model,
                method: 'delete',
                args: {
                    ids: me.curView.getSelected()
                },
                onsuccess: function (r) {
                    Msg.showMsg('删除成功'.t());
                    me.curView.load();
                }
            });
        },
        edit: function () {
            var me = this;
            me.urlHash.id = me.curView.getSelected()[0];
            me.changeView('form');
        },
        create: function () {
            var me = this;
            delete me.urlHash.id;
            me.changeView('form');
            var values = {};
            $.each(me.form.getFields(), function () {
                var field = me.options.fields[this];
                values[this] = field.defaultValue;
            });
            me.form.setData(values);
        },
        copy: function () {
            var me = this;
            delete me.urlHash.id;
            me.changeView('form');
        },
        postSvc: function (svc) {
            var me = this;
            $.jrpc({
                model: me.options.model,
                method: svc,
                args: {
                    ids: me.curView.getSelected()
                },
                onsuccess: function (r) {
                    var d = r.data || {};
                    if (d.message) {
                        Msg.showMsg(d.message);
                    }
                    if (d.action === 'js') {
                        eval(d.script);
                    } else if (d.action === 'reload') {
                        me.curView.load();
                    } else if (d.action === 'service') {
                        //TODO
                    } else if (d.action === 'dialog') {
                        //TODO
                    } else if (d.action === 'view') {
                        //TODO
                    }
                }
            });
        }
    };
    $.fn.jView = JView;
    $.fn.JView = function (opt) {
        return new JView(this, opt);
    };
    //#endregion

    $.loadJView = function () {
        var ps = $.jutil.getParams(window.location.hash.substring(1));
        $.jrpc({
            "model": "ir.ui.view",
            "method": "loadView",
            "args": {
                "model": ps.model,
                "type": ps.view,
            },
            onsuccess: function (r) {
                window.page = $('body').JView({
                    fields: r.data.fields,
                    views: r.data.views,
                    auths: r.data.auths,
                    model: r.data.model,
                    resource: r.data.resource
                });
            }
        });
    }
    $($.loadJView);
    $(function () {
        if (window.history && window.history.pushState) {
            $(window).on('popstate', function () {
                if (window.page) {
                    window.page.urlHash = $.jutil.getParams(window.location.hash.substring(1));
                    window.page.changeView();
                }
            });
        }
    });
}));