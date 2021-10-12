package com.sap.sailing.gwt.home.desktop.partials.subscription;

import org.apache.commons.collections.CollectionUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.shared.places.subscription.SailingSubscriptionStringConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.shared.StringMessagesKey;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class SubscriptionCard extends Composite {

    public static enum Type {
        OWNER, HIGHLIGHT, INDIVIDUAL, DEFAULT
    }

    private static SubscriptionUiBinder uiBinder = GWT.create(SubscriptionUiBinder.class);
    private static final String FEATURE_STYLE = SubscriptionCardResources.INSTANCE.css().feature();
    private static final String HIGHLIGHT_STYLE = SubscriptionCardResources.INSTANCE.css().highlight();
    private static final String INDIVIDUAL_STYLE = SubscriptionCardResources.INSTANCE.css().individual();
    private static final String OWNED_STYLE = SubscriptionCardResources.INSTANCE.css().owned();
    private static final String SUBSCRIPTION_STYLE = SubscriptionCardResources.INSTANCE.css().subscription();

    interface SubscriptionUiBinder extends UiBinder<Widget, SubscriptionCard> {
    }

    @UiField
    Button button;
    @UiField
    HeadingElement title;
    @UiField
    HeadingElement description;
    @UiField
    HeadingElement price;
    @UiField
    FlowPanel features;
    @UiField
    FlowPanel highlightHeader;

    @UiField
    StringMessages i18n;

    private final Runnable subscriptionCallback;

    public SubscriptionCard(SubscriptionPlanDTO subscriptionPlanDTO, Type type, Runnable subscriptionCallback) {
        this.subscriptionCallback = subscriptionCallback;
        SubscriptionCardResources.INSTANCE.css().ensureInjected();
        SharedResources.INSTANCE.mediaCss().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        addStyleName(SUBSCRIPTION_STYLE);
        final String priceText;
        if (CollectionUtils.isNotEmpty(subscriptionPlanDTO.getPrices())) {
            priceText = subscriptionPlanDTO.getPrices().toString();
            //TODO: Implement new Price handling
        } else {
            priceText = i18n.price() + ": " + i18n.individual();
        }
        switch (type) {
        case HIGHLIGHT:
            addStyleName(HIGHLIGHT_STYLE);
            highlightHeader.add(new Label(i18n.subscriptionHighlightText())); // TODO: translate
            button.setText(i18n.subscribe());
            break;
        case DEFAULT:
            button.setText(i18n.subscribe());
            break;
        case OWNER:
            addStyleName(OWNED_STYLE);
            highlightHeader.add(new Label(i18n.subscriptionOwnerHeaderText())); // TODO: translate
            button.setText(i18n.userManagement());
            break;
        case INDIVIDUAL:
            addStyleName(INDIVIDUAL_STYLE);
            button.setText(i18n.send());
            break;
        default:
            break;
        }
        final SailingSubscriptionStringConstants subscriptionStringConstants = SailingSubscriptionStringConstants.INSTANCE;
        title.setInnerText(subscriptionStringConstants.getString(subscriptionPlanDTO.getNameMessageKey()));
        description.setInnerText(subscriptionStringConstants.getString(subscriptionPlanDTO.getDescMessageKey()));
        price.setInnerText(priceText);
        for (StringMessagesKey featureKey : subscriptionPlanDTO.getFeatures()) {
            FlowPanel feature = new FlowPanel();
            feature.addStyleName(FEATURE_STYLE);
            feature.add(new Label(subscriptionStringConstants.getString(featureKey)));
            features.add(feature);
        }
    }

    @UiHandler("button")
    void onClick(ClickEvent e) {
        subscriptionCallback.run();
    }

}
