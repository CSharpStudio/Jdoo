jdoo.component('test.a', {
    data: {
        x: 1,
        y: [1, 2, 3]
    },
    init: function () {
        console.log(this.data);
    },
    run: function () {
        console.log('a.run');
    },
    run2: function () {
        console.log('a.run2');
        this.run3();
    },
    run3: function () {
        console.log('a.run3');
    }
});

jdoo.component('test.b', {
    extends: 'test.a',
    arr: [1, 2],
    data: {
        x: 2,
        y: [4, 5]
    },
    init: function () {
        console.log('b.init');
        console.log(this.data);
        console.log(this.arr);
        this.run();
    },
    run: function () {
        console.log('b.run');
        this.run2();
    },
    run3: function () {
        console.log('b.run3');
        this.callSuper();
    }
});

jdoo.create('test.a', {
    data: {
        x: 5
    }
});

jdoo.create('test.a', {
    data: {
        y: [5, null, null]
    }
});

jdoo.create('test.b', {
    arr: [7],
    data: {
        x: 6
    }
});


jdoo.create('test.b', {
    run2: function () {
        console.log('opt.run2');
        this.callSuper();
    }
});