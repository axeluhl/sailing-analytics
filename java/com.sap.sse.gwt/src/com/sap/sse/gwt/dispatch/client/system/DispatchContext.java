package com.sap.sse.gwt.dispatch.client.system;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * The context provided to the backend action execution.
 * 
 * The context provides access to the services required by the actions.
 */
public interface DispatchContext {
    /**
     * Access to the current http request
     * 
     * @return
     */
    @GwtIncompatible
    HttpServletRequest getRequest();

    /**
     * Base URL of the current http context.
     * 
     * @return
     * @throws DispatchException
     */
    @GwtIncompatible
    URL getRequestBaseURL() throws DispatchException;
}
