package com.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour indiquer qu'une méthode de controller
 * doit retourner du JSON (Web API / REST)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface APIREST {

    /**
     * true  → le développeur a déjà fourni un JSON (String)
     * false → le framework doit convertir le retour en JSON
     */
    boolean alreadyJson() default false;
}