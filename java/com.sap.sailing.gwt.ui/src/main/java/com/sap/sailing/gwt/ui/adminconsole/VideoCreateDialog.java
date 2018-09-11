package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.common.util.ObservableBoolean;
import com.sap.sse.gwt.client.media.VideoDTO;

public class VideoCreateDialog extends VideoDialog {

    public VideoCreateDialog(String initialTag, StringMessages stringMessages, ObservableBoolean storageServiceAvailable, DialogCallback<VideoDTO> callback) {
        super(new Date(), new VideoParameterValidator(stringMessages), stringMessages, storageServiceAvailable, callback);
        createdAtLabel = new Label(creationDate.toString());
        titleTextBox = createTextBox(null);
        titleTextBox.setVisibleLength(50);
        subtitleTextBox = createTextBox(null);
        subtitleTextBox.setVisibleLength(50);
        copyrightTextBox = createTextBox(null);
        copyrightTextBox.setVisibleLength(50);
        lengthIntegerBox = createIntegerBox(null, 10);
        List<String> tags = new ArrayList<>();
        if(initialTag != null && !initialTag.isEmpty()) {
            tags.add(initialTag);
        }
        tagsListEditor.setValue(tags);
        setSelectedMimeType(MimeType.unknown);
    }
}
