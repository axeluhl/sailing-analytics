package com.sap.sailing.gwt.home.shared.places.subscription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCard.Type;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.shared.subscription.InvalidSubscriptionProviderException;
import com.sap.sse.security.shared.subscription.SubscriptionPlan.PlanGroup;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public abstract class AbstractSubscriptionActivity extends AbstractActivity implements SubscriptionView.Presenter {

    private final SubscriptionClientFactory clientFactory;
    private final SubscriptionPlace subscriptionsPlace;
    private final SubscriptionView view;
    private boolean isMailVerificationRequired;

    protected AbstractSubscriptionActivity(final SubscriptionPlace place,
            final SubscriptionClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.subscriptionsPlace = place;
        this.view = clientFactory.createSubscriptionsView();
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        Window.setTitle(subscriptionsPlace.getTitle());
        view.setPresenter(this);
        try {
            clientFactory.getSubscriptionServiceFactory().getDefaultWriteAsyncService()
                    .isMailVerificationRequired(new AsyncCallback<Boolean>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            clientFactory.createErrorView(StringMessages.INSTANCE.currentlyUnableToSubscribe(), caught);
                        }

                        @Override
                        public void onSuccess(Boolean result) {
                            isMailVerificationRequired = Boolean.valueOf(result);
                            renderSubscriptions(eventBus);
                        }
                    });
        } catch (InvalidSubscriptionProviderException e) {
            clientFactory.createErrorView(StringMessages.INSTANCE.currentlyUnableToSubscribe(), e);
        }
        eventBus.addHandler(AuthenticationContextEvent.TYPE, event -> {
            renderSubscriptions(eventBus);
        });

        panel.setWidget(view);
    }
    
    private void renderSubscriptions(final EventBus eventBus) {
        try {
            clientFactory.getSubscriptionServiceFactory().getDefaultAsyncService()
                    .getAllSubscriptionPlans(new AsyncCallback<ArrayList<SubscriptionPlanDTO>>() {
                        @Override
                        public void onSuccess(final ArrayList<SubscriptionPlanDTO> result) {
                            view.resetSubscriptions();
                            addFreePlan(view);
                            Map<PlanGroup, SubscriptionGroupDTO> groupMap = new HashMap<>();
                            result.forEach(plan -> {
                                final Type type;
                                if (checkIfUserIsOwnerOfThePlan(plan) || checkIfUserIsSubscribedToPlanCategory(plan)) {
                                    type = Type.OWNER;
                                } else if (checkIfUserWasAlreadySubscripedToOneTimePlan(plan)) {
                                    type = Type.ONETIMELOCK;
                                } else if (subscriptionsPlace.getPlansToHighlight().contains(plan.getSubscriptionPlanId())) {
                                    type = Type.HIGHLIGHT;
                                } else if (checkIfPlanWouldBeAnUpgrade(plan)) {
                                    type = Type.UPGRADE;
                                } else {
                                    type = Type.DEFAULT;
                                }
                                final SubscriptionGroupDTO groupDTO;
                                if (groupMap.containsKey(plan.getGroup())) {
                                    groupDTO = groupMap.get(plan.getGroup());
                                    plan.getPrices().forEach(price -> {
                                        groupDTO.getPrices().add(price);
                                    });
                                } else {
                                    groupDTO = new SubscriptionGroupDTO(plan.getGroup().getId(),
                                            plan.isUserSubscribedToPlan(), plan.getPrices(), plan.getGroup(),
                                            plan.isUserSubscribedToAllPlanCategories(), plan.getError(), type);
                                    groupMap.put(plan.getGroup(), groupDTO);
                                }
                            });
                            List<SubscriptionGroupDTO> groups = new ArrayList<SubscriptionGroupDTO>(groupMap.values());
                            groups.sort(new Comparator<SubscriptionGroupDTO>() {
                                
                                @Override
                                public int compare(SubscriptionGroupDTO o1, SubscriptionGroupDTO o2) {
                                    // comparing by ordinal
                                    return o1.getGroup().ordinal() - o2.getGroup().ordinal();
                                }
                            });
                            groups.forEach(group -> {
                                view.addSubscriptionGroup(group, group.getType(), eventBus);
                            });
                        }

                        @Override
                        public void onFailure(final Throwable caught) {
                            clientFactory.createErrorView(StringMessages.INSTANCE.currentlyUnableToSubscribe(), caught);
                        }

                        private void addFreePlan(final SubscriptionView view) {
                            final SubscriptionGroupDTO freePlan = new SubscriptionGroupDTO(
                                    "free_subscription_plan" /* id */, /* isUserSubscribedToPlan */ false,
                                    Collections.emptySet() /* prices */, /* group */ null, 
                                    /*isUserSubscribedToPlanCategory*/ false, null /* error */, Type.DEFAULT);
                            view.addSubscriptionGroup(freePlan, Type.FREE, eventBus);
                        }
                    });
        } catch (final InvalidSubscriptionProviderException exc) {
            onInvalidSubscriptionProviderError(exc);
        }
    }

    @Override
    public void startSubscription(final String priceId) {
        try {
            clientFactory.getSubscriptionServiceFactory().getDefaultProvider().getSubscriptionViewPresenter()
                    .startCheckout(priceId, view, () -> clientFactory.getUserService().updateUser(true));
        } catch (final InvalidSubscriptionProviderException e) {
            view.onOpenCheckoutError(e.toString());
        }
    }
    
    @Override
    public AuthenticationContext getAuthenticationContext() {
        return clientFactory.getAuthenticationManager().getAuthenticationContext();
    }

    private boolean checkIfUserIsOwnerOfThePlan(final SubscriptionPlanDTO plan) {
        return plan.isUserSubscribedToPlan();
    }
    
    private boolean checkIfUserIsSubscribedToPlanCategory(final SubscriptionPlanDTO plan) {
        return plan.isUserSubscribedToAllPlanCategories();
    }
    
    private boolean checkIfUserWasAlreadySubscripedToOneTimePlan(final SubscriptionPlanDTO plan) {
        return plan.isUserWasAlreadySubscribedToOneTimePlan();
    }
    
    private boolean checkIfPlanWouldBeAnUpgrade(final SubscriptionPlanDTO plan) {
        return plan.isOneOfTheUserSubscriptionsIsCoveredByPlan();
    }

    private void onInvalidSubscriptionProviderError(final InvalidSubscriptionProviderException exc) {
        clientFactory.createErrorView(StringMessages.INSTANCE.errorInvalidSubscritionProvider(exc.getMessage()), exc);
    }

    @Override
    public boolean isMailVerificationRequired() {
        return isMailVerificationRequired;
    }

}
