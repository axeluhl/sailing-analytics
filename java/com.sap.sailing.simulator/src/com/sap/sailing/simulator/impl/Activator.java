package com.sap.sailing.simulator.impl;

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

import com.sap.sailing.simulator.PolarDiagramFactory;

/**
 * Registers OSGI bundle service for accessing polar diagrams
 * 
 * @author Christopher Ronnewinkel (d036654)
 * 
 */
public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    private static Activator INSTANCE;
    
    private final Future<PolarDiagramFactoryImpl> polarFactory;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Activator() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        logger.info(getClass().getName()+" constructor");
        polarFactory = executor.submit(new Callable<PolarDiagramFactoryImpl>() {
            @Override
            public PolarDiagramFactoryImpl call() {
                logger.info("Creating PolarFactory");
                return new PolarDiagramFactoryImpl();
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
                    context.registerService(PolarDiagramFactory.class, polarFactory.get(), /* properties */ null);
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.SEVERE, "Error trying to register Polar services with OSGi", e);
                    throw new RuntimeException(e);
                }
            }
        });
    }
    
    public static Activator getInstance() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        if (INSTANCE == null) {
            INSTANCE = new Activator(); // probably non-OSGi case, as in test execution
        }
        return INSTANCE;
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
