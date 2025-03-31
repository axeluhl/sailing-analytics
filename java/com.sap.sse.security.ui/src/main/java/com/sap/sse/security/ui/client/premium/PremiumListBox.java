package com.sap.sse.security.ui.client.premium;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.sap.sse.gwt.client.dialog.ConfirmationDialog;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public abstract class PremiumListBox extends PremiumUiElement implements HasAllKeyHandlers {

    private static PremiumCheckBoxUiBinder uiBinder = GWT.create(PremiumCheckBoxUiBinder.class);

    interface PremiumCheckBoxUiBinder extends UiBinder<FocusPanel, PremiumListBox> {
    }

    interface Style extends CssResource {
        @ClassName("premium-container")
        String premiumContainer();

        @ClassName("premium-list-box")
        String premiumListBox();

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
    protected final ListBox listBox;
    private final ConfirmationDialog subscribeDialog;

    /**
     * A Composite component, that includes a list box and an additional premium icon, indicating that the feature to be
     * enabled is a premium feature if the user does not have the permission. By definition there is always minimum one
     * (empty) item with index 0.
     */
    protected PremiumListBox(final String emptyLabel, String emptyValue, final Action action,
            final PaywallResolver paywallResolver, final SecuredDTO contextDTO) {
        super(action, paywallResolver, contextDTO);
        this.listBox = new ListBox();
        this.listBox.addItem(emptyLabel, emptyValue);
        this.listBox.setSelectedIndex(0);
        this.image = createPremiumIcon();
        initWidget(uiBinder.createAndBindUi(this));
        this.subscribeDialog = ConfirmationDialog.create(i18n.subscriptionSuggestionTitle(),
                i18n.pleaseSubscribeToUse(), i18n.takeMeToSubscriptions(), i18n.cancel(), () -> paywallResolver
                        .getUnlockingSubscriptionPlans(action, contextDTO, this::onSubscribeDialogConfirmation));
        updateUserPermission();
        paywallResolver.registerUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {

            }
        });
    }

    public void reset() {
        while (this.listBox.getItemCount() > 1) {
            this.listBox.removeItem(1);
        }
    }

    protected abstract void onSubscribeDialogConfirmation(Iterable<String> unlockingPlans);

    @Override
    protected void onEnsureDebugId(final String baseID) {
        this.listBox.ensureDebugId(baseID);
    }

    @UiHandler("container")
    void onContainerClicked(final ClickEvent event) {
        if (!hasPermission()) {
            this.updateUserPermission();
            subscribeDialog.center();
        }
    }

    @Override
    protected void onUserPermissionUpdate(final boolean isPermitted) {
        listBox.setSelectedIndex(getSelectedIndex());
        listBox.setEnabled(isEnabled() && isPermitted);
        container.setStyleName(style.premiumPermitted(), isPermitted);
    }

    public void addItem(String item, String value) {
        if (hasPermission()) {
            this.listBox.addItem(item, value);
        }
    }

    public void setSelectedIndex(final int index) {
        if (hasPermission()) {
            this.listBox.setSelectedIndex(index);
        }
    }

    public int getSelectedIndex() {
        final int selectedIndex;
        if (hasPermission()) {
            selectedIndex = this.listBox.getSelectedIndex();
        } else {
            selectedIndex = 0;
        }
        return selectedIndex;
    }

    /**
     * If NO permission is granted select empty item (index 0) and return the default empty value.
     */
    public String getSelectedValue() {
        if (!hasPermission()) {
            this.listBox.setSelectedIndex(0);
        }
        return this.listBox.getSelectedValue();
    }

    public void setVisibleItemCount(int visibleItems) {
        if (hasPermission()) {
            this.listBox.setVisibleItemCount(visibleItems);
        }
    }

    public HandlerRegistration addChangeHandler(final ChangeHandler handler) {
        return listBox.addChangeHandler(handler);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        listBox.setEnabled(enabled);
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

    public FocusWidget getFocusWidget() {
        return this.listBox;
    }

    public ListBox getListBox() {
        return this.listBox;
    }
}
