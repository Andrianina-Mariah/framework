package com.framework.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utilitaire pour convertir des objets en JSON
 */
public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Convertit n'importe quel objet en JSON
     */
    public static String toJSON(Object object) {
        if (object == null) {
            return "null";
        }
        return gson.toJson(object);
    }
}