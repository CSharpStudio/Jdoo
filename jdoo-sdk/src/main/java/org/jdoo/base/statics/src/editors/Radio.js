jdoo.editor('radio', {
    getTpl: function () {
        let id = jdoo.nextId();
        let me = this, html = `<div class="form-radio" id="${me.name + '-' + id}">`;
        for (const key in me.options) {
            html += `<label class="form-radio-label">
                        <input class="radio-inline" type="radio" name="radio-${id}" value="${key}"/>
                        ${me.options[key]}
                    </label>`;
        }
        html += "</div>"
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
        me.dom.find('input').on('change', function (e) {
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
        return this.dom.find('input:radio:checked').val();
    },
    setValue: function (v) {
        if (v) {
            this.dom.find("input[value=" + v + "]").prop('checked', true);
        } else {
            this.dom.find("input").prop('checked', false);
        }
    },
});

jdoo.searchEditor('radio', {
    extends: "editors.radio",
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