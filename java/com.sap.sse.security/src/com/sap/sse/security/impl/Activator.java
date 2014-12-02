package com.sap.sse.security.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.replication.Replicable;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.UsernamePasswordRealm;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
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
        UsernamePasswordRealm.setTestUserStore(theTestUserStore);
    }
    
    public static BundleContext getContext() {
        return context;
    }
    
    public static SecurityService getSecurityService() {
        return securityService;
    }

    /**
     * If no {@link #testUserStore} is available, start looking for a {@link UserStore} service in a background thread.
     * As soon as a service reference for a {@link UserStore} implementation becomes available in the service registry.
     * If a {@link UserStore} has been found, one way or another, the {@link SecurityService} is created and
     * registered as an OSGi service.
     */
    public void start(BundleContext bundleContext) throws Exception {
        // Load mail properties
        final String jettyHome = System.getProperty("jetty.home", "configuration");
        final File propertiesDir = new File(jettyHome).getParentFile();
        File propertiesfile = new File(propertiesDir, "security.properties");
        Properties mailProperties = new Properties();
        try {
            mailProperties.load(new FileReader(propertiesfile));
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Couldn't read security properties from "+propertiesfile.getCanonicalPath(), ioe);
        }
        if (testUserStore != null) {
            createAndRegisterSecurityService(testUserStore, mailProperties);
        } else {
            waitForUserStoreService(bundleContext, mailProperties);
        }
    }

    private void createAndRegisterSecurityService(UserStore store, Properties mailProperties) {
        securityService = new SecurityServiceImpl(store, mailProperties);
        registration = context.registerService(SecurityService.class.getName(),
                securityService, null);
        final Dictionary<String, String> replicableServiceProperties = new Hashtable<>();
        replicableServiceProperties.put(Replicable.OSGi_Service_Registry_ID_Property_Name, securityService.getId().toString());
        context.registerService(Replicable.class.getName(), securityService, replicableServiceProperties);
        Logger.getLogger(Activator.class.getName()).info("Security Service registered.");
    }

    private void waitForUserStoreService(BundleContext bundleContext, final Properties mailProperties) {
        context = bundleContext;
        final ServiceTracker<UserStore, UserStore> tracker = new ServiceTracker<>(bundleContext, UserStore.class, /* customizer */ null);
        tracker.open();
        new Thread("ServiceTracker waiting for UserStore service") {
            @Override
            public void run() {
                try {
                    Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); // ensure that classpath:... Shiro ini files are resolved properly
                    logger.info("Waiting for UserStore service...");
                    UserStore userStore = tracker.waitForService(0);
                    logger.info("Obtained UserStore service "+userStore);
                    createAndRegisterSecurityService(userStore, mailProperties);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted while waiting for UserStore service", e);
                }
            }
        }.start();
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
