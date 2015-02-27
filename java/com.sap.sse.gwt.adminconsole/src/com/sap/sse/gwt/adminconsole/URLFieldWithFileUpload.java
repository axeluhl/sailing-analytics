package com.sap.sse.gwt.adminconsole;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A {@link TextBox} together with an upload button that can use the file storage service to
 * save the file and produce a URL for it which is then taken over into the text field.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class URLFieldWithFileUpload extends HorizontalPanel {
    private final TextBox urlTextBox;
    
    private final FormPanel formPanel;
    
    private final FileUpload fileUploadButton;
    
    public URLFieldWithFileUpload(StringMessages stringMessages, String fileUploadURL) {
        urlTextBox = new TextBox();
        add(urlTextBox);
        formPanel = new FormPanel(fileUploadURL);
        fileUploadButton = new FileUpload();
        final InputElement inputElement = fileUploadButton.getElement().cast();
        inputElement.setName(stringMessages.upload());
        add(formPanel);
        formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                String result = event.getResults();
                JSONObject resultJson = (JSONObject) JSONParser.parseLenient(result);
            }
        });
    }
}
