package com.sap.sailing.gwt.home.mobile.places.user.profile.subscriptions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsView;
import com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class UserSubscriptionItem extends Composite {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<MobileSection, UserSubscriptionItem> {
    }

    @UiField SectionHeaderContent sectionHeaderUi;
    @UiField HTMLPanel contentContainerUi;
    @UiField DivElement settingsDataUi;

    private final SubscriptionStringConstants stringConstants = SubscriptionStringConstants.INSTANCE;;
    private final Runnable cancelCallback;

    public UserSubscriptionItem(final SubscriptionDTO subscription, final UserSubscriptionsView.Presenter presenter) {
        this.cancelCallback = () -> presenter.cancelSubscription(subscription.getPlanId(), subscription.getProvider());
        final MobileSection mobileSection = uiBinder.createAndBindUi(this);
        mobileSection.setEdgeToEdgeContent(true);
        initWidget(mobileSection);

        sectionHeaderUi.setSectionTitle(getPlanName(subscription, presenter));
        // sectionHeaderUi.setSubtitle(userSettingsEntry.getDocumentSettingsId());
        // final String userProfileSettings = userSettingsEntry.getProfileData();
        // final boolean hasUserData = (userProfileSettings != null && !userProfileSettings.isEmpty());
        // final String localSettings = userSettingsEntry.getLocalData();
        // settingsDataUi.setInnerText(hasUserData ? userProfileSettings : localSettings);

        sectionHeaderUi.initCollapsibility(contentContainerUi.getElement(), false);
    }

    @UiHandler("cancelControlUi")
    void onCancelControlClicked(final ClickEvent event) {
        this.cancelCallback.run();
    }

    private String getPlanName(final SubscriptionDTO subscription, final UserSubscriptionsView.Presenter presenter) {
        final SubscriptionPlanDTO plan = subscription != null ? presenter.getPlanById(subscription.getPlanId()) : null;
        return plan == null ? "-" : stringConstants.getString(plan.getNameMessageKey());
    }

}
