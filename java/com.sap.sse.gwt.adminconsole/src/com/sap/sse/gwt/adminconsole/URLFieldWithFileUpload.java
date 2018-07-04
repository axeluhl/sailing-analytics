package com.sap.sse.gwt.adminconsole;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

/**
 * A {@link TextBox} together with an upload button that can use the file storage service to
 * save the file and produce a URL for it which is then taken over into the text field.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class URLFieldWithFileUpload extends Composite implements HasValue<String> {
    private final TextBox urlTextBox;
    
    private final FileUpload fileUploadField;
    
    private String uri;

    private final Button removeButton;

    private boolean valueChangeHandlerInitialized = false;
    
    public URLFieldWithFileUpload(final StringMessages stringMessages) {
        final VerticalPanel mainPanel = new VerticalPanel();
        final HorizontalPanel imageUrlPanel = new HorizontalPanel();
        mainPanel.add(new Label(stringMessages.pleaseOnlyUploadContentYouHaveAllUsageRightsFor()));
        mainPanel.add(imageUrlPanel);
        
        final FormPanel removePanel = new FormPanel();
        removePanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                final JSONObject resultJson = parseAfterReplacingSurroundingPreElement(event.getResults()).isObject();
                Notification.notify(stringMessages.removeResult(resultJson.get("status").isString().stringValue(),
                        resultJson.get("message") == null ? "" : resultJson.get("message").isString().stringValue()), NotificationType.INFO);
                setURL("");
            }
        });
        removePanel.setMethod(FormPanel.METHOD_POST);
        removeButton = new Button(stringMessages.removeUploadedFile());
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
        final SubmitButton submitButton = new SubmitButton(stringMessages.send());
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
                JSONArray resultJson = parseAfterReplacingSurroundingPreElement(result).isArray();
                if (resultJson != null) {
                    if (resultJson.get(0).isObject().get("file_uri") != null) {
                        uri = resultJson.get(0).isObject().get("file_uri").isString().stringValue();
                        setValue(uri, true);
                        removeButton.setEnabled(true);
                        Notification.notify(stringMessages.uploadSuccessful(), NotificationType.SUCCESS);
                    } else {
                        Notification.notify(stringMessages.fileUploadResult(resultJson.get(0).isObject().get("status").isString().stringValue(),
                                resultJson.get(0).isObject().get("message").isString().stringValue()), NotificationType.ERROR);
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
    
    public FocusWidget getInitialFocusWidget() {
        return urlTextBox;
    }

    private HandlerRegistration addChangeHandler(ChangeHandler handler) {
        return addDomHandler(handler, ChangeEvent.getType());
      }
    
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        if (!valueChangeHandlerInitialized) {
            valueChangeHandlerInitialized = true;
            addChangeHandler(new ChangeHandler() {
                public void onChange(ChangeEvent event) {
                    ValueChangeEvent.fire(URLFieldWithFileUpload.this, getValue());
                }
            });
        }
        return addHandler(handler, ValueChangeEvent.getType());
      }

    @Override
    public String getValue() {
        return getURL();
    }

    @Override
    public void setValue(String value) {
        setURL(value);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        setValue(value);
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    /**
     * See https://github.com/twilson63/ngUpload/issues/43 and
     * https://www.sencha.com/forum/showthread.php?132949-Fileupload-Invalid-JSON-string. The JSON response of the file
     * upload is wrapped by a &lt;pre&gt; element which needs to be stripped off if present to allow the JSON parser to
     * succeed.
     */
    private JSONValue parseAfterReplacingSurroundingPreElement(String jsonString) {
        return JSONParser.parseStrict(jsonString.replaceFirst("<pre[^>]*>(.*)</pre>", "$1"));
    }
}
