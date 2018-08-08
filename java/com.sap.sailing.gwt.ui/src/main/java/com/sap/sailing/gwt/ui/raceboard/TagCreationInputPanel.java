package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class TagCreationInputPanel extends VerticalPanel{
    private TextBox tagTextBox, commentTextBox, urlTextBox;
    
    public TagCreationInputPanel(StringMessages stringMessages) {
        tagTextBox = new TextBox();
        tagTextBox.setTitle("Tag");
        tagTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelTag());
        add(tagTextBox);
        
        commentTextBox = new TextBox();
        commentTextBox.setTitle("Comment");
        commentTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelComment());
        add(commentTextBox);
        
        urlTextBox = new TextBox();
        urlTextBox.setTitle("Image URL");
        urlTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelImageURL());
        add(urlTextBox);
    }
    
    public String getTagValue() {
        return tagTextBox.getValue();
    }
    
    public String getCommentValue() {
        return commentTextBox.getValue();
    }
    
    public String getImageURLValue() {
        return urlTextBox.getValue();
    }
}
