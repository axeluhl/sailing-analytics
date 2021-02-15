package com.sap.sailing.gwt.home.desktop.partials.uploadpopup;

import java.util.UUID;

import com.sap.sailing.gwt.home.desktop.partials.media.MediaPageResources;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.AbstractMediaUploadPopup;

public class DesktopMediaUploadPopup extends AbstractMediaUploadPopup {

    public DesktopMediaUploadPopup(SailingServiceWriteAsync sailingServiceWrite, UUID eventId) {
        super(sailingServiceWrite, eventId);
        MediaPageResources.INSTANCE.css().ensureInjected();
        addStyleName(MediaPageResources.INSTANCE.css().popup());
    }

}
