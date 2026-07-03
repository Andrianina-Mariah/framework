package com.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom UrlMapping annotation to map a URL pattern and HTTP method to a controller method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlMapping {
    /**
     * The URL pattern to map to this method.
     * @return the URL pattern
     */
    String value();

    /**
     * The HTTP method for this mapping.
     * @return the HTTP method
     */
    HttpMethod method() default HttpMethod.GET;

    /**
     * Supported HTTP methods.
     */
    enum HttpMethod {
        GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH, TRACE
    }
}
