package com.sap.sailing.gwt.home.desktop.partials.subscription;

import static com.sap.sailing.domain.common.subscription.SailingSubscriptionPlan.DATA_MINING_ALL_YEARLY;
import static com.sap.sailing.domain.common.subscription.SailingSubscriptionPlan.DATA_MINING_ARCHIVE_YEARLY;
import static com.sap.sailing.domain.common.subscription.SailingSubscriptionPlan.PREMIUM_YEARLY;
import static com.sap.sse.security.shared.subscription.SubscriptionPlan.PlanCategory.DATA_MINING_ALL;
import static com.sap.sse.security.shared.subscription.SubscriptionPlan.PlanCategory.DATA_MINING_ARCHIVE;
import static com.sap.sse.security.shared.subscription.SubscriptionPlan.PlanCategory.PREMIUM;

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
import com.sap.sse.security.shared.subscription.SubscriptionPlan;
import com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants;

public class SubscriptionCardContainer extends Composite {

    private static final String SUPPORT_EMAIL = "support@sapsailing.com";
    private static SubscriptionContainerUiBinder uiBinder = GWT.create(SubscriptionContainerUiBinder.class);
    private static final SubscriptionStringConstants i18n = SubscriptionStringConstants.INSTANCE;
    private final static int TITLE_DESCRIPTION_ROW = 0;
    private final static int FREE_ROW = 1;
    private final static int PREMIUM_ROW = 2;
    private final static int DATA_MINING_ARCHIVE_ROW = 3;
    private final static int DATA_MINING_ALL_ROW = 4;

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
        featureGrid = new Grid(2, 5);
        final Label freePlanTitle = new Label(i18n.free_subscription_plan_name());
        freePlanTitle.addStyleName(SubscriptionCardResources.INSTANCE.css().featureHeader());
        featureGrid.setWidget(0, FREE_ROW, freePlanTitle);
        final Label premiumPlanTitle = new Label(i18n.premium_name());
        premiumPlanTitle.addStyleName(SubscriptionCardResources.INSTANCE.css().featureHeader());
        featureGrid.setWidget(0, PREMIUM_ROW, premiumPlanTitle);
        final Label dataMiningArchivePlanTitle = new Label(i18n.data_mining_archive_name());
        dataMiningArchivePlanTitle.addStyleName(SubscriptionCardResources.INSTANCE.css().featureHeader());
        featureGrid.setWidget(0, DATA_MINING_ARCHIVE_ROW, dataMiningArchivePlanTitle);
        final Label dataMiningAllPlanTitle = new Label(i18n.data_mining_all_trial_name());
        dataMiningAllPlanTitle.addStyleName(SubscriptionCardResources.INSTANCE.css().featureHeader());
        featureGrid.setWidget(0, DATA_MINING_ALL_ROW, dataMiningAllPlanTitle);
        addFeatureWithLink(i18n.features_organize_events_title(), i18n.features_organize_events_description(),
                "https://support.sapsailing.com/hc/en-us/articles/360018169799-Create-a-simple-event-on-my-sapsailing-com");
        addFeature(i18n.features_events_with_more_regatta_title(), i18n.features_events_with_more_regatta_description());
        addFeatureWithLink(i18n.features_connect_to_tractrac_title(), i18n.features_connect_to_tractrac_description(),
                "https://tractrac.com/");
        addFeature(i18n.features_imports_title(), i18n.features_imports_description());
        addFeature(i18n.features_media_management_title(), i18n.features_media_management_description());
        addFeature(i18n.features_limited_live_analytics_title(), i18n.features_limited_live_analytics_description());
        addFeature(i18n.features_media_tags_title(), i18n.features_media_tags_description());
        addFeature(i18n.features_scoring_title(), i18n.features_scoring_description());
        addFeature(i18n.features_wind_analytics_title(), i18n.features_wind_analytics_description(), PREMIUM);
        addFeature(i18n.features_maneuver_analytics_title(), i18n.features_maneuver_analytics_description(), PREMIUM);
        addFeature(i18n.features_competitor_analytics_title(), i18n.features_competitor_analytics_description(), PREMIUM);
        addFeature(i18n.features_advanced_leaderboard_info_title(), i18n.features_advanced_leaderboard_info_description(), PREMIUM);
        addFeature(i18n.features_simulator_title(), i18n.features_simulator_description(), PREMIUM);
        addFeature(i18n.features_map_analytics_title(), i18n.features_map_analytics_description(), PREMIUM);
        addFeature(i18n.features_data_mining_title(), i18n.features_data_mining_description(), DATA_MINING_ARCHIVE);
        addFeature(i18n.features_data_mining_all_title(), i18n.features_data_mining_all_description(), DATA_MINING_ALL);

