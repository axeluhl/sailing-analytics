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
import org.eclipse.jetty.util.ssl.SslContextFactory.Client;
import org.json.simple.parser.ParseException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;
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
    
    private final Future<IgtimiConnectionFactory> connectionFactory;
    private final Future<IgtimiWindTrackerFactory> windTrackerFactory;
    private FullyInitializedReplicableTracker<SecurityService> securityServiceServiceTracker;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryWithPriority(Thread.NORM_PRIORITY, /* daemon */ true));
    private SecurityService securityServiceTest;

    public Activator() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        logger.info(getClass().getName()+" constructor");
        connectionFactory = executor.submit(new Callable<IgtimiConnectionFactory>() {
            @Override
            public IgtimiConnectionFactory call() {
                logger.info("Creating IgtimiConnectionFactory");
                return new IgtimiConnectionFactoryImpl();
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

    /** Only used by tests. */
    public void setSecurityService(SecurityService securityService) {
        securityServiceTest = securityService;
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
        securityServiceServiceTracker = FullyInitializedReplicableTracker.createAndOpen(context, SecurityService.class);
    }
    
    public static Activator getInstance() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        if (INSTANCE == null) {
            INSTANCE = new Activator(); // probably non-OSGi case, as in test execution
        }
        return INSTANCE;
    }
    
    public SecurityService getSecurityService() {
        try {
            return securityServiceTest == null ? securityServiceServiceTracker.getInitializedService(0) : securityServiceTest;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
