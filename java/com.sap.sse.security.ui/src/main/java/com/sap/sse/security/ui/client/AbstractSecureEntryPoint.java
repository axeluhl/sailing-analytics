package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.sap.sse.gwt.client.StringMessages;

/**
 * Adds user management service and user service for security management to the general abstract entry point.
 * Entry points that want to benefit from security and user management features should extend this abstract class.
 * Subclasses don't need to provide an {@link EntryPoint#onModuleLoad()} implementation. If they still decide to do so,
 * they must invoke <code>super.onModuleLoad()</code> as their first statement.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class AbstractSecureEntryPoint<S extends StringMessages> extends com.sap.sse.gwt.client.AbstractEntryPoint<S> implements WithSecurity {
    private WithSecurity securityProvider;

    protected void doOnModuleLoad() {
        securityProvider = new DefaultWithSecurityImpl();
    }

    @Override
    public UserManagementServiceAsync getUserManagementService() {
        return securityProvider.getUserManagementService();
    }
    
    @Override
    public UserService getUserService() {
        return securityProvider.getUserService();
    }

}
