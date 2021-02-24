package io.appform.opentracing;

/**
 * Tracing manager that needs to be initialized at the start
 */
public class TracingManager {

    private static TracingOptions tracingOptions;

    private TracingManager() {
    }

    public static void initialize(final TracingOptions tracingOptions) {
        TracingManager.tracingOptions = tracingOptions;
    }

    public static TracingOptions getTracingOptions() {
        return tracingOptions;
    }
}
