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
     * and the value is a map of URL pattern + "#" + HTTP method to method.
     *
     * @param basePackage the base package to scan
     * @return a map of controller name to map of URL pattern + method to method
     * @throws IllegalStateException if there are conflicting URL/method mappings
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
                    UrlMapping.HttpMethod httpMethod = urlAnn.method();
                    String key = urlPattern + "#" + httpMethod;

                    // Check for duplicate mapping
                    if (methodMap.containsKey(key)) {
                        throw new IllegalStateException(
                                "Mapping dupliqué trouvé dans controller '" + controllerName +
                                "': URL '" + urlPattern + "' avec la méthode '" + httpMethod +
                                "' qui est déjà mapper à la méthode '" + methodMap.get(key).getName() +
                                "'. Conflit de méthode: '" + method.getName() + "'");
                    }

                    methodMap.put(key, method);
                }
            }
            if (!methodMap.isEmpty()) {
                controllerMap.put(controllerName, methodMap);
            }
        }

        return controllerMap;
    }

    /**
     * Vérifie si la méthode donnée a un paramètre de type spécifié.
     *
     * @param methode la méthode à tester
     * @param param   le type de paramètre recherché
     * @return vrai si la méthode a au moins un paramètre de type param
     */
    public static boolean haveParameter(java.lang.reflect.Method methode, Class<?> param) {
        for (Class<?> p : methode.getParameterTypes()) {
            if (param.isAssignableFrom(p)) {
                return true;
            }
        }
        return false;
    }
}