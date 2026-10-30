package com.framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Parameter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.framework.annotation.APIREST;
import com.framework.model.ModelAndView;
import com.framework.session.MaSession;
import com.framework.util.ClasseUtilitaire;
import com.framework.util.JsonUtil;
import com.framework.util.Util;
import com.framework.annotation.Auth;
import com.framework.util.ResponseHelper;

/**
 * Front controller servlet that scans for controllers and handles requests based on URL mappings.
 * Support ModelAndView (vues) + @APIREST (JSON)
 */
public class FrontControllerServlet extends HttpServlet {

    private static final String CONTROLLER_PACKAGE_INIT_PARAM = "controllerPackage";
    private static final String DEFAULT_CONTROLLER_PACKAGE = "com.app.controller";

    private Map<String, Map<String, java.lang.reflect.Method>> urlMappingMap;
    private final Map<Class<?>, Object> controllerInstances = new HashMap<>();

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
                // ===== Sprint 10 : Auth =====
                if (!checkAuthorization(method, req, resp)) {
                    return; // 401 ou 403 déjà envoyé
                }
                Class<?> controllerClass = method.getDeclaringClass();

                // ===== 1 instance de controller (singleton) =====
                Object controllerInstance = getControllerInstance(controllerClass);

                // ===== Résolution des arguments =====
                Parameter[] methodParams = method.getParameters();
                Object[] args = new Object[methodParams.length];
                MaSession maSession = null;

                for (int i = 0; i < methodParams.length; i++) {
                    Parameter param = methodParams[i];
                    Class<?> paramType = param.getType();
                    String paramName = param.getName();

                    if (paramType == MaSession.class) {
                        maSession = MaSession.from(req.getSession(true));
                        args[i] = maSession;

                    } else if (paramType == Part.class) {
                        args[i] = req.getPart(paramName);

                    } else if (paramType == byte[].class) {
                        Part part = req.getPart(paramName);
                        args[i] = Util.readPartBytes(part);

                    } else if (Util.isSimpleType(paramType)) {
                        String value = req.getParameter(paramName);
                        args[i] = Util.convertValue(value, paramType);

                    } else {
                        args[i] = Util.bindObject(paramType, req);
                    }
                }

                // ===== Invocation =====
                Object result = method.invoke(controllerInstance, args);

                // ===== Flush session =====
                if (maSession != null) {
                    maSession.flushTo(req.getSession(false));
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

    //méthode pour récupèrer l'instance unique
    private Object getControllerInstance(Class<?> controllerClass) throws Exception {
        Object instance = controllerInstances.get(controllerClass);
        if (instance == null) {
            instance = controllerClass.getDeclaredConstructor().newInstance();
            controllerInstances.put(controllerClass, instance);
            System.out.println("Nouvelle instance créée pour : " + controllerClass.getSimpleName());
        } else {
            System.out.println("Réutilisation de l'instance : " + controllerClass.getSimpleName());
        }
        return instance;
    }

    private boolean checkAuthorization(Method method, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // 1. Récupérer @Auth : méthode prioritaire, sinon classe
        Auth auth = method.getAnnotation(Auth.class);
        if (auth == null) {
            auth = method.getDeclaringClass().getAnnotation(Auth.class);
        }

        // Pas d'annotation @Auth → accès libre
        if (auth == null) {
            return true;
        }

        boolean isApi = ResponseHelper.isApiRequest(method);

        // 2. Récupérer la session
        javax.servlet.http.HttpSession httpSession = req.getSession(false);
        if (httpSession == null) {
            ResponseHelper.handleUnauthorized(req, resp, isApi);
            return false;
        }

        // 3. Vérifier présence user / rôle
        String userAttrName = (String) getServletContext().getAttribute("userSessionAttribute");
        String roleAttrName = (String) getServletContext().getAttribute("roleSessionAttribute");

        if (userAttrName == null) userAttrName = "user";
        if (roleAttrName == null) roleAttrName = "role";

        Object user = httpSession.getAttribute(userAttrName);
        Object roleObj = httpSession.getAttribute(roleAttrName);

        // Pas authentifié
        if (user == null) {
            ResponseHelper.handleUnauthorized(req, resp, isApi);
            return false;
        }

        String requiredRole = auth.role();

        // role vide → authentification seule suffit
        if (requiredRole == null || requiredRole.trim().isEmpty()) {
            return true;
        }

        // Vérifier le rôle
        boolean hasRole = false;

        if (roleObj instanceof String) {
            hasRole = requiredRole.equals(roleObj);
        } else if (roleObj instanceof java.util.Collection) {
            hasRole = ((java.util.Collection<?>) roleObj).contains(requiredRole);
        }

        if (!hasRole) {
            ResponseHelper.handleForbidden(req, resp, isApi);
            return false;
        }

        return true;
    }
}