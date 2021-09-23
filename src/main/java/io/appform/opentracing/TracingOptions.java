package io.appform.opentracing;

/**
 * Additional options required for tracing
 */
public class TracingOptions {

    private boolean parameterCaptureEnabled;
    private boolean disableCacheOptimisation;


    public boolean isParameterCaptureEnabled() {
        return parameterCaptureEnabled;
    }

    public void setParameterCaptureEnabled(final boolean parameterCaptureEnabled) {
        this.parameterCaptureEnabled = parameterCaptureEnabled;
    }

    public boolean isDisableCacheOptimisation() {
        return disableCacheOptimisation;
    }

    public void setDisableCacheOptimisation(final boolean disableCacheOptimisation) {
        this.disableCacheOptimisation = disableCacheOptimisation;
    }

    public TracingOptions() {
        /* Nothing to do here */
    }

    public static class TracingOptionsBuilder {
        private boolean parameterCaptureEnabled;
        private boolean disableCacheOptimisation;

        public TracingOptionsBuilder parameterCaptureEnabled(final boolean parameterCaptureEnabled) {
            this.parameterCaptureEnabled = parameterCaptureEnabled;
            return this;
        }

        public TracingOptionsBuilder disableCacheOptimisation(final boolean disableCacheOptimisation) {
            this.disableCacheOptimisation = disableCacheOptimisation;
            return this;
        }

        public TracingOptions build() {
            TracingOptions options = new TracingOptions();
            options.setParameterCaptureEnabled(parameterCaptureEnabled);
            options.setDisableCacheOptimisation(disableCacheOptimisation);
            return options;
        }
    }

}
