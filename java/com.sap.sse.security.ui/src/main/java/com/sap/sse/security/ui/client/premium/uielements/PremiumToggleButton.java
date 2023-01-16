package com.sap.sse.security.ui.client.premium.uielements;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusPanel;
import com.sap.sse.gwt.client.dialog.ConfirmationDialog;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.premium.settings.SecuredBooleanSetting;

public abstract class PremiumToggleButton extends PremiumUiElement<Boolean> implements HasClickHandlers {

    private static PremiumToggleButtonUiBinder uiBinder = GWT.create(PremiumToggleButtonUiBinder.class);

    interface PremiumToggleButtonUiBinder extends UiBinder<FocusPanel, PremiumToggleButton> {}

    @UiField
    StringMessages i18n;
    @UiField
    protected FocusPanel container;
    @UiField(provided = true)
    protected final Button button;
    
    private final ConfirmationDialog subscribeDialog;
    private final Component<?> associatedComponent;
    private Runnable evaluateAndRenderSplitterWithButtons;

    /**
     * A Composite component, that includes a checkbox and an additional premium icon, indicating that the feature to be
     * enabled is a premium feature if the user does not have the permission.
     */
    protected PremiumToggleButton(final String label,
            Component<?> associatedComponent, SecuredBooleanSetting setting) {
        super(setting);
        PremiumToogleButtonResource.INSTANCE.css().ensureInjected();
        this.associatedComponent = associatedComponent;
        this.button = new Button(label);
        initWidget(uiBinder.createAndBindUi(this));
        this.subscribeDialog = ConfirmationDialog.create(i18n.subscriptionSuggestionTitle(),
                i18n.pleaseSubscribeToUse(), i18n.takeMeToSubscriptions(), i18n.cancel(), () -> setting
                        .getUnlockingSubscriptionPlans(this::onSubscribeDialogConfirmation));
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
        if(setting != null) {
            this.addStyleName(PremiumToogleButtonResource.INSTANCE.css().premiumActive());
            button.setStyleName(PremiumToogleButtonResource.INSTANCE.css().premiumPermitted(), isPermitted);
            button.setStyleName(PremiumToogleButtonResource.INSTANCE.css().notPremiumPermitted(), !isPermitted);
            if(!isPermitted && visible && evaluateAndRenderSplitterWithButtons != null) {
                evaluateAndRenderSplitterWithButtons.run();
            }
        } else {
            this.removeStyleName(PremiumToogleButtonResource.INSTANCE.css().premiumActive());
            button.removeStyleName(PremiumToogleButtonResource.INSTANCE.css().premiumPermitted());
            button.removeStyleName(PremiumToogleButtonResource.INSTANCE.css().notPremiumPermitted());
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
