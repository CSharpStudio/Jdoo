jdoo.column('selection', {
    render: function () {
        let me = this;
        return function (data, type, row) {
            let val = me.field.options[data];
            if (val === undefined) {
                val = '';
            }
            return `<span data-value="${data}">${val}</span>`;
        }
    }
});