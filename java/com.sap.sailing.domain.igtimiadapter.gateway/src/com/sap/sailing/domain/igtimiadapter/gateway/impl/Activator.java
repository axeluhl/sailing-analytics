package com.sap.sailing.domain.igtimiadapter.gateway.impl;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.igtimiadapter.Client;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * Maintains data about a default {@link Client} that represents this application when interacting with the Igtimi
 * server. The corresponding default {@link IgtimiConnectionFactory} can be obtained from within this bundle using
 * {@link #getInstance()}.{@link #getConnectionFactory()}. Clients outside this bundle shall track the
 * {@link IgtimiConnectionFactory} OSGi service that this activator registers with the OSGi system upon
 * {@link #start(BundleContext)}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class Activator implements BundleActivator {
    private static Activator INSTANCE;
    private ServiceTracker<IgtimiConnectionFactory, IgtimiConnectionFactory> igtimiConnectionFactoryTracker;
    
    public Activator() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        INSTANCE = this;
        
        igtimiConnectionFactoryTracker = ServiceTrackerFactory
                .createAndOpen(context, IgtimiConnectionFactory.class);
    }
    
    public static Activator getInstance() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        if (INSTANCE == null) {
            INSTANCE = new Activator(); // probably non-OSGi case, as in test execution
        }
        return INSTANCE;
    }
    
    public IgtimiConnectionFactory getConnectionFactory() {
        return igtimiConnectionFactoryTracker.getService();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        igtimiConnectionFactoryTracker.close();
        igtimiConnectionFactoryTracker = null;
    }
}
