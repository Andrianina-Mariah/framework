package com.framework.servlet;

import com.framework.util.ClasseUtilitaire;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * Front controller servlet that scans for controllers and handles requests.
 */
public class FrontControllerServlet extends HttpServlet {

    private static final String CONTROLLER_PACKAGE_INIT_PARAM = "controllerPackage";
    private static final String DEFAULT_CONTROLLER_PACKAGE = "com.app.controller";

    private Set<String> controllerNames;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String controllerPackage = config.getInitParameter(CONTROLLER_PACKAGE_INIT_PARAM);
        if (controllerPackage == null || controllerPackage.isEmpty()) {
            controllerPackage = DEFAULT_CONTROLLER_PACKAGE;
        }
        this.controllerNames = ClasseUtilitaire.getControllerValues(controllerPackage);
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
        out.println("<h2>Front Controller Servlet</h2>");
        out.println("<p>Requested URI: " + req.getRequestURI() + "</p>");
        out.println("<p>Controller names: " + controllerNames + "</p>");
        out.println("</body></html>");
    }
}