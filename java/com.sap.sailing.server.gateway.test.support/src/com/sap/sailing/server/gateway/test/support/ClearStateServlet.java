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
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;

public class ClearStateServlet extends HttpServlet {
    private static final long serialVersionUID = -880795218153271730L;
    
    private static final String OSGI_BUNDLECONTEXT_ATTRIBUTE_NAME = "osgi-bundlecontext"; 
    
    private static String TEST_DB_NAME = "winddbTest"; 
    
    private ServiceTracker<RacingEventServiceWithTestSupport, RacingEventServiceWithTestSupport> racingEventTracker;
    
    @Override
    public void init(ServletConfig config) throws ServletException {  
        super.init(config);  
        
        ServletContext servletContext = config.getServletContext();
        BundleContext bundleContext = (BundleContext) servletContext.getAttribute(OSGI_BUNDLECONTEXT_ATTRIBUTE_NAME);
        
        this.racingEventTracker = new ServiceTracker<>(bundleContext, RacingEventServiceWithTestSupport.class.getName(), null);
        this.racingEventTracker.open();
    }

    @Override
    public void destroy() {
        if(this.racingEventTracker != null) {
            this.racingEventTracker.close();
        }
        
        super.destroy();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        MongoDBConfiguration configuration = MongoDBService.INSTANCE.getConfiguration();
        String databaseName = configuration.getDatabaseName();
        
        if(TEST_DB_NAME.equals(databaseName)) {
	        try {
	            RacingEventServiceWithTestSupport racingEventService = getRacingEventService();
	            racingEventService.clearState();
	            
	            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	        } catch (Exception exception) {
	            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
	        }
	        
	        return;
    	}
    	
    	response.sendError(HttpServletResponse.SC_FORBIDDEN, "Database " + databaseName + " is not the database for testing!");
    }
    
    public RacingEventServiceWithTestSupport getRacingEventService() {
        return this.racingEventTracker.getService();
    }
}
