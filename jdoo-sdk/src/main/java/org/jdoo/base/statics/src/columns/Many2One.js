jdoo.column('many2one', {
    render: function () {
        return function (data, type, row) {
            if (data && data[0]) {
                return `<span data-value="${data[0]}">${data[1]}</span>`;
            }
            return '';
        }
    }
});