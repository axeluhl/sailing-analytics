package com.sap.sailing.gwt.home.desktop.partials.subscription;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.UriUtils;
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

    private static final String SUPPORT_EMAIL = "support@sapsailing.com";
    private static SubscriptionContainerUiBinder uiBinder = GWT.create(SubscriptionContainerUiBinder.class);
    private static final SubscriptionStringConstants subscriptionStringMessages = SubscriptionStringConstants.INSTANCE;

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
        featureGrid = new Grid(2, 4);
        final Label freePlanTitle = new Label(subscriptionStringMessages.free_subscription_plan_shortname());
        freePlanTitle.addStyleName(SubscriptionCardResources.INSTANCE.css().featureHeader());
        featureGrid.setWidget(0, 1, freePlanTitle);
        final Label premiumPlanTitle = new Label(subscriptionStringMessages.premium_subscription_plan_shortname());
        premiumPlanTitle.addStyleName(SubscriptionCardResources.INSTANCE.css().featureHeader());
        featureGrid.setWidget(0, 2, premiumPlanTitle);
        final Label dataMiningArchivePlanTitle = new Label(subscriptionStringMessages.datamining_subscription_plan_shortname());
        dataMiningArchivePlanTitle.addStyleName(SubscriptionCardResources.INSTANCE.css().featureHeader());
        featureGrid.setWidget(0, 3, dataMiningArchivePlanTitle);
        addFeatureWithLink(subscriptionStringMessages.features_organize_events_title(),
                           subscriptionStringMessages.features_organize_events_description(),
                           "https://support.sapsailing.com/hc/en-us/articles/360018169799-Create-a-simple-event-on-my-sapsailing-com",
                           true, true, true);
        addFeature(subscriptionStringMessages.features_events_with_more_regatta_title(), subscriptionStringMessages.features_events_with_more_regatta_description(), true, true, true);
        addFeatureWithLink(subscriptionStringMessages.features_connect_to_tractrac_title(),
                           subscriptionStringMessages.features_connect_to_tractrac_description(),
                           "https://tractrac.com/", true, true, true);
        addFeature(subscriptionStringMessages.features_imports_title(), subscriptionStringMessages.features_imports_description(), true, true, true);
        addFeature(subscriptionStringMessages.features_media_management_title(), subscriptionStringMessages.features_media_management_description(), true, true, true);
        addFeature(subscriptionStringMessages.features_limited_live_analytics_title(), subscriptionStringMessages.features_limited_live_analytics_description(), true, true, true);
        addFeature(subscriptionStringMessages.features_media_tags_title(), subscriptionStringMessages.features_media_tags_description(), true, true, true);
        addFeature(subscriptionStringMessages.features_scoring_title(), subscriptionStringMessages.features_scoring_description(), true, true, true);
        addFeature(subscriptionStringMessages.features_wind_analytics_title(), subscriptionStringMessages.features_wind_analytics_description(), false, true, true);
        addFeature(subscriptionStringMessages.features_maneuver_analytics_title(), subscriptionStringMessages.features_maneuver_analytics_description(), false, true, true);
        addFeature(subscriptionStringMessages.features_competitor_analytics_title(), subscriptionStringMessages.features_competitor_analytics_description(), false, true, true);
        addFeature(subscriptionStringMessages.features_advanced_leaderboard_info_title(), subscriptionStringMessages.features_advanced_leaderboard_info_description(), false, true, true);
        addFeature(subscriptionStringMessages.features_simulator_title(), subscriptionStringMessages.features_simulator_description(), false, true, true);
        addFeature(subscriptionStringMessages.features_map_analytics_title(), subscriptionStringMessages.features_map_analytics_description(), false, true, true);
        addFeature(subscriptionStringMessages.features_data_mining_title(), subscriptionStringMessages.features_data_mining_description(), false, false, true);
        features.add(featureGrid);
        emailContact.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.Location.assign("mailto:" + SUPPORT_EMAIL
                        + "?subject=" + UriUtils.encode(subscriptionStringMessages.support_subject()));
            }
        });
    }
    
    private void addFeature(final String titleString, final String descriptionString, boolean free, boolean premium, boolean dataMining) {
        Label description = new Label(descriptionString);
        description.addStyleName(SubscriptionCardResources.INSTANCE.css().featureDescription());
        addFeatureWithDescription(titleString, free, premium, dataMining, description);
    }

    private void addFeatureWithLink(final String titleString, final String descriptionString, final String link, boolean free, boolean premium, boolean dataMining) {
        SimplePanel descriptionWithLink = new SimplePanel();
        HTML exampleLink = new HTML(descriptionString+ "&nbsp;<a href=\"" 
                + new SafeHtmlBuilder().appendEscaped(link).toSafeHtml().asString()
                + "\" title=\"" + StringMessages.INSTANCE.moreInfo() + "\""
                + " class=\"" + SubscriptionCardResources.INSTANCE.css().featureLink() + "\""
                + "target=\"_blank\">"
                + new SafeHtmlBuilder().appendEscaped("ⓘ").toSafeHtml().asString() 
                + "</a>");
        descriptionWithLink.addStyleName(SubscriptionCardResources.INSTANCE.css().featureDescription());
        descriptionWithLink.add(exampleLink);
        addFeatureWithDescription(titleString, free, premium, dataMining, descriptionWithLink);
    }

    private void addFeatureWithDescription(final String titleString, boolean free, boolean premium, boolean dataMining, Widget description) {
        // get size but index starts with 0 therefore row count is current index + 1
        int currentRowIndex = featureGrid.getRowCount();
        featureGrid.resizeRows(currentRowIndex + 1);
        VerticalPanel line = new VerticalPanel();
        Label title = new Label(titleString);
        title.addStyleName(SubscriptionCardResources.INSTANCE.css().featureTitle());
        line.add(title);
        line.add(description);
        featureGrid.setWidget(currentRowIndex, 0, line);
        createCheckMark(currentRowIndex, 1, free);
        createCheckMark(currentRowIndex, 2, premium);
        createCheckMark(currentRowIndex, 3, dataMining);
    }

    /**
     * Paint a check icon or if state is false a X icon.
     * 
     * @param currentRowIndex
     *            the current row
     * @param column
     *            the column
     * @param checkState
     *            the state (true: check, false: X)
     */
    private void createCheckMark(int currentRowIndex, final int column, boolean checkState) {
        FlowPanel check = new FlowPanel();
        if (checkState) {
            check.add(new Image(SharedDesktopResources.INSTANCE.dropdownCheck().getSafeUri()));
            check.addStyleName(SubscriptionCardResources.INSTANCE.css().featureCheck());
        } else {
            check.add(new Label("-"));
            check.addStyleName(SubscriptionCardResources.INSTANCE.css().featureNone());
        }
        featureGrid.setWidget(currentRowIndex, column, check);
    }
    
    public void addSubscription(SubscriptionCard subscription) {
        if (!isSubscriptionPlanExisting(subscription.getSubscriptionGroupDTO().getSubscriptionGroupId())) {
            container.add(subscription);
        }
    }
    
    private boolean isSubscriptionPlanExisting(String planId) {
        boolean isExisting = false;
        for (int i = 0; i < container.getWidgetCount(); i++) {
            if (container.getWidget(i) instanceof SubscriptionCard) {
                SubscriptionCard card = (SubscriptionCard)container.getWidget(i);
                if (card.getSubscriptionGroupDTO().getSubscriptionGroupId().equals(planId)) {
                    isExisting = true;
                    break;
                }
            }
        }
        return isExisting;
    }
    
    public void resetSubscriptions() {
        List<SubscriptionCard> subscriptionCardsToRemove = new ArrayList<>();
        for (int i = 0; i < container.getWidgetCount(); i++) {
            if (container.getWidget(i) instanceof SubscriptionCard) {
                subscriptionCardsToRemove.add((SubscriptionCard) container.getWidget(i));
            }
        }
        for (SubscriptionCard card: subscriptionCardsToRemove) {
            container.remove(card);
        }
    }

    @UiHandler("businessModelInfoButton")
    void onClick(ClickEvent e) {
        VerticalPanel content = new VerticalPanel();
        HTMLPanel title = new HTMLPanel("h1", subscriptionStringMessages.businessModelTitle());
        content.add(title);
        Label body = new Label(subscriptionStringMessages.businessModelDescription());
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
