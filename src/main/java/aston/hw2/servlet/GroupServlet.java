package aston.hw2.servlet;

import aston.hw2.context.WebApplicationContext;
import aston.hw2.dto.CuratorDto;
import aston.hw2.dto.GroupDto;
import aston.hw2.dto.StudentDto;
import aston.hw2.entity.Curator;
import aston.hw2.entity.Group;
import aston.hw2.entity.Student;
import aston.hw2.mapper.CuratorMapper;
import aston.hw2.mapper.GroupMapper;
import aston.hw2.mapper.Mapper;
import aston.hw2.mapper.StudentMapper;
import aston.hw2.service.*;
import aston.hw2.util.PathMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Сервлет CRUD-операций над группами.
 * Делегирует выполнение бизнес-логику операции {@link GroupService}.
 *
 * @author Максим Яськов
 * @see GroupService
 */
@WebServlet({"/groups","/groups/*"})
public class GroupServlet extends RestHttpServlet {

    private static final PathMatcher PM_GROUPS = new PathMatcher("/groups");

    private static final PathMatcher PM_GROUPS_GID = new PathMatcher("/groups/:groupId");

    private static final PathMatcher PM_GROUPS_GID_CURATOR = new PathMatcher("/groups/:groupId/curator");

    private static final PathMatcher PM_GROUPS_GID_CURATOR_CID = new PathMatcher("/groups/:groupId/curator/:curatorId");

    private static final PathMatcher PM_GROUPS_GID_STUDENTS = new PathMatcher("/groups/:groupId/students");
    private static final PathMatcher PM_GROUPS_GID_STUDENTS_SID = new PathMatcher("/groups/:groupId/students/:studentId");

    private GroupService groupService;

    private Mapper<Group, GroupDto> groupMapper;
    private Mapper<Curator, CuratorDto> curatorMapper;
    private Mapper<Student, StudentDto> studentMapper;

