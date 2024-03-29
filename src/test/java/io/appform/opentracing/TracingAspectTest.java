package io.appform.opentracing;

import com.google.common.base.Stopwatch;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TracingAspect related tests
 */
class TracingAspectTest {

    private static MockTracer mockTracer = new MockTracer();

    @BeforeAll
    static void beforeAll() throws NoSuchFieldException, IllegalAccessException {
        Field globalTracerField = GlobalTracer.class.getDeclaredField("tracer");
        globalTracerField.setAccessible(true);
        globalTracerField.set(null, mockTracer);
        globalTracerField.setAccessible(false);
    }

    @BeforeEach
    void setup() {
        GlobalTracer.registerIfAbsent(mockTracer);
    }

    @AfterEach
    void cleanup() {
        mockTracer.reset();

    }

    @Test
    void testTracingWhenExceptionInMethodCall() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        Assertions.assertNotNull(GlobalTracer.get().activeSpan());
        final TestAnnotation testAnnotation = new TestAnnotation();
        Assertions.assertThrows(RuntimeException.class, testAnnotation::throwException);
        Assertions.assertNotNull(GlobalTracer.get().activeSpan());
        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        Assertions.assertEquals(1, finishedSpans.size());
        MockSpan finishedSpan = finishedSpans.get(0);
        assertSpanMetaData(finishedSpan, "method:throwException", "throwException", "TestAnnotation", null,
                "FAILURE");
    }

    @Test
    void testTracingWhenMethodAndClassNameAreOverloaded() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        TracingManager.initialize(new TracingOptions.TracingOptionsBuilder().parameterCaptureEnabled(true).build());
        Assertions.assertNotNull(GlobalTracer.get().activeSpan());
        final TestAnnotation testAnnotation = new TestAnnotation();
        Assertions.assertDoesNotThrow(() -> testAnnotation.overloadedFunction("test", 2));
        Assertions.assertNotNull(GlobalTracer.get().activeSpan());

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        Assertions.assertEquals(1, finishedSpans.size());
        MockSpan finishedSpan = finishedSpans.get(0);
        assertSpanMetaData(finishedSpan, "method:overloadedMethodName", "overloadedMethodName", "OverloadedClassName", null,
                "SUCCESS");
    }


    @Test
    void testTracingWhenParameterCaptureIsEnabledForValidArguments() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        TracingManager.initialize(new TracingOptions.TracingOptionsBuilder().parameterCaptureEnabled(true).build());
        Assertions.assertNotNull(GlobalTracer.get().activeSpan());
        final TestAnnotation testAnnotation = new TestAnnotation();
        Assertions.assertDoesNotThrow(() -> testAnnotation.parameterValidFunction("test1", "test2"));
        Assertions.assertNotNull(GlobalTracer.get().activeSpan());

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        Assertions.assertEquals(1, finishedSpans.size());
        MockSpan finishedSpan = finishedSpans.get(0);
        assertSpanMetaData(finishedSpan, "method:parameterValidFunction", "parameterValidFunction", "TestAnnotation",
                "test1.test2", "SUCCESS");
    }

    @Test
    void testTracingWhenParameterCaptureIsEnabledWithNoArguments() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        TracingManager.initialize(new TracingOptions.TracingOptionsBuilder().parameterCaptureEnabled(true).build());
        Assertions.assertNotNull(GlobalTracer.get().activeSpan());
        final TestAnnotation testAnnotation = new TestAnnotation();
        Assertions.assertDoesNotThrow(testAnnotation::noArgsFunction);
        Assertions.assertNotNull(GlobalTracer.get().activeSpan());

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        Assertions.assertEquals(1, finishedSpans.size());
        MockSpan finishedSpan = finishedSpans.get(0);
        assertSpanMetaData(finishedSpan, "method:noArgsFunction", "noArgsFunction", "TestAnnotation",
                null, "SUCCESS");
    }

    @Test
    void testTracingWhenParameterCaptureIsEnabledForInValidArguments() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        TracingManager.initialize(new TracingOptions.TracingOptionsBuilder().parameterCaptureEnabled(true).build());
        Assertions.assertNotNull(GlobalTracer.get().activeSpan());
        final TestAnnotation testAnnotation = new TestAnnotation();
        Assertions.assertDoesNotThrow(() -> testAnnotation.invalidArgsFunction(mockTracer));
        Assertions.assertNotNull(GlobalTracer.get().activeSpan());

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        Assertions.assertEquals(1, finishedSpans.size());
        MockSpan finishedSpan = finishedSpans.get(0);
        assertSpanMetaData(finishedSpan, "method:invalidArgsFunction", "invalidArgsFunction", "TestAnnotation",
                null, "SUCCESS");
    }

    @Test
    void testTracingWithMultipleInvocationsAndCacheEnabled() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        TracingManager.initialize(new TracingOptions.TracingOptionsBuilder().parameterCaptureEnabled(true).build());
        final TestAnnotation testAnnotation = new TestAnnotation();
        Assertions.assertDoesNotThrow(() -> testAnnotation.parameterValidFunction("test1", "test2"));
        Assertions.assertDoesNotThrow(() -> testAnnotation.parameterValidFunction("test3", "test4"));

        Assertions.assertNotNull(GlobalTracer.get().activeSpan());

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        Assertions.assertEquals(2, finishedSpans.size());
        assertSpanMetaData(finishedSpans.get(0), "method:parameterValidFunction", "parameterValidFunction", "TestAnnotation",
                "test1.test2", "SUCCESS");
        assertSpanMetaData(finishedSpans.get(1), "method:parameterValidFunction", "parameterValidFunction", "TestAnnotation",
                "test3.test4", "SUCCESS");

    }

    @Test
    void testTracingWithMultipleInvocationsAndCacheDisabled() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        TracingManager.initialize(new TracingOptions.TracingOptionsBuilder().parameterCaptureEnabled(true)
                .disableCacheOptimisation(true)
                .build());
        final TestAnnotation testAnnotation = new TestAnnotation();

        Assertions.assertDoesNotThrow(() -> testAnnotation.parameterValidFunction("test1", "test2"));
        Assertions.assertDoesNotThrow(() -> testAnnotation.parameterValidFunction("test3", "test4"));

        Assertions.assertNotNull(GlobalTracer.get().activeSpan());

        List<MockSpan> finishedSpans = mockTracer.finishedSpans();
        Assertions.assertEquals(2, finishedSpans.size());
        assertSpanMetaData(finishedSpans.get(0), "method:parameterValidFunction", "parameterValidFunction", "TestAnnotation",
                "test1.test2", "SUCCESS");
        assertSpanMetaData(finishedSpans.get(1), "method:parameterValidFunction", "parameterValidFunction", "TestAnnotation",
                "test3.test4", "SUCCESS");
    }

    @Test
    void testCachingMT() {
        final double avgTime = runMTTest();
        System.out.println("Time taken for MT test: " + avgTime);
        TracingManager.initialize(new TracingOptions.TracingOptionsBuilder().parameterCaptureEnabled(true)
                .disableCacheOptimisation(true)
                .build());
        final double avgTimeNoCache = runMTTest();
        System.out.println("Time taken for MT test without cache: " + avgTimeNoCache);
        assertTrue(avgTimeNoCache > avgTime);
    }

    private double runMTTest() {
        final int numThreads = 10;
        final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        final TestAnnotation testAnnotation = new TestAnnotation();
        final List<Future<Long>> futures = IntStream.range(0, numThreads)
                .mapToObj(i -> executorService.submit(() -> {
                    final Stopwatch stopwatch = Stopwatch.createStarted();
                    IntStream.range(0, 1000_000).forEach(j -> testAnnotation.noArgsFunction());
                    final long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                    System.out.println("Done test: " + i + " elapsed: " + elapsed);
                    return elapsed;
                }))
                .collect(Collectors.toList());
        final long total = futures.stream()
                .mapToLong(f -> {
                    try {
                        return f.get();
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return 0;
                    }
                    catch (ExecutionException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .sum();
        return ((double) total) / numThreads;
    }

    private void assertSpanMetaData(final MockSpan finishedSpan,
                                    final String operationName,
                                    final String methodName,
                                    final String className,
                                    final String paramName,
                                    final String status) {
        Assertions.assertEquals(operationName, finishedSpan.operationName());
        Map<String, Object> tags = finishedSpan.tags();
        Assertions.assertEquals(methodName, tags.get(TracingConstants.METHOD_NAME_TAG));
        Assertions.assertEquals(className, tags.get(TracingConstants.CLASS_NAME_TAG));
        Assertions.assertEquals(paramName, tags.get(TracingConstants.PARAMETER_STRING_TAG));
        Assertions.assertEquals(status, tags.get(TracingConstants.METHOD_STATUS_TAG));
    }


    private class TestAnnotation {

        @TracingAnnotation
        private void throwException() {
            throw new RuntimeException("Test exception");
        }

        @TracingAnnotation(method = "overloadedMethodName", className = "OverloadedClassName")
        private void overloadedFunction(String x, int y) {
            System.out.println("Val: " + Objects.toString(x + y));
        }

        @TracingAnnotation()
        private void parameterValidFunction(@TracingParameter String x, @TracingParameter String y) {
            System.out.println(String.format("x = %s, y = %s", x, y));
        }

        @TracingAnnotation(method = "noArgsFunction")
        private void noArgsFunction() {
            //Empty as needed
        }

        @TracingAnnotation()
        private void invalidArgsFunction(@TracingParameter MockTracer mockTracer) {
            System.out.println("Invalid args");
        }
    }
}
