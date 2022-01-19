package com.sap.sailing.gwt.home.desktop.partials.subscription;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.shared.places.subscription.SailingSubscriptionStringConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.shared.subscription.SubscriptionPrice;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class SubscriptionCard extends Composite {

    public static enum Type {
        FREE, OWNER, HIGHLIGHT, INDIVIDUAL, DEFAULT
    }

    private static SubscriptionUiBinder uiBinder = GWT.create(SubscriptionUiBinder.class);
    private static final String FEATURE_STYLE = SubscriptionCardResources.INSTANCE.css().feature();
    private static final String HIGHLIGHT_STYLE = SubscriptionCardResources.INSTANCE.css().highlight();
    private static final String INDIVIDUAL_STYLE = SubscriptionCardResources.INSTANCE.css().individual();
    private static final String OWNED_STYLE = SubscriptionCardResources.INSTANCE.css().owned();
    private static final String SUBSCRIPTION_STYLE = SubscriptionCardResources.INSTANCE.css().subscription();
    private static final String FREE_STYLE = SubscriptionCardResources.INSTANCE.css().free();
    private static final String PRICE_STYLE = SubscriptionCardResources.INSTANCE.css().price();
    private static final String SELECTED_STYLE = SubscriptionCardResources.INSTANCE.css().selected();

    interface SubscriptionUiBinder extends UiBinder<Widget, SubscriptionCard> {
    }

    @UiField
    Button button;
    @UiField
    HeadingElement title;
    @UiField
    HeadingElement description;
    @UiField
    HeadingElement info;
    @UiField
    FlowPanel prices;
    @UiField
    FlowPanel features;
    @UiField
    FlowPanel highlightHeader;

    @UiField
    StringMessages i18n;

    private final Consumer<SubscriptionPrice> subscriptionCallback;
    private SubscriptionPrice currentPrice;
    private final SubscriptionPlanDTO subscriptionPlanDTO;

    public <T> SubscriptionCard(SubscriptionPlanDTO subscriptionPlanDTO, Type type, Consumer<SubscriptionPrice> subscriptionCallback, EventBus eventBus, boolean loggedIn) {
        this.subscriptionCallback = subscriptionCallback;
        this.subscriptionPlanDTO = subscriptionPlanDTO;
        SubscriptionCardResources.INSTANCE.css().ensureInjected();
        SharedResources.INSTANCE.mediaCss().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        addStyleName(SUBSCRIPTION_STYLE);
        final List<SubscriptionPrice> priceList = new ArrayList<SubscriptionPrice>(subscriptionPlanDTO.getPrices());
        priceList.sort(new Comparator<SubscriptionPrice>() {
            @Override
            public int compare(SubscriptionPrice o1, SubscriptionPrice o2) {
                return o1.getPaymentInterval().compareTo(o2.getPaymentInterval());
            }
        });
        if (priceList.size() > 0) {
            currentPrice = priceList.get(0);
        } else {
            currentPrice = null;
        }
        if (priceList != null && !priceList.isEmpty()) {
            for (SubscriptionPrice subscriptionPrice: priceList) {
                FocusPanel price = new FocusPanel();
                price.addStyleName(PRICE_STYLE);
                String intervalTranslated = SubscriptionStringConstants.INSTANCE.getString("payment_interval_" + subscriptionPrice.getPaymentInterval().name());
                String priceText =  NumberFormat.getCurrencyFormat("USD").format(subscriptionPrice.getPrice())
                        + " /" + intervalTranslated;
                price.add(new Label(priceText));
                if (subscriptionPrice.equals(currentPrice)) {
                    price.addStyleName(SELECTED_STYLE);
                }
                price.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        for (int i=0; i<prices.getWidgetCount(); i++) {
                            prices.getWidget(i).removeStyleName(SELECTED_STYLE);
                        }
                        currentPrice = subscriptionPrice;
                        price.addStyleName(SELECTED_STYLE);
                    }
                });
                prices.add(price);
            }
            // TODO: Implement new Price handling
        } else {
            final String priceText;
            if (type == Type.FREE) {
                priceText = i18n.free();
            } else if (type == Type.INDIVIDUAL) {
                priceText = i18n.individual();
            } else {
                priceText = i18n.price() + ": - ";
            }
            FlowPanel price = new FlowPanel();
            price.addStyleName(PRICE_STYLE);
            price.add(new Label(priceText));
            prices.add(price);
        }
        switch (type) {
        case HIGHLIGHT:
            addStyleName(HIGHLIGHT_STYLE);
            highlightHeader.add(new Label(i18n.subscriptionHighlightText()));
            button.setText(i18n.subscribe());
            break;
        case DEFAULT:
            button.setText(i18n.subscribe());
            break;
        case OWNER:
            addStyleName(OWNED_STYLE);
            highlightHeader.add(new Label(i18n.subscriptionOwnerHeaderText()));
            button.setText(i18n.userManagement());
            break;
        case INDIVIDUAL:
            addStyleName(INDIVIDUAL_STYLE);
            button.setText(i18n.send());
            break;
        case FREE:
            addStyleName(FREE_STYLE);
            button.setText(i18n.signInOrUp());
            if (loggedIn) {
                button.getElement().getStyle().setDisplay(Display.NONE);
            } else {
                button.getElement().getStyle().setDisplay(Display.BLOCK);
            }
            eventBus.addHandler(AuthenticationContextEvent.TYPE, event->{
                AuthenticationContext authContext = event.getCtx();
                // make it point to the current server if the user has CREATE_OBJECT permission there
                if (authContext.isLoggedIn()) {
                    button.getElement().getStyle().setDisplay(Display.NONE);
                } else {
                    button.getElement().getStyle().setDisplay(Display.BLOCK);
                }
            });
        default:
            break;
        }       
        final SailingSubscriptionStringConstants subscriptionStringConstants = SailingSubscriptionStringConstants.INSTANCE;
        title.setInnerText(subscriptionStringConstants.getString(subscriptionPlanDTO.getSubscriptionPlanNameMessageKey()));
        description.setInnerText(subscriptionStringConstants.getString(subscriptionPlanDTO.getSubscriptionPlanDescMessageKey()));
        info.setInnerText(subscriptionStringConstants.getString(subscriptionPlanDTO.getSubscriptionPlanInfoMessageKey()));
        final String[] featureStrings = subscriptionStringConstants.getStringArray(subscriptionPlanDTO.getSubscriptionPlanFeatureMessageKey());
        for (String featureString : featureStrings) {
            if(featureString != "") {
                FlowPanel feature = new FlowPanel();
                feature.addStyleName(FEATURE_STYLE);
                feature.add(new Label(featureString));
                features.add(feature);
            }
        }
    }
    
    public SubscriptionPlanDTO getSubscriptionPlanDTO() {
        return subscriptionPlanDTO;
    }

    @UiHandler("button")
    void onClick(ClickEvent e) {
        subscriptionCallback.accept(currentPrice);
    }

}
