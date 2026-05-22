package com.rabbiter.ol.config;

import com.rabbiter.ol.tool.PathUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyWebMvnConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/file/videoFile/**").addResourceLocations("file:" + PathUtils.getClassLoadRootPath() + "/file/videoFile/");
        registry.addResourceHandler("/file/imageFile/**").addResourceLocations("file:" + PathUtils.getClassLoadRootPath() + "/file/imageFile/");
        registry.addResourceHandler("/file/resourceFile/**").addResourceLocations("file:" + PathUtils.getClassLoadRootPath() + "/file/resourceFile/");
        registry.addResourceHandler("/resource/**").addResourceLocations("file:" + PathUtils.getClassLoadRootPath() + "/resource/");
        // 课程结构化资源：项目根目录下的 courses/ 目录
        // PathUtils.getClassLoadRootPath() 指向 backend/，所以向上一级取 courses/
        registry.addResourceHandler("/courses/**").addResourceLocations("file:" + PathUtils.getClassLoadRootPath() + "/../courses/");
    }
}