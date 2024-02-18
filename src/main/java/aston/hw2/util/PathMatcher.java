package aston.hw2.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

public class PathMatcher {

    public static final String PATH_SEPARATOR = "/";

    public static final String WILDCARD = ":";

    private final String[] patternParts;

    public PathMatcher(String pattern) {
        patternParts = pattern.split(PATH_SEPARATOR);
    }

    public boolean match(HttpServletRequest request) {
        return match(getConcatServletPathWithPathInfo(request));
    }

    private boolean match(String path) {
        if (path == null) {
            throw new IllegalArgumentException("A path must not be null");
        }

        String[] pathParts = path.split(PATH_SEPARATOR);

        if (pathParts.length != patternParts.length) {
            return false;
        }

        for (int i = 0; i < pathParts.length; i++) {
            if (!patternParts[i].startsWith(WILDCARD) && !patternParts[i].equals(pathParts[i])) {
                return false;
            }
        }

        return true;
    }


    public <T> T extractRequiredPathVariable(HttpServletRequest request, String name, Class<T> type) {
        T var = extractPathVariable(request, name, type);
        if (var == null) {
            throw new RequiredPathVariableNotFoundException(name);
        }

        return var;
    }

    public int extractRequiredIntPathVariable(HttpServletRequest request, String name) {
        Map<String, String> vars = extractPathVariables(request);
        String var = vars.get(name);

        try {
            return Integer.parseInt(var);
        } catch (NumberFormatException e) {
            throw new RequiredPathVariableNotFoundException(name, e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T extractPathVariable(HttpServletRequest request, String name, Class<T> type) {
        Map<String, String> vars = extractPathVariables(request);
        String s = vars.get(name);

        if (s == null) {
            return null;
        } else if (type == String.class) {
            return (T) s;
        } else if (type == Integer.class) {
            return (T) Integer.valueOf(Integer.parseInt(s));
        } else {
            throw new UnsupportedOperationException("Unsupported type");
        }
    }

    public Map<String, String> extractPathVariables(HttpServletRequest request) {
        return extractPathVariables(getConcatServletPathWithPathInfo(request));
    }

    private Map<String, String> extractPathVariables(String path) {
        Map<String, String> vars = new HashMap<>();

        String[] pathParts = path.split(PATH_SEPARATOR);
        for (int i = 0; i < patternParts.length && i < pathParts.length; i++) {
            String patternPart = patternParts[i];
            if (patternPart.startsWith(WILDCARD)) {
                String varName = patternPart.substring(1);
                vars.put(varName, pathParts[i]);
            }
        }

        return vars;
    }

    private static String getConcatServletPathWithPathInfo(HttpServletRequest request) {
        return request.getServletPath() + (request.getPathInfo() != null ? request.getPathInfo() : "");
    }
}
