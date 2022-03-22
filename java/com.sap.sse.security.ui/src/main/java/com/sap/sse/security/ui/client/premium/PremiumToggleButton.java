package com.sap.sse.security.ui.client.premium;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.sap.sse.gwt.client.dialog.ConfirmationDialog;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public abstract class PremiumToggleButton extends PremiumUiElement implements HasClickHandlers {

    private static PremiumToggleButtonUiBinder uiBinder = GWT.create(PremiumToggleButtonUiBinder.class);

    interface PremiumToggleButtonUiBinder extends UiBinder<FocusPanel, PremiumToggleButton> {
    }

    interface Style extends CssResource {
        @ClassName("premium-container")
        String premiumContainer();

        @ClassName("premium-button")
        String premiumButton();

        @ClassName("premium-permitted")
        String premiumPermitted();

        @ClassName("premium-icon")
        String premiumIcon();
        
        @ClassName("not-premium-permitted")
        String notPremiumPermitted();
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
    private final Component<?> associatedComponent;
    private Runnable evaluateAndRenderSplitterWithButtons;

    /**
     * A Composite component, that includes a checkbox and an additional premium icon, indicating that the feature to be
     * enabled is a premium feature if the user does not have the permission.
     */
    protected PremiumToggleButton(final String label, final Action action, final PaywallResolver paywallResolver,
            Component<?> associatedComponent) {
        super(action, paywallResolver);
        this.action = action;
        this.associatedComponent = associatedComponent;
        this.button = new Button(label);
        if (action != null) {
            this.image = createPremiumIcon();
        } else {
            image = new Image();
            image.setVisible(false);
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
        event.stopPropagation();
        event.preventDefault();
        if (!hasPermission()) {
            this.updateUserPermission();
            subscribeDialog.center();
        }else {
            this.button.click();
        }
    }

    @Override
    protected void onUserPermissionUpdate(final boolean isPermitted) {
        boolean visible = associatedComponent.isVisible();
        if(action != null) {
            container.setStyleName(style.premiumPermitted(), isPermitted);
            container.setStyleName(style.notPremiumPermitted(), !isPermitted);
            if(!isPermitted && visible && evaluateAndRenderSplitterWithButtons != null) {
                evaluateAndRenderSplitterWithButtons.run();
            }
        }
        button.setEnabled(isEnabled() && isPermitted);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        button.setEnabled(enabled);
    }

    @Override
    public HandlerRegistration addClickHandler(final ClickHandler handler) {
        return button.addClickHandler(handler);
    }
    
    public Button getInternalButton() {
        return this.button;
    }
    
    public FocusPanel getFocusPanel() {
        return this.container;
    }

    public void addRunOnUserPermissionChanged(Runnable evaluateAndRenderSplitterWithButtons) {
        this.evaluateAndRenderSplitterWithButtons = evaluateAndRenderSplitterWithButtons;
    }
}
