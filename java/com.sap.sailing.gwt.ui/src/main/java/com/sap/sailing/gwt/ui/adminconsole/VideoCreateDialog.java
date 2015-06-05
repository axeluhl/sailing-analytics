package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collections;

import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.media.VideoDTO;

public class VideoCreateDialog extends VideoDialog {

    public VideoCreateDialog(StringMessages stringMessages, DialogCallback<VideoDTO> callback) {
        super(new VideoParameterValidator(stringMessages), stringMessages, callback);
        createdAtLabel = new Label(creationDate.toString());
        titleTextBox = createTextBox(null);
        titleTextBox.setVisibleLength(50);
        subtitleTextBox = createTextBox(null);
        subtitleTextBox.setVisibleLength(50);
        copyrightTextBox = createTextBox(null);
        copyrightTextBox.setVisibleLength(50);
        lengthIntegerBox = createIntegerBox(null, 10);
        tagsListEditor.setValue(Collections.<String>emptyList());
    }
}
