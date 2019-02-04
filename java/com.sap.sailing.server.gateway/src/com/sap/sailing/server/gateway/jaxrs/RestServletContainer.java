package com.sap.sailing.server.gateway.jaxrs;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.security.SecurityService;
import com.sap.sse.replication.ReplicationService;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class RestServletContainer extends ServletContainer {
    private static final long serialVersionUID = -3132655822281021061L;

    public static final String OSGI_RFC66_WEBBUNDLE_BUNDLECONTEXT_NAME = "osgi-bundlecontext"; 

    public static final String RACING_EVENT_SERVICE_TRACKER_NAME = "racingEventServiceTracker";

    public static final String REPLICATION_SERVICE_TRACKER_NAME = "replicationServiceTracker";

    public static final String DATA_MINING_SERVER_TRACKER_NAME = "dataMiningServerTracker";

    public static final String SECURITY_SERVICE_TRACKER_NAME = "securityServiceTracker";

    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    private ServiceTracker<ReplicationService, ReplicationService> replicationServiceTracker;

    private ServiceTracker<DataMiningServer, DataMiningServer> dataMiningServerTracker;
    
    private ServiceTracker<SecurityService, SecurityService> securityServiceTracker;
    
    public RestServletContainer() {
        super();
    }

    public RestServletContainer(Application app) {
        super(app);
    }

    public RestServletContainer(Class<? extends Application> appClass) {
        super(appClass);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {  
       super.init(config);  
       BundleContext context = (BundleContext) config.getServletContext().getAttribute(OSGI_RFC66_WEBBUNDLE_BUNDLECONTEXT_NAME);
       racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(context, RacingEventService.class.getName(), null);
       racingEventServiceTracker.open();
       replicationServiceTracker = new ServiceTracker<ReplicationService, ReplicationService>(context, ReplicationService.class.getName(), null);
       replicationServiceTracker.open();
       dataMiningServerTracker = new ServiceTracker<DataMiningServer, DataMiningServer>(context, DataMiningServer.class, null);
       dataMiningServerTracker.open();
       securityServiceTracker = new ServiceTracker<SecurityService, SecurityService>(context, SecurityService.class, null);
       securityServiceTracker.open();
       config.getServletContext().setAttribute(RACING_EVENT_SERVICE_TRACKER_NAME, racingEventServiceTracker);
       config.getServletContext().setAttribute(REPLICATION_SERVICE_TRACKER_NAME, replicationServiceTracker);
       config.getServletContext().setAttribute(DATA_MINING_SERVER_TRACKER_NAME, dataMiningServerTracker);
       config.getServletContext().setAttribute(SECURITY_SERVICE_TRACKER_NAME, securityServiceTracker);
   }

    @Override
    public void destroy() {
        super.destroy();
        if (racingEventServiceTracker != null) {
            racingEventServiceTracker.close();
        }
        if (dataMiningServerTracker != null) {
            dataMiningServerTracker.close();
        }
        if (securityServiceTracker != null) {
            securityServiceTracker.close();
        }
    }
}
