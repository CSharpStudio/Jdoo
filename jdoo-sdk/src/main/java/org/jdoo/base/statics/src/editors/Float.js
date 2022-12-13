jdoo.editor('float', {
    extends: "editors.integer",

    getTpl: function (o) {
        return `<input type="number" id="${this.name + '-' + jdoo.nextId()}"
                    ${this.min ? ' min="' + this.min + '"' : ''}
                    ${this.max ? ' max="' + this.max + '"' : ''}
                    ${this.step ? ' step="' + this.step + '"' : ''}
                    ${this.decimals ? ' data-decimals="' + this.decimals + '"' : ''}/>`;
    },
    init: function () {
        var me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        me.min = me.min || dom.attr("min");
        me.max = me.max || dom.attr("max");
        me.step = me.step || dom.attr("step");
        me.decimals = me.decimals || dom.attr('decimals') || "2";
        dom.html(me.getTpl());
        me.initSpinner(dom.find("input[type=number]"));
    }
});

jdoo.searchEditor('float', {
    extends: "editors.float",
    getCriteria: function () {
        var val = this.getValue();
        if (val) {
            return [[this.name, '=', val]];
        }
        return [];
    },
    getText: function () {
        return this.getValue();
    },
});