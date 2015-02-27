package com.sap.sse.gwt.adminconsole;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SubmitButton;
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
    
    private final FileUpload fileUploadField;
    
    private String uri;

    private final PushButton removeButton;
    
    public URLFieldWithFileUpload(final StringMessages stringMessages) {
        final FormPanel removePanel = new FormPanel();
        removePanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                final JSONObject result = (JSONObject) JSONParser.parseLenient(event.getResults());
                Window.alert(stringMessages.removeResult(result.get("status"), result.get("message")));
            }
        });
        removePanel.setMethod("DELETE");
        removeButton = new PushButton(new Image(com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon()));
        removeButton.setEnabled(false); // the button shall only be enabled as long as we know the URI for removal
        removeButton.ensureDebugId("RemoveButton");
        removeButton.setTitle(stringMessages.remove());
        removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removePanel.setAction("/sailingserver/api/v1/file?uri="+uri);
                removePanel.submit();
            }
        });
        urlTextBox = new TextBox();
        add(urlTextBox);
        FormPanel formPanel = new FormPanel();
        formPanel.setAction("/sailingserver/api/v1/file");
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        formPanel.setMethod(FormPanel.METHOD_POST);
        fileUploadField = new FileUpload();
        final InputElement inputElement = fileUploadField.getElement().cast();
        inputElement.setName("file");
        SubmitButton submitButton = new SubmitButton(stringMessages.upload());
        formPanel.add(submitButton);
        add(formPanel);
        formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                String result = event.getResults();
                JSONObject resultJson = (JSONObject) JSONParser.parseLenient(result);
                if (resultJson != null && resultJson.get("file_uri") != null) {
                    uri = resultJson.get("file_uri").toString();
                    enableDelete();
                    Window.alert(stringMessages.uploadSuccessful());
                }
            }
        });
    }

    private void enableDelete() {
        removeButton.setEnabled(true);
    }
}