        features.add(featureGrid);
        emailContact.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.Location
                        .assign("mailto:" + SUPPORT_EMAIL + "?subject=" + UriUtils.encode(i18n.support_subject()));
            }
        });
    }

    private void addFeature(final String titleString, final String descriptionString) {
        addFeature(titleString, descriptionString, null);
    }

    private void addFeatureWithLink(final String titleString, final String descriptionString, final String link) {
        addFeatureWithLink(titleString, descriptionString, link, null);
    }

    private void addFeature(final String titleString, final String descriptionString,
            final SubscriptionPlan.PlanCategory planCategory) {
        Label description = new Label(descriptionString);
        description.addStyleName(SubscriptionCardResources.INSTANCE.css().featureDescription());
        addFeatureWithDescription(titleString, description, planCategory);
    }

    private void addFeatureWithDescription(final String titleString, Widget description,
            final SubscriptionPlan.PlanCategory planCategory) {
        int currentRowIndex = featureGrid.getRowCount();
        // get size but index starts with 0 therefore row count is current index + 1
        featureGrid.resizeRows(currentRowIndex + 1);
        VerticalPanel line = new VerticalPanel();
        Label title = new Label(titleString);
        title.addStyleName(SubscriptionCardResources.INSTANCE.css().featureTitle());
        line.add(title);
        line.add(description);
        featureGrid.setWidget(currentRowIndex, TITLE_DESCRIPTION_ROW, line);
        createCheckMark(currentRowIndex, FREE_ROW, planCategory == null);
        createCheckMark(currentRowIndex, PREMIUM_ROW,
                planCategory == null || PREMIUM_YEARLY.getPlanCategories().contains(planCategory));
        createCheckMark(currentRowIndex, DATA_MINING_ARCHIVE_ROW,
                planCategory == null || DATA_MINING_ARCHIVE_YEARLY.getPlanCategories().contains(planCategory));
        createCheckMark(currentRowIndex, DATA_MINING_ALL_ROW,
                planCategory == null || DATA_MINING_ALL_YEARLY.getPlanCategories().contains(planCategory));
    }

    private void addFeatureWithLink(final String titleString, final String descriptionString, final String link,
            final SubscriptionPlan.PlanCategory planCategory) {
        SimplePanel descriptionWithLink = new SimplePanel();
        HTML exampleLink = new HTML(descriptionString + "&nbsp;<a href=\""
                + new SafeHtmlBuilder().appendEscaped(link).toSafeHtml().asString() + "\" title=\""
                + StringMessages.INSTANCE.moreInfo() + "\"" + " class=\""
                + SubscriptionCardResources.INSTANCE.css().featureLink() + "\"" + "target=\"_blank\">"
                + new SafeHtmlBuilder().appendEscaped("ⓘ").toSafeHtml().asString() + "</a>");
        descriptionWithLink.addStyleName(SubscriptionCardResources.INSTANCE.css().featureDescription());
        descriptionWithLink.add(exampleLink);
        addFeatureWithDescription(titleString, descriptionWithLink, planCategory);
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
                SubscriptionCard card = (SubscriptionCard) container.getWidget(i);
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
        for (SubscriptionCard card : subscriptionCardsToRemove) {
            container.remove(card);
        }
    }

    @UiHandler("businessModelInfoButton")
    void onClick(ClickEvent e) {
        VerticalPanel content = new VerticalPanel();
        HTMLPanel title = new HTMLPanel("h1", i18n.businessModelTitle());
        content.add(title);
        Label body = new Label(i18n.businessModelDescription());
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