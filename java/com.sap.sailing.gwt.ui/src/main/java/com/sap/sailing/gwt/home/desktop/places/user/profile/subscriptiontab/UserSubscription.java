package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptiontab;

import java.util.Date;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscription.UserSubscriptionView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.shared.SubscriptionPlan;
import com.sap.sse.security.shared.SubscriptionPlanHolder;

/**
 * Implementation view for {@link UserSubscriptionView}
 * 
 * @author Tu Tran
 */
public class UserSubscription extends Composite implements UserSubscriptionView {
    interface MyUiBinder extends UiBinder<Widget, UserSubscription> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    DivElement rootUi;
    @UiField
    Button updateSubscriptionButtonUi;
    @UiField
    Button cancelSubscriptionButtonUi;
    @UiField
    SpanElement planNameSpanUi;
    @UiField
    DivElement trialDivUi;
    @UiField
    DivElement subscriptionGroupUi;
    @UiField
    SpanElement subscriptionStatusSpanUi;
    @UiField
    SpanElement paymentStatusSpanUi;
    @UiField
    DivElement paymentStatusDivUi;
    @UiField
    ListBox planListUi;

    private Presenter presenter;

    public UserSubscription(UserSubscriptionView.Presenter presenter) {
        initWidget(uiBinder.createAndBindUi(this));

        presenter.setView(this);
        this.presenter = presenter;
    }

    @UiHandler("updateSubscriptionButtonUi")
    public void handleUpdateSubscriptionClick(ClickEvent e) {
        updateSubscriptionButtonUi.setEnabled(false);
        presenter.openCheckout(planListUi.getSelectedValue());
    }

    @UiHandler("cancelSubscriptionButtonUi")
    public void handleCancelSubscriptionClick(ClickEvent e) {
        cancelSubscriptionButtonUi.setEnabled(false);
        presenter.cancelSubscription();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        SubscriptionProfileDesktopResources.INSTANCE.css().ensureInjected();
    }

    @Override
    public void onStartLoadSubscription() {
        hide();
    }

    @Override
    public void onOpenCheckoutError(String error) {
        updateSubscriptionButtonUi.setEnabled(true);
        Notification.notify(error, NotificationType.ERROR);
    }

    @Override
    public void onCloseCheckoutModal() {
        updateSubscriptionButtonUi.setEnabled(true);
    }

    @Override
    public void updateView(SubscriptionDTO subscription) {
        resetElementsVisibleState();

        updatePlanList(subscription);

        updateSubscriptionButtonUi.setEnabled(true);

        SubscriptionPlan plan = getSubscriptionPlan(subscription);
        if (subscription == null || plan == null) {
            updateViewWithFreePlan();
        } else {
            updateViewWithPlan(subscription, plan);
        }

        show();
    }

    private SubscriptionPlan getSubscriptionPlan(SubscriptionDTO subscription) {
        return subscription != null ? SubscriptionPlanHolder.getInstance().getPlan(subscription.getPlanId()) : null;
    }

    private void updateViewWithFreePlan() {
        planNameSpanUi.setInnerText("Free");
        hideElement(subscriptionGroupUi);
        cancelSubscriptionButtonUi.setVisible(false);
    }

    private void updateViewWithPlan(SubscriptionDTO subscription, SubscriptionPlan plan) {
        planNameSpanUi.setInnerText(plan.getName());
        subscriptionStatusSpanUi.setInnerText(subscription.getSubscriptionStatusLabel());
        if (subscription.isActive()) {
            subscriptionStatusSpanUi.addClassName(SubscriptionProfileDesktopResources.INSTANCE.css().blueText());
            paymentStatusSpanUi.setInnerText(subscription.getPaymentStatusLabel());
            paymentStatusSpanUi.addClassName(
                    subscription.isPaymentSuccess() ? SubscriptionProfileDesktopResources.INSTANCE.css().blueText()
                            : SubscriptionProfileDesktopResources.INSTANCE.css().errorText());
        } else {
            hideElement(paymentStatusDivUi);

            if (subscription.isInTrial()) {
                trialDivUi.setInnerText(buildTrialText(subscription));
            } else {
                hideElement(trialDivUi);
            }
        }
    }

    private void updatePlanList(SubscriptionDTO subscription) {
        planListUi.clear();
        if (subscription == null) {
            planListUi.addItem("", "");
            planListUi.setSelectedIndex(0);
        }
        SubscriptionPlan[] planList = SubscriptionPlanHolder.getInstance().getPlanList();
        int i = 0;
        for (SubscriptionPlan plan : planList) {
            planListUi.addItem(plan.getName(), plan.getId());
            if (subscription != null && subscription.getPlanId().equals(plan.getId())) {
                planListUi.setSelectedIndex(i);
            }
            i++;
        }
    }

    private String buildTrialText(SubscriptionDTO subscription) {
        return StringMessages.INSTANCE.trialText(getTrialRemainingText(subscription),
                DateTimeFormat.getFormat("yyyy-MM-dd HH:mm").format(new Date(subscription.getTrialEnd() * 1000)));
    }

    private String getTrialRemainingText(SubscriptionDTO subscription) {
        long now = Math.round(Duration.currentTimeMillis() / 1000);
        long remain = subscription.getTrialEnd() - now;
        StringBuilder remainText = new StringBuilder();
        if (remain <= 0) {
            remainText.append(StringMessages.INSTANCE.numHours(0));
        } else {
            int days = (int) (remain / 86400);
            if (days > 0) {
                remainText.append(StringMessages.INSTANCE.numDays(days));
            }
            remain = remain % 86400;
            int hours = (int) (remain / 3600);
            if (hours > 0) {
                if (remainText.length() > 0) {
                    remainText.append(" ");
                }
                remainText.append(StringMessages.INSTANCE.numHours(hours));
            }
            if (days == 0) {
                remain = remain % 3600;
                int mins = (int) (remain / 60);
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

    private void resetElementsVisibleState() {
        showElement(subscriptionGroupUi);
        cancelSubscriptionButtonUi.setVisible(true);
        showElement(paymentStatusDivUi);
        showElement(trialDivUi);
    }

    private void hideElement(Element element) {
        element.getStyle().setDisplay(Display.NONE);
    }

    private void showElement(Element element) {
        element.getStyle().setDisplay(Display.BLOCK);
    }

    private void hide() {
        rootUi.getStyle().setDisplay(Display.NONE);
    }

    private void show() {
        rootUi.getStyle().setDisplay(Display.BLOCK);
    }
}
