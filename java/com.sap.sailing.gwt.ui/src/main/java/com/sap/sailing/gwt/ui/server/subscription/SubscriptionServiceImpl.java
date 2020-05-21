package com.sap.sailing.gwt.ui.server.subscription;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.chargebee.Environment;
import com.chargebee.Result;
import com.chargebee.models.HostedPage;
import com.chargebee.models.HostedPage.Content;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionService;
import com.sap.sailing.gwt.ui.shared.subscription.HostedPageResultDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionPlans;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.Subscription;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.ui.server.Activator;

/**
 * Back-end implementation of {@link SubscriptionService} remote service interface.
 * 
 * @author tutran
 */
public class SubscriptionServiceImpl extends RemoteServiceServlet implements SubscriptionService {
    private static final long serialVersionUID = -4276839013785711262L;

    private static final Logger logger = Logger.getLogger(SubscriptionServiceImpl.class.getName());

    private final BundleContext context;
    private final CompletableFuture<SecurityService> securityService;

    public SubscriptionServiceImpl() {
        // Configure payment service
        Environment.configure(SubscriptionConfiguration.getInstance().getSite(),
                SubscriptionConfiguration.getInstance().getApiKey());

        // get SecurityService
        context = Activator.getContext();
        final ServiceTracker<SecurityService, SecurityService> tracker = new ServiceTracker<>(context,
                SecurityService.class, null);
        tracker.open();
        securityService = CompletableFuture.supplyAsync(() -> {
            SecurityService result = null;
            try {
                logger.info("Waiting for SecurityService...");
                result = tracker.waitForService(0);
                logger.info("Obtained SecurityService " + result);
                SecurityUtils.setSecurityManager(result.getSecurityManager());
                return result;
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Interrupted while waiting for SecurityService service", e);
                return null;
            }
        });
    }

    @Override
    public HostedPageResultDTO generateHostedPageObject(String planId) {

        HostedPageResultDTO response = new HostedPageResultDTO();

        // Validate if plan id is valid
        if (planId == null || planId.isEmpty() || SubscriptionPlans.getPlan(planId) == null) {
            response.error = "Invalid plan";
            return response;
        }

        try {
            User user = getCurrentUser();

            // Check if user already subscribed to a plan, and if it's same plan with new plan then we stop the process
            // and send back error
            if (user.getSubscription() != null && user.getSubscription().getPlanId() != null
                    && user.getSubscription().getPlanId().equals(planId)) {
                response.error = "User has already subscribed to " + SubscriptionPlans.getPlan(planId).getName()
                        + " plan";
                return response;
            }

            Result result;

            // If there's no subscription data attach to user model then we create a checkout-new request
            if (user.getSubscription() == null || user.getSubscription().getPlanId() == null) {
                String[] userNameParts = user.getFullName().split("\\s+");
                String firstName = userNameParts[0];
                String lastName = "";
                if (userNameParts.length > 1) {
                    lastName = String.join(" ", Arrays.copyOfRange(userNameParts, 1, userNameParts.length));
                }

                String locale = user.getLocaleOrDefault().getLanguage();

                // Send checkout-new request with all necessary customer information and get back hosted page result
                // object
                result = HostedPage.checkoutNew()
                        // customer id is same as the system user name
                        .customerId(user.getName()).customerEmail(user.getEmail()).customerFirstName(firstName)
                        .customerLastName(lastName).customerLocale(locale).subscriptionPlanId(planId)
                        .billingAddressFirstName(firstName).billingAddressLastName(lastName).billingAddressCountry("US")
                        .request();
            } else {
                // User has already subscribed to a plan, and user wants to change plan
                // so we make a checkout-existing request
                result = HostedPage.checkoutExisting().subscriptionId(user.getSubscription().getSubscriptionId())
                        .subscriptionPlanId(planId).request();
            }

            response.hostedPageJSONString = result.hostedPage().toJson();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in generating Chargebee hosted page data ", e);

            response.error = "Error in generating Chargebee hosted page";
        }

        return response;
    }

