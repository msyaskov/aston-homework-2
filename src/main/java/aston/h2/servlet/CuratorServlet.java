package aston.h2.servlet;

import aston.h2.context.WebApplicationContext;
import aston.h2.dto.CuratorDto;
import aston.h2.dto.GroupDto;
import aston.h2.entity.Curator;
import aston.h2.entity.Group;
import aston.h2.mapper.CuratorMapper;
import aston.h2.mapper.GroupMapper;
import aston.h2.mapper.Mapper;
import aston.h2.service.*;
import aston.h2.util.PathMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Сервлет CRUD-операций над кураторами.
 * Делегирует выполнение бизнес-логики операции {@link }.
 *
 * @author Максим Яськов
 * @see StudentService
 */
@WebServlet({"/curators","/curators/*"})
public class CuratorServlet extends RestHttpServlet {

    private static final PathMatcher PM_CURATORS = new PathMatcher("/curators");

    private static final PathMatcher PM_CURATORS_CID = new PathMatcher("/curators/:curatorId");

    private static final PathMatcher PM_CURATORS_CID_GROUP = new PathMatcher("/curators/:curatorId/group");

    private static final PathMatcher PM_CURATORS_CID_GROUP_GID = new PathMatcher("/curators/:curatorId/group/:groupId");

    private CuratorService curatorService;

    private Mapper<Curator, CuratorDto> curatorMapper;
    private Mapper<Group, GroupDto> groupMapper;

    @Override
    public void init() {
        WebApplicationContext webAppContext = (WebApplicationContext) this.getServletContext().getAttribute(WebApplicationContext.class.getName());
        setObjectMapper(webAppContext.getObject(ObjectMapper.class));
        curatorService = webAppContext.getObject(CuratorService.class);
        curatorMapper = webAppContext.getObject(CuratorMapper.class);
        groupMapper = webAppContext.getObject(GroupMapper.class);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PM_CURATORS_CID.match(request)) {
            doDeleteCurator(request, response);
        } else if (PM_CURATORS_CID_GROUP.match(request)) {
            doDeleteCuratorGroup(request, response);
        } else {
            super.doDelete(request, response);
        }
    }

    // operationId: delete.curator
    private void doDeleteCurator(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int curatorId = PM_CURATORS_CID.extractRequiredIntPathVariable(request, "curatorId");

        try {
            curatorService.removeCurator(curatorId);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (CuratorNotFoundException e) {
            sendErrorCuratorNotFound(response);
        }
    }

    // operationId: delete.curator.group
    private void doDeleteCuratorGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int curatorId = PM_CURATORS_CID_GROUP.extractRequiredIntPathVariable(request, "curatorId");

        try {
            curatorService.unassignGroup(curatorId);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (CuratorNotFoundException e) {
            sendErrorCuratorNotFound(response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PM_CURATORS.match(request)) {
            doGetCurators(request, response);
        } else if (PM_CURATORS_CID.match(request)) {
            doGetCurator(request, response);
        } else if (PM_CURATORS_CID_GROUP.match(request)){
            doGetCuratorGroup(request, response);
        } else {
            super.doGet(request, response);
        }
    }

    // operationId: get.curator
    private void doGetCurator(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int curatorId = PM_CURATORS_CID.extractRequiredIntPathVariable(request, "curatorId");

        try {
            Curator curator = curatorService.getCurator(curatorId);

            response.setStatus(HttpServletResponse.SC_OK);
            sendResponseBody(response, curatorMapper.map(curator));
        } catch (CuratorNotFoundException e) {
            sendErrorCuratorNotFound(response);
        }
    }

    // operationId: get.curators
    private void doGetCurators(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<CuratorDto> curators = curatorService.getAllCurators()
                .map(curatorMapper::map)
                .toList();

        response.setStatus(HttpServletResponse.SC_OK);
        sendResponseBody(response, curators);
    }

    // operationId: get.curator.group
    private void doGetCuratorGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int curatorId = PM_CURATORS_CID_GROUP.extractRequiredIntPathVariable(request, "curatorId");

        try {
            Curator curator = curatorService.getCurator(curatorId);
            if (curator.getGroup() == null) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                sendResponseBody(response, groupMapper.map(curator.getGroup()));
            }
        } catch (CuratorNotFoundException e) {
            sendErrorCuratorNotFound(response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PM_CURATORS.match(request)) {
            doPostCurator(request, response);
        } else {
            super.doPost(request, response);
        }
    }

    // operationId: post.curator
    private void doPostCurator(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Curator candidate = curatorMapper.reverseMap(readRequestBody(request, CuratorDto.class));
            Curator curator = curatorService.createCuratorByCandidate(candidate);

            response.setStatus(HttpServletResponse.SC_CREATED);
            sendResponseBody(response, curatorMapper.map(curator));
        } catch (InvalidCandidateException e) {
            sendErrorInvalidCandidate(response);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PM_CURATORS_CID.match(request)) {
            doPutCurator(request, response);
        } else if (PM_CURATORS_CID_GROUP_GID.match(request)) {
            doPutCuratorGroup(request, response);
        } else {
            super.doPut(request, response);
        }
    }

    // operationId: put.curator
    private void doPutCurator(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int curatorId = PM_CURATORS_CID.extractRequiredIntPathVariable(request, "curatorId");

        try {
            Curator candidate = curatorMapper.reverseMap(readRequestBody(request, CuratorDto.class));
            Curator curator = curatorService.updateCurator(curatorId, candidate);

            response.setStatus(HttpServletResponse.SC_OK);
            sendResponseBody(response, curatorMapper.map(curator));
        } catch (InvalidCandidateException e) {
            sendErrorInvalidCandidate(response);
        } catch (CuratorNotFoundException e) {
            sendErrorCuratorNotFound(response);
        }
    }

    // operationId: put.curator.group
    private void doPutCuratorGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int curatorId = PM_CURATORS_CID_GROUP_GID.extractRequiredIntPathVariable(request, "curatorId");
        int groupId = PM_CURATORS_CID_GROUP_GID.extractRequiredIntPathVariable(request, "groupId");

        try {
            curatorService.assignGroup(curatorId, groupId);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (CuratorNotFoundException e) {
            sendErrorCuratorNotFound(response);
        } catch (GroupNotFoundException e) {
            sendErrorGroupNotFound(response);
        }
    }

    private void sendErrorCuratorNotFound(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "The curator not found");
    }

    private void sendErrorGroupNotFound(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "The group not found");
    }

    private void sendErrorInvalidCandidate(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid candidate");
    }
}
