package com.sap.sse.gwt.dispatch.client;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sse.gwt.dispatch.client.exceptions.DispatchException;


public interface DispatchContext {
    @GwtIncompatible
    HttpServletRequest getRequest();

    @GwtIncompatible
    URL getRequestBaseURL() throws DispatchException;
}
