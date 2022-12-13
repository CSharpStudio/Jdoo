
jdoo.component("Field", {
    name: 'field',
    field_type: 'char',
    label: '字段',
    getTpl: function () {
        return `<div id="${this.id}" data-field="${this.data.id}" class="item-field">
                    <div class="field-tools">
                        <div class="nav-item dropdown">
                            <span data-toggle="dropdown" role="button">
                                <i class="fa fa-bars"></i>
                            </span>
                            <div class="dropdown-menu dropdown-menu-right">
                                <span role="button" class="menu-edit-field dropdown-item">${'编辑'.t()}</span>
                                <span role="button" class="menu-delete-field dropdown-item">${'删除'.t()}</span>
                            </div>
                        </div>
                    </div>
                    <span class="field-label">${this.data.label || ''}</span>(<span class="field-name">${this.data.name}</span>:<span class="field-type">${this.data.field_type}</span>)
            </div>`;
    },
    init: function () {
        var me = this, parent = $(me.renderTo);
        me.id = me.id || jdoo.utils.randomId();
        parent.append(me.getTpl());
        me.dom = parent.find('#' + me.id);
    },
    update: function (values) {
        var me = this;
        if (values.id !== me.data.id) {
            $('[data-field=' + me.data.id + ']').attr('data-field', values.id);
        }
        $.extend(true, me.data, values);
        $('[data-field=' + me.data.id + '] .field-label').html(me.data.label);
        $('[data-field=' + me.data.id + '] .field-name').html(me.data.name);
        $('[data-field=' + me.data.id + '] .field-type').html(me.data.field_type);
    }
});