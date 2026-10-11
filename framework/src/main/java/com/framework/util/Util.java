package com.framework.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

public class Util {

    public static boolean haveParameter(Method method, Class<?> paramType) {
        if (method == null || paramType == null) return false;
        for (Class<?> p : method.getParameterTypes()) {
            if (paramType.isAssignableFrom(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si un type est simple (String, int, Integer, etc.)
     */
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

    /**
     * Convertit une String en type simple
     */
    public static Object convertValue(String value, Class<?> type) {
        if (value == null) return null;

        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == short.class || type == Short.class) {
            return Short.parseShort(value);
        } else if (type == byte.class || type == Byte.class) {
            return Byte.parseByte(value);
        }
        return value;
    }

    /**
     * Binding d’un objet à partir des paramètres de la requête
     */
    public static Object bindObject(Class<?> paramType, HttpServletRequest req) throws Exception {
        // 1. Créer une instance
        Object instance = paramType.getDeclaredConstructor().newInstance();

        // 2. Parcourir tous les champs
        for (Field field : paramType.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            String value = req.getParameter(fieldName);

            if (value != null) {
                Object converted = convertValue(value, field.getType());
                field.set(instance, converted);
            }
        }

        return instance;
    }
}