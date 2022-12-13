//@ sourceURL=menu.js
jdoo.editor('menu_view', {
    getTpl: function () {
        return `<div class="input-group"><input type="text" role="view-type" class="form-control" id="${this.field.name + '-' + jdoo.nextId()}"/>
                    <input type="text" role="view-key" class="form-control"/>
                </div>`;
    },
    init: function () {
        let me = this;
        me.dom.append(me.getTpl());
        me.dom.find('[role=view-type').attr('placeholder', '视图类型'.t());
        me.dom.find('[role=view-key').attr('placeholder', '视图组'.t());
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
        let me = this, t = me.dom.find('[role=view-type]').val(), k = me.dom.find('[role=view-key]').val();
        return t + (k ? '|' + k : '');
    },
    getText: function () {
        return this.dom.find('input').val();
    },
    setValue: function (v) {
        let me = this, t = me.dom.find('[role=view-type]'), k = me.dom.find('[role=view-key]');
        if (v) {
            let d = v.split('|');
            t.val(d[0]);
            k.val(d[1] || '');
        } else {
            t.val('');
            k.val('');
        }
    }
});