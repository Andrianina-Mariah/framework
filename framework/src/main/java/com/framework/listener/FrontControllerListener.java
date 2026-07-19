package com.framework.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * Listener that initializes the FrontController when the web application starts.
 */
public class FrontControllerListener implements ServletContextListener {

    private static final String SPRING_ROOT = "org.springframework.web.context.WebApplicationContext.ROOT";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Initialization logic for FrontController can be placed here
        System.out.println("FrontControllerListener: contextInitialized called.");
        // For example, you could scan controllers and store them in the ServletContext
        // sce.getServletContext().setAttribute("controllerMap", controllerMap);
        ServletContext servletContext = sce.getServletContext();
        servletContext.setAttribute("springContext", servletContext.getAttribute(SPRING_ROOT));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup resources if needed
        System.out.println("FrontControllerListener: contextDestroyed called.");
    }
}