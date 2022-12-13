jdoo.editor('integer', {
    getTpl: function () {
        return `<input type="number" id="${this.name + '-' + jdoo.nextId()}"
                    ${this.min ? ' min="' + this.min + '"' : ''}
                    ${this.max ? ' max="' + this.max + '"' : ''}
                    ${this.step ? ' step="' + this.step + '"' : ''}/>`;
    },
    buttonsClass: "input-group-text",
    decrementButton: '<i class="fa fa-minus"></i>',
    incrementButton: '<i class="fa fa-plus"></i>',
    init: function () {
        let me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        me.min = me.min || dom.attr("min");
        me.max = me.max || dom.attr("max");
        me.step = me.step || dom.attr("step");
        dom.html(me.getTpl());
        me.initSpinner(dom.find("input[type=number]"));
    },
    initSpinner: function (el) {
        let me = this;
        el.inputSpinner({
            buttonsClass: me.buttonsClass,
            decrementButton: me.decrementButton,
            incrementButton: me.incrementButton
        });
    },
    onValueChange: function (handler) {
        let me = this;
        me.dom.find('input').on('change', function (e) {
            handler(e, me);
        });
    },
    setReadonly: function (v) {
        let me = this;
        if (v) {
            me.initSpinner(me.dom.children('input').attr('readonly', true));
        } else {
            me.initSpinner(me.dom.children('input').removeAttr('readonly'));
        }
    },
    getValue: function () {
        return this.dom.children('input').val();
    },
    setValue: function (val) {
        if (val === null) {
            val = undefined;
        }
        this.dom.children('input').val(val);
    }
});

jdoo.searchEditor('integer', {
    extends: "editors.integer",
    getCriteria: function () {
        let val = this.getValue();
        if (val) {
            return [[this.name, '=', val]];
        }
        return [];
    },
    getText: function () {
        return this.getValue();
    },
});