    @Override
    public SubscriptionDTO updatePlanSuccess(String hostedPageId) {
        SubscriptionDTO subscriptionDto = new SubscriptionDTO();

        try {
            User user = getCurrentUser();

            Result result = HostedPage.acknowledge(hostedPageId).request();
            Content content = result.hostedPage().content();
            Subscription subscription = new Subscription();
            subscription.setSubscriptionId(content.subscription().id());
            subscription.setCustomerId(content.customer().id());
            subscription.setPlanId(content.subscription().planId());
            subscription.setTrialStart(content.subscription().trialStart().getTime() / 1000);
            subscription.setTrialEnd(content.subscription().trialEnd().getTime() / 1000);
            subscription.setSubscriptionStatus(content.subscription().status().name().toLowerCase());
            subscription.setSubsciptionCreatedAt(content.subscription().createdAt().getTime() / 1000);
            subscription.setSubsciptionUpdatedAt(content.subscription().updatedAt().getTime() / 1000);
            subscription.setLatestEventTime(0);
            subscription.setManualUpdatedAt(System.currentTimeMillis() / 1000);

            getSecurityService().updateUserSubscription(user.getName(), subscription);

            subscriptionDto.setPlanId(subscription.getPlanId());
            subscriptionDto.setTrialStart(subscription.getTrialStart());
            subscriptionDto.setTrialEnd(subscription.getTrialEnd());
            subscriptionDto.setSubscriptionStatus(subscription.getSubscriptionStatus());
            subscriptionDto.setPaymentStatus(subscription.getPaymentStatus());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in saving subscription ", e);

            subscriptionDto.setError(e.getMessage());
        }

        return subscriptionDto;
    }

    @Override
    public SubscriptionDTO getSubscription() {
        SubscriptionDTO subscriptionDto = new SubscriptionDTO();
        try {
            User user = getCurrentUser();

            Subscription subscription = user.getSubscription();
            if (subscription == null || subscription.getPlanId() == null || subscription.getPlanId().isEmpty()) {
                return null;
            }

            subscriptionDto.setPlanId(subscription.getPlanId());
            subscriptionDto.setSubscriptionStatus(subscription.getSubscriptionStatus());
            subscriptionDto.setPaymentStatus(subscription.getPaymentStatus());
            subscriptionDto.setTrialStart(subscription.getTrialStart());
            subscriptionDto.setTrialEnd(subscription.getTrialEnd());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in getting subscription ", e);

            subscriptionDto.setError(e.getMessage());
        }

        return subscriptionDto;
    }

    @Override
    public boolean cancelSubscription() {
        try {
            User user = getCurrentUser();
            Subscription subscription = user.getSubscription();
            if (subscription == null) {
                return true;
            }

            String subscriptionId = subscription.getSubscriptionId();
            if (subscriptionId != null && !subscriptionId.isEmpty()) {
                // Send cancel request, verify result and only process if the result's subscription
                // status is updated to be cancelled
                Result result = com.chargebee.models.Subscription.cancel(subscriptionId).request();
                if (!result.subscription().status().name().toLowerCase()
                        .equals(Subscription.SUBSCRIPTION_STATUS_CANCELLED)) {
                    return false;
                }
            }

            // we update user's subscription data with plan, subscription, status to be null
            Subscription newSubscription = new Subscription();
            newSubscription.setLatestEventTime(subscription.getLatestEventTime());
            newSubscription.setManualUpdatedAt(System.currentTimeMillis() / 1000);
            getSecurityService().updateUserSubscription(user.getName(), newSubscription);
            return true;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in cancel subscription ", e);
            return false;
        }
    }

    private SecurityService getSecurityService() {
        try {
            return securityService.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private User getCurrentUser() throws UserManagementException {
        User user = getSecurityService().getCurrentUser();
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }

        return user;
    }
}
