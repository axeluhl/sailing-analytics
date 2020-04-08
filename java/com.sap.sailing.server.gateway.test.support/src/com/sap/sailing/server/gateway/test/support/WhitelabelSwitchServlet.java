package com.sap.sailing.server.gateway.test.support;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Use this servlet to turn whitelabelling on and off during testing.
 * @see com.sap.sse.debranding.ClientConfigurationServlet
 * @author Georg Herdt
 *
 */
public class WhitelabelSwitchServlet extends HttpServlet {

    private static final String COM_SAP_SAILING_DEBRANDING = "com.sap.sse.debranding";

    private static final long serialVersionUID = 7132508855846001729L;

    private static final Logger logger = Logger.getLogger(WhitelabelSwitchServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String property = System.getProperty(COM_SAP_SAILING_DEBRANDING);
        resp.setContentLength(property != null ? property.getBytes().length : 0);
        resp.getWriter().write(property);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path.endsWith("on") || path.endsWith("true")) {
            System.setProperty(COM_SAP_SAILING_DEBRANDING, Boolean.toString(true));
            resp.setStatus(HttpServletResponse.SC_OK);
        } else if (path.endsWith("off") || path.endsWith("false")) {
            System.setProperty(COM_SAP_SAILING_DEBRANDING, Boolean.toString(false));
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            logger.config("unrecoginzed path " + path);
            resp.setStatus(418);
        }
    }
    
    

}
