package io.appform.opentracing;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

/**
 * Additional options required for tracing
 */
public class TracingOptions {

    private boolean parameterCaptureEnabled;

    private Converter<String, String> caseFormatConverter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_CAMEL);

    public boolean isParameterCaptureEnabled() {
        return parameterCaptureEnabled;
    }

    public void setParameterCaptureEnabled(final boolean parameterCaptureEnabled) {
        this.parameterCaptureEnabled = parameterCaptureEnabled;
    }

    public Converter<String, String> getCaseFormatConverter() {
        return caseFormatConverter;
    }

    public void setCaseFormatConverter(final Converter<String, String> caseFormatConverter) {
        this.caseFormatConverter = caseFormatConverter;
    }

    public TracingOptions() {
        /* Nothing to do here */
    }

    public static class TracingOptionsBuilder {
        private boolean parameterCaptureEnabled;
        private Converter<String, String> caseFormatConverter;

        public TracingOptionsBuilder parameterCaptureEnabled(final boolean parameterCaptureEnabled) {
            this.parameterCaptureEnabled = parameterCaptureEnabled;
            return this;
        }

        public TracingOptionsBuilder caseFormatConverter(final Converter<String, String> caseFormatConverter) {
            this.caseFormatConverter = caseFormatConverter;
            return this;
        }

        public TracingOptions build() {
            TracingOptions options = new TracingOptions();
            if (caseFormatConverter != null) {
                options.setCaseFormatConverter(caseFormatConverter);
            }
            options.setParameterCaptureEnabled(parameterCaptureEnabled);
            return options;
        }
    }

}
