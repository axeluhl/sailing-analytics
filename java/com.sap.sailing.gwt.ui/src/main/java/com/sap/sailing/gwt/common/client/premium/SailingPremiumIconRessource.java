package com.sap.sailing.gwt.common.client.premium;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.sap.sse.security.ui.client.premium.uielements.PremiumIconRessource;

public interface SailingPremiumIconRessource extends PremiumIconRessource {

    public static final SailingPremiumIconRessource INSTANCE = GWT.create(SailingPremiumIconRessource.class);

    @Source("icon_premium.svg")
    @MimeType("image/svg+xml")
    @Override
    DataResource premiumIcon();

    @Source("icon_premium_permitted.svg")
    @MimeType("image/svg+xml")
    DataResource premiumIconPermitted();

}
