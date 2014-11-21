package com.sap.sse.security.impl;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.User;
import com.sap.sse.security.shared.UserManagementException;

/**
 * Publishes those methods of {@link SecurityServiceImpl} that are required by operations implemented as lambda
 * expressions to fulfill their tasks. These operations should not be invoked by external service clients.
 * {@link SecurityService} is the one registered with the OSGi registry and thus the publicly-visible
 * interface.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ReplicableSecurityService extends SecurityService {

    User internalCreateSimpleUser(String username, String email, String password, String validationBaseURL)
            throws UserManagementException;

    Void internalUpdateSimpleUserPassword(String username, String newPassword) throws UserManagementException;

    Void internalUpdateSimpleUserEmail(String username, String newEmail, String validationBaseURL)
            throws UserManagementException;

}
