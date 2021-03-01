package com.sap.sailing.gwt.home.desktop.partials.uploadpopup;

import java.util.function.Consumer;

import com.sap.sailing.gwt.home.desktop.partials.media.MediaPageResources;
import com.sap.sailing.gwt.ui.shared.AbstractMediaUploadPopup;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;

public class DesktopMediaUploadPopup extends AbstractMediaUploadPopup {

    public DesktopMediaUploadPopup(Consumer<VideoDTO> updateVideo, Consumer<ImageDTO> updateImage) {
        super(updateVideo, updateImage);
        MediaPageResources.INSTANCE.css().ensureInjected();
        addStyleName(MediaPageResources.INSTANCE.css().popup());
    }

    @Override
    protected void updateFileName(String fileName) {
        if (fileName == null) {
            fileName = "";
        }
        final String name;
        if (fileName.contains(".")) {
            name = fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            name = fileName;
        }
        titleTextBox.setValue(name);
        fileNameInput.setValue(fileName);
    }

}
