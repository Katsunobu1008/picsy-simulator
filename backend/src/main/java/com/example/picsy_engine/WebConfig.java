package com.example.picsy_engine;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS を全体適用したい場合（@CrossOrigin をクラスに付ける代替）。
 * ここは任意。どちらか一方で十分。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173","http://localhost:5174")
            .allowedMethods("GET","POST","PUT","DELETE","OPTIONS");
    }
}
