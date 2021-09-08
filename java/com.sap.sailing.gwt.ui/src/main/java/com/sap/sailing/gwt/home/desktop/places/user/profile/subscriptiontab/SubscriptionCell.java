package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptiontab;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiRenderer;
import com.sap.sailing.gwt.home.shared.places.subscription.SailingSubscriptionStringConstants;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscription.UserSubscriptionView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.ui.shared.subscription.SubscriptionItem;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class SubscriptionCell extends AbstractCell<SubscriptionItem> {
    interface Style extends CssResource {
        String subscriptionContainer();

        String textRow();

        String trialText();

        String errorText();

        String blueText();

        String hide();

        String cancelButton();
    }

    interface MyUiRenderer extends UiRenderer {
        void render(SafeHtmlBuilder sb, String planName, String subscriptionStatus, String trial, String paymentStatus,
                String subscriptionStatusCssClass, String paymentStatusCssClass, String trialCssClass);

        void onBrowserEvent(SubscriptionCell cell, NativeEvent event, Element parent, SubscriptionItem value);

        Style getStyle();

        ButtonElement getCancelButton(Element parent);
    }

    private static MyUiRenderer renderer = GWT.create(MyUiRenderer.class);

    private UserSubscriptionView.Presenter presenter;

    private final SailingSubscriptionStringConstants stringConstants;

    public SubscriptionCell(UserSubscriptionView.Presenter presenter) {
        super("click");
        stringConstants = SailingSubscriptionStringConstants.INSTANCE;
        this.presenter = presenter;
    }

    @Override
    public void render(Context context, SubscriptionItem subscription, SafeHtmlBuilder sb) {
        SubscriptionPlanDTO plan = getSubscriptionPlan(subscription);
        if (plan != null) {
            String planName = stringConstants.getString(plan.getNameMessageKey().getKey());
            String subscriptionStatus = getSubscriptionStatusLabel(subscription);
            String subscriptionStatusCssClass = "";
            String paymentStatus = "";
            String paymentStatusCssClass = "";
            String trial = "";
            String trialCssClass = "";
            if (subscription.isActive()) {
                subscriptionStatusCssClass = renderer.getStyle().blueText();
                paymentStatus = getPaymentStatusLabel(subscription);
                paymentStatusCssClass = subscription.isPaymentSuccess() && !subscription.isRefunded()
                        ? renderer.getStyle().blueText()
                        : renderer.getStyle().errorText();
            } else {
                paymentStatusCssClass = renderer.getStyle().hide();
                if (subscription.isInTrial()) {
                    trial = buildTrialText(subscription);
                } else {
                    trialCssClass = renderer.getStyle().hide();
                }
            }
            renderer.render(sb, planName, subscriptionStatus, trial, paymentStatus, subscriptionStatusCssClass,
                    paymentStatusCssClass, trialCssClass);
        }
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, SubscriptionItem value, NativeEvent event,
            ValueUpdater<SubscriptionItem> valueUpdater) {
        renderer.onBrowserEvent(this, event, parent, value);
    }

    @UiHandler({ "cancelButton" })
    void onCancelButtonPressed(ClickEvent event, Element parent, SubscriptionItem subscription) {
        renderer.getCancelButton(parent).setDisabled(true);
        presenter.cancelSubscription(subscription.getPlanId(), subscription.getProvider());
    }

    private SubscriptionPlanDTO getSubscriptionPlan(SubscriptionItem subscription) {
        return subscription != null ? presenter.getPlanById(subscription.getPlanId()) : null;
    }

    private String buildTrialText(SubscriptionItem subscription) {
        return StringMessages.INSTANCE.trialText(getTrialRemainingText(subscription),
                DateTimeFormat.getFormat("yyyy-MM-dd HH:mm").format(subscription.getTrialEnd().asDate()));
    }

    private String getTrialRemainingText(SubscriptionItem subscription) {
        long remainingSecs = Math.round(TimePoint.now().until(subscription.getTrialEnd()).asSeconds());
        StringBuilder remainText = new StringBuilder();
        if (remainingSecs <= 0) {
            remainText.append(StringMessages.INSTANCE.numHours(0));
        } else {
            int days = (int) (remainingSecs / 86400);
            if (days > 0) {
                remainText.append(StringMessages.INSTANCE.numDays(days));
            }
            remainingSecs = remainingSecs % 86400;
            int hours = (int) (remainingSecs / 3600);
            if (hours > 0) {
                if (remainText.length() > 0) {
                    remainText.append(" ");
                }
                remainText.append(StringMessages.INSTANCE.numHours(hours));
            }
            if (days == 0) {
                remainingSecs = remainingSecs % 3600;
                int mins = (int) (remainingSecs / 60);
                if (mins > 0) {
                    if (remainText.length() > 0) {
                        remainText.append(" ");
                    }
                    remainText.append(StringMessages.INSTANCE.numMinutes(mins));
                }
            }
        }

        if (remainText.length() == 0) {
            remainText.append(StringMessages.INSTANCE.numHours(0));
        }

        return remainText.toString();
    }

    public String getSubscriptionStatusLabel(SubscriptionItem subscription) {
        final String label;
        if (subscription.isInTrial()) {
            label = StringMessages.INSTANCE.inTrial();
        } else if (subscription.isActive()) {
            label = StringMessages.INSTANCE.active();
        } else if (subscription.isPaused()) {
            label = StringMessages.INSTANCE.paused();
        } else {
            label = "";
        }
        return label;
    }

    public String getPaymentStatusLabel(SubscriptionItem subscription) {
        final String label;
        final String paymentStatus = subscription.getPaymentStatus();
        if (paymentStatus != null) {
            if (subscription.isPaymentSuccess()) {
                if (subscription.isRefunded()) {
                    label = StringMessages.INSTANCE.refunded();
                } else {
                    label = StringMessages.INSTANCE.paymentStatusSuccess();
                }
            } else {
                label = StringMessages.INSTANCE.paymentStatusNoSuccess();
            }
        } else {
            label = "";
        }
        return label;
    }
}
