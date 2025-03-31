package com.sap.sailing.gwt.common.client.premium;

import static com.sap.sailing.gwt.common.client.premium.SailingPremiumIconRessource.INSTANCE;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.premium.PaywallResolver;
import com.sap.sse.security.ui.client.premium.PremiumListBox;

public class SailingPremiumListBox extends PremiumListBox {

    public SailingPremiumListBox(final String label, final String emptyValue, final Action action, final PaywallResolver paywallResolver, SecuredDTO contextDTO) {
        super(label, emptyValue, action, paywallResolver, contextDTO);
    }

    @Override
    protected Image createPremiumIcon() {
        return new Image();
    }

    @Override
    protected void onUserPermissionUpdate(final boolean isPermitted) {
        super.onUserPermissionUpdate(isPermitted);
        image.setUrl((isPermitted ? INSTANCE.premiumIconPermitted() : INSTANCE.premiumIcon()).getSafeUri());
    }

    @Override
    protected void onSubscribeDialogConfirmation(final Iterable<String> unlockingPlans) {
        Window.open(EntryPointLinkFactory.createSubscriptionPageLink(unlockingPlans), "_blank", "");
    }

}
