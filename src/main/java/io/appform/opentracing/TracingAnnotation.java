package io.appform.opentracing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be added on methods to trace method calls
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TracingAnnotation {

    /**
     * Override the classname being pushed into spans.
     * @return Class name if provided otherwise the actual class name is used.
     */
    String className() default "";

    /**
     * Override the method name being pushed into spans.
     * @return Method name if provided, otherwise actual method name is used.
     */
    String method() default "";

}
