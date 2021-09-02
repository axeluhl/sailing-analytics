package com.sap.sse.security.ui.client.premium;


import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.sap.sse.security.shared.HasPermissions.Action;

public abstract class PremiumUiElement extends Composite {

    protected final Action action;
    protected final PaywallResolver paywallResolver;
    private final HandlerRegistration handlerRegistration;

    protected PremiumUiElement(final Action action, final PaywallResolver paywallResolver) {
        this.action = action;
        this.paywallResolver = paywallResolver;
        this.handlerRegistration = paywallResolver
                .registerUserStatusEventHandler((user, preAuth) -> updateUserPermission());
    }

    protected final boolean hasPermission() {
        return paywallResolver.hasPermission(action);
    }

    protected final void updateUserPermission() {
        onUserPermissionUpdate(hasPermission());
    }

    protected abstract void onUserPermissionUpdate(boolean isPermitted);

    /**
     *
     * This Method can be overridden by Subclasses to accommodate for application specific premium icons.
     *
     * @return Premium Icon
     */
    protected Image createPremiumIcon() {
        return new Image(PremiumIconRessource.INSTANCE.premiumIcon().getSafeUri());
    }

    @Override
    protected void onUnload() {
        handlerRegistration.removeHandler();
    }
}
