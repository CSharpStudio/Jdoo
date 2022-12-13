const path = require('path');

module.exports = {
    mode: 'production',
    entry: './model-designer.js',
    output: {
        filename: 'model-designer.js',
        path: path.resolve(__dirname, '../js'),
    },
};

//npx webpack --config webpack.config.js