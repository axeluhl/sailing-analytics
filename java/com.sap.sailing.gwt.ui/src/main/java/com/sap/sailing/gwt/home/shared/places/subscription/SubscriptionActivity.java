package com.sap.sailing.gwt.home.shared.places.subscription;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
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
                    .getAllSubscriptionPlans(new AsyncCallback<Iterable<SubscriptionPlanDTO>>() {

                        @Override
                        public void onSuccess(final Iterable<SubscriptionPlanDTO> result) {
                            result.forEach(plan -> view.addSubscriptionPlan(plan,
                                    subscriptionsPlace.getPlansToHighlight().contains(plan.getId())));
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

    private void onInvalidSubscriptionProviderError(final InvalidSubscriptionProviderException exc) {
        clientFactory.createErrorView(StringMessages.INSTANCE.errorInvalidSubscritionProvider(exc.getMessage()), exc);
    }

}
