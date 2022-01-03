package com.sap.sailing.gwt.home.mobile.places.user.profile.subscriptions;

import static com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil.formatDateAndTime;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsView;
import com.sap.sailing.gwt.home.shared.places.user.subscriptions.SubscriptionsValueProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;

class UserSubscriptionItem extends Composite {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<MobileSection, UserSubscriptionItem> {
    }

    @UiField SharedResources res;
    @UiField StringMessages i18n;

    @UiField SectionHeaderContent sectionHeaderUi;
    @UiField HTMLPanel contentContainerUi;
    @UiField Element subscriptionInfoUi;

    private final SubscriptionsValueProvider valueProvider;
    private final Runnable cancelCallback;

    UserSubscriptionItem(final SubscriptionDTO subscription, final UserSubscriptionsView.Presenter presenter) {
        final MobileSection mobileSection = uiBinder.createAndBindUi(this);
        mobileSection.setEdgeToEdgeContent(true);
        initWidget(mobileSection);

        this.cancelCallback = () -> presenter.cancelSubscription(subscription.getSubscriptionPlanId(),
                subscription.getProvider());
        this.valueProvider = new SubscriptionsValueProvider(i18n);

        sectionHeaderUi.setSectionTitle(valueProvider.getSubscriptionName(subscription));
        sectionHeaderUi.setLabelType(valueProvider.getSubscriptionStatusLabelType(subscription));
        addInfo(i18n.paymentStatus(), valueProvider.getPaymentStatus(subscription))
                .highlightValue(subscription.isPaymentSuccess() && !subscription.isRefunded());

        if (subscription.isInTrial()) {
            addInfo("", i18n.trialText(valueProvider.getTrialRemainingText(subscription),
                    formatDateAndTime(subscription.getTrialEnd().asDate()))).highlightValue(false);
        }

        sectionHeaderUi.initCollapsibility(contentContainerUi.getElement(), false);
    }

    @UiHandler("cancelControlUi")
    void onCancelControlClicked(final ClickEvent event) {
        this.cancelCallback.run();
    }

    private UserSubscriptionItemInfo addInfo(final String label, final String value) {
        final UserSubscriptionItemInfo info = new UserSubscriptionItemInfo(label, value);
        subscriptionInfoUi.appendChild(info.getElement());
        return info;
    }

}
