package com.sap.sse.security.ui.server.subscription.chargebee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.Result;
import com.chargebee.models.HostedPage;
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
        final ArrayList<SubscriptionPlanDTO> result = new ArrayList<>();
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
    public PrepareCheckoutDTO prepareCheckout(final String priceId) {
        final PrepareCheckoutDTO response = new PrepareCheckoutDTO();
        try {
            final User user = getCurrentUser();
            final SubscriptionPlan planForPrice = getSecurityService().getSubscriptionPlanByItemPriceId(priceId);
            if(planForPrice == null) {
                throw new IllegalArgumentException("No matching subscription plan found for given price id");
            }else if(planForPrice.getIsOneTimePlan() && user.hasAnySubscription(planForPrice.getId())) {
                throw new IllegalArgumentException("Plan can only be subscribed for once");
            }else if(isSubscribedToMutuallyExclusivePlan(user, planForPrice)) {
                throw new IllegalArgumentException("User has already subscribed to mutually exclusive plan");
            }else {
                final Pair<String, String> usernames = getUserFirstAndLastName(user);
                final String locale = user.getLocaleOrDefault().getLanguage();
                final Result result = HostedPage.checkoutNewForItems()
                        .subscriptionItemItemPriceId(0, priceId)
                        .subscriptionItemQuantity(0,1)
                        .customerId(user.getName()).customerEmail(user.getEmail())
                        .customerFirstName(usernames.getA()).customerLastName(usernames.getB())
                        .customerLocale(locale).billingAddressFirstName(usernames.getA())
                        .billingAddressLastName(usernames.getB()).billingAddressCountry("US").request();
                response.setHostedPageJSONString(result.hostedPage().toJson());
            }
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Error in generating Chargebee hosted page data ", e);
            response.setError("Error in generating Chargebee hosted page");
        }
        return response;
    }

    @Override
    public SubscriptionListDTO getSubscriptions(Boolean activeOnly) {
        SubscriptionListDTO subscriptionDto = null;
        try {
            final User user = getCurrentUser();
            final Iterable<Subscription> subscriptions = user.getSubscriptions();
            if (subscriptions != null) {
                final List<SubscriptionDTO> itemList = new ArrayList<>();
                for (final Subscription subscription : subscriptions) {
                    if (subscription.hasSubscriptionId() && (!activeOnly || !isSubscriptionCancelled(subscription))) {
                        itemList.add(
                                new ChargebeeSubscriptionDTO(subscription.getPlanId(), subscription.getSubscriptionId(),
                                        subscription.getSubscriptionStatus(), subscription.getPaymentStatus(),
                                        subscription.getTransactionType(), subscription.getReoccuringPaymentValue(),
                                        subscription.getCurrencyCode(), subscription.getSubscriptionCreatedAt(),
                                        subscription.getTrialEnd(), subscription.getCurrentTermEnd(),
                                        subscription.getCancelledAt(), subscription.getNextBillingAt()));
                    }
                }
                if (!itemList.isEmpty()) {
                    subscriptionDto = new SubscriptionListDTO(itemList.toArray(new SubscriptionDTO[0]), null);
                }
            }
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Error in getting subscription ", e);
            subscriptionDto = new SubscriptionListDTO(null, e.getMessage());
        }
        return subscriptionDto;
    }

    @Override
    protected String getProviderName() {
        return ChargebeeSubscriptionProvider.PROVIDER_NAME;
    }
    
    @Override
    protected boolean isSubscriptionCancelled(final Subscription subscription) {
        return subscription != null
                && subscription.getSubscriptionStatus().equalsIgnoreCase(ChargebeeSubscription.SUBSCRIPTION_STATUS_CANCELLED);
    }

    @Override
    public SubscriptionPlanDTO getSubscriptionPlanDTOById(final String planId) {
        final SubscriptionPlan subscriptionPlanById = getSecurityService().getSubscriptionPlanById(planId);
        return subscriptionPlanById == null ? null : convertToDto(subscriptionPlanById);
    }
}
