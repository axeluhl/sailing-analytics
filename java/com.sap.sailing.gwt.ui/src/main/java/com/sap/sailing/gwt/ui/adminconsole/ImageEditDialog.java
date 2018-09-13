package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.adminconsole.EventDialog.FileStorageServiceConnectionTestObservable;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.ImageResizingTaskDTO;

public class ImageEditDialog extends ImageDialog {
    public ImageEditDialog(ImageDTO imageDTO, SailingServiceAsync sailingService, StringMessages stringMessages, FileStorageServiceConnectionTestObservable storageServiceAvailable, DialogCallback<ImageResizingTaskDTO> dialogCallback) {
        super(imageDTO.getCreatedAtDate(), sailingService, stringMessages, storageServiceAvailable, dialogCallback);
        createdAtLabel = new Label(imageDTO.getCreatedAtDate().toString());
        imageURLAndUploadComposite.setURL(imageDTO.getSourceRef());
        titleTextBox = createTextBox(imageDTO.getTitle());
        titleTextBox.setVisibleLength(40);
        subtitleTextBox = createTextBox(imageDTO.getSubtitle());
        subtitleTextBox.setVisibleLength(40);
        copyrightTextBox = createTextBox(imageDTO.getCopyright());
        copyrightTextBox.setVisibleLength(40);
        widthInPxBox = createIntegerBox(imageDTO.getWidthInPx(), 10);
        widthInPxBox.setEnabled(false);
        heightInPxBox = createIntegerBox(imageDTO.getHeightInPx(), 10);
        heightInPxBox.setEnabled(false);
        List<String> tags = new ArrayList<String>();
        tags.addAll(imageDTO.getTags());
        tagsListEditor.setValue(tags);
    }
}
