package com.sap.sse.security.ui.server.subscription.chargebee;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.Result;
import com.chargebee.models.HostedPage;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscriptionProvider;
import com.sap.sse.security.subscription.chargebee.ChargebeeConfiguration;
import com.sap.sse.security.ui.client.subscription.SubscriptionService;
import com.sap.sse.security.ui.client.subscription.chargebee.ChargebeeSubscriptionService;
import com.sap.sse.security.ui.server.subscription.SubscriptionServiceImpl;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionItem;
import com.sap.sse.security.ui.shared.subscription.chargebee.ChargebeeConfigurationDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.ChargebeeSubscriptionItem;
import com.sap.sse.security.ui.shared.subscription.chargebee.PrepareCheckoutDTO;

/**
 * Back-end implementation of {@link SubscriptionService} remote service interface.
 * 
 * @author Tu Tran
 */
public class ChargebeeSubscriptionServiceImpl extends
        SubscriptionServiceImpl<ChargebeeConfigurationDTO, PrepareCheckoutDTO> implements ChargebeeSubscriptionService {
    private static final long serialVersionUID = -4276839013785711262L;

    private static final Logger logger = Logger.getLogger(ChargebeeSubscriptionServiceImpl.class.getName());

    @Override
    public ChargebeeConfigurationDTO getConfiguration() {
        final ChargebeeConfiguration configuration = ChargebeeConfiguration.getInstance();
        final ChargebeeConfigurationDTO result;
        if (configuration != null) {
            result = new ChargebeeConfigurationDTO(configuration.getSite());
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public PrepareCheckoutDTO prepareCheckout(String planId) {
        PrepareCheckoutDTO response = new PrepareCheckoutDTO();
        if (isValidPlan(planId)) {
            try {
                User user = getCurrentUser();
                if (!isUserSubscribedToPlan(user, planId)
                        || isSubscriptionCancelled(user.getSubscriptionByPlan(planId))) {
                    Pair<String, String> usernames = getUserFirstAndLastName(user);
                    String locale = user.getLocaleOrDefault().getLanguage();
                    Result result = HostedPage.checkoutNewForItems()
                            .subscriptionItemItemPriceId(0, planId)
                            .subscriptionItemQuantity(0,1)
                            .customerId(user.getName()).customerEmail(user.getEmail())
                            .customerFirstName(usernames.getA()).customerLastName(usernames.getB())
                            .customerLocale(locale).billingAddressFirstName(usernames.getA())
                            .billingAddressLastName(usernames.getB()).billingAddressCountry("US").request();
                    response.setHostedPageJSONString(result.hostedPage().toJson());
                } else {
                    response.setError(
                            "User has already subscribed to " + getSecurityService().getSubscriptionPlanById(planId).getId() + " plan");
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
    public SubscriptionDTO getSubscription() {
        SubscriptionDTO subscriptionDto = null;
        try {
            final User user = getCurrentUser();
            final Iterable<Subscription> subscriptions = user.getSubscriptions();
            if (subscriptions != null) {
                List<SubscriptionItem> itemList = new ArrayList<SubscriptionItem>();
                for (Subscription subscription : subscriptions) {
                    if (subscription.hasSubscriptionId() && !isSubscriptionCancelled(subscription)) {
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
    protected String getProviderName() {
        return ChargebeeSubscriptionProvider.PROVIDER_NAME;
    }

    protected boolean isSubscriptionCancelled(Subscription subscription) {
        return subscription != null
                && subscription.getSubscriptionStatus().equals(ChargebeeSubscription.SUBSCRIPTION_STATUS_CANCELLED);
    }

}
