package com.framework.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

public class MaSession {

    private String id;
    private Map<String, Object> attributes = new HashMap<>();
    private Set<String> initialKeys = new HashSet<>();

    public MaSession() {
    }

    public static MaSession from(HttpSession httpSession) {
        MaSession session = new MaSession();
        session.id = httpSession.getId();

        httpSession.getAttributeNames().asIterator().forEachRemaining(name -> {
            session.attributes.put(name, httpSession.getAttribute(name));
            session.initialKeys.add(name);
        });

        return session;
    }

    public void flushTo(HttpSession httpSession) {
        if (httpSession == null) return;

        // Supprimer les clés initiales qui n'existent plus
        for (String key : initialKeys) {
            if (!attributes.containsKey(key)) {
                httpSession.removeAttribute(key);
            }
        }

        // Ajouter / mettre à jour
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            httpSession.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}