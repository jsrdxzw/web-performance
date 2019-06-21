const path = require('path');
const webpack = require('webpack');
const {CleanWebpackPlugin} = require('clean-webpack-plugin');

module.exports = {
    mode: 'production',
    entry: path.join(__dirname, 'demo/src/index.js'),
    output: {
        filename: 'main.js',
        path: path.join(__dirname, 'demo/build')
    },
    plugins: [
        new CleanWebpackPlugin()
    ]
};

