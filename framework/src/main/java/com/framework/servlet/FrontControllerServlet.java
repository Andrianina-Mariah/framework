package com.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.framework.annotation.APIREST;
import com.framework.model.ModelAndView;
import com.framework.util.ClasseUtilitaire;
import com.framework.util.JsonUtil;

/**
 * Front controller servlet that scans for controllers and handles requests based on URL mappings.
 * Support ModelAndView (vues) + @APIREST (JSON)
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

        // Protection contre les appels aux vues JSP
        if (pathInfo.startsWith("WEB-INF/")) {
            return;
        }

        java.lang.reflect.Method method = null;
        String controllerName = null;
        boolean found = false;

        // Split pathInfo into controller name and method path
        String[] pathParts = pathInfo.split("/", 2);
        String controllerNameFromPath = pathParts[0];
        String methodPath = pathParts.length > 1 ? pathParts[1] : "";

        // Look up controller
        if (urlMappingMap.containsKey(controllerNameFromPath)) {
            Map<String, java.lang.reflect.Method> methodMap = urlMappingMap.get(controllerNameFromPath);

            String httpMethod = req.getMethod();
            String methodKey1 = methodPath + "#" + httpMethod;
            String methodKey2 = "/" + methodPath + "#" + httpMethod;
            String methodKey3 = methodPath.startsWith("/") 
                    ? methodPath.substring(1) + "#" + httpMethod 
                    : (methodPath.isEmpty() ? "#" + httpMethod : "/" + methodPath + "#" + httpMethod);

            if (methodMap.containsKey(methodKey1)) {
                method = methodMap.get(methodKey1);
                controllerName = controllerNameFromPath;
                found = true;
            } else if (methodMap.containsKey(methodKey2)) {
                method = methodMap.get(methodKey2);
                controllerName = controllerNameFromPath;
                found = true;
            } else if (methodMap.containsKey(methodKey3)) {
                method = methodMap.get(methodKey3);
                controllerName = controllerNameFromPath;
                found = true;
            }
        }

        if (found && method != null) {
            try {
                java.lang.Class<?> controllerClass = method.getDeclaringClass();
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

                // Récupération du contexte Spring
                Object result;

                // Vérifier si la méthode attend un WebApplicationContext (Spring)
                boolean needsSpring = false;
                try {
                    needsSpring = ClasseUtilitaire.haveParameter(method, Class.forName("org.springframework.web.context.WebApplicationContext"));
                } catch (ClassNotFoundException e) {
                    // Spring n'est pas dans le classpath → on ignore
                    needsSpring = false;
                }

                if (needsSpring) {
                    ServletContext servletContext = this.getServletConfig().getServletContext();
                    Object springCtxObj = servletContext.getAttribute("springContext");
                    
                    if (springCtxObj == null) {
                        throw new ServletException("Spring WebApplicationContext non disponible.");
                    }
                    
                    result = method.invoke(controllerInstance, springCtxObj);
                } else {
                    // Invocation normale (sans Spring)
                    result = method.invoke(controllerInstance);
                }
                // =====================================================
                // SPRINT 6 – REST API
                // =====================================================
                APIREST apiRest = method.getAnnotation(APIREST.class);

                if (apiRest != null) {
                    // ===== Cas JSON =====
                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");

                    String json;

                    if (apiRest.alreadyJson()) {
                        // Le développeur a déjà fourni un JSON (doit être un String)
                        if (result instanceof String) {
                            json = (String) result;
                        } else {
                            throw new ServletException("Quand alreadyJson=true, la méthode doit retourner un String contenant du JSON");
                        }
                    } else {
                        // Le framework convertit automatiquement
                        json = JsonUtil.toJSON(result);
                    }

                    PrintWriter out = resp.getWriter();
                    out.print(json);
                    out.flush();
                    return; // Pas de RequestDispatcher
                }

                // =====================================================
                // Cas Vue normale (ModelAndView)
                // =====================================================
                if (!(result instanceof ModelAndView)) {
                    throw new ServletException("La méthode doit retourner un ModelAndView ou être annotée @APIREST");
                }

                ModelAndView mav = (ModelAndView) result;
                addAttributesToRequest(req, mav.getData());

                String viewPath = "/WEB-INF/views/" + mav.getView() + ".jsp";
                RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
                dispatcher.forward(req, resp);
                return;

            } catch (Exception e) {
                resp.setContentType("text/html");
                PrintWriter out = resp.getWriter();
                out.println("<html><body>");
                out.println("<h3>Error: " + e.getMessage() + "</h3>");
                e.printStackTrace(out);
                out.println("</body></html>");
            }
        } else {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<html><body>");
            out.println("<h3>No mapping found for URL: " + pathInfo + "</h3>");
            out.println("<p>Available controllers: " + urlMappingMap.keySet() + "</p>");
            out.println("</body></html>");
        }
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