
jdoo.define("KeyValue", {
    new: function () {
        var me = this;
        me.values = [];
        me.keyValue = {};
    },
    add: function (key, value) {
        var me = this;
        me.keyValue[key] = value;
        me.values.push(value);
    },
    remove: function (key) {
        var me = this, node = me.get(key);
        delete me.keyValue[key];
        me.values.remove(node);
        return node;
    },
    get: function (key) {
        return this.keyValue[key];
    },
    getValues: function () {
        return this.values;
    },
    each: function (handler) {
        $.each(this.values, handler);
    },
    find: function (filter) {
        var found = [];
        $.each(this.values, function () {
            if (filter(this)) {
                found.push(this);
            }
        });
        return found;
    },
    first: function (filter) {
        var found = null;
        $.each(this.values, function () {
            if (filter(this)) {
                found = this;
                return false;
            }
        });
        return found;
    }
});