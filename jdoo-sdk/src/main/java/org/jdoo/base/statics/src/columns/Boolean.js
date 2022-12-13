jdoo.column('boolean', {
    render: function () {
        return function (data, type, row) {
            if (data) {
                return '<label class="checked-column"></label>';
            }
            return '<label class="unchecked-column" ></label>';
        }
    }
});