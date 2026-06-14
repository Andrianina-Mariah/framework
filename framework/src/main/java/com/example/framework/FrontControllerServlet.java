package com.example.framework;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Simple Front Controller servlet mimicking Spring MVC DispatcherServlet.
 * Handles GET requests by delegating to processRequest (to be implemented by subclasses or framework logic).
 * Handles POST requests by printing the request URL to the response.
 */
public class FrontControllerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Process GET request – delegate to processRequest method (can be overridden)
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // For POST, simply output the request URL
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        out.print(req.getRequestURL());
        out.flush();
    }

    /**
     * Placeholder for request processing logic.
     * In a real framework, this would route to controllers, handle model binding, etc.
     * Subclasses can override to provide custom behavior.
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Default implementation: respond with a simple message
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<h1>FrontControllerServlet processed GET request</h1>");
        out.println("<p>Path: " + req.getRequestURI() + "</p>");
        out.flush();
    }
}