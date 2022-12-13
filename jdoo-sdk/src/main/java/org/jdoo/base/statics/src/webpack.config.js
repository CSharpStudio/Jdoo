const path = require('path');

module.exports = {
    mode: 'production',
    entry: {
      jdoo: './jdoo.js',
      home: './home.js',
      view: './view.js',
    },
    output: {
      filename: '[name].js',
      path: path.resolve(__dirname, '../js'),
    },
};

//npx webpack --config webpack.config.js