package com.framework.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.framework.annotation.APIREST;

public class ResponseHelper {

    /**
     * HTTP 401 - Non authentifié
     */
    public static void handleUnauthorized(HttpServletRequest req, HttpServletResponse resp, boolean isApi)
            throws IOException {

        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        if (isApi) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"error\":\"Unauthorized\",\"message\":\"Vous devez être authentifié\"}");
            out.flush();
        } else {
            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<html><body>");
            out.println("<h2>401 - Non authentifié</h2>");
            out.println("<p>Vous devez vous connecter pour accéder à cette ressource.</p>");
            out.println("<a href=\"/testapp/session/login\">Se connecter</a>");
            out.println("</body></html>");
        }
    }

    /**
     * HTTP 403 - Authentifié mais pas le bon rôle
     */
    public static void handleForbidden(HttpServletRequest req, HttpServletResponse resp, boolean isApi)
            throws IOException {

        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

        if (isApi) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"error\":\"Forbidden\",\"message\":\"Accès refusé : rôle insuffisant\"}");
            out.flush();
        } else {
            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<html><body>");
            out.println("<h2>403 - Accès interdit</h2>");
            out.println("<p>Vous n'avez pas le rôle requis pour accéder à cette ressource.</p>");
            out.println("<a href=\"/testapp/\">Retour</a>");
            out.println("</body></html>");
        }
    }

    /**
     * Détermine si la requête est une API (annotation @APIREST)
     */
    public static boolean isApiRequest(java.lang.reflect.Method method) {
        return method.isAnnotationPresent(APIREST.class);
    }
}