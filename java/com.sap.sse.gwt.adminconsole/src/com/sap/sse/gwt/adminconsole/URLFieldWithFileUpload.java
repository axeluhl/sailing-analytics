package com.sap.sse.gwt.adminconsole;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A {@link TextBox} together with an upload button that can use the file storage service to
 * save the file and produce a URL for it which is then taken over into the text field.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class URLFieldWithFileUpload extends Composite {
    private final TextBox urlTextBox;
    
    private final FileUpload fileUploadField;
    
    private String uri;

    private final PushButton removeButton;
    
    public URLFieldWithFileUpload(final StringMessages stringMessages) {
        final VerticalPanel mainPanel = new VerticalPanel();
        final HorizontalPanel imageUrlPanel = new HorizontalPanel(); 
        mainPanel.add(imageUrlPanel);
        
        final FormPanel removePanel = new FormPanel();
        removePanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                final JSONObject resultJson = JSONParser.parseLenient(event.getResults().replaceFirst("<pre[^>]*>(.*)</pre>", "$1")).isObject();
                Window.alert(stringMessages.removeResult(resultJson.get("status").isString().stringValue(),
                        resultJson.get("message") == null ? "" : resultJson.get("message").isString().stringValue()));
                setURL("");
            }
        });
        removePanel.setMethod(FormPanel.METHOD_POST);
        removeButton = new PushButton(new Image(com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon()));
        removeButton.setEnabled(false); // the button shall only be enabled as long as we know the URI for removal
        removeButton.ensureDebugId("RemoveButton");
        removeButton.setTitle(stringMessages.remove());
        removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removePanel.setAction("/sailingserver/api/v1/file?uri="+uri);
                removePanel.submit();
                uri = null;
            }
        });
        removePanel.add(removeButton);
        urlTextBox = new TextBox();
        urlTextBox.setWidth("400px");
        imageUrlPanel.add(urlTextBox);
        imageUrlPanel.add(removePanel);
        
        // the upload panel
        FormPanel uploadFormPanel = new FormPanel();
        mainPanel.add(uploadFormPanel);
        HorizontalPanel uploadPanel = new HorizontalPanel();
        uploadPanel.setSpacing(3);
        uploadFormPanel.add(uploadPanel);
        uploadFormPanel.setAction("/sailingserver/fileupload");
        uploadFormPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        uploadFormPanel.setMethod(FormPanel.METHOD_POST);
        fileUploadField = new FileUpload();
        final Label uploadLabel = new Label(stringMessages.upload()+ ":");
        uploadPanel.add(uploadLabel);
        uploadPanel.setCellVerticalAlignment(uploadLabel, HasVerticalAlignment.ALIGN_MIDDLE);
        uploadPanel.add(fileUploadField);
        final InputElement inputElement = fileUploadField.getElement().cast();
        inputElement.setName("file");
        final SubmitButton submitButton = new SubmitButton("Send...");
        submitButton.setEnabled(false);
        fileUploadField.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if(fileUploadField.getFilename() != null && !fileUploadField.getFilename().isEmpty()) {
                    submitButton.setEnabled(true);
                }
            }
        });
        uploadPanel.add(submitButton);
        uploadFormPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                String result = event.getResults();
                JSONArray resultJson = (JSONArray) JSONParser.parseLenient(result.replaceFirst("<pre[^>]*>(.*)</pre>", "$1"));
                if (resultJson != null) {
                    if (resultJson.get(0).isObject().get("file_uri") != null) {
                        uri = resultJson.get(0).isObject().get("file_uri").isString().stringValue();
                        urlTextBox.setValue(uri);
                        removeButton.setEnabled(true);
                        Window.alert(stringMessages.uploadSuccessful());
                    } else {
                        Window.alert(stringMessages.fileUploadResult(resultJson.get(0).isObject().get("status").isString().stringValue(),
                                resultJson.get(0).isObject().get("message").isString().stringValue()));
                    }
                }
            }
        });
        initWidget(mainPanel);
    }
    
    /**
     * Returns <code>null</code> if the trimmed URL field contents are empty
     */
    public String getURL() {
        final String trimmedUrl = urlTextBox.getValue().trim();
        return trimmedUrl.isEmpty() ? null : trimmedUrl;
    }

    public void setURL(String imageURL) {
        if (imageURL == null) {
            urlTextBox.setValue("");
            uri = null;
            removeButton.setEnabled(false);
        } else {
            urlTextBox.setValue(imageURL);
            uri = imageURL;
            removeButton.setEnabled(true);
        }
    }
}
