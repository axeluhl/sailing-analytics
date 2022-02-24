package com.sap.sse.landscape;

import java.util.Map;

import com.sap.sse.common.Util;

/**
 * Configuration options can provide "user data" which can be passed to a host during
 * launching or may be appended to an {@code env.sh} file that sets environment variables
 * for processes to launch on a host.
 * 
 * @see ProcessConfigurationVariable
 * 
 * @author Axel Uhl (D043530)
 *
 */
@FunctionalInterface
public interface UserDataProvider {
    /**
     * Values are unquoted strings that will be enclosed in double quotes and will have their double-quotes quoted using
     * backslash characters to precede them.
     */
    Map<ProcessConfigurationVariable, String> getUserData();
    
    default String getAsEnvironmentVariableAssignments() {
        return String.join("\n", Util.map(getUserData().entrySet(), e->getAsEnvironmentVariableAssignment(e.getKey(), e.getValue())));
    }

    static String getAsEnvironmentVariableAssignment(ProcessConfigurationVariable variable, String value) {
        return variable+"=\""+value.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("'", "\\\\'").replaceAll("\\$", "\\\\\\$")+"\"";
    }
}
