package com.sap.sailing.gwt.common.client.premium;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.security.ui.client.premium.settings.SecuredBooleanSetting;
import com.sap.sse.security.ui.client.premium.uielements.PremiumToggleButton;

public class SailingPremiumToggleButton extends PremiumToggleButton {

    public SailingPremiumToggleButton(final String label, Component<?> associatedComponent, SecuredBooleanSetting setting) {
        super(label, associatedComponent, setting);
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