    @Override
    public void init() {
        WebApplicationContext webAppContext = (WebApplicationContext) this.getServletContext().getAttribute(WebApplicationContext.class.getName());
        setObjectMapper(webAppContext.getObject(ObjectMapper.class));
        groupService = webAppContext.getObject(GroupService.class);
        groupMapper = webAppContext.getObject(GroupMapper.class);
        studentMapper = webAppContext.getObject(StudentMapper.class);
        curatorMapper = webAppContext.getObject(CuratorMapper.class);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PM_GROUPS_GID.match(request)) {
            doDeleteGroup(request, response);
        } else if (PM_GROUPS_GID_CURATOR.match(request)) {
            doDeleteGroupCurator(request, response);
        } else if (PM_GROUPS_GID_STUDENTS_SID.match(request)) {
            doDeleteGroupStudent(request, response);
        } else {
            super.doDelete(request, response);
        }
    }

    // operationId: delete.group
    private void doDeleteGroup(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int groupId = PM_GROUPS_GID.extractRequiredIntPathVariable(request, "groupId");

        try {
            groupService.removeGroup(groupId);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (GroupNotFoundException e) {
            sendErrorGroupNotFound(response);
        }
    }

    // operationId: delete.group.curator
    private void doDeleteGroupCurator(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int groupId = PM_GROUPS_GID_CURATOR.extractRequiredIntPathVariable(request, "groupId");

        try {
            groupService.unassignCurator(groupId);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (GroupNotFoundException e) {
            sendErrorGroupNotFound(response);
        }
    }

    // operationId: delete.group.student
    private void doDeleteGroupStudent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int groupId = PM_GROUPS_GID_STUDENTS_SID.extractRequiredIntPathVariable(request, "groupId");
        int studentId = PM_GROUPS_GID_STUDENTS_SID.extractRequiredIntPathVariable(request, "studentId");

        try {
            groupService.unassignStudent(groupId, studentId);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (GroupNotFoundException e) {
            sendErrorGroupNotFound(response);
        } catch (StudentNotFoundException e) {
            sendErrorStudentNotFound(response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PM_GROUPS.match(request)) {
            doGetGroups(request, response);
        } else if (PM_GROUPS_GID.match(request)) {
            doGetGroup(request, response);
        } else if (PM_GROUPS_GID_CURATOR.match(request)) {
            doGetGroupCurator(request, response);
        } else if (PM_GROUPS_GID_STUDENTS.match(request)) {
            doGetGroupStudents(request, response);
        } else {
            super.doGet(request, response);
        }
    }

    // operationId: get.group
    private void doGetGroup(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int groupId = PM_GROUPS_GID.extractRequiredIntPathVariable(request, "groupId");

        try {
            Group group = groupService.getGroup(groupId);

            response.setStatus(HttpServletResponse.SC_OK);
            sendResponseBody(response, groupMapper.map(group));
        } catch (GroupNotFoundException e) {
            sendErrorGroupNotFound(response);
        }
    }

    // operationId: get.groups
    private void doGetGroups(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<GroupDto> groups = groupService.getAllGroups()
                .map(groupMapper::map)
                .toList();

        response.setStatus(HttpServletResponse.SC_OK);
        sendResponseBody(response, groups);
    }

    // operationId: get.group.curator
    private void doGetGroupCurator(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int groupId = PM_GROUPS_GID_CURATOR.extractRequiredIntPathVariable(request, "groupId");

        try {
            Group group = groupService.getGroup(groupId);
            if (group.getCurator() == null) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                sendResponseBody(response, curatorMapper.map(group.getCurator()));
            }
        } catch (GroupNotFoundException e) {
            sendErrorGroupNotFound(response);
        }
    }

    // operationId: get.group.students
    private void doGetGroupStudents(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int groupId = PM_GROUPS_GID_STUDENTS.extractRequiredIntPathVariable(request, "groupId");

        try {
            Group group = groupService.getGroup(groupId);
            List<StudentDto> students = group.getStudents().stream()
                    .map(studentMapper::map)
                    .toList();

            response.setStatus(HttpServletResponse.SC_OK);
            sendResponseBody(response, students);
        } catch (GroupNotFoundException e) {
            sendErrorGroupNotFound(response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PM_GROUPS.match(request)) {
            doPostGroup(request, response);
        } else {
            super.doPost(request, response);
        }
    }

    // operationId: post.group
    private void doPostGroup(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Group candidate = groupMapper.reverseMap(readRequestBody(request, GroupDto.class));
            Group group = groupService.createGroupByCandidate(candidate);

            response.setStatus(HttpServletResponse.SC_CREATED);
            sendResponseBody(response, groupMapper.map(group));
        } catch (InvalidCandidateException e) {
            sendErrorInvalidCandidate(response);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PM_GROUPS_GID.match(request)) {
            doPutGroup(request, response);
        } else if (PM_GROUPS_GID_CURATOR_CID.match(request)) {
            doPutGroupCurator(request, response);
        } else if (PM_GROUPS_GID_STUDENTS_SID.match(request)) {
            doPutGroupStudent(request, response);
        } else {
            super.doPut(request, response);
        }
    }

    // operationId: put.group
    private void doPutGroup(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int groupId = PM_GROUPS_GID.extractRequiredIntPathVariable(request, "groupId");

        try {
            Group candidate = groupMapper.reverseMap(readRequestBody(request, GroupDto.class));
            Group group = groupService.updateGroupByCandidate(groupId, candidate);

            response.setStatus(HttpServletResponse.SC_OK);
            sendResponseBody(response, groupMapper.map(group));
        } catch (GroupNotFoundException e) {
            sendErrorGroupNotFound(response);
        } catch (InvalidCandidateException e) {
            sendErrorInvalidCandidate(response);
        }
    }

    // operationId: put.group.curator
    private void doPutGroupCurator(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int groupId = PM_GROUPS_GID_CURATOR_CID.extractRequiredIntPathVariable(request, "groupId");
        int curatorId = PM_GROUPS_GID_CURATOR_CID.extractRequiredIntPathVariable(request, "curatorId");

        try {
            groupService.assignCurator(groupId, curatorId);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (CuratorNotFoundException e) {
            sendErrorCuratorNotFound(response);
        } catch (GroupNotFoundException e) {
            sendErrorGroupNotFound(response);
        }
    }

    // operationId: put.group.student
    private void doPutGroupStudent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int groupId = PM_GROUPS_GID_STUDENTS_SID.extractRequiredIntPathVariable(request, "groupId");
        int studentId = PM_GROUPS_GID_STUDENTS_SID.extractRequiredIntPathVariable(request, "studentId");

        try {
            groupService.assignStudent(groupId, studentId);

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

    private void sendErrorStudentNotFound(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "The student not found");
    }

    private void sendErrorCuratorNotFound(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "The curator not found");
    }

    private void sendErrorInvalidCandidate(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid candidate");
    }
}
