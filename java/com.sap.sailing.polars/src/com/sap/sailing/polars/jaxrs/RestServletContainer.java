package com.sap.sailing.polars.jaxrs;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class RestServletContainer extends ServletContainer {
    private static final long serialVersionUID = 3129261471808425410L;

    private static final String OSGI_RFC66_WEBBUNDLE_BUNDLECONTEXT_NAME = "osgi-bundlecontext";

    public static final String POLAR_DATA_SERVICE_TRACKER_NAME = "polarDataServiceTracker";

    private ServiceTracker<PolarDataService, PolarDataService> polarDataServiceTracker;

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
        polarDataServiceTracker = new ServiceTracker<PolarDataService, PolarDataService>(context, PolarDataService.class.getName(), null);
        polarDataServiceTracker.open();
        config.getServletContext().setAttribute(POLAR_DATA_SERVICE_TRACKER_NAME, polarDataServiceTracker);
    }

    @Override
    public void destroy() {
        super.destroy();

        if (polarDataServiceTracker != null) {
            polarDataServiceTracker.close();
        }
    }

}
