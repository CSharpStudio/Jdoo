$.component("FormView", {    
    getTpl: function () {
        return `<div class="form-panel">
                    <div class="content-header">
                    </div>
                    <div class="content">
                        <div role="form" class="form-content">
                            <div class="container-fluid form-body">
                                <div class="h-100 card-body row">
                                    <div class="col-md-12 form-droppable">
                                        <div class="grid form-view" style="grid-template-columns: repeat(4, 1fr);">
                                            <div class="form-field hide">
                                                <div class="w-100 hook-left" style="height:90px">
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>`;
    },
    getHookTpl: function () {
        return `<div class="form-group col-12 form-hook"><div></div></div>`;
    },
    getField: function (opt) {
        return `<div class="form-group col-12 form-field">
                    <div class="tools position-absolute toasts-top-right" style="display:none">
                        <span role="button" class="fa fa-times mr-1"></span>
                        <span role="button" class="fa fa-pencil-alt mr-1"></span>
                        <span role="button" class="fa fa-arrows-alt mr-1"></span>
                    </div>
                    <div class="position-absolute d-flex w-100 h-100">
                        <div class="w-100 h-100 hook-left"></div>
                        <div class="w-100 h-100 hook-right"></div>
                    </div>
                    <label>${opt.label}</label>
                    <div class="form-control mb-1"></div>
                </div>`;
    },
    init: function () {
        let me = this;
        me.dom.append(me.getTpl());
        me.dom.find('.form-droppable').droppable({
            over: function (e, ui) {
                ui.helper.addClass('draggable-over');
                me.dom.find('.form-field').removeClass('selected');
                me.dom.find('.form-field .tools').hide();
                if (me.dom.find('.form-field').length == 1) {
                    me.dom.find('.form-view').prepend(me.getHookTpl());
                }
            },
            out: function (e, ui) {
                ui.helper.removeClass('draggable-over');
                me.dom.find('.form-hook').remove();
            },
            drop: function (e, ui) {
                console.log('drop1');
                if (ui.draggable.hasClass('studio-component')) {
                    me.createField(ui.draggable);
                } else if (ui.draggable.hasClass('form-field')) {
                    me.dom.find('.form-hook').after(ui.draggable).remove();
                    ui.draggable.addClass('selected');
                    ui.draggable.find('.tools').show();
                }
            }
        });
        me.hook();
        me.dom.on('click', '.form-field', function () {
            let target = $(this);
            if (!target.hasClass('hide')) {
                me.dom.find('.form-field').removeClass('selected');
                me.dom.find('.form-field .tools').hide();
                target.addClass('selected');
                target.find('.tools').show();
            }
        });
    },
    hook: function () {
        let me = this;
        me.dom.find('.hook-left,.hook-right').droppable({
            over: function (e, ui) {
                let target = $(e.target);
                me.dom.find('.form-hook').remove();
                if (target.hasClass('hook-left')) {
                    target.parents('.form-field').before(me.getHookTpl());
                }
                if (target.hasClass('hook-right')) {
                    target.parents('.form-field').after(me.getHookTpl());
                }
            },
            drop: function (e, ui) {
                console.log('drop');
                if (ui.draggable.hasClass('studio-component')) {
                    me.createField(ui.draggable);
                } else if (ui.draggable.hasClass('form-field')) {
                    me.dom.find('.form-hook').after(ui.draggable).remove();
                    ui.draggable.addClass('selected');
                    ui.draggable.find('.tools').show();
                }
            }
        });
        me.dom.find('.form-field').draggable({
            handle: '.fa-arrows-alt',
            helper: function (e) {
                return $(e.currentTarget).clone(true).addClass('studio-component-dragging').css("width", "24%");
            },
            start: function (e, ui) {
                $(e.currentTarget).hide();
            },
            stop: function (e, ui) {
                console.log('stop');
                $(e.target).show();
            }
        });
    },
    createField: function (e) {
        let me = this;
        me.dom.find('.form-hook').after(me.getField({ label: '测试' + jdoo.nextId() })).remove();
        me.hook();
    },
});