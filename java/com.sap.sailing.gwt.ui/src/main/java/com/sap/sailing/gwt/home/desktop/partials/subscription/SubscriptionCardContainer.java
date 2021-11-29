package com.sap.sailing.gwt.home.desktop.partials.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DListElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.AnimationType;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants;

public class SubscriptionCardContainer extends Composite {

    private static SubscriptionContainerUiBinder uiBinder = GWT.create(SubscriptionContainerUiBinder.class);

    @UiField
    Button businessModelInfoButton;

    interface SubscriptionContainerUiBinder extends UiBinder<Widget, SubscriptionCardContainer> {
    }

    public SubscriptionCardContainer() {
        initWidget(uiBinder.createAndBindUi(this));

        addFeature("features_live_analytics");
        addFeatureWithLink("features_organize_events");
        addFeature("features_events_with_more_regatta");
        addFeatureWithLink("features_connect_to_tractrac");
        addFeature("features_imports");
        addFeature("features_media_management");
        addFeature("features_analytic_charts");
        addFeature("features_map_analytics");
        addFeature("features_maneuver_analytics");
        addFeature("features_media_tags");
        addFeature("features_scoring");
    }
    
    private void addFeature(String featureKey) {
        final Element dt = DOM.createElement("dt");
        dt.setInnerHTML(SubscriptionStringConstants.INSTANCE.getString(featureKey + "_title"));
        final Element dd = DOM.createElement("dd");
        dd.setInnerHTML(SubscriptionStringConstants.INSTANCE.getString(featureKey + "_description"));
        features.appendChild(dt);
        features.appendChild(dd);
    }
    
    private void addFeatureWithLink(String featureKey) {
        final Element dt = DOM.createElement("dt");
        dt.setInnerHTML(SubscriptionStringConstants.INSTANCE.getString(featureKey + "_title"));
        final Element dd = DOM.createElement("dd");
        HorizontalPanel description = new HorizontalPanel();
        description.add(new Label(SubscriptionStringConstants.INSTANCE.getString(featureKey + "_description")));
        String link = SubscriptionStringConstants.INSTANCE.getString(featureKey + "_link");
        HTML exampleLink = new HTML("<a href=\"" 
                + new SafeHtmlBuilder().appendEscaped(link).toSafeHtml().asString()
                + "\" target=\"_blank\">&nbsp;"
                + new SafeHtmlBuilder().appendEscaped("[...]").toSafeHtml().asString() 
                + "</a>");
        description.add(exampleLink);
        dd.appendChild(description.getElement());
        features.appendChild(dt);
        features.appendChild(dd);
    }

    @UiField
    FlowPanel container;

    @UiField
    DListElement features;

    public void addSubscription(SubscriptionCard subscription) {
        container.add(subscription);
    }

    @UiHandler("businessModelInfoButton")
    void onClick(ClickEvent e) {
        VerticalPanel content = new VerticalPanel();
        HTMLPanel title = new HTMLPanel("h1", SubscriptionStringConstants.INSTANCE.businessModelTitle());
        content.add(title);
        Label body = new Label(SubscriptionStringConstants.INSTANCE.businessModelDescription());
        content.add(body);
        PopupPanel popup = new PopupPanel();
        popup.setWidget(content);
        popup.addStyleName(SubscriptionCardResources.INSTANCE.css().popupContent());
        popup.setGlassStyleName(SubscriptionCardResources.INSTANCE.css().popupGlass());
        popup.setAnimationEnabled(true);
        popup.setAnimationType(AnimationType.CENTER);
        popup.setGlassEnabled(true);
        popup.setModal(true);
        popup.setAutoHideEnabled(true);
        popup.addAutoHidePartner(businessModelInfoButton.getElement());
        popup.center();
    }

}
