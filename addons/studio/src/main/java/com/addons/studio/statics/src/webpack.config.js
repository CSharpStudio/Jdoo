const path = require('path');

module.exports = {
    mode: 'production',
    entry: './studio.js',
    output: {
        filename: 'studio.js',
        path: path.resolve(__dirname, '../js'),
    },
};

//npx webpack --config webpack.config.js