package io.appform.opentracing;

import java.util.regex.Pattern;

/**
 *
 */
public class TracingConstants {
    static final Pattern VALID_PARAM_VALUE_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");
    static final String PARAMETER_DELIMITER = ".";
    static final String METHOD_NAME_TAG = "method.name";
    static final String CLASS_NAME_TAG = "class.name";
    static final String PARAMETER_STRING_TAG = "method.parameters";
    static final String METHOD_STATUS_TAG = "method.status";
}
