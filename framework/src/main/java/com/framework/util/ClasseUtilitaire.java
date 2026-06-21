package com.framework.util;

import com.framework.annotation.Controller;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for scanning annotated controllers using Reflections library.
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
}