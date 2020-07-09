package com.sap.sailing.gwt.ui.server.subscription.chargebee;

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
import org.osgi.util.tracker.ServiceTracker;

import com.chargebee.Environment;
import com.chargebee.Result;
import com.chargebee.models.HostedPage;
import com.chargebee.models.HostedPage.Content;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.gwt.ui.client.subscription.chargebee.SubscriptionService;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.HostedPageResultDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.SubscriptionDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.Subscription;
import com.sap.sse.security.shared.SubscriptionPlanHolder;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.ChargebeeSubscription;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.ui.server.Activator;
import static com.chargebee.models.Subscription.cancel;

/**
 * Back-end implementation of {@link SubscriptionService} remote service interface.
 * 
 * @author Tu Tran
 */
public class SubscriptionServiceImpl extends RemoteServiceServlet implements SubscriptionService {
    private static final long serialVersionUID = -4276839013785711262L;

    private static final Logger logger = Logger.getLogger(SubscriptionServiceImpl.class.getName());

    private BundleContext context;
    private CompletableFuture<SecurityService> securityService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        initPaymentService();
        initSecurityService();
    }

    @Override
    public HostedPageResultDTO generateHostedPageObject(String planId) {

        HostedPageResultDTO response = new HostedPageResultDTO();

        if (!isValidPlan(planId)) {
            response.setError("Invalid plan");
            return response;
        }

        try {
            User user = getCurrentUser();

            if (isUserSubscribedToPlan(user.getSubscription(), planId)) {
                response.setError("User has already subscribed to "
                        + SubscriptionPlanHolder.getInstance().getPlan(planId).getName() + " plan");
                return response;
            }

            Result result;

            if (!hasUserSubscription(user.getSubscription())) {
                Pair<String, String> usernames = getUserFirstAndLastName(user.getFullName());
                String locale = user.getLocaleOrDefault().getLanguage();

                // Make a checkout-new request
                result = HostedPage.checkoutNew().customerId(user.getName()).customerEmail(user.getEmail())
                        .customerFirstName(usernames.getA()).customerLastName(usernames.getB()).customerLocale(locale)
                        .subscriptionPlanId(planId).billingAddressFirstName(usernames.getA())
                        .billingAddressLastName(usernames.getB()).billingAddressCountry("US").request();
            } else {
                // Make a checkout-existing request
                result = HostedPage.checkoutExisting().subscriptionId(user.getSubscription().getSubscriptionId())
                        .subscriptionPlanId(planId).request();
            }

            response.setHostedPageJSONString(result.hostedPage().toJson());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in generating Chargebee hosted page data ", e);
            response.setError("Error in generating Chargebee hosted page");
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

            Subscription subscription = new ChargebeeSubscription(content.subscription().id(),
                    content.subscription().planId(), content.customer().id(),
                    content.subscription().trialStart().getTime() / 1000,
                    content.subscription().trialEnd().getTime() / 1000,
                    content.subscription().status().name().toLowerCase(), null,
                    content.subscription().createdAt().getTime() / 1000,
                    content.subscription().updatedAt().getTime() / 1000, 0, System.currentTimeMillis() / 1000);

            getSecurityService().updateUserSubscription(user.getName(), subscription);

            subscriptionDto.setPlanId(subscription.getPlanId());
            subscriptionDto.setTrialStart(subscription.getTrialStart());
            subscriptionDto.setTrialEnd(subscription.getTrialEnd());
            subscriptionDto.setSubscriptionStatus(subscription.getSubscriptionStatus());
            subscriptionDto.setPaymentStatus(subscription.getPaymentStatus());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in saving subscription ", e);

            subscriptionDto.setError(e.getMessage());
        }

        return subscriptionDto;
    }

    @Override
    public SubscriptionDTO getSubscription() {
        SubscriptionDTO subscriptionDto = new SubscriptionDTO();
        try {
            final User user = getCurrentUser();
            final Subscription subscription = user.getSubscription();
            if (subscription == null || subscription.getPlanId() == null || subscription.getPlanId().isEmpty()) {
                return null;
            }
            subscriptionDto.setPlanId(subscription.getPlanId());
            subscriptionDto.setSubscriptionStatus(subscription.getSubscriptionStatus());
            subscriptionDto.setPaymentStatus(subscription.getPaymentStatus());
            subscriptionDto.setTrialStart(subscription.getTrialStart());
            subscriptionDto.setTrialEnd(subscription.getTrialEnd());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getting subscription ", e);

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
                Result result = cancel(subscriptionId).request();
                if (!result.subscription().status().name().toLowerCase()
                        .equals(ChargebeeSubscription.SUBSCRIPTION_STATUS_CANCELLED)) {
                    return false;
                }
            }

            Subscription newSubscription = ChargebeeSubscription
                    .createEmptySubscription(subscription.getLatestEventTime(), System.currentTimeMillis() / 1000);
            getSecurityService().updateUserSubscription(user.getName(), newSubscription);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in cancel subscription ", e);
            return false;
        }
    }

    private void initPaymentService() {
        Environment.configure(SubscriptionConfiguration.getInstance().getSite(),
                SubscriptionConfiguration.getInstance().getApiKey());
    }

    private void initSecurityService() {
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

    private boolean isValidPlan(String planId) {
        return StringUtils.isNotEmpty(planId) && SubscriptionPlanHolder.getInstance().getPlan(planId) != null;
    }

    private boolean isUserSubscribedToPlan(Subscription userSubscription, String planId) {
        return userSubscription != null && userSubscription.getPlanId() != null
                && userSubscription.getPlanId().equals(planId);
    }

    private boolean hasUserSubscription(Subscription userSubscription) {
        return userSubscription != null && userSubscription.getPlanId() != null;
    }

    private Pair<String, String> getUserFirstAndLastName(String fullName) {
        String[] userNameParts = fullName.split("\\s+");
        String firstName = userNameParts[0];
        String lastName = "";
        if (userNameParts.length > 1) {
            lastName = String.join(" ", Arrays.copyOfRange(userNameParts, 1, userNameParts.length));
        }

        return new Pair<String, String>(firstName, lastName);
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
