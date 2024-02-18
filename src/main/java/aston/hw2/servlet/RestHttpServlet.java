package aston.hw2.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;

import java.io.IOException;

public abstract class RestHttpServlet extends HttpServlet {

    @Setter
    private ObjectMapper objectMapper = new ObjectMapper();

    protected void sendResponseBody(HttpServletResponse response, Object body) throws IOException {
        if (body == null) {
            throw new IllegalArgumentException("A body mut not be null");
        }

        response.addHeader("Content-Type", "application/json");
        objectMapper.writeValue(response.getWriter(), body);
    }

    protected  <T> T readRequestBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        return objectMapper.readValue(request.getReader(), clazz);
    }

}
