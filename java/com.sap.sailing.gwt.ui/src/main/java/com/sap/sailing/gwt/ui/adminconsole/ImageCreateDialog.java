package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collections;

import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.media.ImageDTO;

public class ImageCreateDialog extends ImageDialog {

    public ImageCreateDialog(StringMessages stringMessages, DialogCallback<ImageDTO> callback) {
        super(new ImageParameterValidator(stringMessages), stringMessages, callback);
        createdAtLabel = new Label(creationDate.toString());
        titleTextBox = createTextBox(null);
        titleTextBox.setVisibleLength(50);
        subtitleTextBox = createTextBox(null);
        subtitleTextBox.setVisibleLength(50);
        copyrightTextBox = createTextBox(null);
        copyrightTextBox.setVisibleLength(50);
        widthInPxBox = createIntegerBox(null, 10);
        heightInPxBox = createIntegerBox(null, 10);
        tagsListEditor.setValue(Collections.<String>emptyList());
    }
}
