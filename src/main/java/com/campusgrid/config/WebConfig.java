package com.campusgrid.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/login.html");
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/admin/users").setViewName("forward:/admin-users.html");
        registry.addViewController("/admin/dashboard").setViewName("forward:/admin-users.html");
        registry.addViewController("/admin/campus").setViewName("forward:/admin-campus.html");
        registry.addViewController("/admin/rooms").setViewName("forward:/admin-rooms.html");
        registry.addViewController("/admin/seating").setViewName("forward:/admin-seating.html");
        registry.addViewController("/faculty/dashboard").setViewName("forward:/faculty-dashboard.html");
        registry.addViewController("/student/dashboard").setViewName("forward:/student-dashboard.html");
        registry.addViewController("/student/timetable").setViewName("forward:/student-timetable.html");
        registry.addViewController("/student/examinations").setViewName("forward:/student-examinations.html");
        registry.addViewController("/student/campus").setViewName("forward:/student-campus.html");
        registry.addViewController("/student/profile").setViewName("forward:/student-profile.html");
    }
}
