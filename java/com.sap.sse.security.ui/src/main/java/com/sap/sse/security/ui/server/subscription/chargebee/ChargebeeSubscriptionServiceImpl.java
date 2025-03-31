package com.sap.sse.security.ui.server.subscription.chargebee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.models.PortalSession;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.SubscriptionPlan;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscriptionProvider;
import com.sap.sse.security.ui.client.subscription.SubscriptionService;
import com.sap.sse.security.ui.client.subscription.chargebee.ChargebeeSubscriptionService;
import com.sap.sse.security.ui.server.subscription.SubscriptionServiceImpl;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionListDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.ChargebeeSubscriptionDTO;

/**
 * Back-end implementation of {@link SubscriptionService} remote service interface.
 *
 * @author Tu Tran
 */
public class ChargebeeSubscriptionServiceImpl extends SubscriptionServiceImpl implements ChargebeeSubscriptionService {
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
        } catch(final UserManagementException e) {
            logger.log(Level.FINE, "No user is logged in.");
            subscriptionDto = new SubscriptionListDTO(null, e.getMessage());
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
    
    @Override
    public String getSelfServicePortalSession() {
        try {
            User currentUser = getCurrentUser();
            CompletableFuture<PortalSession> result = new CompletableFuture<>();
            getApiService().getUserSelfServicePortalSession(currentUser.getId().toString(), (session) -> result.complete(session));
            final PortalSession portalSession = result.get();
            if(portalSession != null) {
                return portalSession.accessUrl();
            }else {
                return null;
            }
        } catch (UserManagementException e) {
            logger.log(Level.FINE, "No user is logged in.");
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getting session", e);
            return null;
        }
    }
}
