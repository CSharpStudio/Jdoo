$.component("ViewDesigner", {
    getTpl: function () {
        return `<div data-id="${this.dataId}" class="view-designer">
                    <div class="tool-bar">
                        <button title="${"刷新".t()}" class="btn btn-default btn-sm btn-reload">
                            <span class="fas fa-sync-alt"></span>
                        </button>
                        <button title="${"撤销".t()}" class="btn btn-default btn-sm btn-undo">
                            <span class="fas fa-undo-alt"></span>
                        </button>
                        <button title="${"恢复".t()}" class="btn btn-default btn-sm btn-redo">
                            <span class="fas fa-redo-alt"></span>
                        </button>
                        <button title="${"查看代码".t()}" class="btn btn-default btn-sm btn-code-view">
                            <span class="fas fa-laptop-code"></span>
                        </button>
                        <button title="${"下载代码".t()}" class="btn btn-default btn-sm btn-code-down">
                            <span class="fas fa-download"></span>
                        </button>
                        <button title="${"从源码包生成模型图".t()}" class="btn btn-default btn-sm btn-reflact">
                            <span class="fas fa-gg"></span>
                        </button>
                    </div>
                    <aside class="left-box">
                    </aside>
                    <div class="main-box">
                    </div>
                </div>`;
    },
    init: function () {
        let me = this;
        me.dom.append(me.getTpl());
        me.dom.find('.left-box').Fields({ designer: me });
        me.view = me.dom.find('.main-box').FormView({});
    }
});