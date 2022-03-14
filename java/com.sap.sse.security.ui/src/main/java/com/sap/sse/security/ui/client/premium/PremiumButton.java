package com.sap.sse.security.ui.client.premium;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Image;
import com.sap.sse.gwt.client.dialog.ConfirmationDialog;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public abstract class PremiumButton extends PremiumUiElement implements HasAllKeyHandlers, HasClickHandlers {

    private static PremiumButtonUiBinder uiBinder = GWT.create(PremiumButtonUiBinder.class);

    interface PremiumButtonUiBinder extends UiBinder<FocusPanel, PremiumButton> {
    }

    interface Style extends CssResource {
        @ClassName("premium-container")
        String premiumContainer();

        @ClassName("premium-button")
        String premiumButton();

        @ClassName("non-premium-button")
        String nonPremiumButton();

        @ClassName("premium-permitted")
        String premiumPermitted();

        @ClassName("premium-icon")
        String premiumIcon();
    }

    @UiField
    StringMessages i18n;
    @UiField
    Style style;
    @UiField
    FocusPanel container;
    @UiField(provided = true)
    protected final Image image;
    @UiField(provided = true)
    protected final Button button;

    private final ConfirmationDialog subscribeDialog;
    protected final Action action;

    /**
     * A Composite component, that includes a checkbox and an additional premium icon, indicating that the feature to be
     * enabled is a premium feature if the user does not have the permission.
     */
    protected PremiumButton(final String label, final Action action, final PaywallResolver paywallResolver) {
        super(action, paywallResolver);
        this.action = action;
        this.button = new Button(label);
        if(action != null) {
            this.image = createPremiumIcon();
        }else {
            // If no action was given, image remains empty and premium css classes are removed.
            image = new Image();
            image.setVisible(false);
            button.removeStyleDependentName("premium-button");
            button.removeStyleName("premium-button");
            button.addStyleDependentName("non-premium-button");
            button.addStyleName("non-premium-button");
        }
        initWidget(uiBinder.createAndBindUi(this));
        this.subscribeDialog = ConfirmationDialog.create(i18n.subscriptionSuggestionTitle(),
                i18n.pleaseSubscribeToUse(), i18n.takeMeToSubscriptions(), i18n.cancel(),
                () -> paywallResolver.getUnlockingSubscriptionPlans(action, this::onSubscribeDialogConfirmation));
        updateUserPermission();
    }

    protected abstract void onSubscribeDialogConfirmation(Iterable<String> unlockingPlans);

    @Override
    protected void onEnsureDebugId(final String baseID) {
        this.button.ensureDebugId(baseID);
    }

    @UiHandler("container")
    void onContainerClicked(final ClickEvent event) {
        if (!hasPermission()) {
            this.updateUserPermission();
            subscribeDialog.center();
        }else {
            this.button.click();
        }
    }

    @Override
    protected void onUserPermissionUpdate(final boolean isPermitted) {
        button.setEnabled(isEnabled() && isPermitted);
        if(action != null) {
            container.setStyleName(style.premiumPermitted(), isPermitted);
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        button.setEnabled(enabled);
    }

    @Override
    public HandlerRegistration addKeyDownHandler(final KeyDownHandler handler) {
        return container.addKeyDownHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyPressHandler(final KeyPressHandler handler) {
        return container.addKeyPressHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyUpHandler(final KeyUpHandler handler) {
        return container.addKeyUpHandler(handler);
    }
    
    @Override
    public HandlerRegistration addClickHandler(final ClickHandler handler) {
        return button.addClickHandler(handler);
    }

    public FocusWidget getFocusWidget() {
        return this.button;
    }
}
