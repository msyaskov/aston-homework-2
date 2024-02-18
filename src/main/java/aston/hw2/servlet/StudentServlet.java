package aston.hw2.servlet;

import aston.hw2.context.WebApplicationContext;
import aston.hw2.dto.GroupDto;
import aston.hw2.dto.StudentDto;
import aston.hw2.entity.Group;
import aston.hw2.entity.Student;
import aston.hw2.mapper.GroupMapper;
import aston.hw2.mapper.Mapper;
import aston.hw2.mapper.StudentMapper;
import aston.hw2.service.GroupNotFoundException;
import aston.hw2.service.InvalidCandidateException;
import aston.hw2.service.StudentNotFoundException;
import aston.hw2.service.StudentService;
import aston.hw2.util.PathMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Сервлет CRUD-операций над студентами.
 * Делегирует выполнение бизнес-логику операции {@link StudentService}.
 *
 * @author Максим Яськов
 * @see StudentService
 */
@WebServlet({"/students","/students/*"})
public class StudentServlet extends RestHttpServlet {

    private static final PathMatcher PM_STUDENTS = new PathMatcher("/students");

    private static final PathMatcher PM_STUDENTS_SID = new PathMatcher("/students/:studentId");

    private static final PathMatcher PM_STUDENTS_SID_GROUP = new PathMatcher("/students/:studentId/group");

    private static final PathMatcher PM_STUDENTS_SID_GROUP_GID = new PathMatcher("/students/:studentId/group/:groupId");

    private StudentService studentService;

    private Mapper<Student, StudentDto> studentMapper;
    private Mapper<Group, GroupDto> groupMapper;

    @Override
    public void init() throws ServletException {
        WebApplicationContext webAppContext = (WebApplicationContext) this.getServletContext().getAttribute(WebApplicationContext.class.getName());
        setObjectMapper(webAppContext.getObject(ObjectMapper.class));
        studentService = webAppContext.getObject(StudentService.class);
        studentMapper = webAppContext.getObject(StudentMapper.class);
        groupMapper = webAppContext.getObject(GroupMapper.class);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PM_STUDENTS_SID.match(request)) {
            doDeleteStudent(request, response);
        } else if (PM_STUDENTS_SID_GROUP.match(request)) {
            doDeleteStudentGroup(request, response);
        } else {
            super.doDelete(request, response);
        }
    }

    // operationId: delete.student
    private void doDeleteStudent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int studentId = PM_STUDENTS_SID.extractRequiredIntPathVariable(request, "studentId");

        try {
            studentService.removeStudent(studentId);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (StudentNotFoundException e) {
            sendErrorStudentNotFound(response);
        }
    }

    // operationId: delete.student.group
    private void doDeleteStudentGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int studentId = PM_STUDENTS_SID_GROUP.extractRequiredIntPathVariable(request, "studentId");

        try {
            studentService.unassignGroup(studentId);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (StudentNotFoundException e) {
            sendErrorStudentNotFound(response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PM_STUDENTS.match(request)) {
            doGetStudents(request, response);
        } else if (PM_STUDENTS_SID.match(request)) {
            doGetStudent(request, response);
        } else if (PM_STUDENTS_SID_GROUP.match(request)){
            doGetStudentGroup(request, response);
        } else {
            super.doGet(request, response);
        }
    }

    // operationId: get.student
    private void doGetStudent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int studentId = PM_STUDENTS_SID.extractRequiredIntPathVariable(request, "studentId");

        try {
            Student student = studentService.getStudent(studentId);

            response.setStatus(HttpServletResponse.SC_OK);
            sendResponseBody(response, studentMapper.map(student));
        } catch (StudentNotFoundException e) {
            sendErrorStudentNotFound(response);
        }
    }

    // operationId: get.students
    private void doGetStudents(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<StudentDto> students = studentService.getAllStudents()
                .map(studentMapper::map)
                .toList();

        response.setStatus(HttpServletResponse.SC_OK);
        sendResponseBody(response, students);
    }

    // operationId: get.student.group
    private void doGetStudentGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int studentId = PM_STUDENTS_SID_GROUP.extractRequiredIntPathVariable(request, "studentId");

        try {
            Student student = studentService.getStudent(studentId);
            if (student.getGroup() == null) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                sendResponseBody(response, groupMapper.map(student.getGroup()));
            }
        } catch (StudentNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The student not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PM_STUDENTS.match(request)) {
            doPostStudent(request, response);
        } else {
            super.doPost(request, response);
        }
    }

    // operationId: post.student
    private void doPostStudent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Student candidate = studentMapper.reverseMap(readRequestBody(request, StudentDto.class));
            Student student = studentService.createStudentByCandidate(candidate);

            response.setStatus(HttpServletResponse.SC_CREATED);
            sendResponseBody(response, studentMapper.map(student));
        } catch (InvalidCandidateException e) {
            sendErrorInvalidCandidate(response);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PM_STUDENTS_SID.match(request)) {
            doPutStudent(request, response);
        } else if (PM_STUDENTS_SID_GROUP_GID.match(request)) {
            doPutStudentGroup(request, response);
        } else {
            super.doPut(request, response);
        }
    }

    // operationId: put.student
    private void doPutStudent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int studentId = PM_STUDENTS_SID.extractRequiredIntPathVariable(request, "studentId");

        try {
            Student candidate = studentMapper.reverseMap(readRequestBody(request, StudentDto.class));
            Student student = studentService.updateStudent(studentId, candidate);

            response.setStatus(HttpServletResponse.SC_OK);
            sendResponseBody(response, studentMapper.map(student));
        } catch (InvalidCandidateException e) {
            sendErrorInvalidCandidate(response);
        } catch (StudentNotFoundException e) {
            sendErrorStudentNotFound(response);
        }
    }

    // operationId: put.student.group
    private void doPutStudentGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int studentId = PM_STUDENTS_SID_GROUP_GID.extractRequiredIntPathVariable(request, "studentId");
        int groupId = PM_STUDENTS_SID_GROUP_GID.extractRequiredIntPathVariable(request, "groupId");

        try {
            studentService.assignGroup(studentId, groupId);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (GroupNotFoundException e) {
            sendErrorGroupNotFound(response);
        } catch (StudentNotFoundException e) {
            sendErrorStudentNotFound(response);
        }
    }

    private void sendErrorGroupNotFound(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "The group not found");
    }

    private void sendErrorInvalidCandidate(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid candidate");
    }

    private void sendErrorStudentNotFound(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "The student not found");
    }
}
