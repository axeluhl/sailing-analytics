package com.sap.sailing.gwt.home.desktop.partials.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.AnimationType;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.resources.SharedDesktopResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants;

public class SubscriptionCardContainer extends Composite {

    private static SubscriptionContainerUiBinder uiBinder = GWT.create(SubscriptionContainerUiBinder.class);

    @UiField
    Button businessModelInfoButton;
    @UiField
    Button emailContact;
    @UiField
    FlowPanel container;
    @UiField
    FlowPanel features;
    
    final Grid featureGrid;

    interface SubscriptionContainerUiBinder extends UiBinder<Widget, SubscriptionCardContainer> {
    }

    public SubscriptionCardContainer() {
        initWidget(uiBinder.createAndBindUi(this));
        
        featureGrid = new Grid(2, 3);
        
        Label freePlanTitle = new Label(SubscriptionStringConstants.INSTANCE.free_subscription_plan_shortname());
        freePlanTitle.addStyleName(SubscriptionCardResources.INSTANCE.css().featureHeader());
        featureGrid.setWidget(0, 1, freePlanTitle);
        Label premiumPlanTitle = new Label(SubscriptionStringConstants.INSTANCE.premium_subscription_plan_shortname());
        premiumPlanTitle.addStyleName(SubscriptionCardResources.INSTANCE.css().featureHeader());
        featureGrid.setWidget(0, 2, premiumPlanTitle);

        addFeatureWithLink("features_organize_events", true, true);
        addFeature("features_events_with_more_regatta", true, true);
        addFeatureWithLink("features_connect_to_tractrac", true, true);
        addFeature("features_imports", true, true);
        addFeature("features_media_management", true, true);
        addFeature("features_limited_live_analytics", true, true);
        addFeature("features_full_live_analytics", false, true);
        addFeature("features_analytic_charts", false, true);
        addFeature("features_map_analytics", false, true);
        addFeature("features_maneuver_analytics", false, true);
        addFeature("features_media_tags", false, true);
        addFeature("features_scoring", false, true);
        
        features.add(featureGrid);
        
        emailContact.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.Location.assign("mailto:john.doe@mail.com");
            }
        });
    }
    
    private void addFeature(String featureKey, boolean free, boolean premium) {
        // get size but index starts with 0 therefore row count is current index + 1
        int currentRowIndex = featureGrid.getRowCount();
        featureGrid.resizeRows(currentRowIndex + 1);
        
        VerticalPanel line = new VerticalPanel();
        Label title = new Label(SubscriptionStringConstants.INSTANCE.getString(featureKey + "_title"));
        title.addStyleName(SubscriptionCardResources.INSTANCE.css().featureTitle());
        line.add(title);
        Label description = new Label(SubscriptionStringConstants.INSTANCE.getString(featureKey + "_description"));
        description.addStyleName(SubscriptionCardResources.INSTANCE.css().featureDescription());
        line.add(description);
        featureGrid.setWidget(currentRowIndex, 0, line);
        if (free) {
            FlowPanel check = new FlowPanel();
            check.add(new Image(SharedDesktopResources.INSTANCE.dropdownCheck().getSafeUri()));
            check.addStyleName(SubscriptionCardResources.INSTANCE.css().featureCheck());
            featureGrid.setWidget(currentRowIndex, 1, check);
        }
        if (premium) {
            FlowPanel check = new FlowPanel();
            check.add(new Image(SharedDesktopResources.INSTANCE.dropdownCheck().getSafeUri()));
            check.addStyleName(SubscriptionCardResources.INSTANCE.css().featureCheck());
            featureGrid.setWidget(currentRowIndex, 2, check);
        }
    }
    
    private void addFeatureWithLink(String featureKey, boolean free, boolean premium) {
        // get size but index starts with 0 therefore row count is current index + 1
        int currentRowIndex = featureGrid.getRowCount();
        featureGrid.resizeRows(currentRowIndex + 1);
        VerticalPanel line = new VerticalPanel();
        Label title = new Label(SubscriptionStringConstants.INSTANCE.getString(featureKey + "_title"));
        title.addStyleName(SubscriptionCardResources.INSTANCE.css().featureTitle());
        line.add(title);
        SimplePanel descriptionWithLink = new SimplePanel();
        String link = SubscriptionStringConstants.INSTANCE.getString(featureKey + "_link");
        HTML exampleLink = new HTML(SubscriptionStringConstants.INSTANCE.getString(featureKey + "_description")+ "&nbsp;<a href=\"" 
                + new SafeHtmlBuilder().appendEscaped(link).toSafeHtml().asString()
                + "\" title=\"" + StringMessages.INSTANCE.moreInfo() + "\""
                + " class=\"" + SubscriptionCardResources.INSTANCE.css().featureLink() + "\""
                + "target=\"_blank\">"
                + new SafeHtmlBuilder().appendEscaped("ⓘ").toSafeHtml().asString() 
                + "</a>");
        descriptionWithLink.addStyleName(SubscriptionCardResources.INSTANCE.css().featureDescription());
        descriptionWithLink.add(exampleLink);
        line.add(descriptionWithLink);
        featureGrid.setWidget(currentRowIndex, 0, line);
        if (free) {
            FlowPanel check = new FlowPanel();
            check.add(new Image(SharedDesktopResources.INSTANCE.dropdownCheck().getSafeUri()));
            check.addStyleName(SubscriptionCardResources.INSTANCE.css().featureCheck());
            featureGrid.setWidget(currentRowIndex, 1, check);
        }
        if (premium) {
            FlowPanel check = new FlowPanel();
            check.add(new Image(SharedDesktopResources.INSTANCE.dropdownCheck().getSafeUri()));
            check.addStyleName(SubscriptionCardResources.INSTANCE.css().featureCheck());
            featureGrid.setWidget(currentRowIndex, 2, check);
        }
    }

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
