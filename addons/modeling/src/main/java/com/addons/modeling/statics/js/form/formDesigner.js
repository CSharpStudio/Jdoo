var isDownControl=false;
var isCanBePlaced = false //是否可以放置

$.define('formBody',{
    extends: "JComponent",
    models: [],
    statics: {
        defaults: {
            mouseX:0, //鼠标X轴位置
            mouseY:0,//鼠标y轴位置
        }
    },
    init: function () {
        var me = this;opt = me.options;    
         //鼠标移动事件
         me.elem.on('mousemove',function(e){
            if (isDownControl) {
                console.log(000)
                mouseX = e.clientX //获取鼠标所在的x坐标
                mouseY = e.clientY //获取鼠标所在的y坐标
                //让鼠标在控件的中心位置
                var width = $('.controlNameMove').width() / 2
                var height = $('.controlNameMove').height() / 2
                $('.controlNameMove')
                    .css('top', mouseY - height + 'px')
                    .css('left', mouseX - width + 'px')
            }
        });
         //鼠标松开事件
         me.elem.on('mouseup', function (e) { 
            e.preventDefault()
            //松开了控件，不触发滑动事件里的效果
            isDownControl = false
           // var type = $(this).data('type')
            //处于放置的位置才能在中部区域增加控件
           // if (isCanBePlaced) {
                isCanBePlaced = false
                me.addControl()
              //  ApplyConfig()
            //}
        });
    },
    addControl:function(type){
        console.log('333');
        html='<div  style="grid-column:span 1;grid-row:span 1" class="drag-tool form-group col-12 el-row">'+
               '<div class="drag-l">'+
                 '<div class="drag-btn" style="cursor: move;">'+
                   '<i class="fc-icon fa fa-bell-slash"></i>'+
                 '</div>'+
               '</div>'+ 
               '<div class="drag-r">'+
                 '<div class="drag-btn">'+
                   '<i class="fc-icon fa fa-copy"></i>'+
                 '</div>'+ 
                '<div class="drag-btn drag-btn-danger">'+
                  '<i class="fc-icon  fa fa-trash"></i>'+
                '</div>'+
               '</div>'+ 
               '<div class="form-item">'+
                  '<label>作者 </label>'+
                  '<div data-field="author" name="author" data-toggle="tooltip" data-original-title="版权所有者">'+
                    '<input type="text" class="form-control" id="author-9">'+
                  '</div>'+
                  '<span class="invalid-feedback" style="display: none;"></span>'+
               '</div>'+
              '</div>'; 
              $('.grid').append(html);
              $('.dragControl').empty();
      }
})

//工具栏目
$.define("formToolItem", {
    extends: "JComponent",
    models: [],
    statics: {
        defaults: {
            mouseX:0, //鼠标的位置
            mouseY:0,//鼠标的位置
            isCanBePlaced:false,
            formLeft : 0 ,//form表单的坐标
            formTop : 0,
            formRight : 0,
            formBottom : 0,
            controlHtml:''
        }
    },
    controlList:{
        text: {
            name: '文本框', //控件名称
            icon: 'icon-duohangshurukuang', //控件图标
            datas: {
                name: { name: '控件名称', value: '文本框', type: 'text' },
                placeholder: { name: '提示语句', value: '', type: 'text' },
                value: { name: '默认内容', value: '', type: 'text' },
            },
            btns: {
                move: { class: 'icon-yidong controlMove' },
                delete: { class: 'icon-shanchu controlDelete' },
            },
            getHtml: function () {
                return '<input name="'+this.datas.id.value+'" type="text" class="form-control formVal">'
            },
        }, //文本框
    },
    init: function () {
        var me = this;opt = me.options;
        //鼠标按下事件
        me.elem.on('mousedown', function (e) {
            e.preventDefault()
            mouseX = e.clientX //获取鼠标所在的x坐标
            mouseY = e.clientY //获取鼠标所在的y坐标
            controlHtml = $(this).prop('outerHTML')
            var style = 'class="l-item controlNameMove" '
            style += 'style="width:' + $(this).width() + 'px;'
            style += 'position: absolute;'
            style += 'background: #fff;'
            style += 'z-index: 100;'
            style += 'top: ' + new Number(mouseY - $(this).height() / 2) + 'px;'
            style += 'left: ' + new Number(mouseX - $(this).width() / 2) + 'px;'
            style += '"'
            controlHtml = controlHtml.replace('class="l-item"', style)
            $('.dragControl').append(controlHtml)
            //获取form的坐标
            var LdgFormSubjectForm = $('.center-main')
            formLeft = $(LdgFormSubjectForm).offset().left
            formTop = $(LdgFormSubjectForm).offset().top
            formRight = opt.formLeft + LdgFormSubjectForm.width()
            formBottom = opt.formTop + LdgFormSubjectForm.height()
            //按下了控件,触发滑动事件里的效果
            isDownControl = true
        });
    },
});

$(function () {
    $(".form-body").formBody({});
    $(".l-item").formToolItem({});//组件
    
});