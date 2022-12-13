jdoo.editor('boolean', {
    css: 'custom-switch custom-control',
    getTpl: function () {
        let id = this.name + '-' + jdoo.nextId();
        return `<input type="checkbox" class="custom-control-input" id="${id}"/><label for="${id}" class="custom-control-label mt-1"></label>`;
    },
    getAllowNullTpl: function () {
        return `<select class="form-control" id="${this.name + '-' + jdoo.nextId()}">
                    <option value=""></option>
                    <option value="true">${'是'.t()}</option>
                    <option value="false">${'否'.t()}</option>
                </select>`;
    },
    init: function () {
        let me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        if (me.allowNull) {
            dom.html(me.getAllowNullTpl());
        } else {
            dom.addClass(me.css).append(me.getTpl());
        }
    },
    onValueChange: function (handler) {
        let me = this;
        me.dom.find('select,input').on('change', function (e) {
            handler(e, me);
        });
    },
    setReadonly: function (v) {
        let me = this, selector = me.allowNull ? 'select' : 'input';
        if (v) {
            me.dom.children(selector).attr('disabled', true);
        } else {
            me.dom.children(selector).removeAttr('disabled');
        }
    },
    getValue: function () {
        let me = this;
        if (me.allowNull) {
            let v = me.dom.children('select').val();
            if (v === 'true') {
                return true;
            } else if (v === 'false') {
                return false;
            }
            return null;
        }
        return me.dom.children('input').is(":checked");
    },
    setValue: function (v) {
        let me = this;
        if (typeof v === "string" && v.length > 0) {
            v = Boolean(eval(v));
        }
        if (me.allowNull) {
            let val = (v === null || v === undefined || v === '') ? '' : (v ? 'true' : 'false');
            return me.dom.children('select').val(val);
        } else {
            me.dom.children('input').prop("checked", v === true);
        }
    }
});

jdoo.searchEditor('boolean', {
    extends: "editors.boolean",
    allowNull: true,
    getCriteria: function () {
        let val = this.getValue();
        if (val != null) {
            return [[this.name, '=', val]];
        }
        return [];
    },
    getText: function () {
        let me = this;
        if (me.allowNull) {
            let v = me.dom.children('select').val();
            if (v === 'true') {
                return '是'.t();
            } else if (v === 'false') {
                return '否'.t();
            }
            return '';
        }
        return me.dom.children('input').is(":checked") ? '是'.t() : '否'.t();
    },
});