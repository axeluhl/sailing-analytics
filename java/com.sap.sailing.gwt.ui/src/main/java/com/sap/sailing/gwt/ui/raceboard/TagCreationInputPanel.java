package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class TagCreationInputPanel extends VerticalPanel{
    private TextBox tagTextBox, urlTextBox;
    private TextArea commentTextBox; 
    
    public TagCreationInputPanel(StringMessages stringMessages) {
        VerticalPanel mainPanel = new VerticalPanel();
        HorizontalPanel subPanel = new HorizontalPanel();
        
        tagTextBox = new TextBox();
        tagTextBox.setTitle(stringMessages.tagLabelTag());
        tagTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelTag());
        subPanel.add(tagTextBox);
        
        urlTextBox = new TextBox();
        urlTextBox.setTitle(stringMessages.tagLabelImageURL());
        urlTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelImageURL());
        subPanel.add(urlTextBox);
        
        mainPanel.add(subPanel);
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
}
