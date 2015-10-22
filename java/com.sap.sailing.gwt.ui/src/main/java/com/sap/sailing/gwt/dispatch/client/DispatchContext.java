package com.sap.sailing.gwt.dispatch.client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.core.shared.GwtIncompatible;


public interface DispatchContext {
    @GwtIncompatible
    HttpServletRequest getRequest();

    @GwtIncompatible
    URL getRequestBaseURL() throws MalformedURLException;
}
