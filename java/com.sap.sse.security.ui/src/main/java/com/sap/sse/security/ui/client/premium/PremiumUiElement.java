package com.sap.sse.security.ui.client.premium;


import com.google.gwt.user.client.ui.Composite;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.UserStatusEventHandler;

public abstract class PremiumUiElement extends Composite {

    protected final Action action;
    protected final PaywallResolver paywallResolver;

    public PremiumUiElement(Action action, PaywallResolver paywallResolver) {
        this.action = action;
        this.paywallResolver = paywallResolver;
        UserStatusEventHandler userStatusEventHandler = (user, preAuth) -> {
            changePermissionSensitiveParts(paywallResolver.isPermitted(action));
        };
        paywallResolver.registerUserStatusEventHandler(userStatusEventHandler);
    }
    
    public abstract void changePermissionSensitiveParts(boolean isPermitted);
}
