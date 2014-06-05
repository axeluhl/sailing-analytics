package com.sap.sse.security;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    private static BundleContext context;
    private static SecurityService securityService;
    private ServiceRegistration<?> registration;

    static BundleContext getContext() {
        return context;
    }
    
    static SecurityService getSecurityService(){
        return securityService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        
        securityService = new SecurityServiceImpl();
        registration = context.registerService(SecurityService.class.getName(),
                securityService, null);
        Logger.getLogger(Activator.class.getName()).info("Security Service registered.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
        Activator.context = null;
    }

}
