package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.media.ImageResizingTaskDTO;

public class ImageCreateDialog extends ImageDialog {

    public ImageCreateDialog(String initialTag, SailingServiceAsync sailingService, StringMessages stringMessages, DialogCallback<ImageResizingTaskDTO> dialogCallback) {
        super(new Date(), sailingService, stringMessages, dialogCallback);
        createdAtLabel = new Label(creationDate.toString());
        titleTextBox = createTextBox(null);
        titleTextBox.setVisibleLength(40);
        subtitleTextBox = createTextBox(null);
        subtitleTextBox.setVisibleLength(40);
        copyrightTextBox = createTextBox(null);
        copyrightTextBox.setVisibleLength(40);
        widthInPxBox = createIntegerBox(null, 10);
        widthInPxBox.setEnabled(false);
        heightInPxBox = createIntegerBox(null, 10);
        heightInPxBox.setEnabled(false);
        List<String> tags = new ArrayList<>();
        if (initialTag != null && !initialTag.isEmpty()) {
            tags.add(initialTag);
        }
        tagsListEditor.setValue(tags);
        image = null;
    }
}
