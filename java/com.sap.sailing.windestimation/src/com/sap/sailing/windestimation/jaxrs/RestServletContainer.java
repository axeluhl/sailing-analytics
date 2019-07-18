package com.sap.sailing.windestimation.jaxrs;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.windestimation.WindEstimationFactoryService;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class RestServletContainer extends ServletContainer {
    private static final long serialVersionUID = 3129261471808425410L;

    private static final String OSGI_RFC66_WEBBUNDLE_BUNDLECONTEXT_NAME = "osgi-bundlecontext";

    public static final String WIND_ESTIMATION_FACTORY_SERVICE_TRACKER_NAME = "windEstimationFactoryServiceTracker";

    private ServiceTracker<WindEstimationFactoryService, WindEstimationFactoryService> windEstimationFactoryServiceTracker;

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
        BundleContext context = (BundleContext) config.getServletContext()
                .getAttribute(OSGI_RFC66_WEBBUNDLE_BUNDLECONTEXT_NAME);
        windEstimationFactoryServiceTracker = new ServiceTracker<WindEstimationFactoryService, WindEstimationFactoryService>(
                context, WindEstimationFactoryService.class.getName(), null);
        windEstimationFactoryServiceTracker.open();
        config.getServletContext().setAttribute(WIND_ESTIMATION_FACTORY_SERVICE_TRACKER_NAME,
                windEstimationFactoryServiceTracker);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (windEstimationFactoryServiceTracker != null) {
            windEstimationFactoryServiceTracker.close();
        }
    }

}
