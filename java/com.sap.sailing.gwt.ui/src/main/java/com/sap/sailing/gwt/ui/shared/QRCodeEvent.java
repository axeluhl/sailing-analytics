package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.gwt.client.media.ImageDTO;

public class QRCodeEvent implements IsSerializable {
    private String displayName;
    private String locationAndVenue;
    private ImageDTO logo;

    // GWT only
    public QRCodeEvent() {
    }

    @GwtIncompatible
    public QRCodeEvent(String displayName, String locationAndVenue, ImageDTO logo) {
        super();
        this.displayName = displayName;
        this.locationAndVenue = locationAndVenue;
        this.logo = logo;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLocationAndVenue() {
        return locationAndVenue;
    }

    public ImageDTO getLogoImage() {
        return logo;
    }

}
