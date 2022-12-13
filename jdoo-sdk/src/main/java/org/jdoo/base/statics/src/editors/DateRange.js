jdoo.searchEditor('date_range', {
    format: 'YYYY-MM-DD',
    startDate: '2022-01-01',
    endDate: '2022-02-01',
    getTpl: function () {
        return `<div class="input-group dateRanges" id="${this.name + '-' + jdoo.nextId()}">
                    <div class="input-group-prepend">
                        <span class="input-group-text">
                            <span class="far fa-calendar-alt"></span>
                        </span>
                    </div>
                    <input type="text" class="form-control float-right"/>
                </div>`;
    },
    init: function () {
        let me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        me.startDate = dom.attr('startDate') || me.startDate;
        me.endDate = dom.attr('endDate') || me.endDate;
        dom.html(me.getTpl())
            .find('input').daterangepicker({
                parentEl: ".dateRanges",
                startDate: me.startDate,
                endDate: me.endDate,
                locale: {
                    format: me.format,
                },
            }
            ).on('apply.daterangepicker', function (ev, picher) {
                me.startDate = picher.startDate.format(me.format);
                me.endDate = picher.endDate.format(me.format);
            });
    },
    getValue: function () {
        let me = this,
            text = me.dom.find('input').val();
        if (text) {
            return [me.startDate, me.endDate];
        }
        return "";
    },
    getCriteria: function () {
        let me = this;
        if (this.dom.find('input').val()) {
            return ['&', [me.name, '>=', me.startDate], [me.name, '<=', me.endDate]];
        }
        return [];
    },
    getText: function () {
        let me = this,
            text = me.dom.find('input').val();
        if (text) {
            return [me.startDate + "至".t() + me.endDate];
        }
        return "";
    },
    setValue: function (v) {
        let me = this;
        if (!v || v === undefined || v === '') {
            me.dom.find('input').val('');
        } else {
            let values = v.split(',');
            me.dom.find('input').val(values[0] + "-" + values[1]);
            me.startDate = values[0];
            me.endDate = values[1];
        }
    }
});