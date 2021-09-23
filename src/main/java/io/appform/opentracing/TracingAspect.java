package io.appform.opentracing;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.appform.opentracing.TracingConstants.VALID_PARAM_VALUE_PATTERN;

/**
 * Only methods annotated with {@link TracingAnnotation} are traced
 */
@Aspect
public class TracingAspect {
    private static final Logger log = LoggerFactory.getLogger(TracingAspect.class.getSimpleName());

    private final Map<String, FunctionData> paramCache = new ConcurrentHashMap<>();


    @Pointcut("@annotation(io.appform.opentracing.TracingAnnotation)")
    public void tracingAnnotationCalled() {
        //Empty as required
    }

    @Pointcut("execution(* *(..))")
    public void anyFunctionCalled() {
        //Empty as required
    }

    @Around("tracingAnnotationCalled() && anyFunctionCalled()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        final Signature callSignature = joinPoint.getSignature();
        final TracingOptions options = TracingManager.getTracingOptions();
        final MethodSignature methodSignature = (MethodSignature) callSignature;

        final FunctionData functionData = cacheDisabled(options)
                ? getFunctionData(callSignature, methodSignature)
                : paramCache.computeIfAbsent(callSignature.toLongString(), key -> getFunctionData(callSignature, methodSignature));

        final String parameterString = getParameterString(options, methodSignature, joinPoint,
                functionData.getClassName(), functionData.getMethodName());

        Span span = null;
        Scope scope = null;
        try {
            final Tracer tracer = TracingHandler.getTracer();
            span = TracingHandler.startSpan(tracer, functionData, parameterString);
            scope = TracingHandler.startScope(tracer, span);
            final Object response = joinPoint.proceed();
            TracingHandler.addSuccessTagToSpan(span);
            return response;
        } catch (Throwable t) {
            TracingHandler.addErrorTagToSpan(span);
            throw t;
        } finally {
            TracingHandler.closeSpanAndScope(span, scope);
        }
    }

    private FunctionData getFunctionData(final Signature callSignature,
                                         final MethodSignature methodSignature) {
        final TracingAnnotation tracingAnnotation = methodSignature.getMethod().getAnnotation(TracingAnnotation.class);
        final String className = getClassName(tracingAnnotation, callSignature);
        final String methodName = getMethodName(tracingAnnotation, callSignature);
        return new FunctionData(className, methodName);
    }

    private String getClassName(final TracingAnnotation tracingAnnotation,
                                final Signature callSignature) {
        return Strings.isNullOrEmpty(tracingAnnotation.className())
                ? callSignature.getDeclaringType().getSimpleName()
                : tracingAnnotation.className();
    }

    private String getMethodName(final TracingAnnotation tracingAnnotation,
                                 final Signature callSignature) {
        return Strings.isNullOrEmpty(tracingAnnotation.method())
                ? callSignature.getName()
                : tracingAnnotation.method();
    }

    private String getParameterString(final TracingOptions tracingOptions,
                                      final MethodSignature methodSignature,
                                      final ProceedingJoinPoint joinPoint,
                                      final String className,
                                      final String methodName) {
        if (tracingOptions == null || !tracingOptions.isParameterCaptureEnabled()) {
            return null;
        }

        if (methodSignature.getMethod().getParameterCount() != joinPoint.getArgs().length) {
            log.warn("Number of parameters does not match with args [class = {}, method = {}]", className, methodName);
            return null;
        }

        final List<String> paramValues
                = IntStream.range(0, methodSignature.getMethod().getParameterCount())
                .mapToObj(i -> {
                    final TracingParameter tracingParameter = methodSignature.getMethod()
                            .getParameters()[i].getAnnotation(TracingParameter.class);
                    if (tracingParameter == null) {
                        return null;
                    }
                    final String paramValueStr = convertToString(joinPoint.getArgs()[i]).trim();
                    boolean matches = VALID_PARAM_VALUE_PATTERN.matcher(paramValueStr).matches();
                    return matches ? paramValueStr : "";
                })
                .filter(Objects::nonNull)
                .filter(value -> !Strings.isNullOrEmpty(value))
                .collect(Collectors.toList());

        if (!paramValues.isEmpty()) {
            return Joiner.on(TracingConstants.PARAMETER_DELIMITER).join(paramValues);
        }

        return null;
    }

    private String convertToString(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof Enum) {
            return ((Enum) obj).name();
        }
        return "";
    }

    private boolean cacheDisabled(final TracingOptions options) {
        return options != null && options.isDisableCacheOptimisation();
    }
}
