window.onload = function () {
    document.body.style.background = "#343a40";
    document.body.insertBefore(document.createElement('canvas'), document.body.firstChild);
    var canvas = document.querySelector('canvas'), w = window.innerWidth, h = window.innerHeight, ctx = canvas.getContext("2d"), rate = 60, arc = 100, time, count, size = 7, speed = 20, parts = new Array, colors = ["red", "#f57900", "yellow", "#ce5c00", "#5c3566"];
    var mouse = { x: 0, y: 0 }; canvas.style.position = 'fixed'; canvas.setAttribute("width", w); canvas.setAttribute("height", h);
    function create() {
        time = 0; count = 0;
        for (var a = 0; a < arc; a++) {
            parts[a] = { x: Math.ceil(Math.random() * w), y: Math.ceil(Math.random() * h), toX: Math.random() * 5 - 1, toY: Math.random() * 2 - 1, c: colors[Math.floor(Math.random() * colors.length)], size: Math.random() * size }
        }
    }
    function particles() {
        ctx.clearRect(0, 0, w, h); canvas.addEventListener("mousemove", MouseMove, false);
        for (var b = 0; b < arc; b++) {
            var c = parts[b]; var a = DistanceBetween(mouse, parts[b]); var a = Math.max(Math.min(15 - (a / 10), 10), 1); ctx.beginPath(); ctx.arc(c.x, c.y, c.size * a, 0, Math.PI * 2, false); ctx.fillStyle = c.c; ctx.strokeStyle = c.c;
            if (b % 2 == 0) { ctx.stroke() } else { ctx.fill() } c.x = c.x + c.toX * (time * 0.05); c.y = c.y + c.toY * (time * 0.05);
            if (c.x > w) { c.x = 0 } if (c.y > h) { c.y = 0 } if (c.x < 0) { c.x = w } if (c.y < 0) { c.y = h }
        }
        if (time < speed) { time++ } setTimeout(particles, 1000 / rate)
    }
    function MouseMove(a) { mouse.x = a.layerX; mouse.y = a.layerY }
    function DistanceBetween(c, d) { var a = d.x - c.x; var b = d.y - c.y; return Math.sqrt(a * a + b * b) }
    create();
    particles();
};