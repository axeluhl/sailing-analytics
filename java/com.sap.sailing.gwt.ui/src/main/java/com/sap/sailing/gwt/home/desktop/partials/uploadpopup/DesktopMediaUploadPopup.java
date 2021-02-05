package com.sap.sailing.gwt.home.desktop.partials.uploadpopup;

import com.sap.sailing.gwt.home.desktop.partials.media.MediaPageResources;
import com.sap.sailing.gwt.ui.shared.AbstractMediaUploadPopup;

public class DesktopMediaUploadPopup extends AbstractMediaUploadPopup {

    public DesktopMediaUploadPopup() {
        super();
        MediaPageResources.INSTANCE.css().ensureInjected();
        addStyleName(MediaPageResources.INSTANCE.css().popup());
    }

}
