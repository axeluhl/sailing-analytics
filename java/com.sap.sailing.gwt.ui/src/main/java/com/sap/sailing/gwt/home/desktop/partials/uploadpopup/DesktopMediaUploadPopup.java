package com.sap.sailing.gwt.home.desktop.partials.uploadpopup;

import java.util.UUID;
import java.util.function.Consumer;

import com.sap.sailing.gwt.home.desktop.partials.media.MediaPageResources;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.AbstractMediaUploadPopup;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;

public class DesktopMediaUploadPopup extends AbstractMediaUploadPopup {

    public DesktopMediaUploadPopup(SailingServiceWriteAsync sailingServiceWrite, UUID eventId, 
    		Consumer<VideoDTO> updateVideo, Consumer<ImageDTO> updateImage) {
        super(sailingServiceWrite, eventId, updateVideo, updateImage);
        MediaPageResources.INSTANCE.css().ensureInjected();
        addStyleName(MediaPageResources.INSTANCE.css().popup());
    }

}
