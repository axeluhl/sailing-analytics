package com.sap.sse.gateway;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;

import com.sap.sse.InvalidDateException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.DateParser;

/**
 * A servlet that holds an OSGi service reference
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <S> the service type
 */
public abstract class AbstractHttpServlet extends HttpServlet {
    private static final long serialVersionUID = -6514453597593669376L;

    protected static final String PARAM_NAME_TIME = "time";

    protected static final String PARAM_NAME_TIME_MILLIS = "timeasmillis";
    
    private static final String OSGI_RFC66_WEBBUNDLE_BUNDLECONTEXT_NAME = "osgi-bundlecontext"; 

    private BundleContext context;
    
    @Override
    public void init(ServletConfig config) throws ServletException {  
       super.init(config);  
       context = (BundleContext) config.getServletContext().getAttribute(OSGI_RFC66_WEBBUNDLE_BUNDLECONTEXT_NAME);  
   }
    
    protected BundleContext getContext() {
        return context;
    }

    protected TimePoint readTimePointParam(HttpServletRequest req, String nameOfISOTimeParam, String nameOfMillisTime) throws InvalidDateException {
        return readTimePointParam(req, nameOfISOTimeParam, nameOfMillisTime, null);
    }
    
    protected TimePoint readTimePointParam(HttpServletRequest req, String nameOfISOTimeParam, String nameOfMillisTime,
            TimePoint defaultValue) throws InvalidDateException {
        String time = req.getParameter(nameOfISOTimeParam);
        TimePoint timePoint;
        if (time != null && time.length() > 0) {
            timePoint = new MillisecondsTimePoint(DateParser.parse(time).getTime());
        } else {
            String timeAsMillis = req.getParameter(nameOfMillisTime);
            if (timeAsMillis != null && timeAsMillis.length() > 0) {
                timePoint = new MillisecondsTimePoint(Long.valueOf(timeAsMillis));
            } else {
                timePoint = defaultValue;
            }
        }
        return timePoint;
    }
}
