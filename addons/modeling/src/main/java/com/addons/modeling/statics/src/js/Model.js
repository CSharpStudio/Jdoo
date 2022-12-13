
jdoo.component("Model", {
    x: '0',
    y: '0',
    getTpl: function () {
        let me = this, css = me.diagram.module_id === me.data.module_id ? '' : ' other-module';
        return `<div id="${me.id}" data-model="${me.data.id}" class="item-model${css}" style="top:${me.y}px;left:${me.x}px;">
                        <div class="model-meta">
                            <div class="model-tools">
                                <div class="nav-item dropdown">
                                    <span data-toggle="dropdown" role="button">
                                        <i class="fa fa-bars"></i>
                                    </span>
                                    <div class="dropdown-menu dropdown-menu-right">
                                        <span role="button" class="menu-model dropdown-item">${'详情'.t()}</span>
                                        <span role="button" class="menu-code-view dropdown-item">${'生成代码'.t()}</span>
                                        <span role="button" class="menu-code-download dropdown-item">${'下载代码'.t()}</span>
                                        <span role="button" class="menu-publish dropdown-item">${'发布'.t()}</span>
                                        <div role="separator" class="dropdown-divider"></div>
                                        <span role="button" title="${'从当前模型图移除'.t()}" class="menu-unlink dropdown-item">${'移除'.t()}</span>
                                        <span role="button" title="${'删除模型'.t()}" class="menu-delete dropdown-item">${'删除'.t()}</span>
                                    </div>
                                </div>
                                <span class="btn-edit" role="button"><i class="fa fa-pencil-alt"></i></span>
                                <span class="btn-add-field" role="button"><i class="fa fa-plus"></i></span>
                            </div>
                            <div class="model-name">${me.data.name}</div>
                            <div class="model-model">${'模型'.t()}:<span>${me.data.model}</span></div>
                            <div class="model-class">${'类名'.t()}:<span>${me.data.model_class}</span></div>
                            <div class="model-inherit" style="display:${me.data.inherit ? 'block' : 'none'}">${'继承'.t()}:<span>${me.data.inherit}</span></div>
                        </div>
                        <div class="model-members"></div>
                    </div>`;
    },
    init: function () {
        let me = this;
        me.id = me.id || jdoo.utils.randomId();
        me.canvas = $(me.renderTo);
        me.canvas.append(me.getTpl());
        me.dom = me.canvas.find('#' + me.id);
        me.dom.on('click', '.btn-add-field', function () {
            me.diagram.createField(me.id);
        }).on('click', '.menu-edit-field', function () {
            let fieldId = $(this).parents('.item-field').attr('id');
            me.diagram.editField(me.id, fieldId);
        }).on('dblclick', '.item-field', function () {
            let fieldId = $(this).attr('id');
            me.diagram.editField(me.id, fieldId);
        }).on('click', '.menu-delete-field', function () {
            let fieldId = $(this).parents('.item-field').attr('id');
            jdoo.msg.confirm({
                content: "确定删除字段?",
                submit: function () {
                    me.diagram.deleteField(me.id, fieldId);
                }
            });
        }).on('click', '.menu-model', function () {
            //show detail
        }).on('click', '.menu-delete', function () {
            me.delete();
        }).on('click', '.menu-unlink', function () {
            me.unlink();
        }).on('click', '.menu-code-view', function () {
            me.diagram.viewCode([me.id]);
        }).on('click', '.menu-publish', function () {
            //publish
        }).on('click', '.btn-edit', function () {
            me.diagram.editModel(me.id);
        }).on('dblclick', '.model-meta', function () {
            me.diagram.editModel(me.id);
        });
        me.dom.draggable({
            scroll: true,
            opacity: 0.8,
            zIndex: 999,
            cursor: "move",
            containment: 'parent',
            stack: ".item-model",
            delay: 100,
            start: function (e, ui) {
                if (!me.selected) {
                    me.diagram.unselectModels();
                    me.select();
                }
            },
            drag: function (e, ui) {
                $.each(me.connectors, function () {
                    this.update();
                });
                let o = ui.originalPosition, p = ui.position, z = me.diagram.zoom;
                p.left = o.left + (p.left - o.left) * 100 / z;
                p.top = o.top + (p.top - o.top) * 100 / z;
                if (p.left < 0) {
                    p.left = 0;
                }
                if (p.top < 0) {
                    p.top = 0;
                }
            },
            stop: function (e) {
                $.each(me.connectors, function () {
                    this.update();
                });
                let param = { id: me.id, modelId: me.data.id, x0: me.x, y0: me.y };
                param.x = me.x = me.dom.css('left').replace('px', '');
                param.y = me.y = me.dom.css('top').replace('px', '');
                me.diagram.updateLocation(param);
            }
        });
        me.fields = jdoo.create("KeyValue");
        me.connectors = [];
        me.dom.find('[data-toggle=dropdown]').dropdown({ boundary: me.canvas.parent() });
    },
    moveTo: function (point) {
        let me = this;
        me.x = point.x;
        me.y = point.y;
        me.dom.css('left', me.x + 'px').css('top', me.y + 'px');
        $.each(me.connectors, function () {
            this.update();
        });
    },
    update: function (values) {
        let me = this;
        if (values.id !== me.data.id) {
            $('[data-model=' + me.data.id + ']').attr('data-model', values.id);
        }
        $.extend(true, me.data, values);
        $('[data-model=' + me.data.id + '] .model-name').html(me.data.name);
        $('[data-model=' + me.data.id + '] .model-model span').html(me.data.model);
        $('[data-model=' + me.data.id + '] .model-class span').html(me.data.model_class);
        $('[data-model=' + me.data.id + '] .model-inherit span').html(me.data.inherit);
        if (me.data.inherit) {
            $('[data-model=' + me.data.id + ']' + ' .model-inherit').show();
        } else {
            $('[data-model=' + me.data.id + ']' + ' .model-inherit').hide();
        }
    },
    addField: function (data) {
        let me = this;
        data.model = me;
        data.renderTo = me.canvas.find('[data-model=' + me.data.id + '] .model-members');
        let field = jdoo.create("Field", data);
        me.fields.add(field.id, field);
        return field;
    },
    removeField: function (fieldId) {
        this.fields.remove(fieldId);
        this.dom.find('#' + fieldId).remove();
    },
    delete: function () {
        let me = this;
        jdoo.msg.confirm({
            content: "确定删除模型?".t(),
            submit: function () {
                me.diagram.deleteModel(me.id);
            }
        });
    },
    unlink: function () {
        let me = this;
        jdoo.msg.confirm({
            content: "确定从模型图移除模型?".t(),
            submit: function () {
                me.diagram.unlinkModel(me.id);
            }
        });
    },
    publish: function () {

    },
    genCode: function () {

    },
    select: function () {
        let me = this;
        me.selected = true;
        me.dom.addClass('selected');
    },
    unselect: function () {
        let me = this;
        me.selected = false;
        me.dom.removeClass('selected');
    }
});