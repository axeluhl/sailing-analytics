package com.sap.sse.security;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import com.sap.sse.security.userstore.shared.UserStore;

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
        BundleContext context = Activator.getContext();
        ServiceReference<?> serviceReference = context.
                getServiceReference(UserStore.class.getName());
        UserStore store = (UserStore) context.getService(serviceReference);
        securityService = new SecurityServiceImpl(store);
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
