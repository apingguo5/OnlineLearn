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
        proxy: {  //配置跨域
            '/api': {
                target: 'http://localhost:9251/',  //这里后台的地址模拟的;应该填写你们真实的后台接口
                changOrigin: true,  //允许跨域
                pathRewrite: {
                     '^/api': ''  
                }
            },
        },
    }
};


