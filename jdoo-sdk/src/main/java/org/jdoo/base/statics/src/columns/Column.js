jdoo.columns = {};
jdoo.column = function (name, define) {
    jdoo.columns[name] = jdoo.component('columns.' + name, define);
}
jdoo.column('default', {
    render: function () {
        let me = this, t = me.field.type;
        return function (data, type, row) {
            if (data === null) {
                data = '';
            }
            if (t === 'integer' || t === 'float') {
                return '<div class="text-right">' + data + '</div>';
            }
            return data;
        }
    }
});