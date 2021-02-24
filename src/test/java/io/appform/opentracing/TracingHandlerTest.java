package io.appform.opentracing;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.noop.NoopScopeManager;
import io.opentracing.noop.NoopSpan;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.ThreadLocalScope;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases related to TracingHandler
 */
class TracingHandlerTest {

    private static MockTracer mockTracer = new MockTracer();

    @BeforeEach
    void setup() {
        GlobalTracer.registerIfAbsent(mockTracer);
    }

    @AfterEach
    void cleanup() {
        mockTracer.reset();
    }

    @Test
    void testGetTracer() {
        Tracer tracer = TracingHandler.getTracer();
        Assertions.assertNotNull(tracer);
    }

    @Test
    void testStartSpan() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        final String methodName = "test";
        final String className = "testClass";
        Assertions.assertNull(TracingHandler.startSpan(null, null, "test", null));
        Span span = TracingHandler.startSpan(GlobalTracer.get(), methodName, className, "test");
        Assertions.assertNotNull(span);
        Assertions.assertTrue(span instanceof MockSpan);
        MockSpan mockSpan = (MockSpan) span;
        Map<String, Object> tags = mockSpan.tags();
        Assertions.assertEquals(methodName, tags.get(TracingConstants.METHOD_NAME_TAG));
        Assertions.assertEquals(className, tags.get(TracingConstants.CLASS_NAME_TAG));
        Assertions.assertEquals("test", tags.get(TracingConstants.PARAMETER_STRING_TAG));
    }

    @Test
    void testStartScope() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        Assertions.assertNull(TracingHandler.startScope(null, NoopSpan.INSTANCE));
        Assertions.assertNull(TracingHandler.startScope(GlobalTracer.get(), null));
        Scope scope = TracingHandler.startScope(GlobalTracer.get(), mockTracer.activeSpan());
        Assertions.assertNotNull(scope);
        Assertions.assertTrue(scope instanceof ThreadLocalScope);
    }

    @Test
    void testAddSuccessTagToSpan() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        Assertions.assertDoesNotThrow(() -> TracingHandler.addSuccessTagToSpan(null));
        Assertions.assertDoesNotThrow(() -> TracingHandler.addSuccessTagToSpan(mockTracer.activeSpan()));
        Map<String, Object> tags = ((MockSpan) mockTracer.activeSpan()).tags();
        Assertions.assertEquals("SUCCESS", tags.get(TracingConstants.METHOD_STATUS_TAG));
    }

    @Test
    void testAddErrorTagToSpan() {
        mockTracer.activateSpan(mockTracer.buildSpan("test").start());
        Assertions.assertDoesNotThrow(() -> TracingHandler.addErrorTagToSpan(null));
        Assertions.assertDoesNotThrow(() -> TracingHandler.addErrorTagToSpan(mockTracer.activeSpan()));
        Map<String, Object> tags = ((MockSpan) mockTracer.activeSpan()).tags();
        Assertions.assertEquals("FAILURE", tags.get(TracingConstants.METHOD_STATUS_TAG));
    }

    @Test
    void testCloseSpanAndScope() {
        Assertions.assertDoesNotThrow(() -> TracingHandler.closeSpanAndScope(null, null));
        Assertions.assertDoesNotThrow(() -> TracingHandler.closeSpanAndScope(null, NoopScopeManager.NoopScope.INSTANCE));
        Assertions.assertDoesNotThrow(() -> TracingHandler.closeSpanAndScope(NoopSpan.INSTANCE, null));
        Assertions.assertDoesNotThrow(() -> TracingHandler.closeSpanAndScope(NoopSpan.INSTANCE, NoopScopeManager.NoopScope.INSTANCE));
    }
}
