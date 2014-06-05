package com.sap.sse.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class EntryPointHelper {
    /**
     * Must be used by each entry point's {@link EntryPoint#onModuleLoad()} method to bind remote services to the proper URL.
     * Example:
     * <pre>
     *           EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, "service/sailing");
     * </pre>
     * where <code>service/sailing</code> is the URL path where the remote servlet is registered in the <code>web.xml</code>
     * descriptor.
     */
    public static void registerASyncService(ServiceDefTarget serviceToRegister, String servicePath) {
        String moduleBaseURL = GWT.getModuleBaseURL();
        String baseURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf('/', moduleBaseURL.length()-2)+1);
        serviceToRegister.setServiceEntryPoint(baseURL + servicePath);
    }

}
