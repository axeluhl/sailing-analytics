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

    /**
     * In a non-OSGi test environment, having Shiro instantiate this class with a
     * default constructor makes it difficult to get access to the user store
     * implementation which may live in a bundle that this bundle has no direct
     * access to. Therefore, test cases must set the UserStore implementation
     * by invoking {@link #setTestUserStore} before the default constructor is
     * invoked.
     */
    private static UserStore testUserStore;
    
    public static void setTestUserStore(UserStore theTestUserStore) {
        testUserStore = theTestUserStore;
    }
    
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
        context = bundleContext;
        ServiceReference<?> serviceReference = context.
                getServiceReference(UserStore.class.getName());
        final UserStore store;
        if (serviceReference == null) {
            store = testUserStore;
        } else {
            store = (UserStore) context.getService(serviceReference);
        }
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
