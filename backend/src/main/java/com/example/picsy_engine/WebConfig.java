package com.example.picsy_engine;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // ← 追加

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override public void addCorsMappings(@NonNull CorsRegistry registry){ // ← 追加
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173","http://localhost:5174")
            .allowedMethods("GET","POST","PUT","DELETE","OPTIONS");
    }
}
