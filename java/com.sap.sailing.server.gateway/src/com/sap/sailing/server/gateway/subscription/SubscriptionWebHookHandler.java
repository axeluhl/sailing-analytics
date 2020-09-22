package com.sap.sailing.server.gateway.subscription;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import com.sap.sse.security.shared.Subscription;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.User;

public abstract class SubscriptionWebHookHandler {
    private static final Logger logger = Logger.getLogger(SubscriptionWebHookHandler.class.getName());

    protected SubscriptionWebHookServlet context;

    public SubscriptionWebHookHandler(SubscriptionWebHookServlet context) {
        this.context = context;
    }

    public abstract void handle(HttpServletRequest request, HttpServletResponse response);

    protected User getUser(String customerId) {
        return context.getSecurityService().getUserByName(customerId);
    }

    protected void updateUserSubscription(User user, Subscription subscription) throws UserManagementException {
        logger.info(() -> "Update subscription, user " + user.getName() + ", new subscription "
                + (subscription != null ? subscription.toString() : "null"));
        context.getSecurityService().updateUserSubscription(user.getName(), subscription);
    }

    protected void sendSuccess(HttpServletResponse response) {
        response.setStatus(Response.Status.OK.getStatusCode());
    }

    protected void sendFail(HttpServletResponse response) {
        response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
