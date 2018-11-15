package com.sap.sse.security;

import com.sap.sse.security.shared.RolePrototype;

/**
 * Implementations provide {@link RolePrototype} instances to be automatically created in the system if a role with the
 * specific ID does not already exist. Implementations need to be registered as OSGi service.
 */
public interface RolePrototypeProvider {
    
    RolePrototype getRolePrototype();

}
