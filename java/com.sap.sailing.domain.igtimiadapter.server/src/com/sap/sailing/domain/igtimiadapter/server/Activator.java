package com.sap.sailing.domain.igtimiadapter.server;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;
import com.sap.sailing.domain.igtimiadapter.persistence.MongoObjectFactory;
import com.sap.sailing.domain.igtimiadapter.persistence.PersistenceFactory;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sailing.domain.igtimiadapter.server.riot.impl.RiotServerImpl;
import com.sap.sse.util.ClearStateTestSupport;

/**
 * When {@link #start(BundleContext) started}, creates a {@link RiotServer} instance listening on the
 * TCP port as specified by the property named {@link #RIOT_PORT_PROPERTY_NAME}, defaulting to the
 * value specified in {@link #RIOT_PORT_DEFAULT} and using the default persistence.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class Activator implements BundleActivator {

    private static final String RIOT_PORT_PROPERTY_NAME = "igtimi.riot.port";
    private static final String RIOT_PORT_DEFAULT = "6000";

    private static BundleContext context;
    private RiotServer riotServer;

    static BundleContext getContext() {
        return context;
    }

    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        final String riotPortAsString = System.getProperty(RIOT_PORT_PROPERTY_NAME, RIOT_PORT_DEFAULT);
        final int riotPort = Integer.valueOf(riotPortAsString);
        final DomainObjectFactory domainObjectFactory = PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory();
        final MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory();
        final RiotServerImpl riotServerImpl = new RiotServerImpl(riotPort, domainObjectFactory, mongoObjectFactory);
        riotServer = riotServerImpl;
        context.registerService(RiotServer.class, riotServer, null);
        context.registerService(ClearStateTestSupport.class.getName(), new ClearStateTestSupport() {
            @Override
            public void clearState() throws Exception {
                riotServerImpl.clear();
            }
        }, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }
}
