package com.sap.sse.security.ui.client.premium;


import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.ConfirmationDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.subscription.BaseUserSubscriptionView;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public abstract class PremiumCheckBox extends Composite implements BaseUserSubscriptionView {

    private final CheckBox checkBox;
    private final Image image;
    private final HorizontalPanel layoutPanel;
    private final FocusPanel wrapperPanel;
    private final Action action;
    private final PaywallResolver paywallResolver;
    private final StringMessages stringMessages;

    /**
     * A Composite component, that includes a checkbox and an additional premium icon, 
     * indicating that the feature to be enabled is a premium feature if the user does not have the permission.
     * @param label
     * @param userService
     * @param permission
     * @param ownership
     * @param acl
     */
    public PremiumCheckBox(String label, Action action, PaywallResolver paywallResolver) {
        stringMessages = StringMessages.INSTANCE;
        this.action = action;
        this.paywallResolver = paywallResolver;
        this.wrapperPanel = new FocusPanel();
        this.layoutPanel = new HorizontalPanel();
        this.wrapperPanel.add(layoutPanel);
        this.checkBox = new CheckBox(label);
        if(!paywallResolver.hasPermission(action)) {
            ConfirmationDialog pleasSubscribeDialog = new ConfirmationDialog(stringMessages.subscriptionSuggestionTitle(),
                    stringMessages.pleaseSubscribeToUse(), stringMessages.takeMeToSubscriptions(),
                    stringMessages.cancel(), (confirmed) -> {
                        if (confirmed) {
                            // TODO open SubscriptionPlansite with action as Parameter here!!
                        }
                    });
            wrapperPanel.addClickHandler(clickEvent -> pleasSubscribeDialog.center());
            layoutPanel.getElement().getStyle().setCursor(Cursor.POINTER);
            checkBox.setEnabled(false);
            image = createPremiumIcon();
            layoutPanel.add(image);
            //FIXME: See bug5593 - This message should contain the plan, which would provide the needed access (if the access is not otherwise blocked)
            layoutPanel.setTitle(stringMessages.unlockWithSubscription());
            image.setWidth("1em");
            image.setHeight("1em");
        }else {
            //TODO: Might want to use an "unlocked" image.
            image = null;
            checkBox.setEnabled(true);
        }
        layoutPanel.add(checkBox);
        initWidget(wrapperPanel);
    }
    
    public PremiumCheckBox(String label, Action action, PaywallResolver paywallResolver, DataEntryDialog<?> dialog) {
        this(label, action, paywallResolver);
        dialog.ensureHasValueIsValidated(checkBox);
        dialog.ensureFocusWidgetIsLinkedToKeyStrokes(checkBox);
    }

    /**
     * This Method can be overridden by Subclasses to accommodate for application specific premium icons.
     * @return Premium Icon
     */
    protected Image createPremiumIcon() {
        return new Image(PremiumIconRessource.INSTANCE.premiumIcon().getSafeUri());
    }
    
    
    public FocusPanel getFocusWidget() {
        return this.wrapperPanel;
    }
    
    public CheckBox getCheckBox() {
        return this.checkBox;
    }
    
    public void setValueifUserHasPermission(Boolean value) {
        if(paywallResolver.hasPermission(action)) {
            this.checkBox.setValue(value);
        }
    }
    
    public Boolean getValue() {
        if(!paywallResolver.hasPermission(action)) {
            Notification.notify(stringMessages.pleaseSubscribeToUseSpecific(action.name()), NotificationType.ERROR);
            return false;
        }else {
            return checkBox.getValue();
        }
    }

    public void setEnabledIfUserHasPermission(boolean value) {
        if(paywallResolver.hasPermission(action)) {
            this.checkBox.setEnabled(value);
        }
    }
    
    @Override
    public void updateView(SubscriptionDTO subscription, Iterable<SubscriptionPlanDTO> planList) {
        //TODO: As of now, there is no update mechanism implemented, therefore a page reload is forced.
        Window.Location.reload();
    }

    @Override
    public void onCloseCheckoutModal() {
        // No Action required.
    }

    @Override
    public void onOpenCheckoutError(String error) {
        Notification.notify(stringMessages.currentlyUnableToSubscribe(), NotificationType.ERROR);
    }
}
