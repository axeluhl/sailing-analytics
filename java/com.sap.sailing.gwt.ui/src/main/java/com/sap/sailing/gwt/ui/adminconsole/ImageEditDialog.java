package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.media.ImageDTO;

public class ImageEditDialog extends ImageDialog {
    public ImageEditDialog(ImageDTO image, StringMessages stringMessages, DialogCallback<ImageDTO> callback) {
        super(new ImageParameterValidator(stringMessages), stringMessages, callback);
        createdAtLabel = new Label(image.getCreatedAtDate().toString());
        imageURLAndUploadComposite.setURL(image.getSourceRef());
        titleTextBox = createTextBox(image.getTitle());
        titleTextBox.setVisibleLength(50);
        subtitleTextBox = createTextBox(image.getSubtitle());
        subtitleTextBox.setVisibleLength(50);
        copyrightTextBox = createTextBox(image.getCopyright());
        copyrightTextBox.setVisibleLength(50);
        widthInPxBox = createIntegerBox(image.getWidthInPx(), 10);
        heightInPxBox = createIntegerBox(image.getHeightInPx(), 10);
        List<String> tags = new ArrayList<String>();
        tags.addAll(image.getTags());
        tagsListEditor.setValue(tags);
    }
}
