package com.sap.sailing.gwt.home.mobile.partials.uploadpopup;

import java.util.function.Consumer;

import com.sap.sailing.gwt.home.mobile.places.event.media.MediaViewResources;
import com.sap.sailing.gwt.ui.shared.AbstractMediaUploadPopup;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;

public class MobileMediaUploadPopup extends AbstractMediaUploadPopup {

    public MobileMediaUploadPopup(Consumer<VideoDTO> updateVideo, Consumer<ImageDTO> updateImage) {
        super(updateVideo, updateImage);
        MediaViewResources.INSTANCE.css().ensureInjected();
        addStyleName(MediaViewResources.INSTANCE.css().popup());
    }

    @Override
    protected void updateFileName(String fileName) {
        titleTextBox.setValue("");
        fileNameInput.setValue("");
    }
}
