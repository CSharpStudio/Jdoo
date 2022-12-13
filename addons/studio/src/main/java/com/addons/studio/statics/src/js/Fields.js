$.component("Fields", {
    getTpl: function () {
        return `<div class="field-tools">
                    <div class="items-layout">
                        <span class="group-head">布局</span>
                        <div class="studio-component studio-field-tabs">页签</div>
                        <div class="studio-component studio-field-group">分组</div>
                    </div>
                    <div class="items-field">
                        <span class="group-head">新字段</span>
                        <div class="studio-component studio-field-char" title="char">文本</div>
                        <div class="studio-component studio-field-text" title="text">多行文字</div>
                        <div class="studio-component studio-field-integer" title="integer">整数</div>
                        <div class="studio-component studio-field-float" title="float">小数</div>
                        <div class="studio-component studio-field-date" title="date">日期</div>
                        <div class="studio-component studio-field-datetime" title="datetime">日期时间</div>
                        <div class="studio-component studio-field-boolean" title="boolean">勾选框</div>
                        <div class="studio-component studio-field-selection" title="selection">下拉框</div>
                        <div class="studio-component studio-field-binary" title="binary">文件</div>
                        <div class="studio-component studio-field-image" title="image">图片</div>
                        <div class="studio-component studio-field-many2one" title="many2one">多对一</div>
                        <div class="studio-component studio-field-one2many" title="one2many">一对多</div>
                        <div class="studio-component studio-field-many2many" title="many2many">多对多</div>
                        <div class="studio-component studio-field-tags" title="many2many">标签</div>
                        <div class="studio-component studio-field-priority" title="selection">优先级</div>
                        <div class="studio-component studio-field-related" title="related">关联字段</div>
                    </div>
                    <div class="field-list">
                        <span>现有字段</span>
                    </div>
                </div>`;
    },
    init: function () {
        let me = this;
        me.dom.append(me.getTpl())
            .find('.studio-component').draggable({
                containment: me.designer.dom,
                helper: function (e) {
                    return $(e.currentTarget).clone(true).addClass('studio-component-dragging');
                }
            });
    }
});