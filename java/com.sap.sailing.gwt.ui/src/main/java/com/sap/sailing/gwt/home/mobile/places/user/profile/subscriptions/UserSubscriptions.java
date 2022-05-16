package com.sap.sailing.gwt.home.mobile.places.user.profile.subscriptions;

import static java.util.Arrays.asList;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsView;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionListDTO;

/**
 * Implementation of {@link UserSettingsView} where users can change their preferred selections and notifications.
 */
public class UserSubscriptions extends Composite implements UserSubscriptionsView {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserSubscriptions> {
    }

    @UiField
    SharedResources res;
    @UiField
    FlowPanel subscriptionsContainerUi;

    private final Presenter presenter;

    public UserSubscriptions(final UserSubscriptionsView.Presenter presenter) {
        this.presenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
        presenter.setView(this);
    }

    @Override
    public void updateView(final SubscriptionListDTO subscription) {
        subscriptionsContainerUi.clear();
        if (subscription != null) {
            final List<SubscriptionDTO> subscriptionItems = asList(subscription.getSubscriptionItems());
            if (!subscriptionItems.isEmpty()) {
                subscriptionItems.forEach(s -> subscriptionsContainerUi.add(new UserSubscriptionItem(s, presenter)));
                return;
            }

        }
        final Label noDataFoundWidget = new Label(StringMessages.INSTANCE.noDataFound());
        noDataFoundWidget.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
        noDataFoundWidget.addStyleName(res.mainCss().spacermargintopsmall());
        subscriptionsContainerUi.add(noDataFoundWidget);
    }

    @UiHandler("subscribeButtonUi")
    public void onSubscribeClicked(final ClickEvent event) {
        presenter.navigateToSubscribe();
    }
    
    @UiHandler("selfServiceControlUi")
    void onSelfServiceControlClicked(final ClickEvent event) {
        presenter.openSelfServicePortal();
    }

}
