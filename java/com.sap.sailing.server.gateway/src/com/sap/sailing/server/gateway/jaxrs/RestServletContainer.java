package com.sap.sailing.server.gateway.jaxrs;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.datamining.DataMiningServer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class RestServletContainer extends ServletContainer {
    private static final long serialVersionUID = -3132655822281021061L;

    public static final String OSGI_RFC66_WEBBUNDLE_BUNDLECONTEXT_NAME = "osgi-bundlecontext"; 

    public static final String RACING_EVENT_SERVICE_TRACKER_NAME = "racingEventServiceTracker";

    public static final String DATA_MINING_SERVER_TRACKER_NAME = "dataMiningServerTracker";

    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    private ServiceTracker<DataMiningServer, DataMiningServer> dataMiningServerTracker;
    
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
       dataMiningServerTracker = new ServiceTracker<DataMiningServer, DataMiningServer>(context, DataMiningServer.class, null);
       dataMiningServerTracker.open();
       config.getServletContext().setAttribute(RACING_EVENT_SERVICE_TRACKER_NAME, racingEventServiceTracker);
       config.getServletContext().setAttribute(DATA_MINING_SERVER_TRACKER_NAME, dataMiningServerTracker);
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
    }
    
    public RacingEventService getService() {
        return racingEventServiceTracker.getService();
    }
}
