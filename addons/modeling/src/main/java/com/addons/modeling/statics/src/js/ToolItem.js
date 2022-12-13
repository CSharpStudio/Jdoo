
$.component("ToolItem", {
    init: function () {
        let me = this;
        me.dom.draggable({
            scroll: false,
            opacity: 0.8,
            zIndex: 999,
            cursor: "cell",
            containment: me.containment || 'window',
            cursorAt: { top: 0, left: 0 },
            helper: function (e) {
                return $(e.currentTarget).clone(true).css({
                    zIndex: "99999",
                    'background-color': "gray",
                    position: 'absolute',
                    'border-radius': 0
                });
            },
            start: function (e) {
            },
            drag: function (e) {
            },
            stop: function (e) {
                let canvas = me.diagram.canvas.offset(), zoom = 100 / me.diagram.zoom;
                let point = { x: e.pageX * zoom - canvas.left, y: e.pageY * zoom - canvas.top };
                me.diagram.newModel(point);
            }
        });
    }
});