jdoo.editors = {};
jdoo.searchEditors = {};
jdoo.component("editors.Editor", {
    getRawValue: function () {
        return this.getValue();
    },
    getValue: jdoo.emtpyFn,
    setValue: jdoo.emtpyFn,
    onValueChange: jdoo.emtpyFn,
    setReadonly: jdoo.emtpyFn,
    valid: jdoo.emtpyFn,
});
jdoo.editor = function (name, define) {
    if (typeof define === "function") {
        define = define();
    }
    define.extends = define.extends || 'editors.Editor';
    jdoo.editors[name] = jdoo.component('editors.' + name, define);
}
jdoo.searchEditor = function (name, define) {
    if (typeof define === "function") {
        define = define();
    }
    define.extends = define.extends || 'editors.Editor';
    jdoo.searchEditors[name] = jdoo.component('searchEditors.' + name, define);
}