package com.framework.util;

import com.framework.annotation.Controller;
import com.framework.annotation.UrlMapping;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Utility class for scanning annotated controllers and their URL mappings using Reflections library.
 */
public class ClasseUtilitaire {

    /**
     * Scans the given base package for classes annotated with Controller and returns their annotation values.
     *
     * @param basePackage the base package to scan
     * @return a set of controller annotation values
     */
    public static Set<String> getControllerValues(String basePackage) {
        ConfigurationBuilder config = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(basePackage))
                .setScanners(Scanners.TypesAnnotated);

        Reflections reflections = new Reflections(config);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Controller.class);

        return annotatedClasses.stream()
                .map(clazz -> clazz.getAnnotation(Controller.class).value())
                .collect(Collectors.toSet());
    }

    /**
     * Scans the given base package for controllers and their URL-mapped methods.
     * Returns a map where the key is the controller name (from @Controller value),
     * and the value is a map of URL pattern to method.
     *
     * @param basePackage the base package to scan
     * @return a map of controller name to map of URL pattern to method
     */
    public static Map<String, Map<String, Method>> getUrlMappingMap(String basePackage) {
        ConfigurationBuilder config = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(basePackage))
                .setScanners(Scanners.TypesAnnotated);

        Reflections reflections = new Reflections(config);
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);

        Map<String, Map<String, Method>> controllerMap = new HashMap<>();

        for (Class<?> clazz : controllerClasses) {
            Controller controllerAnn = clazz.getAnnotation(Controller.class);
            if (controllerAnn == null) {
                continue;
            }
            String controllerName = controllerAnn.value();

            Map<String, Method> methodMap = new HashMap<>();
            for (Method method : clazz.getDeclaredMethods()) {
                UrlMapping urlAnn = method.getAnnotation(UrlMapping.class);
                if (urlAnn != null) {
                    String urlPattern = urlAnn.value();
                    methodMap.put(urlPattern, method);
                }
            }
            if (!methodMap.isEmpty()) {
                controllerMap.put(controllerName, methodMap);
            }
        }

        return controllerMap;
    }
}