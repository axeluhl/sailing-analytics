package com.sap.sailing.gwt.home.shared.places.user.subscriptions;

import static com.sap.sailing.gwt.home.shared.utils.LabelTypeUtil.renderLabelType;
import static com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants.INSTANCE;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.event.LabelType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;

/**
 * Utility class providing convenient values to be used by UI components showing information about user
 * {@link SubscriptionDTO subscriptions}.
 */
public class SubscriptionsValueProvider {

    private final StringMessages i18n;

    public SubscriptionsValueProvider(final StringMessages i18n) {
        this.i18n = i18n;
    }

    public String getSubscriptionName(final SubscriptionDTO subscription) {
        return subscription == null ? "---" : INSTANCE.getString(subscription.getSubscriptionPlanNameMessageKey());
    }

    public LabelType getSubscriptionStatusLabelType(final SubscriptionDTO subscription) {
        if (subscription.isCancelled()) return LabelType.CANCELLED;
        if (subscription.isInTrial()) return LabelType.IN_TRIAL;
        if (subscription.isActive()) return LabelType.ACTIVE;
        return LabelType.UNKNOWN;
    }

    public SafeHtml getSubscriptionStatusLabel(final SubscriptionDTO subscription) {
        final Element labelElement = Document.get().createDivElement();
        labelElement.addClassName(SharedResources.INSTANCE.mainCss().label());
        renderLabelType(labelElement, getSubscriptionStatusLabelType(subscription));
        return () -> labelElement.getString();
    }

    public String getPaymentStatus(final SubscriptionDTO subscription) {
        if (subscription.isActive()) {
            if (subscription.isPaymentSuccess()) {
                if (subscription.isRefunded()) {
                    return i18n.refunded();
                } else {
                    return i18n.paymentStatusSuccess();
                }
            } else if (subscription.isPaymentNoSuccess()) {
                return i18n.paymentStatusNoSuccess();
            }
        }
        return "---";
    }

    // FIXME: Refactor and move to date/time util class or use existing formatter method instead
    public String getTrialRemainingText(final SubscriptionDTO subscription) {
        long remainingSecs = Math.round(TimePoint.now().until(subscription.getTrialEnd()).asSeconds());
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
