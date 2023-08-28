package com.sap.sse.security.ui.client.premium;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface PremiumIconRessource extends ClientBundle {
    public static final PremiumIconRessource INSTANCE = GWT.create(PremiumIconRessource.class);
    
    @Source("../images/icon_premium.svg")
    @MimeType("image/svg+xml")
    DataResource premiumIcon();
}
