package com.sap.sailing.gwt.common.client.premium;

import java.util.Set;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.premium.PaywallResolver;
import com.sap.sse.security.ui.client.premium.PremiumCheckBox;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class SailingPremiumCheckBox extends PremiumCheckBox{

    public SailingPremiumCheckBox(final String label, final Action action, final PaywallResolver paywallResolver) {
        super(label, action, paywallResolver);
    }

    @Override
    protected Image createPremiumIcon() {
        return new Image(SailingPremiumIconRessource.INSTANCE.premiumIcon().getSafeUri());
    }

    @Override
    protected void onSubscribeDialogConfirmation(final Set<SubscriptionPlanDTO> unlockingPlans) {
        Window.open(EntryPointLinkFactory.createSubscriptionPageLink(unlockingPlans), "_blank", "");
    }


}
