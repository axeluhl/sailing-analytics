package com.sap.sse.security.ui.server.subscription.chargebee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.Result;
import com.chargebee.models.HostedPage;
import com.chargebee.models.ItemPrice;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.SubscriptionPlan;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscriptionProvider;
import com.sap.sse.security.subscription.chargebee.ChargebeeConfiguration;
import com.sap.sse.security.ui.client.subscription.SubscriptionService;
import com.sap.sse.security.ui.client.subscription.chargebee.ChargebeeSubscriptionService;
import com.sap.sse.security.ui.server.subscription.SubscriptionServiceImpl;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionListDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.ChargebeeConfigurationDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.ChargebeeSubscriptionDTO;
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
    public ArrayList<SubscriptionPlanDTO> getAllSubscriptionPlans() {
        ArrayList<SubscriptionPlanDTO> result = new ArrayList<>();
        final Collection<SubscriptionPlan> plans = getSecurityService().getAllSubscriptionPlans().values();
        plans.forEach(plan -> {
            result.add(convertToDto(plan));
        });
        return result;
    }
    
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
    public PrepareCheckoutDTO prepareCheckout(String priceId) {
        PrepareCheckoutDTO response = new PrepareCheckoutDTO();
        try {
            User user = getCurrentUser();
            final SubscriptionPlan planForPrice = getSubscriptionPlanForPrice(priceId);
            if (!isUserSubscribedToPlan(user, planForPrice.getId())
                    || isSubscriptionCancelled(user.getSubscriptionByPlan(planForPrice.getId()))) {
                Pair<String, String> usernames = getUserFirstAndLastName(user);
                String locale = user.getLocaleOrDefault().getLanguage();
                Result result = HostedPage.checkoutNewForItems()
                        .subscriptionItemItemPriceId(0, priceId)
                        .subscriptionItemQuantity(0,1)
                        .customerId(user.getName()).customerEmail(user.getEmail())
                        .customerFirstName(usernames.getA()).customerLastName(usernames.getB())
                        .customerLocale(locale).billingAddressFirstName(usernames.getA())
                        .billingAddressLastName(usernames.getB()).billingAddressCountry("US").request();
                response.setHostedPageJSONString(result.hostedPage().toJson());
            } else {
                response.setError(
                        "User has already subscribed to " + planForPrice.getId() + " plan");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in generating Chargebee hosted page data ", e);
            response.setError("Error in generating Chargebee hosted page");
        }
        return response;
    }

    protected SubscriptionPlan getSubscriptionPlanForPrice(String priceId) throws Exception {
        final ItemPrice itemPrice = ItemPrice.retrieve(priceId).request().itemPrice();
        final SubscriptionPlan planForPrice = getSecurityService().getSubscriptionPlanById(itemPrice.itemId());
        return planForPrice;
    }

    @Override
    public SubscriptionListDTO getSubscriptions() {
        SubscriptionListDTO subscriptionDto = null;
        try {
            final User user = getCurrentUser();
            final Iterable<Subscription> subscriptions = user.getSubscriptions();
            if (subscriptions != null) {
                List<SubscriptionDTO> itemList = new ArrayList<SubscriptionDTO>();
                for (Subscription subscription : subscriptions) {
                    if (subscription.hasSubscriptionId() && !isSubscriptionCancelled(subscription)) {
                        itemList.add(
                                new ChargebeeSubscriptionDTO(subscription.getPlanId(), subscription.getTrialStart(),
                                        subscription.getTrialEnd(), subscription.getSubscriptionStatus(),
                                        subscription.getPaymentStatus(), subscription.getTransactionType()));
                    }
                }
                if (!itemList.isEmpty()) {
                    subscriptionDto = new SubscriptionListDTO(itemList.toArray(new SubscriptionDTO[0]), null);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getting subscription ", e);
            subscriptionDto = new SubscriptionListDTO(null, e.getMessage());
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
