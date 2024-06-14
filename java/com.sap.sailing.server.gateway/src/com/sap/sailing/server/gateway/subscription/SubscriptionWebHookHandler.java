package com.sap.sailing.server.gateway.subscription;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;

/**
 * Base class for subscription webhook handler
 */
public abstract class SubscriptionWebHookHandler {
    private static final Logger logger = Logger.getLogger(SubscriptionWebHookHandler.class.getName());

    protected SubscriptionWebHookServlet context;
    
    /**
     * When working with a user's subscriptions, such as first reading, then changing and updating a user's subscription
     * based on what was read, a user-specific write lock must be obtained to ensure that no writes can cut in between.
     * See also {@link #lockSubscriptionsForUser} and {@link #unlockSubscriptionsForUser}.
     */
    private final static ConcurrentMap<User, NamedReentrantReadWriteLock> subscriptionLocksForUsers = new ConcurrentHashMap<>();

    /**
     * Handle webhook
     */
    public abstract void handle(HttpServletRequest request, HttpServletResponse response);

    /**
     * Return request path which this handler will be able to handle for. Webhook servlet url mapping has to be configured
     * with a wildcard, ie <code>subscription/hook/*</code>, so if path at the wildcard matches with this handler's path
     * then it will handle the webhook request
     */
    public abstract String getHandlerPath();

    public void setServletContext(SubscriptionWebHookServlet context) {
        this.context = context;
    }

    protected User getUser(String customerId) {
        return context.getSecurityService().getUserByName(customerId);
    }
    
    protected void lockSubscriptionsForUser(final User user) {
        LockUtil.lockForWrite(subscriptionLocksForUsers.computeIfAbsent(user, u->new NamedReentrantReadWriteLock("Subscriptions lock for user "+user.getName(), /* fair */ false)));
    }

    protected void unlockSubscriptionsForUser(final User user) {
        LockUtil.unlockAfterWrite(subscriptionLocksForUsers.computeIfAbsent(user, u->new NamedReentrantReadWriteLock("Subscriptions lock for user "+user.getName(), /* fair */ false)));
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
