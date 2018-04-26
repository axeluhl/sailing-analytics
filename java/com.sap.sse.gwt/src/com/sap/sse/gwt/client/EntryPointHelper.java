package com.sap.sse.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sse.gwt.shared.RpcConstants;

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
        serviceToRegister.setRpcRequestBuilder(new RpcRequestBuilder() {
            @Override
            protected void doFinish(RequestBuilder rb) {
                super.doFinish(rb);
                rb.setHeader(RpcConstants.HEADER_LOCALE, LocaleInfo.getCurrentLocale().getLocaleName());
            }
        });
    }
    
    /**
     * Must be used by each entry point's {@link EntryPoint#onModuleLoad()} method to bind remote services to the proper
     * URL. With this variant it is possible to register services defined in a bundle other than the one using it. If
     * the {@link #registerASyncService(ServiceDefTarget, String)} flavor were used instead, the calling bundle's web
     * context path would be used instead of the one of the bundle actually hosting the service. Therefore, each service
     * that may be used by other bundles needs to be able to tell its hosting bundle's web context path.
     * <p>
     * 
     * Example:
     * 
     * <pre>
     *            EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, &quot;security/ui&quot;, &quot;service/sailing&quot;);
     * </pre>
     * 
     * where <code>security/ui</code> is the web context path of the hosting bundle and <code>service/usermanagement</code> is
     * the URL path where the remote servlet is registered in the <code>web.xml</code> descriptor.
     */
    public static void registerASyncService(ServiceDefTarget serviceToRegister, String webContextPath, String servicePath) {
        String moduleBaseURL = GWT.getModuleBaseURL();
        String baseURL = moduleBaseURL.substring(0, moduleBaseURL.indexOf('/', moduleBaseURL.indexOf('/', moduleBaseURL.indexOf('/')+1)+1)+1);
        serviceToRegister.setServiceEntryPoint(baseURL + webContextPath + "/" + servicePath);
    }
    
}
