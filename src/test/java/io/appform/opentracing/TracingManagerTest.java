package io.appform.opentracing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tracing Manager Test
 */
class TracingManagerTest {

    @Test
    void testGetTracingOptions() {
        TracingManager.initialize(null);
        Assertions.assertNull(TracingManager.getTracingOptions());

        TracingOptions tracingOptions = new TracingOptions.TracingOptionsBuilder()
                .parameterCaptureEnabled(true)
                .build();
        TracingManager.initialize(tracingOptions);
        TracingOptions result = TracingManager.getTracingOptions();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isParameterCaptureEnabled());
    }
}
