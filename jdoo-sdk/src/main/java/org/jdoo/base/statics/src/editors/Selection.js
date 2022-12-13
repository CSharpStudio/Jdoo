jdoo.editor('selection', {
    getTpl: function () {
        let me = this, html = `<select class="form-control" id="${me.name + '-' + jdoo.nextId()}">`;
        if (me.allowNull) {
            html += '<option value=""> </option>';
        }
        for (const key in me.options) {
            html += `<option value="${key}">${me.options[key]}</option>`;
        }
        html += '</select>';
        return html;
    },
    init: function () {
        let me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        if (field.type === 'many2one') {
            //TODO 加载options
        } else {
            me.options = eval(dom.attr('options')) || field.options;
            dom.html(me.getTpl());
        }
    },
    onValueChange: function (handler) {
        let me = this;
        me.dom.find('select').on('change', function (e) {
            handler(e, me);
        });
    },
    setReadonly: function (v) {
        if (v) {
            this.dom.children('select').attr('disabled', true);
        } else {
            this.dom.children('select').removeAttr('disabled');
        }
    },
    getValue: function () {
        return this.dom.children('select').val();
    },
    setValue: function (v) {
        this.dom.children('select').val(v);
    }
});

jdoo.searchEditor('selection', {
    extends: "editors.selection",
    getCriteria: function () {
        let val = this.getValue();
        if (val) {
            return [[this.name, '=', val]];
        }
        return [];
    },
    getText: function () {
        return this.options[this.getValue()];
    },
});