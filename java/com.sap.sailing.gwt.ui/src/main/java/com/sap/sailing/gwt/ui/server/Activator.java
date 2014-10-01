package com.sap.sailing.gwt.ui.server;

import org.apache.shiro.SecurityUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sap.sse.security.SecurityService;

public class Activator implements BundleActivator {
    private static BundleContext context;
    private SailingServiceImpl sailingServiceToStopWhenStopping;
    private static Activator INSTANCE;

    public Activator() {
        INSTANCE = this;
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        ServiceReference<?> serviceReference = context.getServiceReference(SecurityService.class.getName());
        SecurityService securityService = (SecurityService) context.getService(serviceReference);
        SecurityUtils.setSecurityManager(securityService.getSecurityManager());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (sailingServiceToStopWhenStopping != null) {
            sailingServiceToStopWhenStopping.stop();
        }
    }
    
    public static Activator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Activator();
        }
        return INSTANCE;
    }
    
    public static BundleContext getDefault() {
        return context;
    }

    public void setSailingService(SailingServiceImpl sailingServiceImpl) {
        sailingServiceToStopWhenStopping = sailingServiceImpl;
    }

}
