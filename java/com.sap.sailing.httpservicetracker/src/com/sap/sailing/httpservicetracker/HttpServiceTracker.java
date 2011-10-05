package com.sap.sailing.httpservicetracker;

import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

public class HttpServiceTracker extends ServiceTracker<HttpService, HttpService> {
    private static final Logger log = Logger.getLogger(HttpServiceTracker.class.getName());
    
    private final Map<String, Class<? extends Servlet>> pathMap;
    private final Map<String, String> resourcePathMap;

    public HttpServiceTracker(BundleContext context, Map<String, Class<? extends Servlet>> servletPathMap, Map<String, String> resourcePathMap) {
        super(context, HttpService.class, /* customizer */ null);
        this.pathMap = servletPathMap;
        this.resourcePathMap = resourcePathMap;
    }
    
    @Override
    public HttpService addingService(ServiceReference<HttpService> reference) {
        HttpService httpService = super.addingService(reference);
        if (httpService != null) {
            if (pathMap != null) {
                for (Map.Entry<String, Class<? extends Servlet>> e : pathMap.entrySet()) {
                    try {
                        httpService.registerServlet(e.getKey(), e.getValue().newInstance(), /* initParams */null, /* HttpContext */
                                null);
                    } catch (Exception ex) {
                        log.throwing(HttpServiceTracker.class.getName(), "addingService", ex);
                    }
                }
            }
            if (resourcePathMap != null) {
                for (Map.Entry<String, String> e : resourcePathMap.entrySet()) {
                    try {
                        httpService.registerResources(e.getKey(), e.getValue(), /* HttpContext */ null);
                    } catch (Exception ex) {
                        log.throwing(HttpServiceTracker.class.getName(), "addingService", ex);
                    }
                }
            }
        }
        return httpService;
    }

    @Override
    public void removedService(ServiceReference<HttpService> reference, HttpService httpService) {
        if (pathMap != null) {
            for (Map.Entry<String, Class<? extends Servlet>> e : pathMap.entrySet()) {
                try {
                    httpService.unregister(e.getKey());
                } catch (Exception ex) {
                    log.throwing(HttpServiceTracker.class.getName(), "removedService", ex);
                }
            }
        }
        if (resourcePathMap != null) {
            for (String resourceName : resourcePathMap.keySet()) {
                try {
                    httpService.unregister(resourceName);
                } catch (Exception ex) {
                    log.throwing(HttpServiceTracker.class.getName(), "removedService", ex);
                }
            }
        }
    }
}
