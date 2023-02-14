package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.ImageResizingTaskDTO;

public class ImageEditDialog extends ImageDialog {
    public ImageEditDialog(ImageDTO imageDTO, SailingServiceAsync sailingService, StringMessages stringMessages, FileStorageServiceConnectionTestObservable storageServiceAvailable, DialogCallback<List<ImageResizingTaskDTO>> dialogCallback) {
        super(imageDTO.getCreatedAtDate(), sailingService, stringMessages, storageServiceAvailable, dialogCallback);
        createdAtLabel = new Label(imageDTO.getCreatedAtDate().toString());
        imageURLAndUploadComposite.setUri(imageDTO.getSourceRef());
        titleTextBox = createTextBox(imageDTO.getTitle());
        titleTextBox.setVisibleLength(40);
        subtitleTextBox = createTextBox(imageDTO.getSubtitle());
        subtitleTextBox.setVisibleLength(40);
        copyrightTextBox = createTextBox(imageDTO.getCopyright());
        copyrightTextBox.setVisibleLength(40);
        List<String> tags = new ArrayList<String>();
        tags.addAll(imageDTO.getTags());
        tagsListEditor.setValue(tags);
        ValueChangeEvent.fire(imageURLAndUploadComposite, imageURLAndUploadComposite.getValue());
    }
}
