package com.sap.sailing.gwt.home.mobile.partials.uploadpopup;

import com.sap.sailing.gwt.home.mobile.places.event.media.MediaViewResources;
import com.sap.sailing.gwt.ui.shared.AbstractMediaUploadPopup;

public class MobileMediaUploadPopup extends AbstractMediaUploadPopup {

    public MobileMediaUploadPopup() {
        super();
        MediaViewResources.INSTANCE.css().ensureInjected();
        addStyleName(MediaViewResources.INSTANCE.css().popup());
        upload.getElement().setAttribute("accept", "image/*;capture=camera");
    }

}
