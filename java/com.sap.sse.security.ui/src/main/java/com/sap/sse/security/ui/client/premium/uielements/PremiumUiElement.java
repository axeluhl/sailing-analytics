package com.sap.sse.security.ui.client.premium.uielements;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Image;
import com.sap.sse.security.ui.client.premium.settings.AbstractSecuredValueSetting;

public abstract class PremiumUiElement<T> extends Composite implements HasEnabled {


    /**
     * Flag to keep track of the actual enabled/disabled state of this UI component independent of the representing
     * widget's state, as this might also be used to indicate if the current user is permitted to use this component as
     * well as to disable sub-options if the top-level option is not enabled.
     */
    private boolean enabled = true;
    protected AbstractSecuredValueSetting<T> setting;

    protected PremiumUiElement(AbstractSecuredValueSetting<T> setting) {
        this.setting = setting;
        setting.registerUserStatusEventHandler((user, preAuth) -> updateUserPermission());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    protected final boolean hasPermission() {
        return setting.hasPermission();
    }

    protected final void updateUserPermission() {
        onUserPermissionUpdate(hasPermission());
    }

    protected abstract void onUserPermissionUpdate(boolean isPermitted);

    /**
     * This method can be overridden by sub classes to accommodate for application specific premium icons.
     *
     * @return the {@link Image premium icon}
     */
    protected Image createPremiumIcon() {
        return new Image(PremiumIconRessource.INSTANCE.premiumIcon().getSafeUri());
    }
}
