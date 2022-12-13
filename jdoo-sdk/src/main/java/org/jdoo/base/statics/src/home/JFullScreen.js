jdoo.component("JFullScreen", {
    selector: '[data-widget=full-screen]',
    changeEvents: 'webkitfullscreenchange mozfullscreenchange fullscreenchange MSFullscreenChange',
    enterText: '全屏'.t(),
    exitText: '退出全屏'.t(),
    init: function () {
        let me = this;
        $(document).on("click", me.selector, function () {
            me.toggle();
        });
        $(document).on(me.changeEvents, function () {
            me.toggleText();
        });
    },
    toggleText: function () {
        let me = this;
        if (document.fullscreenElement || document.mozFullScreenElement || document.webkitFullscreenElement || document.msFullscreenElement) {
            $(me.selector).html(me.exitText);
        } else {
            $(me.selector).html(me.enterText);
        }
    },
    toggle: function () {
        if (document.fullscreenElement || document.mozFullScreenElement || document.webkitFullscreenElement || document.msFullscreenElement) {
            this.windowed();
        } else {
            this.fullscreen();
        }
    },
    fullscreen: function () {
        if (document.documentElement.requestFullscreen) {
            document.documentElement.requestFullscreen();
        } else if (document.documentElement.webkitRequestFullscreen) {
            document.documentElement.webkitRequestFullscreen();
        } else if (document.documentElement.msRequestFullscreen) {
            document.documentElement.msRequestFullscreen();
        }
    },
    windowed: function () {
        if (document.exitFullscreen) {
            document.exitFullscreen();
        } else if (document.webkitExitFullscreen) {
            document.webkitExitFullscreen();
        } else if (document.msExitFullscreen) {
            document.msExitFullscreen();
        }
    }
});