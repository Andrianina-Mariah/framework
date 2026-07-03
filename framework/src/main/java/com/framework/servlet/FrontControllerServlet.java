package com.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.framework.util.ClasseUtilitaire;

/**
 * Front controller servlet that scans for controllers and handles requests based on URL mappings.
 */
public class FrontControllerServlet extends HttpServlet {

    private static final String CONTROLLER_PACKAGE_INIT_PARAM = "controllerPackage";
    private static final String DEFAULT_CONTROLLER_PACKAGE = "com.app.controller";

    private Map<String, Map<String, java.lang.reflect.Method>> urlMappingMap;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String controllerPackage = config.getInitParameter(CONTROLLER_PACKAGE_INIT_PARAM);
        if (controllerPackage == null || controllerPackage.isEmpty()) {
            controllerPackage = DEFAULT_CONTROLLER_PACKAGE;
        }
        this.urlMappingMap = ClasseUtilitaire.getUrlMappingMap(controllerPackage);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        //out.println("<h2>Front Controller Servlet</h2>");

        // Extract path after context path (more reliable than getServletPath() for /* mapping)
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String pathInfo = requestURI.substring(contextPath.length());
        // Remove leading slash if present
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1);
        }

        out.println("<p>Requested URI: " + requestURI + "</p>");
        out.println("<p>Path info after servlet: " + pathInfo + "</p>");

        java.lang.reflect.Method method = null;
        String controllerName = null;
        boolean found = false;
        String methodKey = pathInfo + "#" + req.getMethod();
        for (Map.Entry<String, Map<String, java.lang.reflect.Method>> entry : urlMappingMap.entrySet()) {
            String ctrlName = entry.getKey();
            Map<String, java.lang.reflect.Method> methodMap = entry.getValue();
            if (methodMap.containsKey(methodKey)) {
                method = methodMap.get(methodKey);
                controllerName = ctrlName;
                found = true;
                break;
            }
        }

        out.println("<p>Controller names map: " + urlMappingMap.keySet() + "</p>");

        if (found && method != null) {
            try {
                // Get the declaring class of the method
                java.lang.Class<?> controllerClass = method.getDeclaringClass();
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                Object result = method.invoke(controllerInstance);
                out.println("<h3>Controller->Method: " + controllerName + "->" + method.getName() + "</h3>");
                out.println("<p>Result: " + result + "</p>");
            } catch (Exception e) {
                out.println("<p>Error invoking controller method: " + e.getMessage() + "</p>");
                e.printStackTrace(out);
            }
        } else {
            out.println("<h3>No mapping found for URL: " + pathInfo + " with method " + req.getMethod() + "</p>");
            out.println("<p>Available mappings:</p>");
            out.println("<ul>");
            for (Map.Entry<String, Map<String, java.lang.reflect.Method>> entry : urlMappingMap.entrySet()) {
                String ctrl = entry.getKey();
                for (Map.Entry<String, java.lang.reflect.Method> mEntry : entry.getValue().entrySet()) {
                    String key = mEntry.getKey();
                    // key format: urlPattern#HTTP_METHOD
                    String[] parts = key.split("#", 2);
                    String urlPart = parts[0];
                    String methodPart = parts.length > 1 ? parts[1] : "";
                    out.println("<li>" + ctrl + " -> " + urlPart + " (method: " + methodPart + ") (method: " + mEntry.getValue().getName() + ")</li>");
                }
            }
            out.println("</ul>");
        }

        out.println("</body></html>");
    }
}