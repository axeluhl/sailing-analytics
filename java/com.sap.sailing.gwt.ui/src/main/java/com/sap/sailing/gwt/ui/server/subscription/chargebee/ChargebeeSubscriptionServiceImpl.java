package com.sap.sailing.gwt.ui.server.subscription.chargebee;

import static com.chargebee.models.Subscription.cancel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;

import org.apache.commons.lang.StringUtils;

import com.chargebee.Environment;
import com.chargebee.Result;
import com.chargebee.models.HostedPage;
import com.chargebee.models.HostedPage.Content;
import com.chargebee.models.Invoice;
import com.chargebee.models.Transaction;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionService;
import com.sap.sailing.gwt.ui.client.subscription.chargebee.ChargebeeSubscriptionService;
import com.sap.sailing.gwt.ui.server.subscription.BaseSubscriptionServiceImpl;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionItem;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.ChargebeeSubscriptionItem;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.FinishCheckoutDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.PrepareCheckoutDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.SubscriptionPlan;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;

/**
 * Back-end implementation of {@link SubscriptionService} remote service interface.
 * 
 * @author Tu Tran
 */
public class ChargebeeSubscriptionServiceImpl extends BaseSubscriptionServiceImpl
        implements ChargebeeSubscriptionService {
    private static final long serialVersionUID = -4276839013785711262L;

    private static final Logger logger = Logger.getLogger(ChargebeeSubscriptionServiceImpl.class.getName());

    @Override
    public PrepareCheckoutDTO prepareCheckout(String planId) {
        PrepareCheckoutDTO response = new PrepareCheckoutDTO();
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
                            + SubscriptionPlan.getPlan(planId).getName() + " plan");
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
    public SubscriptionDTO finishCheckout(String planId, FinishCheckoutDTO data) {
        logger.info("finishCheckout hostedPageId: " + data.getHostedPageId());
        SubscriptionDTO subscriptionDto;
        try {
            User user = getCurrentUser();
            Result result = HostedPage.acknowledge(data.getHostedPageId()).request();
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
            final Iterable<Subscription> subscriptions = user.getSubscriptions();
            if (subscriptions != null) {
                List<SubscriptionItem> itemList = new ArrayList<SubscriptionItem>();
                for (Subscription subscription : subscriptions) {
                    if (StringUtils.isNotEmpty(subscription.getSubscriptionId())) {
                        itemList.add(
                                new ChargebeeSubscriptionItem(subscription.getPlanId(), subscription.getTrialStart(),
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
                logger.info(() -> "Cancel user subscription, user " + user.getName() + ", plan " + planId);
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

    @Override
    protected void initService(ServletConfig config) {
        Environment.configure(SubscriptionConfiguration.getInstance().getSite(),
                SubscriptionConfiguration.getInstance().getApiKey());
    }
}
