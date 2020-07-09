package com.sap.sailing.server.gateway.subscription;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.server.gateway.SailingServerHttpServlet;
import com.sap.sailing.server.gateway.subscription.chargebee.ChargebeeWebHookHandler;

public class SubscriptionWebHookServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = -6953289975184826299L;

    private SubscriptionWebHookHandler webHookHandler;

    public SubscriptionWebHookServlet() {
        webHookHandler = new ChargebeeWebHookHandler(this);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        webHookHandler.handle(request, response);
    }
}
