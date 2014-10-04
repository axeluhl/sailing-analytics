package com.sap.sailing.gwt.ui.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.security.SecurityService;

public class Activator implements BundleActivator {
    private static Logger logger = Logger.getLogger(Activator.class.getName());
    private static BundleContext context;
    private SailingServiceImpl sailingServiceToStopWhenStopping;
    private static Activator INSTANCE;

    public Activator() {
        INSTANCE = this;
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        final ServiceTracker<SecurityService, SecurityService> tracker = new ServiceTracker<>(context, SecurityService.class, /* customizer */ null);
        new Thread("ServiceTracker in bundle com.sap.sailing.gwt.ui waiting for SecurityService") {
            @Override
            public void run() {
                try {
                    logger.info("Waiting for SecurityService...");
                    SecurityService securityService = tracker.waitForService(0);
                    logger.info("Obtained SecurityService "+securityService+
                            ". Setting it as SecurityUtils\' security manager.");
                    SecurityUtils.setSecurityManager(securityService.getSecurityManager());
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted while waiting for UserStore service", e);
                }
            }
        }.start();
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
