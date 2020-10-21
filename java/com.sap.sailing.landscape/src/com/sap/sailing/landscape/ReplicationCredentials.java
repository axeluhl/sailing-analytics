package com.sap.sailing.landscape;

import com.sap.sse.landscape.UserDataProvider;

/**
 * Can be used to authenticate a replica to its master. Authentication may happen by
 * username/password or by a bearer token. These different types of credentials know how
 * to represent themselves as environment variable settings which can, e.g., be passed as
 * user data or be appended to an {@code env.sh} file directly.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ReplicationCredentials extends UserDataProvider {
}
