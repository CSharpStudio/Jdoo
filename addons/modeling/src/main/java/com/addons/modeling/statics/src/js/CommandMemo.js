jdoo.component("CommandMemo", {
  init: function () {
    let me = this;
    me.observer = [];
    me.undoStack = [];
    me.redoStack = [];
    me.onChange(me.change);
  },
  add: function (cmd) {
    let me = this;
    me.redoStack = [];
    me.undoStack.push(cmd);
    me.triggerChange();
  },
  clear: function () {
    let me = this;
    me.undoStack = [];
    me.redoStack = [];
    me.triggerChange();
  },
  clearRedo: function () {
    let me = this;
    me.redoStack = [];
    me.triggerChange();
  },
  clearUndo: function () {
    let me = this;
    me.undoStack = [];
    me.triggerChange();
  },
  undo: function () {
    let me = this;
    if (me.undoStack.length > 0) {
      let cmd = me.undoStack.pop();
      cmd.undo();
      me.redoStack.push(cmd);
      me.triggerChange();
      return cmd;
    }
  },
  redo: function () {
    let me = this;
    if (me.redoStack.length > 0) {
      let cmd = me.redoStack.pop();
      cmd.redo();
      me.undoStack.push(cmd);
      me.triggerChange();
      return cmd;
    }
  },
  canRedo: function () {
    return this.redoStack.length > 0;
  },
  canUndo: function () {
    return this.undoStack.length > 0;
  },
  triggerChange: function () {
    let me = this;
    $.each(me.observer, function () {
      this(me);
    });
  },
  onChange: function (handler) {
    this.observer.push(handler);
  }
});