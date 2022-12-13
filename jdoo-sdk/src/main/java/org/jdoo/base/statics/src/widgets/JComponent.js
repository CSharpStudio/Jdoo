/** 组件的基类
 *  重写init方法开始
 */
jdoo.define("JComponent", {
    /**
     * 创建组件实例
     * 
     * @param {Object} opt 初始参数
     */
    new: function (opt) {
        let me = this;
        if(opt === undefined){
            opt = {};
        }
        let events = opt.on;
        delete opt.on;
        jdoo.utils.apply(true, me, opt);
        if (events) {
            for (let e in events) {
                let fn = events[e];
                if (typeof fn === 'function') {
                    me.on(e, fn);
                } else if (fn.selector) {
                    me.on(e, fn.selector, fn.fn);
                }
            }
        }
        me.init();
    },
    /**
     * 模板方法，定义组件功能的入口，子类重写此方法开始。
     */
    init: jdoo.emptyFn,
    /**
     * on (eventName, [fn], [{selector, fn}])
     * 注册事件到当前组件。
     * 
     * @example myComponent.on('click', this.onClick)
     * 
     * @returns this
     */
    on: function () {
        let me = this, dom = me.dom || $('body');
        dom.on(...arguments);
        return me;
    }
});
/**
 * 定义组件，默认继承自JComponent
 * 
 * @param {String} name 名称
 * @param {Object|Function} define 定义
 * @returns class对象
 */
jdoo.component = function (name, define) {
    if (typeof define === "function") {
        define = define();
    }
    define.extends = define.extends || 'JComponent';
    return jdoo.define(name, define);
}
/** 定义组件，并且绑定到$.fn
 * 
 * @example
 * $.component("go",{
 *      content: '',
 *      init: function(){
 *          let me = this;
 *          me.dom.html(me.content);
 *      }
 * });
 * $('div').go({
 *      content:'hello, click me',
 *      on: {
 *          click: function(event){
 *              console.log('component click')
 *          }
 *      }
 * });
 * 
 * @param {String} name 名称
 * @param {Object|Function} define 定义
 * @returns class对象
 */
$.component = function (name, define) {
    let clz = jdoo.component(name, define);
    $.fn[name] = function (opt) {
        return new clz($.extend({ dom: this }, opt));
    }
    return clz;
}