package com.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.framework.model.ModelAndView;
import com.framework.util.ClasseUtilitaire;
import org.springframework.web.context.WebApplicationContext;

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
        String pathInfo = getPathInfo(req);
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");

        out.println("<p>Requested URI: " + req.getRequestURI() + "</p>");
        out.println("<p>Path info: " + pathInfo + "</p>");

        java.lang.reflect.Method method = null;
        String controllerName = null;
        boolean found = false;

        // Split pathInfo into controller name and method path
        String[] pathParts = pathInfo.split("/", 2);
        String controllerNameFromPath = pathParts[0];
        String methodPath = pathParts.length > 1 ? pathParts[1] : "";

        // Debug information
        out.println("<p>Debug: controllerNameFromPath='" + controllerNameFromPath + "', methodPath='" + methodPath + "', method='" + req.getMethod() + "'</p>");

        // Look up controller first
        if (urlMappingMap.containsKey(controllerNameFromPath)) {
            out.println("<p>Debug: Found controller '" + controllerNameFromPath + "'</p>");
            Map<String, java.lang.reflect.Method> methodMap = urlMappingMap.get(controllerNameFromPath);
            out.println("<p>Debug: Method map keys: " + methodMap.keySet() + "</p>");

            // Try multiple variations of methodKey to handle potential slash mismatches
            String httpMethod = req.getMethod();
            String methodKey1 = methodPath + "#" + httpMethod;                 // No leading slash
            String methodKey2 = "/" + methodPath + "#" + httpMethod;          // With leading slash
            String methodKey3 = "";
            if (methodPath.startsWith("/")) {
                methodKey3 = methodPath.substring(1) + "#" + httpMethod;      // Remove leading slash if present
            } else if (!methodPath.isEmpty()) {
                methodKey3 = "/" + methodPath + "#" + httpMethod;             // Add leading slash if not present
            } else {
                methodKey3 = "#" + httpMethod;                                // Empty path case
            }

            out.println("<p>Debug: Trying methodKey1='" + methodKey1 + "'</p>");
            out.println("<p>Debug: Trying methodKey2='" + methodKey2 + "'</p>");
            out.println("<p>Debug: Trying methodKey3='" + methodKey3 + "'</p>");

            if (methodMap.containsKey(methodKey1)) {
                method = methodMap.get(methodKey1);
                controllerName = controllerNameFromPath;
                found = true;
                out.println("<p>Debug: Found method with key1!</p>");
            } else if (methodMap.containsKey(methodKey2)) {
                method = methodMap.get(methodKey2);
                controllerName = controllerNameFromPath;
                found = true;
                out.println("<p>Debug: Found method with key2!</p>");
            } else if (methodMap.containsKey(methodKey3)) {
                method = methodMap.get(methodKey3);
                controllerName = controllerNameFromPath;
                found = true;
                out.println("<p>Debug: Found method with key3!</p>");
            } else {
                out.println("<p>Debug: Method NOT found for any key variant</p>");
                out.println("<p>Debug: Available keys contain: " +
                           methodMap.keySet().stream()
                                   .filter(k -> k.contains("#" + httpMethod))
                                   .findFirst()
                                   .orElse("none") + "</p>");
            }
        } else {
            out.println("<p>Debug: Controller '" + controllerNameFromPath + "' NOT found</p>");
            out.println("<p>Debug: Available controllers: " + urlMappingMap.keySet() + "</p>");
        }

        out.println("<p>Controller names: " + urlMappingMap.keySet() + "</p>");

        if (found && method != null) {
            try {
                java.lang.Class<?> controllerClass = method.getDeclaringClass();
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

                // Get Spring context from ServletContext
                ServletContext servletContext = this.getServletConfig().getServletContext();
                WebApplicationContext springContext = (WebApplicationContext) servletContext.getAttribute("springContext");

                // Check if method expects a WebApplicationContext parameter
                if (ClasseUtilitaire.haveParameter(method, WebApplicationContext.class)) {
                    if (springContext == null) {
                        throw new Exception("Spring WebApplicationContext not available. Please ensure Spring is properly configured in your web application.");
                    }
                    Object result = method.invoke(controllerInstance, springContext);

                    if (result instanceof ModelAndView) {
                        ModelAndView mav = (ModelAndView) result;

                        out.println("<h1>✅ " + mav.getData().get("titre") + "</h1>");
                        out.println("<ul>");

                        String[] personnes = (String[]) mav.getData().get("personnes");
                        if (personnes != null) {
                            for (String p : personnes) {
                                out.println("<li>" + p + "</li>");
                            }
                        }
                        out.println("</ul>");
                        out.println("<a href='/testapp/'>Retour</a>");
                    }
                } else {
                    Object result = method.invoke(controllerInstance);

                    if (result instanceof ModelAndView) {
                        ModelAndView mav = (ModelAndView) result;

                        out.println("<h1>✅ " + mav.getData().get("titre") + "</h1>");
                        out.println("<ul>");

                        String[] personnes = (String[]) mav.getData().get("personnes");
                        if (personnes != null) {
                            for (String p : personnes) {
                                out.println("<li>" + p + "</li>");
                            }
                        }
                        out.println("</ul>");
                        out.println("<a href='/testapp/'>Retour</a>");
                    }
                }
            } catch (Exception e) {
                out.println("<p>Error: " + e.getMessage() + "</p>");
                e.printStackTrace(out);
            }
        } else {
            out.println("<h3>No mapping found for URL: " + pathInfo + "</h3>");
        }
        out.println("</body></html>");
    }

    private void addAttributesToRequest(HttpServletRequest req, java.util.Map<String, Object> data) {
        if (data != null) {
            for (java.util.Map.Entry<String, Object> entry : data.entrySet()) {
                req.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    private String getPathInfo(HttpServletRequest req) {
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = requestURI.substring(contextPath.length());
        if (path.startsWith("/")) path = path.substring(1);
        return path;
    }
}