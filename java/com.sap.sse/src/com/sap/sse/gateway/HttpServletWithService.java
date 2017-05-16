package com.sap.sse.gateway;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.osgi.util.tracker.ServiceTracker;

/**
 * A servlet that holds an OSGi service reference
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <S> the service type
 */
public abstract class HttpServletWithService<S> extends AbstractHttpServlet {
    private static final long serialVersionUID = -6514453597593669376L;

    private ServiceTracker<S, S> racingEventServiceTracker;

    private final String serviceClassName;
    
    protected HttpServletWithService(Class<S> serviceClass) {
        this.serviceClassName = serviceClass.getName();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {  
       super.init(config);  
       racingEventServiceTracker = new ServiceTracker<S, S>(getContext(), serviceClassName, null);
       racingEventServiceTracker.open();
   }
    
    @Override
    public void destroy() {
        super.destroy();
        if (racingEventServiceTracker != null) {
            racingEventServiceTracker.close();
        }
    }
    
    public S getService() {
        return racingEventServiceTracker.getService();
    }
}
