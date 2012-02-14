package com.sap.sailing.server.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class ExpeditionThroughHttpPostServlet extends AbstractHttpPostServlet {
    private static final long serialVersionUID = 2038882388961739487L;

    @Override
    protected HttpPostServletRequestHandler createHandler(HttpServletResponse resp) throws IOException {
        return new ExpeditionThroughHttpPostServletHandler(resp, this);
    }
}
