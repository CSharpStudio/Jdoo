jdoo.component("Connector", {
    getTpl: function () {
        return '<svg id="' + this.id + '" pointer-events="none" version="1.1" xmlns="http://www.w3.org/2000/svg" class="model-connector">\
                    <path id="path-'+ this.id + '" version="1.1" xmlns="http://www.w3.org/2000/svg" fill="none" stroke="#456" stroke-width="2"></path>\
                </svg>\
                <svg id="start-point-'+ this.id + '" width="16" height="16" pointer-events="none" version="1.1" xmlns="http://www.w3.org/2000/svg" class="connector-endpoint">\
                    <circle cx="8" cy="8" r="8" version="1.1" xmlns="http://www.w3.org/2000/svg" fill="#f76258" stroke="none"></circle>\
                </svg>\
                <svg id="end-point-'+ this.id + '" width="16" height="16" pointer-events="none" version="1.1" xmlns="http://www.w3.org/2000/svg" class="connector-endpoint">\
                    <circle cx="8" cy="8" r="8" version="1.1" xmlns="http://www.w3.org/2000/svg" fill="#f76258" stroke="none"></circle>\
                </svg>\
                <div id="start-note-'+ this.id + '" class="connector-note">' + (this.type === 'one2many' ? '1' : 'N') + '</div>\
                <div id="end-note-' + this.id + '" class="connector-note">' + (this.type === 'many2one' ? '1' : 'N') + '</div>';
    },
    init: function () {
        let me = this;
        me.id = me.id || jdoo.utils.randomId();
        me.canvas = $(me.renderTo);
        me.canvas.append(me.getTpl());
        me.svg = me.canvas.find('#' + me.id);
        me.path = me.canvas.find('#path-' + me.id);
        me.startPoint = me.canvas.find('#start-point-' + me.id);
        me.endPoint = me.canvas.find('#end-point-' + me.id);
        me.startNote = me.canvas.find('#start-note-' + me.id);
        me.endNote = me.canvas.find('#end-note-' + me.id);
        me._from = $(me.from);
        me._to = $(me.to);
        me.canvasParent = me.canvas.parent();
        me.update();
    },
    update: function () {
        let me = this, f = me._from.offset(), t = me._to.offset(), c = me.canvas.offset(),
            m = { x: (f.left - c.left), y: (f.top - c.top), h: me._from.height(), w: me._from.width() },
            n = { x: (t.left - c.left), y: (t.top - c.top), h: me._to.height(), w: me._to.width() };
        m.p = m.y + m.h / 2;
        n.p = n.y + n.h / 2;
        let d = {};
        if (m.p > n.p) {
            me.svg.css('top', (n.p - 10) + 'px');
            d.y1 = m.p - n.p + 10;
            d.y2 = 10;
        } else {
            me.svg.css('top', (m.p - 10) + 'px');
            d.y1 = 10;
            d.y2 = n.p - m.p + 10;
        }
        let delta = -Math.min(40, Math.floor(Math.abs(d.y1 - d.y2) / 10));
        if (Math.abs(m.x - n.x) < 50) {
            if (m.x < n.x) {
                me.svg.css('left', (m.x - 45) + 'px');
                me.svg.width(80);
                d.x1 = 30;
                d.x2 = n.x - m.x + 30;
            } else {
                me.svg.css('left', (n.x - 45) + 'px');
                me.svg.width(80);
                d.x1 = m.x - n.x + 30;
                d.x2 = 30;
            }
            me.startPoint.css("left", (m.x - 20) + 'px');
            me.endPoint.css("left", (n.x - 20) + 'px');
            me.startNote.css("left", (m.x - 20) + 'px');
            me.endNote.css("left", (n.x - 20) + 'px');
        } else if (m.x + m.w < n.x) {
            me.svg.css('left', (m.x + m.w - 40) + 'px');
            me.svg.width(n.x - m.x - m.w + 80);
            me.startPoint.css("left", (m.x + m.w + 5) + 'px');
            me.endPoint.css("left", (n.x - 20) + 'px');
            me.startNote.css("left", (m.x + m.w + 5) + 'px');
            me.endNote.css("left", (n.x - 20) + 'px');
            d.x1 = 55;
            d.x2 = n.x - m.x - m.w + 25;
            if (d.y1 < d.y2) {
                delta *= -1;
            }
        } else if (n.x + n.w < m.x) {
            me.svg.css('left', (n.x + n.w - 40) + 'px');
            me.svg.width(m.x - n.x - n.w + 80);
            me.startPoint.css("left", (m.x - 20) + 'px');
            me.endPoint.css("left", (n.x + n.w + 5) + 'px');
            me.startNote.css("left", (m.x - 20) + 'px');
            me.endNote.css("left", (n.x + n.w + 5) + 'px');
            d.x1 = m.x - n.x - n.w + 25;
            d.x2 = 55;
            if (d.y1 > d.y2) {
                delta *= -1;
            }
        } else if (m.x < n.x) {
            me.svg.css('left', (n.x - 40) + 'px');
            me.svg.width(m.x + m.w - n.x + 80);
            me.startPoint.css("left", (m.x + m.w + 5) + 'px');
            me.endPoint.css("left", (n.x - 20) + 'px');
            me.startNote.css("left", (m.x + m.w + 5) + 'px');
            me.endNote.css("left", (n.x - 20) + 'px');
            d.x1 = m.x + m.w - n.x + 55;
            d.x2 = 25;
        } else {
            me.svg.css('left', (m.x - 40) + 'px');
            me.svg.width(n.x + n.w - m.x + 80);
            me.startPoint.css("left", (m.x - 20) + 'px');
            me.endPoint.css("left", (n.x + n.w + 5) + 'px');
            me.startNote.css("left", (m.x - 20) + 'px');
            me.endNote.css("left", (n.x + n.w + 5) + 'px');
            d.x2 = n.x + n.w - m.x + 55;
            d.x1 = 25;
        }
        me.startPoint.css("top", (m.y + m.h / 2 - 8) + 'px');
        me.endPoint.css("top", (n.y + n.h / 2 - 8) + 'px');
        me.startNote.css("top", (m.y + m.h / 2 - 9.5) + 'px');
        me.endNote.css("top", (n.y + n.h / 2 - 9.5) + 'px');
        me.svg.height(Math.abs((m.y + m.h / 2) - (n.y + n.h / 2)) + 20);
        let cx = (d.x1 + d.x2) / 2 + delta;
        let cy = (d.y1 + d.y2) / 2 + delta;
        me.path.attr('d', 'M ' + d.x1 + ' ' + d.y1 + ' C ' + cx + ' ' + cy + ' ' + cx + ' ' + cy + ' ' + d.x2 + ' ' + d.y2);
    }
});