package com.sap.sailing.server.gateway;

import javax.servlet.http.HttpServletResponse;


public abstract class JsonExportServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = 7007196727805110847L;

    protected void setJsonResponseHeader(HttpServletResponse resp) {
        // to allow access to the json document directly from a client side javascript
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }
}
