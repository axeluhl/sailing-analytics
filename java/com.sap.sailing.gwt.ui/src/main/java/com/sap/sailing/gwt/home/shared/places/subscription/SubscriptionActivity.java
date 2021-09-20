package com.sap.sailing.gwt.home.shared.places.subscription;

import java.util.HashSet;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.desktop.partials.subscription.Subscription.Type;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.shared.StringMessagesKey;
import com.sap.sse.security.shared.subscription.InvalidSubscriptionProviderException;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class SubscriptionActivity extends AbstractActivity {

    private final SubscriptionClientFactory clientFactory;
    private final SubscriptionPlace subscriptionsPlace;

    public SubscriptionActivity(final SubscriptionPlace place, final SubscriptionClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.subscriptionsPlace = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        Window.setTitle(subscriptionsPlace.getTitle());
        final SubscriptionView view = clientFactory.createSubscriptionsView();

        try {
            clientFactory.getSubscriptionServiceFactory().getDefaultAsyncService()
                    .getAllSubscriptionPlans(new AsyncCallback<Iterable<SubscriptionPlanDTO>>() {
                        @Override
                        public void onSuccess(final Iterable<SubscriptionPlanDTO> result) {
                            result.forEach(plan -> {
                                if (checkIfUserIsOwnerOfThePlan(plan)) {
                                    view.addSubscriptionPlan(plan, Type.OWNER);
                                } else if (subscriptionsPlace.getPlansToHighlight().contains(plan.getId())) {
                                    view.addSubscriptionPlan(plan, Type.HIGHLIGHT);
                                } else {
                                    view.addSubscriptionPlan(plan, Type.DEFAULT);
                                }
                            });
                            // TODO: remove dummy impl of subscription plan
                            result.forEach(plan -> {
                                view.addSubscriptionPlan(plan, Type.OWNER);
                            });
                            // TODO: remove dummy impl of subscription plan
                            result.forEach(plan -> {
                                view.addSubscriptionPlan(plan, Type.DEFAULT);
                            });
                            // TODO: remove dummy impl of subscription plan
                            result.forEach(plan -> {
                                view.addSubscriptionPlan(plan, Type.HIGHLIGHT);
                            });
                            SubscriptionPlanDTO individualPlan = new SubscriptionPlanDTO(null /* id */,
                                    new StringMessagesKey("individual_subscription_plan_name"),
                                    new StringMessagesKey("individual_subscription_plan_description"), 
                                    null /* price */,
                                    new HashSet<StringMessagesKey>() /* features */,
                                    null /* error */);
                            view.addSubscriptionPlan(individualPlan, Type.INDIVIDUAL);
                        }

                        @Override
                        public void onFailure(final Throwable caught) {
                            clientFactory.createErrorView("TODO Failed to load subscription plans", caught);
                        }

                    });
        } catch (final InvalidSubscriptionProviderException exc) {
            onInvalidSubscriptionProviderError(exc);
        }

        panel.setWidget(view);
    }

    private boolean checkIfUserIsOwnerOfThePlan(SubscriptionPlanDTO plan) {
        // TODO: implement check
        return false;
    }

    private void onInvalidSubscriptionProviderError(final InvalidSubscriptionProviderException exc) {
        clientFactory.createErrorView(StringMessages.INSTANCE.errorInvalidSubscritionProvider(exc.getMessage()), exc);
    }

}
