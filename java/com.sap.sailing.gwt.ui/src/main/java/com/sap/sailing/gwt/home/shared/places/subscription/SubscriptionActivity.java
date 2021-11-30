package com.sap.sailing.gwt.home.shared.places.subscription;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCard.Type;
import com.sap.sailing.gwt.ui.client.StringMessages;
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
                    .getAllSubscriptionPlans(new AsyncCallback<ArrayList<SubscriptionPlanDTO>>() {
                        @Override
                        public void onSuccess(final ArrayList<SubscriptionPlanDTO> result) {
                            addFreePlan(view);
                            result.forEach(plan -> {
                                if (checkIfUserIsOwnerOfThePlan(plan)) {
                                    view.addSubscriptionPlan(plan, Type.OWNER, eventBus);
                                } else if (subscriptionsPlace.getPlansToHighlight().contains(plan.getId())) {
                                    view.addSubscriptionPlan(plan, Type.HIGHLIGHT, eventBus);
                                } else {
                                    view.addSubscriptionPlan(plan, Type.DEFAULT, eventBus);
                                }
                            });
                        }

                        @Override
                        public void onFailure(final Throwable caught) {
                            clientFactory.createErrorView("TODO Failed to load subscription plans", caught);
                        }

                        private void addFreePlan(final SubscriptionView view) {
                            final SubscriptionPlanDTO freePlan = new SubscriptionPlanDTO("free_subscription_plan" /* id */,
                                    /* isUserSubscribedToPlan */ false,
                                    Collections.emptySet() /* prices */, null /* error */);
                            view.addSubscriptionPlan(freePlan, Type.FREE, eventBus);
                        }
                    });
        } catch (final InvalidSubscriptionProviderException exc) {
            onInvalidSubscriptionProviderError(exc);
        }
        panel.setWidget(view);
    }

    private boolean checkIfUserIsOwnerOfThePlan(SubscriptionPlanDTO plan) {
        return plan.isUserSubscribedToPlan();
    }

    private void onInvalidSubscriptionProviderError(final InvalidSubscriptionProviderException exc) {
        clientFactory.createErrorView(StringMessages.INSTANCE.errorInvalidSubscritionProvider(exc.getMessage()), exc);
    }

}
