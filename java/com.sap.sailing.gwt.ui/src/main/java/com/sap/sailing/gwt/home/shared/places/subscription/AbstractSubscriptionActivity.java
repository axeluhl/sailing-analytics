package com.sap.sailing.gwt.home.shared.places.subscription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCard.Type;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.shared.subscription.InvalidSubscriptionProviderException;
import com.sap.sse.security.shared.subscription.SubscriptionPlan.PlanCategory;
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
                            Map<PlanCategory, SubscriptionCategoryDTO> categoryMap = new HashMap<PlanCategory, SubscriptionCategoryDTO>();
                            result.forEach(plan -> {
                                final Type type;
                                if (checkIfUserIsOwnerOfThePlan(plan) || checkIfUserIsSubscribedToPlanCategory(plan)) {
                                    type = Type.OWNER;
                                } else if (subscriptionsPlace.getPlansToHighlight().contains(plan.getSubscriptionPlanId())) {
                                    type = Type.HIGHLIGHT;
                                } else {
                                    type = Type.DEFAULT;
                                }
                                plan.getPlanCategory().forEach(category -> {
                                    final SubscriptionCategoryDTO categoryDTO;
                                    if (categoryMap.containsKey(category)) {
                                        categoryDTO = categoryMap.get(category);
                                        plan.getPrices().forEach(price -> {
                                            price.setDisablePlan(plan.isUserWasAlreadySubscribedToOneTimePlan());
                                            categoryDTO.getPrices().add(price);
                                        });
                                    } else {
                                        categoryDTO = new SubscriptionCategoryDTO(category.getId(), plan.isUserSubscribedToPlan(), plan.getPrices(), category, plan.isUserWasAlreadySubscribedToOneTimePlan(), plan.isUserSubscribedToPlanCategory(), plan.getError(), type);
                                        categoryMap.put(category, categoryDTO);
                                    }
                                    categoryDTO.getPrices().forEach(price -> {
                                        price.setDisablePlan(plan.isUserWasAlreadySubscribedToOneTimePlan());
                                    });
                                });
                            });
                            List<SubscriptionCategoryDTO> categories = new ArrayList<SubscriptionCategoryDTO>(categoryMap.values());
                            categories.sort(new Comparator<SubscriptionCategoryDTO>() {

                                @Override
                                public int compare(SubscriptionCategoryDTO o1, SubscriptionCategoryDTO o2) {
                                    return o1.getPlanCategory().compareTo(o2.getPlanCategory());
                                }
                            });
                            categories.forEach(category -> {
                                GWT.log("Category: " + category.getSubscriptionCategoryId());
                                view.addSubscriptionCategory(category, category.getType(), eventBus);
                            });
                        }

                        @Override
                        public void onFailure(final Throwable caught) {
                            clientFactory.createErrorView(StringMessages.INSTANCE.currentlyUnableToSubscribe(), caught);
                        }

                        private void addFreePlan(final SubscriptionView view) {
                            final SubscriptionCategoryDTO freePlan = new SubscriptionCategoryDTO(
                                    "free_subscription_plan" /* id */, /* isUserSubscribedToPlan */ false,
                                    Collections.emptySet() /* prices */, /* planCategory */ null,
                                    /* userWasAlreadySubscribedToOneTimePlan */ false, /*isUserSubscribedToPlanCategory*/ false, null /* error */, Type.DEFAULT);
                            view.addSubscriptionCategory(freePlan, Type.FREE, eventBus);
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
        return plan.isUserSubscribedToPlanCategory();
    }

    private void onInvalidSubscriptionProviderError(final InvalidSubscriptionProviderException exc) {
        clientFactory.createErrorView(StringMessages.INSTANCE.errorInvalidSubscritionProvider(exc.getMessage()), exc);
    }

    @Override
    public boolean isMailVerificationRequired() {
        return isMailVerificationRequired;
    }

}
