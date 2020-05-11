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
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscription.UserSubscriptionView;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionPlans;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionPlans.Plan;

public class UserSubscription extends Composite implements UserSubscriptionView {
    interface MyUiBinder extends UiBinder<Widget, UserSubscription> {}
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    
    @UiField DivElement rootUi;
    @UiField Button upgradeButtonUi;
    @UiField Button cancelSubscriptionButtonUi;
    @UiField SpanElement planNameSpanUi;
    @UiField DivElement trialDivUi;
    @UiField DivElement subscriptionGroupUi;
    @UiField SpanElement subscriptionStatusSpanUi;
    @UiField SpanElement paymentStatusSpanUi;
    @UiField DivElement paymentStatusDivUi;
    @UiField Button openPortalSessionButtonUi;
    
    private Presenter presenter;

    public UserSubscription(UserSubscriptionView.Presenter presenter) {
        initWidget(uiBinder.createAndBindUi(this));
        
        presenter.setView(this);
        this.presenter = presenter;
    }
    
    @UiHandler("upgradeButtonUi")
    public void handleUpgradeClick(ClickEvent e) {
        upgradeButtonUi.setEnabled(false);
        presenter.openCheckout();
    }
    
    @UiHandler("cancelSubscriptionButtonUi")
    public void handleCancelSubscriptionClick(ClickEvent e) {
        cancelSubscriptionButtonUi.setEnabled(false);
        presenter.cancelSubscription();
    }
    
    @UiHandler("openPortalSessionButtonUi")
    public void handleOpenPortalClick(ClickEvent e) {
        openPortalSessionButtonUi.setEnabled(false);
        presenter.openPortalSession();
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
    public void onCloseCheckoutModal() {
        upgradeButtonUi.setEnabled(true);
    }
    
    @Override
    public void onClosePortalModal() {
        openPortalSessionButtonUi.setEnabled(true);
    }

    @Override
    public void updateView(SubscriptionDTO subscription) {
        resetElementsVisibleState();
        
        upgradeButtonUi.setEnabled(true);
        
        Plan plan = null;
        if (subscription != null) {
            plan = SubscriptionPlans.getPlan(subscription.planId);
        }
        
        if (subscription == null || plan == null) {
            planNameSpanUi.setInnerText("Free");
            hideElement(subscriptionGroupUi);
            cancelSubscriptionButtonUi.setVisible(false);
            openPortalSessionButtonUi.setVisible(false);
            show();
            return;
        }
        
        upgradeButtonUi.setVisible(false);
        
        planNameSpanUi.setInnerText(plan.getName());
        subscriptionStatusSpanUi.setInnerText(subscription.subscriptionStatus);
        if (subscription.isActive()) {
            subscriptionStatusSpanUi.addClassName(SubscriptionProfileDesktopResources.INSTANCE.css().blueText());
            paymentStatusSpanUi.setInnerText(subscription.isPaymentSuccess() ? "Success" : "No success");
            paymentStatusSpanUi.addClassName(
                    subscription.isPaymentSuccess() ? 
                            SubscriptionProfileDesktopResources.INSTANCE.css().blueText() :
                                SubscriptionProfileDesktopResources.INSTANCE.css().errorText());
        } else {
            hideElement(paymentStatusDivUi);
            
            if (subscription.isInTrial()) {
                trialDivUi.setInnerText(buildTrialText(subscription));
            } else {
                hideElement(trialDivUi);
            }
        }
        
        show();
    }
    
    private String buildTrialText(SubscriptionDTO subscription) {
        long now = Math.round(Duration.currentTimeMillis() / 1000);
        long remain = subscription.trialEnd - now;
        StringBuilder remainText = new StringBuilder();
        if (remain <= 0) {
            remainText.append("0 day");
        } else {
            long days = remain / 86400;
            if (days > 0) {
                remainText.append(days).append(" day");
                if (days > 1) {
                    remainText.append("s");
                }
            }
            remain = remain % 86400;
            int hours = (int)(remain / 3600);
            if (hours > 0) {
                if (remainText.length() > 0) {
                    remainText.append(" ");
                }
                remainText.append(hours).append(" hour");
                if (hours > 1) {
                    remainText.append("s");
                }
            }
            if (days == 0) {
                remain = remain % 3600;
                int mins = (int)(remain / 60);
                if (mins > 0) {
                    if (remainText.length() > 0) {
                        remainText.append(" ");
                    }
                    remainText.append(mins).append(" min");
                    if (mins > 1) {
                        remainText.append("s");
                    }
                }
            }
        }
        
        if (remainText.length() == 0) {
            remainText.append("0 day");
        }
        
        StringBuilder trialText = new StringBuilder();
        trialText.append("Your trial expires in ")
            .append(remainText.toString())
            .append(" (").append("ends on ")
            .append(DateTimeFormat.getFormat("yyyy-MM-dd HH:mm").format(new Date(subscription.trialEnd * 1000)))
            .append(")");
        
        return trialText.toString();
    }
    
    private void resetElementsVisibleState() {
        upgradeButtonUi.setVisible(true);
        showElement(subscriptionGroupUi);
        cancelSubscriptionButtonUi.setVisible(true);
        showElement(paymentStatusDivUi);
        showElement(trialDivUi);
        openPortalSessionButtonUi.setVisible(true);
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
