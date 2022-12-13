jdoo.editor('password', {
    extends: "editors.char",
    statics: {
        getTpl: function () {
            return `<input type="password" autocomplete="off" class="form-control" id="${this.name + '-' + jdoo.nextId()}"/>`;
        }
    }
});