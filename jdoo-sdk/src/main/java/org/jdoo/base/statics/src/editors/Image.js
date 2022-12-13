jdoo.editor('image', {
    size: 10, //大小
    width: 150,
    type: ['image/png', 'image/jpeg', 'image/gif'],//上传的类型 
    compress: false,//是否开启压缩
    quality: 1,//压缩系数0-1之间,大于1默认为1
    placeholder: '/res/org/jdoo/base/statics/img/placeholder.png',
    getTpl: function () {
        let me = this;
        return `<div class="image-edit" id="${me.name + '-' + jdoo.nextId()}">
                    <ul class="content-img">
                        <li style="width:${me.width}px"><img src="${me.src}" alt="">
                            <div class="hide tbar">
                                <a class="delete-btn"><span class="fa fa-trash"></span></a>
                                <a class="edit-btn"><span class="fa fa-pencil-alt"></span></a>
                            </div>
                        </li>
                    </ul>
                    <input type="file" style="display:none" name="file" accept="image/*" />
                </div>`;
    },
    init: function () {
        let me = this, dom = me.dom, field = me.field;
        me.name = me.name || dom.attr('data-field') || field.name;
        me.size = eval(dom.attr('size') || me.size);
        me.type = eval(dom.attr('type') || me.type);
        me.compress = eval(dom.attr('compress') || me.compress);
        me.quality = eval(dom.attr('quality') || me.quality);
        me.width = eval(dom.attr('width') || me.width);
        me.length = eval(dom.attr('length') || me.field.length);
        me.src = me.placeholder;
        dom.html(me.getTpl())
            .on('mouseover', '.content-img', function () {
                if (!dom.hasClass('readonly')) {
                    dom.find('.tbar').removeClass('hide');
                }
            }).on('mouseleave', '.content-img', function () {
                dom.find('.tbar').addClass('hide');
            }).on("click", '.delete-btn', function () {
                me.setValue(null);
                dom.triggerHandler("valueChange", [me]);
            }).on("click", '.edit-btn', function () {
                dom.find('input[type=file]').click();
            }).on('change', 'input', function () {
                let file = this.files[0];
                if (file) {
                    let imgSize = file.size;
                    if (imgSize > 1024 * 500 * me.size) { //1M
                        let error = "上传图片不能超过".t() + me.size + "M";
                        me.elem.next('.invalid-feedback').html(error).show();
                        return;
                    };
                    if (me.type.indexOf(file.type) == -1) {
                        let error = "图片上传格式不正确,必须是png,jpeg,gif".t();
                        me.elem.next('.invalid-feedback').html(error).show();
                        return;
                    }
                    let reader = new FileReader();
                    reader.readAsDataURL(file);
                    let image = new Image();
                    reader.onload = function (e) {
                        let data = e.target.result;
                        //图片压缩
                        if (me.compress) {
                            image.src = e.target.result;
                            image.onload = function () {
                                let canvas = document.createElement("canvas");
                                let ctx = canvas.getContext("2d");
                                let imgWidth = image.width;
                                let imgHeight = image.height;
                                if (Math.max(imgWidth, imgHeight) > me.width) {
                                    if (imgWidth > imgHeight) {
                                        canvas.width = me.width;
                                        canvas.height = me.width * imgHeight / imgWidth;
                                    } else {
                                        canvas.height = me.width;
                                        canvas.width = me.width * imgWidth / imgHeight;
                                    }
                                } else {
                                    canvas.width = imgWidth;
                                    canvas.height = imgHeight;
                                }
                                ctx.clearRect(0, 0, canvas.width, canvas.height);
                                ctx.drawImage(this, 0, 0, canvas.width, canvas.height);
                                data = canvas.toDataURL("image/jpeg", quality);
                            }
                        }
                        me.setValue(data);
                        dom.find('input').val('');
                        dom.triggerHandler("valueChange", [me]);
                    };
                }
            });
    },
    onValueChange: function (handler) {
        this.dom.on('valueChange', handler);
    },
    setReadonly: function (v) {
        let me = this;
        if (v) {
            me.dom.addClass('readonly');
        } else {
            me.dom.removeClass('readonly');
        }
        me.updateImg();
    },
    getValue: function () {
        let me = this, d = me.data, i = (d || '').indexOf("base64,");
        if (d && i > -1) {
            return d.substring(i + 7);
        }
        return d;
    },
    setValue: function (v) {
        let me = this;
        if (v && !v.startsWith('data:image')) {
            v = 'data:image/png;base64,' + v;
        }
        me.data = v;
        me.updateImg();
    },
    updateImg: function () {
        let me = this, dom = me.dom, src = me.data || me.placeholder;
        dom.find('img').attr('src', src);
        if (me.data) {
            dom.find('.delete-btn').show();
        } else {
            dom.find('.delete-btn').hide();
        }
    }
});