package com.sap.sailing.gwt.common.client.premium;

import static com.sap.sailing.gwt.common.client.premium.SailingPremiumIconRessource.INSTANCE;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.premium.PaywallResolver;
import com.sap.sse.security.ui.client.premium.PremiumToggleButton;

public class SailingPremiumToggleButton extends PremiumToggleButton {

    public SailingPremiumToggleButton(final String label, final Action action, final PaywallResolver paywallResolver,
            Component<?> associatedComponent) {
        super(label, action, paywallResolver, associatedComponent);
    }

    @Override
    protected Image createPremiumIcon() {
        return new Image();
    }

    @Override
    protected void onUserPermissionUpdate(final boolean isPermitted) {
        super.onUserPermissionUpdate(isPermitted);
        if(action != null) {
            image.setUrl((isPermitted ? INSTANCE.premiumIconPermitted() : INSTANCE.premiumIcon()).getSafeUri());
        }
    }

    @Override
    protected void onSubscribeDialogConfirmation(final Iterable<String> unlockingPlans) {
        Window.open(EntryPointLinkFactory.createSubscriptionPageLink(unlockingPlans), "_blank", "");
    }

}
