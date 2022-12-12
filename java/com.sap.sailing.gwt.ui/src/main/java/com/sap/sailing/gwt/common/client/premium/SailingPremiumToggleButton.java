package com.sap.sailing.gwt.common.client.premium;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.premium.PaywallResolverImpl;
import com.sap.sse.security.ui.client.premium.PremiumToggleButton;

public class SailingPremiumToggleButton extends PremiumToggleButton {

    public SailingPremiumToggleButton(final String label, final Action action, final PaywallResolverImpl paywallResolver,
            SecuredDTO dtoContext, Component<?> associatedComponent) {
        super(label, action, paywallResolver, associatedComponent, dtoContext);
    }

    @Override
    protected Image createPremiumIcon() {
        return new Image();
    }

    @Override
    protected void onUserPermissionUpdate(final boolean isPermitted) {
        super.onUserPermissionUpdate(isPermitted);
    }

    @Override
    protected void onSubscribeDialogConfirmation(final Iterable<String> unlockingPlans) {
        Window.open(EntryPointLinkFactory.createSubscriptionPageLink(unlockingPlans), "_blank", "");
    }

}
