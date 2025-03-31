package com.sap.sse.security.ui.authentication;

import com.sap.sse.security.ui.client.UserService;

/**
 * Interface which provides access to an {@link UserService} instance.
 */
public interface WithUserService {
    
    /**
     * @return the {@link UserService}
     */
    UserService getUserService();
}
