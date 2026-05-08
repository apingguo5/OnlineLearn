/*
 * @Description: 
 * @Author: Rabbiter
 * @Date: 2024-04-15 23:20:35
 */
module.exports = {
    lintOnSave: false, // 关闭eslint校验
    devServer: {
        port: 9252,
        overlay: { // 关闭eslint校验
            warning: false,
            errors: false
        },
        proxy: {
            '/api': {
                target: 'http://localhost:9251/',
                changOrigin: true,
                pathRewrite: {
                     '^/api': ''
                }
            },
            '/study': {
                target: 'http://localhost:9251/',
                changOrigin: true
            },
        },
    }
};


