package com.sap.sailing.gwt.home.mobile.partials.uploadpopup;

import java.util.UUID;

import com.sap.sailing.gwt.home.mobile.places.event.media.MediaViewResources;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.AbstractMediaUploadPopup;

public class MobileMediaUploadPopup extends AbstractMediaUploadPopup {

    public MobileMediaUploadPopup(SailingServiceWriteAsync sailingServiceWrite, UUID eventId) {
        super(sailingServiceWrite, eventId);
        MediaViewResources.INSTANCE.css().ensureInjected();
        addStyleName(MediaViewResources.INSTANCE.css().popup());
        upload.getElement().setAttribute("accept", "image/*;capture=camera");
    }

    @Override
    protected void updateFileName(String fileName) {
    }

}
