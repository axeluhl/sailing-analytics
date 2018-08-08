package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TagCreationInputPanel extends VerticalPanel{
    private TextBox tagTextBox, commentTextBox, urlTextBox;
    
    public TagCreationInputPanel() {
        tagTextBox = new TextBox();
        tagTextBox.setTitle("Tag");
        tagTextBox.setText("Tag");
        add(tagTextBox);
        
        commentTextBox = new TextBox();
        commentTextBox.setTitle("Comment");
        commentTextBox.setText("Comment");
        add(commentTextBox);
        
        urlTextBox = new TextBox();
        urlTextBox.setTitle("Image URL");
        urlTextBox.setText("Image URL");
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
