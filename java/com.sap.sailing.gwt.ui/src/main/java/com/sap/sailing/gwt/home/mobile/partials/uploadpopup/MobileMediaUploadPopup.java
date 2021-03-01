package com.sap.sailing.gwt.home.mobile.partials.uploadpopup;

import java.util.UUID;
import java.util.function.Consumer;

import com.sap.sailing.gwt.home.mobile.places.event.media.MediaViewResources;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.AbstractMediaUploadPopup;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;

public class MobileMediaUploadPopup extends AbstractMediaUploadPopup {

    public MobileMediaUploadPopup(SailingServiceWriteAsync sailingServiceWrite, UUID eventId, 
            Consumer<VideoDTO> updateVideo, Consumer<ImageDTO> updateImage) {
        super(sailingServiceWrite, eventId, updateVideo, updateImage);
        MediaViewResources.INSTANCE.css().ensureInjected();
        addStyleName(MediaViewResources.INSTANCE.css().popup());
        upload.getElement().setAttribute("accept", "image/*;capture=camera");
    }

        titleTextBox.setValue("");
        fileNameInput.setValue("");
    }
