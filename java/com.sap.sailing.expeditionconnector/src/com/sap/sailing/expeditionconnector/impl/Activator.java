package com.sap.sailing.expeditionconnector.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sailing.expeditionconnector.ExpeditionWindTrackerFactory;

public class Activator implements BundleActivator {
    private static Logger logger = Logger.getLogger(Activator.class.getName());
    
    private static final String EXPEDITION_UDP_PORT_PROPERTY_NAME = "expedition.udp.port";
    
    private static Activator instance;
    
    private static final int DEFAULT_PORT = 2013;
    
    private int port;
    
    public Activator() {
        port = Integer.valueOf(System.getProperty(EXPEDITION_UDP_PORT_PROPERTY_NAME, ""+DEFAULT_PORT));
        logger.log(Level.INFO, "setting default for "+EXPEDITION_UDP_PORT_PROPERTY_NAME+" to "+port);
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        if (instance == null) {
            instance = this;
        }
        if(context.getProperty(EXPEDITION_UDP_PORT_PROPERTY_NAME) != null) {
            port = Integer.valueOf(context.getProperty(EXPEDITION_UDP_PORT_PROPERTY_NAME));
            logger.log(Level.INFO, "found "+EXPEDITION_UDP_PORT_PROPERTY_NAME+"="+port+" in OSGi context");
        }
        // register the Expedition wind tracker factory as an OSGi service
        context.registerService(ExpeditionWindTrackerFactory.class, ExpeditionWindTrackerFactory.getInstance(), /* properties */null);
        context.registerService(WindTrackerFactory.class, ExpeditionWindTrackerFactory.getInstance(), /* properties */null);
    }
    
    public static Activator getInstance() {
        if (instance == null) {
            instance = new Activator();
        }
        return instance;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
    
    public int getExpeditionUDPPort() {
        return port;
    }

}
