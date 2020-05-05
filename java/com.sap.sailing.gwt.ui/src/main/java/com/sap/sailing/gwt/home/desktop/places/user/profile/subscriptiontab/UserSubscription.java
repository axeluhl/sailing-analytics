package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptiontab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscription.UserSubscriptionView;

public class UserSubscription extends Composite implements UserSubscriptionView {
    interface MyUiBinder extends UiBinder<Widget, UserSubscription> {}
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    
    @UiField Button upgradeButton;
    
    private Presenter presenter;

    public UserSubscription(UserSubscriptionView.Presenter presenter) {
        initWidget(uiBinder.createAndBindUi(this));
        
        presenter.setView(this);
        this.presenter = presenter;
    }
    
    @UiHandler("upgradeButton")
    public void handleUpgradeClick(ClickEvent e) {
        upgradeButton.setEnabled(false);
        presenter.openCheckout();
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        SubscriptionProfileDesktopResources.INSTANCE.css().ensureInjected();
    }
}
