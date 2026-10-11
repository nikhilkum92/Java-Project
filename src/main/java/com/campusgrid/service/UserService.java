package com.campusgrid.service;

import com.campusgrid.dto.CreateUserRequest;
import com.campusgrid.dto.UserTableDto;
import com.campusgrid.model.Faculty;
import com.campusgrid.model.Role;
import com.campusgrid.model.Student;
import com.campusgrid.model.User;
import com.campusgrid.repository.FacultyRepository;
import com.campusgrid.repository.StudentRepository;
import com.campusgrid.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public UserService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            FacultyRepository facultyRepository
    ) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    public List<UserTableDto> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        List<UserTableDto> dtoList = new ArrayList<>();

        for (Student s : students) {
            User u = s.getUser();
            UserTableDto dto = new UserTableDto();
            dto.setId(u.getId());
            dto.setUid(u.getUid());
            dto.setUsername(u.getUsername());
            dto.setEmail(u.getEmail());
            dto.setFullName(u.getFullName());
            dto.setRole(u.getRole());
            dto.setRollNumber(s.getRollNumber());
            dto.setDepartment(s.getDepartment());
            dto.setSectionName(s.getSectionName());
            dto.setYear(s.getYear());
            dto.setSemester(s.getCurrentSemester());
            dto.setPhoneNumber(s.getPhoneNumber());
            dto.setActive(u.isActive());
            dto.setCreatedAt(u.getCreatedAt());
            dtoList.add(dto);
        }
        return dtoList;
    }

    public List<UserTableDto> getAllFaculty() {
        List<Faculty> facultyList = facultyRepository.findAll();
        List<UserTableDto> dtoList = new ArrayList<>();

        for (Faculty f : facultyList) {
            User u = f.getUser();
            UserTableDto dto = new UserTableDto();
            dto.setId(u.getId());
            dto.setUid(u.getUid());
            dto.setUsername(u.getUsername());
            dto.setEmail(u.getEmail());
            dto.setFullName(u.getFullName());
            dto.setRole(u.getRole());
            dto.setEmployeeId(f.getEmployeeId());
            dto.setDepartment(f.getDepartment());
            dto.setDesignation(f.getDesignation());
            dto.setPhoneNumber(f.getPhoneNumber());
            dto.setActive(u.isActive());
            dto.setCreatedAt(u.getCreatedAt());
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Transactional
    public UserTableDto createUser(CreateUserRequest req) {
        // Validation checks
        if (userRepository.existsByUsername(req.getUsername().trim())) {
            throw new IllegalArgumentException("Username '" + req.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(req.getEmail().trim())) {
            throw new IllegalArgumentException("Email '" + req.getEmail() + "' is already registered.");
        }

        // Create core User
        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setEmail(req.getEmail().trim().toLowerCase());
        
        String initialPassword = req.getPassword();
        if (initialPassword == null || initialPassword.isBlank()) {
            initialPassword = (req.getRole() == Role.STUDENT ? "Stu@" : "Fac@") + 
                    java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase() + "9!";
        }
        user.setPasswordHash(passwordEncoder.encode(initialPassword));
        user.setFullName(req.getFullName().trim());
        user.setRole(req.getRole());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        UserTableDto resDto = new UserTableDto();
        resDto.setId(savedUser.getId());
        resDto.setUid(savedUser.getUid());
        resDto.setUsername(savedUser.getUsername());
        resDto.setEmail(savedUser.getEmail());
        resDto.setFullName(savedUser.getFullName());
        resDto.setRole(savedUser.getRole());
        resDto.setActive(true);
        resDto.setCreatedAt(savedUser.getCreatedAt());

        if (req.getRole() == Role.STUDENT) {
            if (req.getRollNumber() == null || req.getRollNumber().isBlank()) {
                throw new IllegalArgumentException("Roll number is required for students.");
            }
            if (studentRepository.existsByRollNumber(req.getRollNumber().trim())) {
                throw new IllegalArgumentException("Roll number '" + req.getRollNumber() + "' is already registered.");
            }

            Student student = new Student();
            student.setUser(savedUser);
            student.setRollNumber(req.getRollNumber().trim().toUpperCase());
            student.setDepartment(req.getDepartment() != null ? req.getDepartment() : "Computer Science");
            student.setSectionName(req.getSectionName() != null ? req.getSectionName() : "CSE-A");
            student.setYear(req.getYear() > 0 ? req.getYear() : 2);
            student.setCurrentSemester(req.getSemester() > 0 ? req.getSemester() : 3);
            student.setPhoneNumber(req.getPhoneNumber());

            Student savedStudent = studentRepository.save(student);

            resDto.setRollNumber(savedStudent.getRollNumber());
            resDto.setDepartment(savedStudent.getDepartment());
            resDto.setSectionName(savedStudent.getSectionName());
            resDto.setYear(savedStudent.getYear());
            resDto.setSemester(savedStudent.getCurrentSemester());
        } else if (req.getRole() == Role.FACULTY) {
            if (req.getEmployeeId() == null || req.getEmployeeId().isBlank()) {
                throw new IllegalArgumentException("Employee ID is required for faculty.");
            }
            if (facultyRepository.existsByEmployeeId(req.getEmployeeId().trim())) {
                throw new IllegalArgumentException("Employee ID '" + req.getEmployeeId() + "' is already registered.");
            }

            Faculty faculty = new Faculty();
            faculty.setUser(savedUser);
            faculty.setEmployeeId(req.getEmployeeId().trim().toUpperCase());
            faculty.setDepartment(req.getDepartment() != null ? req.getDepartment() : "Computer Science");
            faculty.setDesignation(req.getDesignation() != null ? req.getDesignation() : "Assistant Professor");
            faculty.setPhoneNumber(req.getPhoneNumber());
            faculty.setOfficeRoom(req.getOfficeRoom());

            Faculty savedFaculty = facultyRepository.save(faculty);

            resDto.setEmployeeId(savedFaculty.getEmployeeId());
            resDto.setDepartment(savedFaculty.getDepartment());
            resDto.setDesignation(savedFaculty.getDesignation());
        }

        return resDto;
    }

    @Transactional
    public boolean toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Prevent deactivating primary bootstrap admin
        if (user.getUsername().equalsIgnoreCase("admin") || (user.getId() != null && user.getId() == 1L)) {
            throw new IllegalStateException("The primary administrator account cannot be deactivated.");
        }

        user.setActive(!user.isActive());
        userRepository.save(user);
        return user.isActive();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Prevent deleting primary bootstrap admin
        if (user.getUsername().equalsIgnoreCase("admin") || (user.getId() != null && user.getId() == 1L)) {
            throw new IllegalStateException("The primary administrator account cannot be deleted.");
        }

        // Delete associated role-specific records first to maintain relational integrity
        if (user.getRole() == Role.STUDENT) {
            studentRepository.findByUserId(userId).ifPresent(studentRepository::delete);
        } else if (user.getRole() == Role.FACULTY) {
            facultyRepository.findByUserId(userId).ifPresent(facultyRepository::delete);
        }

        userRepository.delete(user);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserTableDto updateUser(Long userId, com.campusgrid.dto.UpdateUserRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Check email uniqueness if changed
        if (!user.getEmail().equalsIgnoreCase(req.getEmail().trim())) {
            if (userRepository.existsByEmail(req.getEmail().trim())) {
                throw new IllegalArgumentException("Email '" + req.getEmail() + "' is already in use.");
            }
            user.setEmail(req.getEmail().trim().toLowerCase());
        }

        user.setFullName(req.getFullName().trim());
        User savedUser = userRepository.save(user);

        UserTableDto resDto = new UserTableDto();
        resDto.setId(savedUser.getId());
        resDto.setUid(savedUser.getUid());
        resDto.setUsername(savedUser.getUsername());
        resDto.setEmail(savedUser.getEmail());
        resDto.setFullName(savedUser.getFullName());
        resDto.setRole(savedUser.getRole());
        resDto.setActive(savedUser.isActive());
        resDto.setCreatedAt(savedUser.getCreatedAt());

        if (savedUser.getRole() == Role.STUDENT) {
            Student student = studentRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Student s = new Student();
                        s.setUser(savedUser);
                        return s;
                    });

            if (req.getRollNumber() != null && !req.getRollNumber().isBlank()) {
                String newRoll = req.getRollNumber().trim().toUpperCase();
                if (!newRoll.equalsIgnoreCase(student.getRollNumber()) && studentRepository.existsByRollNumber(newRoll)) {
                    throw new IllegalArgumentException("Roll number '" + newRoll + "' is already registered.");
                }
                student.setRollNumber(newRoll);
            }

            if (req.getDepartment() != null) student.setDepartment(req.getDepartment().trim());
            if (req.getSectionName() != null) student.setSectionName(req.getSectionName().trim());
            if (req.getYear() > 0) student.setYear(req.getYear());
            if (req.getSemester() > 0) student.setCurrentSemester(req.getSemester());
            if (req.getPhoneNumber() != null) student.setPhoneNumber(req.getPhoneNumber().trim());

            Student savedStudent = studentRepository.save(student);
            resDto.setRollNumber(savedStudent.getRollNumber());
            resDto.setDepartment(savedStudent.getDepartment());
            resDto.setSectionName(savedStudent.getSectionName());
            resDto.setYear(savedStudent.getYear());
            resDto.setSemester(savedStudent.getCurrentSemester());
            resDto.setPhoneNumber(savedStudent.getPhoneNumber());
        } else if (savedUser.getRole() == Role.FACULTY) {
            Faculty faculty = facultyRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Faculty f = new Faculty();
                        f.setUser(savedUser);
                        return f;
                    });

            if (req.getEmployeeId() != null && !req.getEmployeeId().isBlank()) {
                String newEmp = req.getEmployeeId().trim().toUpperCase();
                if (!newEmp.equalsIgnoreCase(faculty.getEmployeeId()) && facultyRepository.existsByEmployeeId(newEmp)) {
                    throw new IllegalArgumentException("Employee ID '" + newEmp + "' is already registered.");
                }
                faculty.setEmployeeId(newEmp);
            }

            if (req.getDepartment() != null) faculty.setDepartment(req.getDepartment().trim());
            if (req.getDesignation() != null) faculty.setDesignation(req.getDesignation().trim());
            if (req.getOfficeRoom() != null) faculty.setOfficeRoom(req.getOfficeRoom().trim());
            if (req.getPhoneNumber() != null) faculty.setPhoneNumber(req.getPhoneNumber().trim());

            Faculty savedFaculty = facultyRepository.save(faculty);
            resDto.setEmployeeId(savedFaculty.getEmployeeId());
            resDto.setDepartment(savedFaculty.getDepartment());
            resDto.setDesignation(savedFaculty.getDesignation());
            resDto.setPhoneNumber(savedFaculty.getPhoneNumber());
        }

        return resDto;
    }

    public List<String> getDistinctDepartments() {
        java.util.Set<String> set = new java.util.TreeSet<>();
        set.addAll(studentRepository.findDistinctDepartments());
        set.addAll(facultyRepository.findDistinctDepartments());
        if (set.isEmpty()) {
            set.add("Computer Science");
            set.add("Electronics & Communication");
            set.add("Mechanical Engineering");
            set.add("Civil Engineering");
        }
        return new ArrayList<>(set);
    }

    public List<String> getDistinctSections() {
        List<String> sections = studentRepository.findDistinctSectionNames();
        if (sections.isEmpty()) {
            return List.of("CSE-A", "CSE-B", "CSE-C");
        }
        return sections;
    }

    public com.campusgrid.dto.UserStatsDto getUserStats() {
        long totalStudents = studentRepository.count();
        long activeStudents = studentRepository.findAll().stream().filter(s -> s.getUser().isActive()).count();
        long totalFaculty = facultyRepository.count();
        long activeFaculty = facultyRepository.findAll().stream().filter(f -> f.getUser().isActive()).count();
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(User::isActive).count();
        long totalDepartments = getDistinctDepartments().size();
        long totalSections = getDistinctSections().size();

        return new com.campusgrid.dto.UserStatsDto(
                totalStudents, activeStudents,
                totalFaculty, activeFaculty,
                totalUsers, activeUsers,
                totalDepartments, totalSections
        );
    }

    public static String sanitizeCsvCell(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        // Neutralize CSV Formula Injection (=, +, -, @, \t, \r) by prepending a single quote
        if (!trimmed.isEmpty()) {
            char first = trimmed.charAt(0);
            if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
                trimmed = "'" + trimmed;
            }
        }
        // RFC 4180 double-quote escaping
        return trimmed.replace("\"", "\"\"");
    }

    public String exportUsersCsv(String type) {
        StringBuilder sb = new StringBuilder();
        if ("faculty".equalsIgnoreCase(type)) {
            sb.append("ID,Employee ID,Full Name,Email,Department,Designation,Phone,Status\n");
            for (UserTableDto f : getAllFaculty()) {
                sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        sanitizeCsvCell(f.getUid()),
                        sanitizeCsvCell(f.getEmployeeId()),
                        sanitizeCsvCell(f.getFullName()),
                        sanitizeCsvCell(f.getEmail()),
                        sanitizeCsvCell(f.getDepartment()),
                        sanitizeCsvCell(f.getDesignation()),
                        sanitizeCsvCell(f.getPhoneNumber()),
                        f.isActive() ? "ACTIVE" : "INACTIVE"
                ));
            }
        } else {
            sb.append("ID,Roll Number,Full Name,Email,Department,Section,Year,Semester,Phone,Status\n");
            for (UserTableDto s : getAllStudents()) {
                sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",\"%s\"\n",
                        sanitizeCsvCell(s.getUid()),
                        sanitizeCsvCell(s.getRollNumber()),
                        sanitizeCsvCell(s.getFullName()),
                        sanitizeCsvCell(s.getEmail()),
                        sanitizeCsvCell(s.getDepartment()),
                        sanitizeCsvCell(s.getSectionName()),
                        s.getYear(),
                        s.getSemester(),
                        sanitizeCsvCell(s.getPhoneNumber()),
                        s.isActive() ? "ACTIVE" : "INACTIVE"
                ));
            }
        }
        return sb.toString();
    }

    @Transactional
    public com.campusgrid.dto.CsvImportResultDto importUsersCsv(String csvContent, Role targetRole) {
        if (csvContent == null || csvContent.isBlank()) {
            throw new IllegalArgumentException("CSV content is empty.");
        }

        String[] lines = csvContent.split("\\r?\\n");
        com.campusgrid.dto.CsvImportResultDto result = new com.campusgrid.dto.CsvImportResultDto();
        int totalValidLines = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            // Remove UTF-8 BOM if present
            if (line.startsWith("\uFEFF")) {
                line = line.substring(1).trim();
            }
            if (line.isEmpty()) {
                continue;
            }
            // Check if this is the header row
            String lower = line.toLowerCase();
            if (i == 0 && (lower.contains("full name") || lower.contains("email") || lower.contains("username") || lower.contains("roll number") || lower.contains("employee id"))) {
                continue; // skip header row
            }
            totalValidLines++;

            // Split line handling quotes
            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (parts.length < 4) {
                result.setErrorsCount(result.getErrorsCount() + 1);
                result.addIssue(i + 1, "Line " + (i + 1), "Missing required columns (Expected: Name, Email, Username, ID, ...)", "ERROR");
                continue;
            }

            String fullName = parts[0].replaceAll("^\"|\"$", "").trim();
            String email = parts[1].replaceAll("^\"|\"$", "").trim();
            String username = parts[2].replaceAll("^\"|\"$", "").trim();
            String specificId = parts[3].replaceAll("^\"|\"$", "").trim();
            String dept = parts.length > 4 ? parts[4].replaceAll("^\"|\"$", "").trim() : "Computer Science";
            String subField = parts.length > 5 ? parts[5].replaceAll("^\"|\"$", "").trim() : (targetRole == Role.STUDENT ? "CSE-A" : "Assistant Professor");
            String phone = parts.length > 6 ? parts[6].replaceAll("^\"|\"$", "").trim() : "";

            if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || specificId.isEmpty()) {
                result.setErrorsCount(result.getErrorsCount() + 1);
                result.addIssue(i + 1, username.isEmpty() ? "Row " + (i + 1) : username, "Required fields cannot be empty", "ERROR");
                continue;
            }

            if (!email.contains("@") || !email.contains(".")) {
                result.setErrorsCount(result.getErrorsCount() + 1);
                result.addIssue(i + 1, email, "Malformed email address format", "ERROR");
                continue;
            }

            if (userRepository.existsByUsername(username)) {
                result.setDuplicatesCount(result.getDuplicatesCount() + 1);
                result.addIssue(i + 1, username, "Username '" + username + "' is already registered", "DUPLICATE");
                continue;
            }

            if (userRepository.existsByEmail(email)) {
                result.setDuplicatesCount(result.getDuplicatesCount() + 1);
                result.addIssue(i + 1, email, "Email '" + email + "' is already in use", "DUPLICATE");
                continue;
            }

            if (targetRole == Role.STUDENT && studentRepository.existsByRollNumber(specificId.toUpperCase())) {
                result.setDuplicatesCount(result.getDuplicatesCount() + 1);
                result.addIssue(i + 1, specificId, "Roll number '" + specificId + "' already exists", "DUPLICATE");
                continue;
            } else if (targetRole == Role.FACULTY && facultyRepository.existsByEmployeeId(specificId.toUpperCase())) {
                result.setDuplicatesCount(result.getDuplicatesCount() + 1);
                result.addIssue(i + 1, specificId, "Employee ID '" + specificId + "' already exists", "DUPLICATE");
                continue;
            }

            try {
                // Generate a randomized secure activation credential rather than static plain text
                String secureInitialPassword = (targetRole == Role.STUDENT ? "Stu@" : "Fac@") + 
                        java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase() + "9!";

                CreateUserRequest req = new CreateUserRequest();
                req.setRole(targetRole);
                req.setFullName(fullName);
                req.setEmail(email);
                req.setUsername(username);
                req.setPassword(secureInitialPassword);
                req.setDepartment(dept);
                req.setPhoneNumber(phone);

                if (targetRole == Role.STUDENT) {
                    req.setRollNumber(specificId);
                    req.setSectionName(subField);
                    req.setYear(2);
                    req.setSemester(3);
                } else {
                    req.setEmployeeId(specificId);
                    req.setDesignation(subField);
                }

                createUser(req);
                result.setSuccessfullyImported(result.getSuccessfullyImported() + 1);
            } catch (Exception ex) {
                result.setErrorsCount(result.getErrorsCount() + 1);
                result.addIssue(i + 1, specificId, ex.getMessage(), "ERROR");
            }
        }

        result.setTotalSubmitted(totalValidLines);
        return result;
    }



    @Transactional
    public void seedInitialUsers() {
        if (userRepository.count() > 0) {
            return; // Data already exists
        }

        // 1. Seed Admin
        User admin = new User("admin", "admin@campusgrid.ac.in", passwordEncoder.encode("Admin@123"), "Campus Administrator", Role.ADMIN);
        userRepository.save(admin);

        // 2. Seed Faculty
        User f1 = new User("meera.faculty", "meera.sharma@campusgrid.ac.in", passwordEncoder.encode("Faculty@123"), "Dr. Meera Sharma", Role.FACULTY);
        userRepository.save(f1);
        Faculty faculty1 = new Faculty(f1, "FAC001", "Computer Science", "Associate Professor", "+91 9876543210", "Block A, Room A-305");
        facultyRepository.save(faculty1);

        User f2 = new User("arjun.faculty", "arjun.singh@campusgrid.ac.in", passwordEncoder.encode("Faculty@123"), "Prof. Arjun Singh", Role.FACULTY);
        userRepository.save(f2);
        Faculty faculty2 = new Faculty(f2, "FAC002", "Computer Science", "Assistant Professor", "+91 9876543211", "Block A, Room A-306");
        facultyRepository.save(faculty2);

        // 3. Seed Students (matching mockup sample data!)
        Object[][] studentData = {
                {"rahul123", "rahul.sharma23@college.ac.in", "Rahul Sharma", "23CSE001", "Computer Science", "CSE-A", 2, 3},
                {"ananya.verma", "ananya.verma@college.ac.in", "Ananya Verma", "23CSE002", "Computer Science", "CSE-A", 2, 3},
                {"priya.singh", "priya.singh@college.ac.in", "Priya Singh", "23CSE003", "Computer Science", "CSE-A", 2, 3},
                {"arjun.mehta", "arjun.mehta@college.ac.in", "Arjun Mehta", "23CSE004", "Computer Science", "CSE-B", 2, 3},
                {"neha.gupta", "neha.gupta@college.ac.in", "Neha Gupta", "23CSE005", "Computer Science", "CSE-B", 2, 3},
                {"karan.patel", "karan.patel@college.ac.in", "Karan Patel", "23CSE006", "Computer Science", "CSE-B", 2, 3},
                {"rohan.kumar", "rohan.kumar@college.ac.in", "Rohan Kumar", "23CSE007", "Computer Science", "CSE-C", 2, 3},
                {"sneha.joshi", "sneha.joshi@college.ac.in", "Sneha Joshi", "23CSE008", "Computer Science", "CSE-C", 2, 3}
        };

        for (Object[] sRow : studentData) {
            String uName = (String) sRow[0];
            String email = (String) sRow[1];
            String name = (String) sRow[2];
            String roll = (String) sRow[3];
            String dept = (String) sRow[4];
            String sec = (String) sRow[5];
            int year = (Integer) sRow[6];
            int sem = (Integer) sRow[7];

            User sUser = new User(uName, email, passwordEncoder.encode("Student@123"), name, Role.STUDENT);
            userRepository.save(sUser);

            Student student = new Student(sUser, roll, dept, sec, year, sem, "+91 98765432" + roll.substring(roll.length() - 2));
            studentRepository.save(student);
        }
    }
}
