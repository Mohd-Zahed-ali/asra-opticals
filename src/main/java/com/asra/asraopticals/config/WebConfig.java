package com.asra.asraopticals.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded product images from /uploads/ URL path
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}