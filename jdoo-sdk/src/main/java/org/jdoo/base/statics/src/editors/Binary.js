jdoo.editor('binary', {
    getTpl: function () {
        return `<div class="input-group">
                    <div class="custom-file">
                        <input type="file" class="custom-file-input" id="exampleInputFile"/>
                        <label class="custom-file-label" for="exampleInputFile">${'选择文件...'.t()}</label>
                    </div>
                    <div class="input-group-prepend">
                        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
                            Action
                        </button>
                        <ul class="dropdown-menu">
                            <li class="dropdown-item"><a href="#">Action</a></li>
                            <li class="dropdown-item"><a href="#">Another action</a></li>
                            <li class="dropdown-item"><a href="#">Something else here</a></li>
                            <li class="dropdown-divider"></li>
                            <li class="dropdown-item"><a href="#">Separated link</a></li>
                        </ul>
                    </div>
                </div>`;
    },
    init: function () {
        var me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        me.trim = eval(me.trim || el.attr('trim') || field.trim);
        me.length = eval(me.length || el.attr('length') || field.length);
        dom.html(me.getTpl())
            .find('input[type=file]').on('change', function () {
                let file = this.files[0];
                if (file) {
                    dom.find('.custom-file-label').text(file.name);
                    let reader = new FileReader();
                    // 将文件加载进入
                    reader.readAsDataURL(file);
                    reader.onload = function (e) {
                        me.data = { file_name: file.name, data: this.result };
                        // me.setValue(this.result);
                    }
                }
            });
    },
    onValueChange: function (handler) {
        let me = this;
        me.dom.find('input[type=file]').on('change', function () {
            handler(e, me);
        });
    },
    setReadonly: function (val) {

    },
    getValue: function () {
        var me = this;
        return me.data;
    },
    setValue: function (v) {
        console.log(v);
        let me = this;
        let dom = me.dom
        dom.find('.custom-file-label').text(v);
        if (v) {
            dom.find(".button_download").show()
            dom.find(".button_remove").show()
        }
    }
});