jdoo.editor('text', {
    getTpl: function (o) {
        return `<textarea id="${this.name + '-' + jdoo.nextId()}" rows="${this.rows}" type="text" class="form-control"/>`;
    },
    init: function () {
        let me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        me.rows = me.rows || dom.attr("rows") || 3;
        dom.html(me.getTpl());
    },
    onValueChange: function (handler) {
        let me = this;
        me.dom.find('textarea').on('change', function (e) {
            handler(e, me);
        });
    },
    setReadonly: function (v) {
        if (v) {
            this.dom.children('textarea').attr('disabled', true);
        } else {
            this.dom.children('textarea').removeAttr('disabled');
        }
    },
    getValue: function () {
        return this.dom.children('textarea').val();
    },
    setValue: function (v) {
        this.dom.children('textarea').val(v);
    }
});

jdoo.searchEditor('text', {
    extends: "editors.text",
    getCriteria: function () {
        let val = this.getValue(), vals = val.split(';');
        if (vals.length > 1) {
            return [[this.name, 'in', vals]];
        }
        if (val) {
            return [[this.name, '=', val]];
        }
        return [];
    },
    getText: function () {
        return this.getValue();
    },
});