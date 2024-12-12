package com.sap.sailing.domain.igtimiadapter.gateway.impl;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;

/**
 * Maintains a tracker for the {@link SecurityService} that REST resources in this bundle can access through
 * the {@link #getInstance()}.{@link #getSecurityService()} combination.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class Activator implements BundleActivator {
    private static Activator INSTANCE;
    private FullyInitializedReplicableTracker<SecurityService> securityServiceTracker;
    
    public Activator() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        INSTANCE = this;
        securityServiceTracker = FullyInitializedReplicableTracker.createAndOpen(context, SecurityService.class);
    }
    
    public static Activator getInstance() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        if (INSTANCE == null) {
            INSTANCE = new Activator(); // probably non-OSGi case, as in test execution
        }
        return INSTANCE;
    }
    
    public SecurityService getSecurityService() {
        try {
            return securityServiceTracker.getInitializedService(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        securityServiceTracker.close();
        securityServiceTracker = null;
    }
}
