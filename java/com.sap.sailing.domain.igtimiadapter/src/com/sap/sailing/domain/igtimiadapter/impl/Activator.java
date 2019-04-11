package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.Client;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;
import com.sap.sailing.domain.igtimiadapter.persistence.MongoObjectFactory;
import com.sap.sailing.domain.igtimiadapter.persistence.PersistenceFactory;
import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sse.security.SecurityService;
import com.sap.sse.util.ServiceTrackerFactory;
import com.sap.sse.util.impl.ThreadFactoryWithPriority;

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
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    private static Activator INSTANCE;
    
    private static final String DEFAULT_CLIENT_ID = "d29eae61621af3057db0e638232a027e96b1d2291b1b89a1481dfcac075b0bf4";
    private static final String DEFAULT_CLIENT_SECRET = "537dbd14a84fcb470c91d85e8c4f8f7a356ac5ffc8727594d1bfe900ee5942ef";
    private static final String DEFAULT_REDIRECT_PROTOCOL = "https";
    private static final String DEFAULT_REDIRECT_HOST = "www.sapsailing.com";
    private static final String DEFAULT_REDIRECT_PORT = null; // port==null means default
    private static final String CLIENT_REDIRECT_PATH = "/igtimi/oauth/v1/authorizationcallback";
    private static final String CLIENT_ID_PROPERTY_NAME = "igtimi.client.id";
    private static final String CLIENT_SECRET_PROPERTY_NAME = "igtimi.client.secret";
    private static final String CLIENT_REDIRECT_PROTOCOL_PROPERTY_NAME = "igtimi.client.redirect.protocol";
    private static final String CLIENT_REDIRECT_HOST_PROPERTY_NAME = "igtimi.client.redirect.host";
    private static final String CLIENT_REDIRECT_PORT_PROPERTY_NAME = "igtimi.client.redirect.port";
    private final Future<IgtimiConnectionFactoryImpl> connectionFactory;
    private final Future<IgtimiWindTrackerFactory> windTrackerFactory;
    private ServiceTracker<SecurityService, SecurityService> securityServiceServiceTracker;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryWithPriority(Thread.NORM_PRIORITY, /* daemon */ true));

    public Activator() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        logger.info(getClass().getName()+" constructor");
        final String clientId = System.getProperty(CLIENT_ID_PROPERTY_NAME, DEFAULT_CLIENT_ID);
        final String clientSecret = System.getProperty(CLIENT_SECRET_PROPERTY_NAME, DEFAULT_CLIENT_SECRET);
        final String clientRedirectProtocol = System.getProperty(CLIENT_REDIRECT_PROTOCOL_PROPERTY_NAME, DEFAULT_REDIRECT_PROTOCOL);
        final String clientRedirectHost = System.getProperty(CLIENT_REDIRECT_HOST_PROPERTY_NAME, DEFAULT_REDIRECT_HOST);
        final String clientRedirectPort = System.getProperty(CLIENT_REDIRECT_PORT_PROPERTY_NAME, DEFAULT_REDIRECT_PORT);
        final Client client = new ClientImpl(clientId, clientSecret, clientRedirectProtocol, clientRedirectHost, clientRedirectPort, CLIENT_REDIRECT_PATH);
        final DomainObjectFactory domainObjectFactory = PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory();
        final MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory();
        connectionFactory = executor.submit(new Callable<IgtimiConnectionFactoryImpl>() {
            @Override
            public IgtimiConnectionFactoryImpl call() {
                logger.info("Creating IgtimiConnectionFactory");
                return new IgtimiConnectionFactoryImpl(client, domainObjectFactory, mongoObjectFactory);
            }
        });
        windTrackerFactory = executor.submit(new Callable<IgtimiWindTrackerFactory>() {
            @Override
            public IgtimiWindTrackerFactory call() throws InterruptedException, ExecutionException {
                logger.info("Creating IgtimiWindTrackerFactory");
                return new IgtimiWindTrackerFactory(connectionFactory.get());
            }
        });
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        INSTANCE = this;
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    context.registerService(IgtimiConnectionFactory.class, connectionFactory.get(), /* properties */ null);
                    context.registerService(WindTrackerFactory.class, windTrackerFactory.get(), /* properties */ null);
                    context.registerService(IgtimiWindTrackerFactory.class, windTrackerFactory.get(), /* properties */ null);
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.SEVERE, "Error trying to register Igtimi services with OSGi", e);
                    throw new RuntimeException(e);
                }
            }
        });
        
        securityServiceServiceTracker = ServiceTrackerFactory.createAndOpen(context, SecurityService.class);
        new Thread(() -> {
            try {
                final SecurityService securityService = securityServiceServiceTracker.waitForService(0);
                IgtimiConnectionFactoryImpl igtimiConnectionFactory = connectionFactory.get();
                for (Account account : igtimiConnectionFactory.getAllAccounts()) {
                    securityService.migrateOwnership(account);
                }
                securityService.assumeOwnershipMigrated(SecuredDomainType.IGTIMI_ACCOUNT.getName());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception trying to migrate IgtimiAccounts implementation", e);
            }
        }, getClass().getName() + " registering connectivity handler").start();
        
    }
    
    public static Activator getInstance() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        if (INSTANCE == null) {
            INSTANCE = new Activator(); // probably non-OSGi case, as in test execution
        }
        return INSTANCE;
    }
    
    public IgtimiConnectionFactoryImpl getConnectionFactory() {
        try {
            return connectionFactory.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error trying to retrieve Igtimi connection factory", e);
            throw new RuntimeException(e);
        }
    }
    
    public SecurityService getSecurityService() {
        return securityServiceServiceTracker.getService();
    }

    public IgtimiWindTrackerFactory getWindTrackerFactory() {
        try {
            return windTrackerFactory.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error trying to retrieve Igtimi wind tracker factory", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        securityServiceServiceTracker.close();
        securityServiceServiceTracker = null;
    }
}
