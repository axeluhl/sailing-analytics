package com.sap.sailing.gwt.home.desktop.partials.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class SubscriptionContainer extends Composite {

    private static SubscriptionContainerUiBinder uiBinder = GWT.create(SubscriptionContainerUiBinder.class);

    interface SubscriptionContainerUiBinder extends UiBinder<Widget, SubscriptionContainer> {
    }

    public SubscriptionContainer() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiField
    FlowPanel container;
    
    public void addSubscription(Subscription subscription) {
        container.add(subscription);
    }

}
