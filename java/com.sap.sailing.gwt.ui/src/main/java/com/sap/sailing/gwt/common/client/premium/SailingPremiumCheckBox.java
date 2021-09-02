package com.sap.sailing.gwt.common.client.premium;

import com.google.gwt.user.client.ui.Image;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.premium.PaywallResolver;
import com.sap.sse.security.ui.client.premium.PremiumCheckBox;

public class SailingPremiumCheckBox extends PremiumCheckBox{

    public SailingPremiumCheckBox(final String label, final Action action, final PaywallResolver paywallResolver) {
        super(label, action, paywallResolver);
    }
    
    @Override
    protected Image createPremiumIcon() {
        return new Image(SailingPremiumIconRessource.INSTANCE.premiumIcon().getSafeUri());
    }

}
