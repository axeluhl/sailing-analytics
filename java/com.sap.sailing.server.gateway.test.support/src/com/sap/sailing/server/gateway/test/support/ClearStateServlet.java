package com.sap.sailing.server.gateway.test.support;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.test.support.RacingEventServiceWithTestSupport;

public class ClearStateServlet extends HttpServlet {
    private static final long serialVersionUID = -880795218153271730L;

    private static final String OSGI_BUNDLECONTEXT_ATTRIBUTE_NAME = "osgi-bundlecontext"; 
    
    private ServiceTracker<RacingEventServiceWithTestSupport, RacingEventServiceWithTestSupport> tracker;
    
    @Override
    public void init(ServletConfig config) throws ServletException {  
        super.init(config);  
        
        ServletContext servletContext = config.getServletContext();
        BundleContext bundleContext = (BundleContext) servletContext.getAttribute(OSGI_BUNDLECONTEXT_ATTRIBUTE_NAME);
        
        this.tracker = new ServiceTracker<>(bundleContext, RacingEventServiceWithTestSupport.class.getName(), null);
        this.tracker.open();
    }

    @Override
    public void destroy() {
        if(this.tracker != null) {
            this.tracker.close();
        }
        
        super.destroy();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            RacingEventServiceWithTestSupport service = getService();
            service.clearState();
            
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception exception) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }
    
    public RacingEventServiceWithTestSupport getService() {
        return this.tracker.getService();
    }
}
