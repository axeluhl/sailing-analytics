package com.sap.sailing.domain.igtimiadapter.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.igtimiadapter.Client;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;

/**
 * Maintains data about a default {@link Client} that represents this application when interacting with the Igtimi
 * server. The corresponding default {@link IgtimiConnectionFactory} can be obtained from within this bundle using
 * {@link #getInstance()}.{@link #getConnectionFactory()}. Clients outside this bundle shall track the
 * {@link IgtimiConnectionFactory} OSGi service that this activator registeres with the OSGi system upon
 * {@link #start(BundleContext)}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class Activator implements BundleActivator {
    private static Activator INSTANCE;
    
    private static final String CLIENT_ID = "d29eae61621af3057db0e638232a027e96b1d2291b1b89a1481dfcac075b0bf4";
    private static final String CLIENT_SECRET = "537dbd14a84fcb470c91d85e8c4f8f7a356ac5ffc8727594d1bfe900ee5942ef";
    private IgtimiConnectionFactory connectionFactory;
    
    @Override
    public void start(BundleContext context) throws Exception {
        INSTANCE = this;
        Client client = new ClientImpl(CLIENT_ID, CLIENT_SECRET);
        connectionFactory = new IgtimiConnectionFactoryImpl(client);
        context.registerService(IgtimiConnectionFactory.class, connectionFactory, /* properties */ null);
    }
    
    public static Activator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Activator(); // probably non-OSGi case, as in test execution
        }
        return INSTANCE;
    }
    
    public IgtimiConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
