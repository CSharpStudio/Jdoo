jdoo.editor('priority', {
    fontIcon: "fa fa-star",
    getTpl: function () {
        let me = this, html = `<ul class="priority" id="${me.name + '-' + jdoo.nextId()}">`;
        for (const key in me.options) {
            html += `<li class="priority-item" title="${me.options[key]}">
                        <input name="priority" value="${key}" type="radio" style="display:none">
                        <span><i class="${me.fontIcon}"></i></span></input>
                    </li>`;
        }
        html += '</ul>';
        return html;
    },
    init: function () {
        let me = this, dom = me.dom, field = me.field;
        me.num = 0;
        me.name = me.name || dom.attr('data-field') || field.name;
        if (field.type === 'many2one') {
            //TODO 加载options
        } else {
            me.options = eval(dom.attr('options')) || field.options;
            dom.html(me.getTpl());
        }
        dom.on('mouseover', '.priority-item', function () {
            me.lightOn($(this).index() + 1);
        }).on('click', '.priority-item', function () {
            me.num = $(this).index() + 1;
            $(this).find("input").prop('checked', true).change();
        }).on('mouseout', function () {
            me.lightOn(me.num);
        });
    },
    onValueChange: function (handler) {
        let me = this;
        me.dom.find('input').on('change', function (e) {
            handler(e, me);
        });
    },
    setReadonly: function (v) {
        if (v) {
            this.dom.children('input').attr('disabled', true);
        } else {
            this.dom.children('input').removeAttr('disabled');
        }
    },
    lightOn: function (num) {
        this.dom.find('.priority-item').each(function (index) {
            if (index < num) {
                $(this).addClass("light-on");
            } else {
                $(this).removeClass("light-on");
            }
        });
    },
    getValue: function () {
        return this.dom.find('input:radio:checked').val();
    },
    setValue: function (v) {
        if (v) {
            this.num = this.dom.find("input[value=" + v + "]").prop('checked', true).parent('.priority-item').index() + 1;
            this.lightOn(this.num)
        }
        else {
            this.dom.find("input").prop('checked', false);
            this.num = 0;
            this.lightOn(this.num)
        }
    },
});