package com.campusgrid;

import com.campusgrid.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CampusGridApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusGridApplication.class, args);
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "campusgrid.seeding.enabled", havingValue = "true", matchIfMissing = true)
    public CommandLineRunner initDatabase(
            org.springframework.beans.factory.ObjectProvider<UserService> userServiceProvider,
            org.springframework.beans.factory.ObjectProvider<com.campusgrid.service.SpaceService> spaceServiceProvider,
            org.springframework.beans.factory.ObjectProvider<com.campusgrid.service.TimetableService> timetableServiceProvider,
            org.springframework.beans.factory.ObjectProvider<com.campusgrid.service.ExamService> examServiceProvider
    ) {
        return args -> {
            try {
                UserService userService = userServiceProvider.getIfAvailable();
                if (userService != null) {
                    userService.seedInitialUsers();
                }
                com.campusgrid.service.SpaceService spaceService = spaceServiceProvider.getIfAvailable();
                if (spaceService != null) {
                    spaceService.seedInitialSpaces();
                }
                com.campusgrid.service.TimetableService timetableService = timetableServiceProvider.getIfAvailable();
                if (timetableService != null) {
                    timetableService.seedInitialSubjectsAndTimetable();
                }
                com.campusgrid.service.ExamService examService = examServiceProvider.getIfAvailable();
                if (examService != null) {
                    examService.seedInitialExams();
                }
                System.out.println("=================================================");
                System.out.println(" CampusGrid Platform Initialized Successfully! ");
                System.out.println(" Default Admin:   admin          / Admin@123    ");
                System.out.println(" Default Faculty: meera.faculty  / Faculty@123  ");
                System.out.println(" Default Student: rahul123       / Student@123  ");
                System.out.println(" Web Portal:      http://localhost:8080/login   ");
                System.out.println("=================================================");
            } catch (Exception e) {
                System.err.println("Database seeding notice: " + e.getMessage());
            }
        };
    }
}
