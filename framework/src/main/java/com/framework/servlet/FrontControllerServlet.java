package com.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.framework.model.ModelAndView;
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
        String pathInfo = getPathInfo(req);

        // Protection : ne pas traiter les requêtes vers les vues JSP
        if (pathInfo.startsWith("WEB-INF/") || pathInfo.startsWith("/WEB-INF/")) {
            return;
        }

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");

        out.println("<p>Requested URI: " + req.getRequestURI() + "</p>");
        out.println("<p>Path info: " + pathInfo + "</p>");

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

        out.println("<p>Controller names: " + urlMappingMap.keySet() + "</p>");

        if (found && method != null) {
            try {
                java.lang.Class<?> controllerClass = method.getDeclaringClass();
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
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
            } catch (Exception e) {
                out.println("<p>Error: " + e.getMessage() + "</p>");
                e.printStackTrace(out);
            }
        } else {
            out.println("<h3>No mapping found for URL: " + pathInfo + "</h3>");
        }        out.println("</body></html>");
    }

    private void addAttributesToRequest(HttpServletRequest req, Map<String, Object> data) {
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
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