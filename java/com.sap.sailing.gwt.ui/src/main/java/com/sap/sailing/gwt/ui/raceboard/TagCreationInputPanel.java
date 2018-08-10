package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class TagCreationInputPanel extends VerticalPanel{
    private TextBox tagTextBox, urlTextBox;
    private TextArea commentTextBox; 
    
    public TagCreationInputPanel(StringMessages stringMessages) {
        VerticalPanel mainPanel = new VerticalPanel();
        
        tagTextBox = new TextBox();
        tagTextBox.setWidth("100%");
        tagTextBox.setTitle(stringMessages.tagLabelTag());
        tagTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelTag());
        mainPanel.add(tagTextBox);
        
        urlTextBox = new TextBox();
        urlTextBox.setWidth("100%");
        urlTextBox.setTitle(stringMessages.tagLabelImageURL());
        urlTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelImageURL());
        mainPanel.add(urlTextBox);
        
        //resize must be set to none by css
        commentTextBox = new TextArea();
        commentTextBox.setWidth("100%");
        commentTextBox.setVisibleLines(4);
        commentTextBox.setTitle(stringMessages.tagLabelComment());
        commentTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelComment());
        mainPanel.add(commentTextBox);
        add(mainPanel);
        
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
    
    public void setTagValue(String tag) {
        tagTextBox.setValue(tag);
    }
    
    public void setCommentValue(String comment) {
        commentTextBox.setValue(comment);
    }
    
    public void setImageURLValue(String imageURL) {
        urlTextBox.setValue(imageURL);
    }
    
    public void clearAllValues() {
        tagTextBox.setText("");
        urlTextBox.setText("");
        commentTextBox.setText("");
    }
}
