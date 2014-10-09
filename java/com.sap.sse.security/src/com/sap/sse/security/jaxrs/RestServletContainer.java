package com.sap.sse.security.jaxrs;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.security.SecurityService;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class RestServletContainer extends ServletContainer {
    private static final long serialVersionUID = 3129261471808425410L;

    private static final String OSGI_RFC66_WEBBUNDLE_BUNDLECONTEXT_NAME = "osgi-bundlecontext";

    public static final String SECURITY_SERVICE_TRACKER_NAME = "securityServiceTracker";

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
        securityServiceTracker = new ServiceTracker<SecurityService, SecurityService>(context, SecurityService.class.getName(), null);
        securityServiceTracker.open();
        config.getServletContext().setAttribute(SECURITY_SERVICE_TRACKER_NAME, securityServiceTracker);
    }

    @Override
    public void destroy() {
        super.destroy();

        if (securityServiceTracker != null) {
            securityServiceTracker.close();
        }
    }

    public SecurityService getService() {
        return securityServiceTracker.getService();
    }
}
