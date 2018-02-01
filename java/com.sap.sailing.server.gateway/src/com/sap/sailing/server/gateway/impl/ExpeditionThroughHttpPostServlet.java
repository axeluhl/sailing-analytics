package com.sap.sailing.server.gateway.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.server.gateway.AbstractHttpPostServlet;
import com.sap.sailing.server.gateway.HttpMessageSenderServletRequestHandler;

public class ExpeditionThroughHttpPostServlet extends AbstractHttpPostServlet {
    private static final long serialVersionUID = 2038882388961739487L;

    @Override
    protected HttpMessageSenderServletRequestHandler createHandler(HttpServletResponse resp) throws IOException {
        return new ExpeditionThroughHttpPostServletHandler(resp, this);
    }
}
