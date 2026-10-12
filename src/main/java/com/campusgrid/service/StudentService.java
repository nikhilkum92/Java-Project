package com.campusgrid.service;

import com.campusgrid.dto.*;
import com.campusgrid.model.Role;
import com.campusgrid.model.Student;
import com.campusgrid.model.User;
import com.campusgrid.repository.StudentRepository;
import com.campusgrid.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public StudentService(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    public Student getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student academic profile not found for user ID: " + userId));
    }
}
