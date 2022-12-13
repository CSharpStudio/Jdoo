jdoo.searchEditor('datetime_range', {
    extends: "searchEditors.date_range",
    format: 'YYYY-MM-DD HH:mm:ss',
    startDate: '2022-01-01 00:00:00',
    endDate: '2022-02-01 00:00:00',
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
                timePicker: true, //是否显示小时和分钟 
                timePicker24Hour: true, //24小时制显示
            }
            ).on('apply.daterangepicker', function (ev, picher) {
                me.startDate = picher.startDate.format(me.format);
                me.endDate = picher.endDate.format(me.format);
            });
    }
});