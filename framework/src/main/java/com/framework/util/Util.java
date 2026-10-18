package com.framework.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

public class Util {

    public static boolean haveParameter(Method method, Class<?> paramType) {
        if (method == null || paramType == null) return false;
        for (Class<?> p : method.getParameterTypes()) {
            if (paramType.isAssignableFrom(p)) return true;
        }
        return false;
    }

    public static boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == double.class || type == Double.class
                || type == float.class || type == Float.class
                || type == boolean.class || type == Boolean.class
                || type == short.class || type == Short.class
                || type == byte.class || type == Byte.class
                || type == char.class || type == Character.class;
    }

    public static Object convertValue(String value, Class<?> type) {
        if (value == null) return null;

        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        if (type == float.class || type == Float.class) return Float.parseFloat(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        if (type == short.class || type == Short.class) return Short.parseShort(value);
        if (type == byte.class || type == Byte.class) return Byte.parseByte(value);

        return value;
    }

    /**
     * Lit un Part et retourne son contenu en byte[]
     */
    public static byte[] readPartBytes(Part part) throws Exception {
        if (part == null || part.getSize() <= 0) return null;
        return part.getInputStream().readAllBytes();
    }

    /**
     * Binding d'un objet (champs simples + fichiers byte[])
     */
    public static Object bindObject(Class<?> paramType, HttpServletRequest req) throws Exception {
        Object instance = paramType.getDeclaredConstructor().newInstance();

        for (Field field : paramType.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();

            if (fieldType == byte[].class) {
                // ===== Fichier =====
                Part part = req.getPart(fieldName);
                field.set(instance, readPartBytes(part));

            } else if (isSimpleType(fieldType)) {
                // ===== Champ normal =====
                String value = req.getParameter(fieldName);
                field.set(instance, convertValue(value, fieldType));
            }
            // autres types : ignorer pour l'instant
        }

        return instance;
    }
}