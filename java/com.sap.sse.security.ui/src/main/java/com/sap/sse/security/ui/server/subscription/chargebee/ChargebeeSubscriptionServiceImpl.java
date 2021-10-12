package com.sap.sse.security.ui.server.subscription.chargebee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.chargebee.ListResult;
import com.chargebee.Result;
import com.chargebee.models.HostedPage;
import com.chargebee.models.ItemPrice;
import com.chargebee.models.ItemPrice.Status;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.SubscriptionPlan;
import com.sap.sse.security.shared.subscription.SubscriptionPrice;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscriptionProvider;
import com.sap.sse.security.subscription.chargebee.ChargebeeConfiguration;
import com.sap.sse.security.ui.client.subscription.SubscriptionService;
import com.sap.sse.security.ui.client.subscription.chargebee.ChargebeeSubscriptionService;
import com.sap.sse.security.ui.server.subscription.SubscriptionServiceImpl;
import com.sap.sse.security.ui.shared.subscription.SubscriptionListDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;
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
        Set<ItemPrice> itemPrices = retrieveItemPrices();
        plans.forEach(plan -> {
            final SubscriptionPlanDTO dto = convertToDto(plan);
            final Set<SubscriptionPrice> matchingPrices = itemPrices.stream()
                .filter(price -> price.itemId().equals(plan.getId()))
                .map(price -> convertToSubcriptionPrice(price))
                .collect(Collectors.toSet());
            if(!matchingPrices.isEmpty()) {
                dto.getPrices().addAll(matchingPrices);
                result.add(dto);
            }
        });
        return result;
    }
    
    private SubscriptionPrice convertToSubcriptionPrice(ItemPrice price) {
        return new SubscriptionPrice(price.priceInDecimal(),
                SubscriptionPrice.PaymentInterval.valueOf(price.periodUnit().name()));
    }

    private Set<ItemPrice> retrieveItemPrices() {
        final HashSet<ItemPrice> result = new HashSet<>();
        try {
            final ListResult allActiveItemPrices = ItemPrice.list().status().is(Status.ACTIVE).request();
            for (ListResult.Entry entry : allActiveItemPrices) {
                result.add(entry.itemPrice());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public PrepareCheckoutDTO prepareCheckout(String planId) {
        PrepareCheckoutDTO response = new PrepareCheckoutDTO();
        if (isValidPlan(planId)) {
            try {
                User user = getCurrentUser();
                if (!isUserSubscribedToPlan(user, planId)
                        || isSubscriptionCancelled(user.getSubscriptionByPlan(planId))) {
                    Pair<String, String> usernames = getUserFirstAndLastName(user);
                    String locale = user.getLocaleOrDefault().getLanguage();
                    //TODO: Convert to a Product Catalogue 2.0 compatible model.
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
    public SubscriptionListDTO getSubscription() {
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
