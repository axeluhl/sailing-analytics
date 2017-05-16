package com.sap.sailing.server.gateway;

import javax.servlet.http.HttpServletResponse;

/**
 * Base servlet for json exports
 * @author Frank
 */
@SuppressWarnings("serial")
public abstract class AbstractJsonHttpServlet extends SailingServerHttpServlet {

    protected void setJsonResponseHeader(HttpServletResponse resp) {
        // to allow access to the json document directly from a client side javascript
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }
}
