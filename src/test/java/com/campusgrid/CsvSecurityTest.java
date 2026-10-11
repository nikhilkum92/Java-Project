package com.campusgrid;

import com.campusgrid.dto.UserTableDto;
import com.campusgrid.model.Role;
import com.campusgrid.repository.FacultyRepository;
import com.campusgrid.repository.StudentRepository;
import com.campusgrid.repository.UserRepository;
import com.campusgrid.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CsvSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should neutralize CSV formula injection characters (=, +, -, @)")
    void testSanitizeFormulaInjectionCharacters() {
        assertEquals("'=SUM(A1:A10)", UserService.sanitizeCsvCell("=SUM(A1:A10)"));
        assertEquals("'+cmd|' /C calc'!A0", UserService.sanitizeCsvCell("+cmd|' /C calc'!A0"));
        assertEquals("'-100+200", UserService.sanitizeCsvCell("-100+200"));
        assertEquals("'@SUM(1,2)", UserService.sanitizeCsvCell("@SUM(1,2)"));
    }

    @Test
    @DisplayName("Should preserve normal text without modifying prefix")
    void testPreserveNormalText() {
        assertEquals("John Doe", UserService.sanitizeCsvCell("John Doe"));
        assertEquals("Computer Science", UserService.sanitizeCsvCell("Computer Science"));
        assertEquals("CSE-A", UserService.sanitizeCsvCell("CSE-A"));
    }

    @Test
    @DisplayName("Should handle null and empty values gracefully")
    void testHandleNullAndEmpty() {
        assertEquals("", UserService.sanitizeCsvCell(null));
        assertEquals("", UserService.sanitizeCsvCell(""));
        assertEquals("", UserService.sanitizeCsvCell("   "));
    }

    @Test
    @DisplayName("Should escape double quotes according to RFC 4180")
    void testEscapeDoubleQuotes() {
        assertEquals("John \"\"The Legend\"\" Doe", UserService.sanitizeCsvCell("John \"The Legend\" Doe"));
    }
}
