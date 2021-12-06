package com.sap.sailing.gwt.home.mobile.places.user.profile.subscriptions;

import static com.sap.sailing.gwt.home.mobile.places.user.profile.subscriptions.UserSubscriptionItemInfo.create;
import static com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil.formatDateAndTime;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants;
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

    private final SubscriptionStringConstants stringConstants = SubscriptionStringConstants.INSTANCE;;
    private final Runnable cancelCallback;

    UserSubscriptionItem(final SubscriptionDTO subscription, final UserSubscriptionsView.Presenter presenter) {
        this.cancelCallback = () -> presenter.cancelSubscription(subscription.getPlanId(), subscription.getProvider());
        final MobileSection mobileSection = uiBinder.createAndBindUi(this);
        mobileSection.setEdgeToEdgeContent(true);
        initWidget(mobileSection);

        sectionHeaderUi.setSectionTitle(getPlanName(subscription, presenter));
        sectionHeaderUi.setSubtitle(getStatus(subscription));
        addInfo(create(i18n.paymentStatus(), getPaymentStatus(subscription))
                .highlightValue(subscription.isPaymentSuccess() && !subscription.isRefunded()));

        if (subscription.isInTrial()) {
            addInfo(create("", i18n.trialText(getTrialRemainingText(subscription),
                    formatDateAndTime(subscription.getCurrentEnd().asDate()))).highlightValue(false));
        }

        sectionHeaderUi.initCollapsibility(contentContainerUi.getElement(), false);
    }

    @UiHandler("cancelControlUi")
    void onCancelControlClicked(final ClickEvent event) {
        this.cancelCallback.run();
    }

    private void addInfo(final UIObject child) {
        subscriptionInfoUi.appendChild(child.getElement());
    }

    private String getPlanName(final SubscriptionDTO subscription, final UserSubscriptionsView.Presenter presenter) {
        return subscription != null ? "-" : "[TODO]";
        // FIXME return subscription != null ? "-" : stringConstants.getString(object.getSubscriptionPlanNameMessageKey());
    }

    private String getStatus(final SubscriptionDTO object) {
        if (object.isInTrial()) {
            return i18n.inTrial();
        } else if (object.isActive()) {
            return i18n.active();
        } else if (object.isPaused()) {
            return i18n.paused();
        }
        return "-";
    }

    private String getPaymentStatus(final SubscriptionDTO object) {
        if (object.isActive() && object.getPaymentStatus() != null) {
            if (object.isPaymentSuccess()) {
                if (object.isRefunded()) {
                    return i18n.refunded();
                } else {
                    return i18n.paymentStatusSuccess();
                }
            } else {
                return i18n.paymentStatusNoSuccess();
            }
        }
        return "-";
    }

    private String getTrialRemainingText(final SubscriptionDTO subscription) {
        long remainingSecs = Math.round(TimePoint.now().until(subscription.getCurrentEnd()).asSeconds());
        final StringBuilder remainText = new StringBuilder();
        if (remainingSecs <= 0) {
            remainText.append(i18n.numHours(0));
        } else {
            final int days = (int) (remainingSecs / 86400);
            if (days > 0) {
                remainText.append(i18n.numDays(days));
            }
            remainingSecs = remainingSecs % 86400;
            final int hours = (int) (remainingSecs / 3600);
            if (hours > 0) {
                if (remainText.length() > 0) {
                    remainText.append(" ");
                }
                remainText.append(i18n.numHours(hours));
            }
            if (days == 0) {
                remainingSecs = remainingSecs % 3600;
                final int mins = (int) (remainingSecs / 60);
                if (mins > 0) {
                    if (remainText.length() > 0) {
                        remainText.append(" ");
                    }
                    remainText.append(i18n.numMinutes(mins));
                }
            }
        }

        if (remainText.length() == 0) {
            remainText.append(i18n.numHours(0));
        }

        return remainText.toString();
    }

}
