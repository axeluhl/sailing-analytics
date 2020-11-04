package com.sap.sse.landscape;

import java.util.Map;

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
    Map<ProcessConfigurationVariable, String> getUserData();
}
