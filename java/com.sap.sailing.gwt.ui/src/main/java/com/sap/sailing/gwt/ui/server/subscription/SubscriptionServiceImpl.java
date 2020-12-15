package com.sap.sailing.gwt.ui.server.subscription;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.SubscriptionPlan;
import com.sap.sse.security.subscription.SubscriptionApiService;
import com.sap.sse.security.ui.server.Activator;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * Base class with some util methods for backend subscription remote service implementation. To setup service provider
 * regarding logic, or initialize any custom service logics, particular child implementation should override
 * {@code initService} method
 */
public abstract class SubscriptionServiceImpl extends RemoteServiceServlet {
    private static final long serialVersionUID = -2953209842119970755L;
    private static final Logger logger = Logger.getLogger(SubscriptionServiceImpl.class.getName());

    private BundleContext context;
    private CompletableFuture<SecurityService> securityService;
    private ServiceTracker<SubscriptionApiService, SubscriptionApiService> subscriptionApiServiceTracker;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        context = Activator.getContext();
        securityService = initSecurityService();
        subscriptionApiServiceTracker = ServiceTrackerFactory.createAndOpen(context, SubscriptionApiService.class);
    }

    private CompletableFuture<SecurityService> initSecurityService() {
        final FullyInitializedReplicableTracker<SecurityService> tracker = FullyInitializedReplicableTracker.createAndOpen(context, SecurityService.class);
        return CompletableFuture.supplyAsync(() -> {
            SecurityService result = null;
            try {
                logger.info("Waiting for SecurityService...");
                result = tracker.getInitializedService(0);
                logger.info("Obtained SecurityService " + result);
                SecurityUtils.setSecurityManager(result.getSecurityManager());
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Interrupted while waiting for SecurityService service", e);
            }
            return result;
        });
    }

    /**
     * Save user subscription
     */
    protected void updateUserSubscription(User user, Subscription subscription) throws UserManagementException {
        logger.info(() -> "Update user subscription, user " + user.getName() + ", new subsription "
                + subscription.toString());
        getSecurityService().updateUserSubscription(user.getName(), subscription);
    }

    /**
     * Check if planId is valid
     */
    protected boolean isValidPlan(String planId) {
        return StringUtils.isNotEmpty(planId) && SubscriptionPlan.getPlan(planId) != null;
    }

    /**
     * Return true if user already subscribed to plan
     */
    protected boolean isUserSubscribedToPlan(User user, String planId) {
        return isValidSubscription(user.getSubscriptionByPlan(planId));
    }

    /**
     * Check if subscription is valid
     */
    protected boolean isValidSubscription(Subscription subscription) {
        return subscription != null && StringUtils.isNotEmpty(subscription.getSubscriptionId());
    }

    /**
     * Util method for getting user first and last name
     */
    protected Pair<String, String> getUserFirstAndLastName(User user) {
        final Pair<String, String> result;
        if (user.getFullName() == null || user.getFullName().isEmpty()) {
            result = new Pair<>(user.getName(), "");
        } else {
            final String[] userNameParts = user.getFullName().split("\\s+");
            final String firstName = userNameParts[0];
            final String lastName;
            if (userNameParts.length > 1) {
                lastName = String.join(" ", Arrays.copyOfRange(userNameParts, 1, userNameParts.length));
            } else {
                lastName = "";
            }
            result = new Pair<>(firstName, lastName);
        }
        return result;
    }

    protected SecurityService getSecurityService() {
        final SecurityService service;
        try {
            service = securityService.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return service;
    }

    protected User getCurrentUser() throws UserManagementException {
        User user = getSecurityService().getCurrentUser();
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }

        return user;
    }

    protected SubscriptionApiService getApiService() {
        ServiceReference<SubscriptionApiService>[] serviceReferences = subscriptionApiServiceTracker.getServiceReferences();
        for (final ServiceReference<SubscriptionApiService> serviceReference : serviceReferences) {
            if (Util.equalsWithNull(serviceReference.getProperty(SubscriptionApiService.PROVIDER_NAME_OSGI_REGISTRY_KEY), getProviderName())) {
                return context.getService(serviceReference);
            }
        }
        return null;
    }

    protected abstract String getProviderName();
}
