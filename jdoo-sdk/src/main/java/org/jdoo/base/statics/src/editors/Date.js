jdoo.editor('date', {
    format: 'YYYY-MM-DD',
    max: '2100-12-31',
    min: '1920-1-1',
    todayBtn: true,
    sideBySide: true,
    getTpl: function () {
        let id = this.name + '-' + jdoo.nextId();
        return `<div class="input-group date-edit" id="${id}" data-target-input="nearest">
                    <input type="text" class="form-control datetimepicker-input" data-target="#${id}" />
                    <div class="input-group-append" data-target="#${id}" data-toggle="datetimepicker">
                        <div class="input-group-text"><i class="fa fa-calendar"></i></div>
                    </div>
                </div>`;
    },
    init: function () {
        let me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        me.max = dom.attr('max') || me.max;
        me.min = dom.attr('min') || me.min;
        dom.html(me.getTpl())
            .find('.date-edit').datetimepicker({
                format: me.format,
                locale: moment.locale('zh-cn'),
                minDate: new Date(me.min),
                maxDate: new Date(me.max),
                todayBtn: me.todayBtn,
                sideBySide: me.sideBySide
            });
    },
    onValueChange: function (handler) {
        let me = this;
        me.dom.find('.date-edit').on('change.datetimepicker', function (e) {
            handler(e, me);
        });
    },
    setReadonly: function (v) {
        if (v) {
            this.dom.find('input').attr('readonly', true);
        } else {
            this.dom.find('input').removeAttr('readonly');
        }
    },
    getValue: function () {
        let me = this,
            text = me.dom.find('input').val(),
            date = me.dom.find('.date-edit').datetimepicker('viewDate').format(me.format);
        if (text) {
            return date;
        }
        return '';
    },
    setValue: function (v) {
        let me = this;
        if (v === undefined || v === '') {
            me.dom.find('.date-edit').datetimepicker('clear');
        } else {
            me.dom.find('.date-edit').datetimepicker('date', v);
        }
    }
});

jdoo.searchEditor('date', {
    extends: "editors.date",
    getCriteria: function () {
        var val = this.getValue();
        if (val) {
            return [[this.name, '=', val]];
        }
        return [];
    },
    getText: function () {
        return this.getValue();
    },
});