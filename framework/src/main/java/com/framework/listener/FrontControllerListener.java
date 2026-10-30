package com.framework.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class FrontControllerListener implements ServletContextListener {

    private static final String SPRING_ROOT =
            "org.springframework.web.context.WebApplicationContext.ROOT";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        // Spring (si présent)
        Object springContext = ctx.getAttribute(SPRING_ROOT);
        ctx.setAttribute("springContext", springContext);

        // Nom de l'attribut user dans la session (Sprint 10)
        String userAttr = ctx.getInitParameter("userSessionAttribute");
        if (userAttr == null || userAttr.isEmpty()) {
            userAttr = "user";
        }
        ctx.setAttribute("userSessionAttribute", userAttr);

        System.out.println("FrontControllerListener OK");
        System.out.println("userSessionAttribute = " + userAttr);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("FrontControllerListener destroyed");
    }
}