package com.sap.sailing.gwt.home.desktop.partials.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.shared.StringMessagesKey;
import com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class Subscription extends Composite {

    private static SubscriptionUiBinder uiBinder = GWT.create(SubscriptionUiBinder.class);
    private static final String FEATURE_STYLE = SubscriptionResources.INSTANCE.css().feature();
    private static final String HIGHLIGHT_STYLE = SubscriptionResources.INSTANCE.css().highlight();

    interface SubscriptionUiBinder extends UiBinder<Widget, Subscription> {
    }

    public Subscription() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiField
    Button subscribeButton;
    @UiField
    HeadingElement title;
    @UiField
    HeadingElement description;
    @UiField
    HeadingElement price;
    @UiField
    FlowPanel features;
    
    @UiField
    StringMessages i18n;

    public Subscription(SubscriptionPlanDTO subscriptionPlanDTO, boolean highlight) {
        SubscriptionResources.INSTANCE.css().ensureInjected();
        SharedResources.INSTANCE.mediaCss().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        if (highlight) {
            addStyleName(HIGHLIGHT_STYLE);
        }
        title.setInnerText(SubscriptionStringConstants.INSTANCE.getString(subscriptionPlanDTO.getNameMessageKey()));
        description.setInnerText(SubscriptionStringConstants.INSTANCE.getString(subscriptionPlanDTO.getDescMessageKey()));
        NumberFormat nf = NumberFormat.getCurrencyFormat("USD"); 
        String priceText = i18n.price() + ": " + nf.format(subscriptionPlanDTO.getPrice());
        price.setInnerText(priceText);
        for (StringMessagesKey featureKey: subscriptionPlanDTO.getFeatures()) {
            FlowPanel feature = new FlowPanel();
            feature.addStyleName(FEATURE_STYLE);
            feature.add(new Label(SubscriptionStringConstants.INSTANCE.getString(featureKey)));
            features.add(feature);
        }
    }

    @UiHandler("subscribeButton")
    void onClick(ClickEvent e) {
        Window.alert("Selected!");
    }

    public void setText(String text) {
        subscribeButton.setText(text);
    }

    public String getText() {
        return subscribeButton.getText();
    }

}
