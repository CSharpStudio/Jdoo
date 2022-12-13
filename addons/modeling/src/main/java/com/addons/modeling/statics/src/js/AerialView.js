$.component('AerialView', {
  width: 160,
  minimize: false,
  getTpl: function () {
    return '<span class="fas fa-angle-up minimize" role="button"></span><div class="viewport"></div><div class="thumbnails"></div>';
  },
  init: function () {
    let me = this, dom = me.dom, canvas = me.diagram.canvas, container = canvas.parent();
    dom.append(me.getTpl());
    me.viewport = dom.find('.viewport');
    me.thumbnail = dom.find('.thumbnails');
    let dragging = false, scroll = function (ui) {
      dragging = true;
      let zoom = me.diagram.zoom / 100,
        canvasWidth = canvas.width() * zoom,
        parentWidth = container.width(),
        rate = canvasWidth > parentWidth ? canvasWidth / me.width : parentWidth / me.width;
      container.scrollLeft(ui.position.left * rate);
      container.scrollTop(ui.position.top * rate);
      dragging = false;
    };
    me.viewport.draggable({
      containment: 'parent',
      drag: function (e, ui) {
        scroll(ui);
      },
      stop: function (e, ui) {
        scroll(ui);
      }
    });
    dom.on('click', '.minimize', function () {
      let m = $(this);
      if (me.minimize) {
        m.removeClass('fa-angle-down').addClass('fa-angle-up');
        dom.find('.viewport,.thumbnails').show();
        me.minimize = false;
        me.updateView();
      } else {
        m.removeClass('fa-angle-up').addClass('fa-angle-down');
        dom.find('.viewport,.thumbnails').hide();
        dom.width(20).height(20);
        me.minimize = true;
      }
    });
    $(window).on('resize', function () {
      me.updateView();
    });
    me.diagram.onZoom(function () {
      me.updateView();
    });
    container.scroll(function () {
      if (!dragging && !me.minimize) {
        let zoom = me.diagram.zoom / 100,
          canvasWidth = canvas.width() * zoom,
          parentWidth = container.width(),
          rate = canvasWidth > parentWidth ? me.width / canvasWidth : me.width / parentWidth;
        me.viewport.css('top', container.scrollTop() * rate).css('left', container.scrollLeft() * rate);
      }
    });
    if (me.minimize) {
      m.removeClass('fa-angle-up').addClass('fa-angle-down');
      dom.find('.viewport,.thumbnails').hide();
      dom.width(20).height(20);
    } else {
      me.updateView();
    }
  },
  updateView: function () {
    let me = this;
    if (me.minimize) {
      return;
    }
    let dom = me.dom, canvas = me.diagram.canvas, canvasContainer = canvas.parent(),
      zoom = me.diagram.zoom / 100,
      canvasWidth = canvas.width() * zoom,
      canvasHeight = canvas.height() * zoom,
      parentWidth = canvasContainer.width(),
      parentHeight = canvasContainer.height(),
      rate = canvasWidth > parentWidth ? me.width / canvasWidth : me.width / parentWidth,
      viewWidth = canvasWidth > parentWidth ? rate * parentWidth : me.width,
      viewHeight = canvasHeight > parentHeight ? viewWidth * parentHeight / parentWidth : rate * canvasHeight;
    dom.width(me.width + 2);
    dom.height(rate * canvasHeight + 2);
    me.viewport.height(viewHeight).width(viewWidth).css('top', canvasContainer.scrollTop() * rate).css('left', canvasContainer.scrollLeft() * rate);
    if (canvasWidth > parentWidth) {
      me.thumbnail.css('zoom', me.width * 100 / canvas.width() + '%');
    } else {
      me.thumbnail.css('zoom', me.width * 100 / canvas.width() * canvasWidth / parentWidth + '%');
    }
  },
  load: function () {
    let me = this;
    me.thumbnail.empty();
    me.diagram.models.each(function () {
      me.add(this.id);
    });
  },
  update: function (id) {
    let me = this, shape = me.diagram.canvas.find('#' + id);
    me.thumbnail.find('[data-id=' + id + ']').height(shape.height()).width(shape.width()).css('top', shape.css('top')).css('left', shape.css('left'));
  },
  add: function (id) {
    let me = this, shape = me.diagram.canvas.find('#' + id);
    me.thumbnail.append('<div class="model-thumbnail" data-id="' + id + '" style="height:' + shape.height() + 'px;width:' + shape.width() + 'px;top:' + shape.css('top') + ';left:' + shape.css('left') + ';"></div>');
  },
  remove: function (id) {
    this.thumbnail.find('[data-id=' + id + ']').remove();
  }
});