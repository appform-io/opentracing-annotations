package io.appform.opentracing;

/**
 * Additional options required for tracing
 */
public class TracingOptions {

    private boolean parameterCaptureEnabled;

    public boolean isParameterCaptureEnabled() {
        return parameterCaptureEnabled;
    }

    public void setParameterCaptureEnabled(final boolean parameterCaptureEnabled) {
        this.parameterCaptureEnabled = parameterCaptureEnabled;
    }

    public TracingOptions() {
        /* Nothing to do here */
    }

    public static class TracingOptionsBuilder {
        private boolean parameterCaptureEnabled;

        public TracingOptionsBuilder parameterCaptureEnabled(final boolean parameterCaptureEnabled) {
            this.parameterCaptureEnabled = parameterCaptureEnabled;
            return this;
        }

        public TracingOptions build() {
            TracingOptions options = new TracingOptions();
            options.setParameterCaptureEnabled(parameterCaptureEnabled);
            return options;
        }
    }

}
