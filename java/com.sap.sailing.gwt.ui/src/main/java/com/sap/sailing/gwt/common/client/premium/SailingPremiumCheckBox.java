package com.sap.sailing.gwt.common.client.premium;

import com.google.gwt.user.client.ui.Image;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.premium.PayWallResolver;
import com.sap.sse.security.ui.client.premium.PremiumCheckBox;

public class SailingPremiumCheckBox extends PremiumCheckBox{

    public SailingPremiumCheckBox(String label, Action action, PayWallResolver payWallResolver) {
        super(label, action, payWallResolver);
    }
    
    @Override
    protected Image createPremiumIcon() {
        return new Image(SailingPremiumIconRessource.INSTANCE.premiumIcon().getSafeUri());
    }

}
