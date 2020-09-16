package com.sap.sailing.gwt.ui.server.subscription.chargebee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.chargebee.models.Invoice;
import com.chargebee.models.Transaction;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.gwt.ui.client.subscription.chargebee.SubscriptionService;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.HostedPageResultDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.SubscriptionDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.SubscriptionItem;
import com.sap.sse.common.TimePoint;
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
        if (isValidPlan(planId)) {
            try {
                User user = getCurrentUser();
                if (!isUserSubscribedToPlan(user, planId)) {
                    Pair<String, String> usernames = getUserFirstAndLastName(user);
                    String locale = user.getLocaleOrDefault().getLanguage();
                    Result result = HostedPage.checkoutNew().customerId(user.getName()).customerEmail(user.getEmail())
                            .customerFirstName(usernames.getA()).customerLastName(usernames.getB())
                            .customerLocale(locale).subscriptionPlanId(planId).billingAddressFirstName(usernames.getA())
                            .billingAddressLastName(usernames.getB()).billingAddressCountry("US").request();
                    response.setHostedPageJSONString(result.hostedPage().toJson());
                } else {
                    response.setError("User has already subscribed to "
                            + SubscriptionPlanHolder.getInstance().getPlan(planId).getName() + " plan");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error in generating Chargebee hosted page data ", e);
                response.setError("Error in generating Chargebee hosted page");
            }
        } else {
            response.setError("Invalid plan: " + planId);
        }
        return response;
    }

    @Override
    public SubscriptionDTO updatePlanSuccess(String hostedPageId) {
        SubscriptionDTO subscriptionDto;
        try {
            User user = getCurrentUser();
            Result result = HostedPage.acknowledge(hostedPageId).request();
            Content content = result.hostedPage().content();
            String transactionType = null;
            String transactionStatus = null;
            Transaction transaction = content.transaction();
            if (transaction != null) {
                transactionType = transaction.type().name().toLowerCase();
                transactionStatus = transaction.status().name().toLowerCase();
            }
            Invoice invoice = content.invoice();
            String invoiceId = null;
            String invoiceStatus = null;
            if (invoice != null) {
                invoiceId = invoice.id();
                invoiceStatus = invoice.status().name().toLowerCase();
            }
            Subscription subscription = new ChargebeeSubscription(content.subscription().id(),
                    content.subscription().planId(), content.customer().id(),
                    TimePoint.of(content.subscription().trialStart()), TimePoint.of(content.subscription().trialEnd()),
                    content.subscription().status().name().toLowerCase(), null, transactionType, transactionStatus,
                    invoiceId, invoiceStatus, TimePoint.of(content.subscription().createdAt()),
                    TimePoint.of(content.subscription().updatedAt()), Subscription.emptyTime(), TimePoint.now());
            updateUserSubscription(user, subscription);
            subscriptionDto = getSubscription();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in saving subscription", e);
            subscriptionDto = new SubscriptionDTO(null, e.getMessage());
        }
        return subscriptionDto;
    }

    @Override
    public SubscriptionDTO getSubscription() {
        SubscriptionDTO subscriptionDto = null;
        try {
            final User user = getCurrentUser();
            final Subscription[] subscriptions = user.getSubscriptions();
            if (subscriptions != null && subscriptions.length > 0) {
                List<SubscriptionItem> itemList = new ArrayList<SubscriptionItem>();
                for (Subscription subscription : subscriptions) {
                    if (StringUtils.isNotEmpty(subscription.getSubscriptionId())) {
                        itemList.add(new SubscriptionItem(subscription.getPlanId(), subscription.getTrialStart(),
                                subscription.getTrialEnd(), subscription.getSubscriptionStatus(),
                                subscription.getPaymentStatus(), subscription.getTransactionType()));
                    }
                }
                if (!itemList.isEmpty()) {
                    subscriptionDto = new SubscriptionDTO(itemList.toArray(new SubscriptionItem[0]), null);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getting subscription ", e);
            subscriptionDto = new SubscriptionDTO(null, e.getMessage());
        }

        return subscriptionDto;
    }

    @Override
    public boolean cancelSubscription(String planId) {
        boolean result;
        try {
            User user = getCurrentUser();
            Subscription subscription = user.getSubscriptionByPlan(planId);
            if (isValidSubscription(subscription)) {
                logger.log(Level.INFO, "Cancel user subscription, user " + user.getName() + ", plan " + planId);
                Result resultData = cancel(subscription.getSubscriptionId()).request();
                if (resultData.subscription().status().name().toLowerCase()
                        .equals(ChargebeeSubscription.SUBSCRIPTION_STATUS_CANCELLED)) {
                    Subscription newSubscription = ChargebeeSubscription.createEmptySubscription(
                            subscription.getPlanId(), subscription.getLatestEventTime(), TimePoint.now());
                    updateUserSubscription(user, newSubscription);
                    result = true;
                } else {
                    result = false;
                }
            } else {
                result = false;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in cancel subscription ", e);
            result = false;
        }
        return result;
    }

    private void updateUserSubscription(User user, Subscription subscription) throws UserManagementException {
        logger.log(Level.INFO,
                "Update user subscription, user " + user.getName() + ", new subsription " + subscription.toString());
        getSecurityService().updateUserSubscription(user.getName(), subscription);
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
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Interrupted while waiting for SecurityService service", e);
            }
            return result;
        });
    }

    private boolean isValidPlan(String planId) {
        return StringUtils.isNotEmpty(planId) && SubscriptionPlanHolder.getInstance().getPlan(planId) != null;
    }

    private boolean isUserSubscribedToPlan(User user, String planId) {
        return isValidSubscription(user.getSubscriptionByPlan(planId));
    }

    private boolean isValidSubscription(Subscription subscription) {
        return subscription != null && StringUtils.isNotEmpty(subscription.getSubscriptionId());
    }

    private Pair<String, String> getUserFirstAndLastName(User user) {
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

    private SecurityService getSecurityService() {
        final SecurityService service;
        try {
            service = securityService.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return service;
    }

    private User getCurrentUser() throws UserManagementException {
        User user = getSecurityService().getCurrentUser();
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }

        return user;
    }
}
