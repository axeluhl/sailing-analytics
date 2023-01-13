package com.sap.sse.gwt.adminconsole;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.common.fileupload.FileUploadConstants;
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
    
    private final Logger logger = Logger.getLogger(getClass().getName());
    private static final URLFieldWithFileUploadResources RESOURCES = URLFieldWithFileUploadResources.INSTANCE;
    private static final String UPLOAD_URL = "/sailingserver/fileupload";

    private final TextBox urlTextBox;
    
    private final FileUpload fileUploadField;
    
    private String uri;
    private String name;

    private boolean valueChangeHandlerInitialized = false;
    private StartUploadEvent startUploadEvent;
    private EndUploadEvent endUploadEvent;
    
    private final Button removeButton;
    private final FormPanel uploadFormPanel;
    private final FlowPanel uploadPanel;
    
    public URLFieldWithFileUpload(final StringMessages stringMessages, String acceptedFileTypes) {
        this(stringMessages, true, true, acceptedFileTypes);
    }
   
    public URLFieldWithFileUpload(final StringMessages stringMessages, boolean initiallyEnableUpload, boolean showUrlAfterUpload, String acceptedFileTypes) {
        RESOURCES.urlFieldWithFileUploadStyle().ensureInjected();
        startUploadEvent = new StartUploadEvent() {
            @Override
            public void startUpload() {
                logger.log(Level.FINE, "start upload of file.");
            }
        };
        endUploadEvent = new EndUploadEvent() {
            @Override
            public void endUpload() {
                logger.log(Level.FINE, "end upload of file,");
            }
        };
        final VerticalPanel mainPanel = new VerticalPanel();
        final FlowPanel imageUrlPanel = new FlowPanel();
        imageUrlPanel.addStyleName(RESOURCES.urlFieldWithFileUploadStyle().spaceDirectChildrenClass());
        mainPanel.add(new Label(stringMessages.pleaseOnlyUploadContentYouHaveAllUsageRightsFor()));
        mainPanel.add(imageUrlPanel);
        final FormPanel removePanel = new FormPanel();
        removePanel.addStyleName(RESOURCES.urlFieldWithFileUploadStyle().inlineClass());
        removePanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                setUriAndFireEvent(null);
            }
        });
        removePanel.setMethod(FormPanel.METHOD_POST);
        removeButton = new Button();
        removeButton.setStyleName(RESOURCES.urlFieldWithFileUploadStyle().deleteButtonClass(), true);
        removeButton.addStyleName("btn-primary");
        removeButton.setEnabled(false); // the button shall only be enabled as long as we know the URI for removal
        removeButton.ensureDebugId("RemoveButton");
        removeButton.setTitle(stringMessages.resetToDefault());
        removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removePanel.setAction("/sailingserver/api/v1/file?uri=" + uri);
                removePanel.submit();
                setUriAndFireEvent(null);
                urlTextBox.setEnabled(true);
            }
        });
        removePanel.add(removeButton);
        urlTextBox = new TextBox();
        urlTextBox.getElement().addClassName("url-textbox");
        urlTextBox.addStyleName(RESOURCES.urlFieldWithFileUploadStyle().urlTextboxClass());
        urlTextBox.addValueChangeHandler(valueChangedEvent->{
            setUriAndFireEvent(valueChangedEvent.getValue());
        });
        imageUrlPanel.add(urlTextBox);
        final Button selectUploadButton = new Button();
        selectUploadButton.setStyleName(RESOURCES.urlFieldWithFileUploadStyle().uploadButtonClass(), true);
        selectUploadButton.addStyleName("btn-primary");
        imageUrlPanel.add(selectUploadButton);
        imageUrlPanel.add(removePanel);
        // the upload panel
        uploadFormPanel = new FormPanel();
        uploadPanel = new FlowPanel();
        uploadPanel.setStylePrimaryName(RESOURCES.urlFieldWithFileUploadStyle().spaceDirectChildrenClass());
        uploadFormPanel.setAction(UPLOAD_URL);
        uploadFormPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        uploadFormPanel.setMethod(FormPanel.METHOD_POST);
        fileUploadField = new FileUpload();
        if (acceptedFileTypes != null) {
            fileUploadField.getElement().setAttribute("accept", acceptedFileTypes);
        }
        fileUploadField.setStylePrimaryName(RESOURCES.urlFieldWithFileUploadStyle().fileInputClass());
        final InputElement inputElement = fileUploadField.getElement().cast();
        inputElement.setName("file");
        uploadPanel.add(fileUploadField);
        selectUploadButton.addClickHandler(click -> {
            fileUploadField.click();
        });        
        // the hidden submit button for uploading the file
        final SubmitButton submitButton = new SubmitButton(stringMessages.send());
        submitButton.setVisible(false);
        submitButton.setEnabled(false);
        
        fileUploadField.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (fileUploadField.getFilename() != null && !fileUploadField.getFilename().isEmpty()) {
                    submitButton.setEnabled(true);
                    submitButton.click();
                }
            }
        });
        uploadPanel.add(submitButton);
        uploadFormPanel.addSubmitHandler(submitEvent-> {
            startUploadEvent.startUpload();
            urlTextBox.setValue(stringMessages.upload());
            selectUploadButton.addStyleName(RESOURCES.urlFieldWithFileUploadStyle().loadingClass());
        });
        uploadFormPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                String localUri = uri;
                if (localUri != null && !"".equals(localUri)) {
                    GWT.log("(1) remove uploaded file " + localUri);
                    deleteFileOnServer(localUri);
                }
                selectUploadButton.removeStyleName(RESOURCES.urlFieldWithFileUploadStyle().loadingClass());
                endUploadEvent.endUpload();
                String result = event.getResults();
                JSONArray resultJson = parseAfterReplacingSurroundingPreElement(result).isArray();
                if (resultJson != null) {
                    if (resultJson.get(0).isObject().get(FileUploadConstants.FILE_URI) != null) {
                        String uri = resultJson.get(0).isObject().get(FileUploadConstants.FILE_URI).isString().stringValue();
                        setUriAndFireEvent(uri);
                        String fileName = resultJson.get(0).isObject().get(FileUploadConstants.FILE_NAME).isString().stringValue();
                        if (showUrlAfterUpload) {
                            urlTextBox.setValue(uri);
                            urlTextBox.setEnabled(true);
                        } else {
                            urlTextBox.setEnabled(false);
                            String textShown = "";
                            int dotPos = fileName.lastIndexOf('.');
                            if (dotPos >= 0) {
                                String fileEnding = fileName.substring(dotPos + 1);
                                textShown = fileEnding + " - ";
                            } 
                            name = fileName.substring(dotPos + 1);
                            textShown += stringMessages.uploadSuccessful();
                            urlTextBox.setValue(textShown);
                        }
                        urlTextBox.setTitle(fileName);
                        name = getFileNameWithoutEnding(fileName);
                        fireResultUri(uri);
                        removeButton.setEnabled(true);
                    } else {
                        urlTextBox.setValue(stringMessages.error());
                        Notification.notify(stringMessages.fileUploadResult(resultJson.get(0).isObject().get(FileUploadConstants.STATUS).isString().stringValue(),
                                resultJson.get(0).isObject().get(FileUploadConstants.MESSAGE).isString().stringValue()), NotificationType.ERROR);
                    }
                } else {
                    urlTextBox.setValue(stringMessages.error());
                }
            }
        });
        if (initiallyEnableUpload) {
            uploadFormPanel.add(uploadPanel);
        }
        mainPanel.add(uploadFormPanel);
        initWidget(mainPanel);
    }
    
    private String getFileNameWithoutEnding(String fileName) {
        String result = fileName;
        int dotPos = fileName.lastIndexOf('.');
        if (dotPos >= 0) {
            String fileEnding = fileName.substring(dotPos);
            result = fileName.replace(fileEnding, "");
        } 
        result = result.replaceAll("(?=[-_][^\\\\S])(\\-)", " ");
        return result;
    }
    
    private void fireResultUri(String uri) {
        this.uri = uri;
        ValueChangeEvent.fire(this, uri);
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Returns <code>null</code> if the trimmed URL field contents are empty
     */
    public String getURL() {
        final String trimmedUrl = (uri != null ? uri : "").trim();
        return trimmedUrl.isEmpty() ? null : trimmedUrl;
    }

    public void setURL(String imageURL) {
        if (imageURL == null) {
            urlTextBox.setValue("");
            urlTextBox.setTitle("");
            uri = null;
            removeButton.setEnabled(false);
        } else {
            urlTextBox.setValue(imageURL);
            uri = imageURL;
            removeButton.setEnabled(true);
        }
    }
    
    private void setUriAndFireEvent(String uri) {
        this.uri = uri;
        if (uri == null) {
            urlTextBox.setValue("");
            urlTextBox.setTitle("");
        }
        removeButton.setEnabled(uri != null);
        ValueChangeEvent.fire(this, uri);
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
        return this.uri;
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
    
    public void setStartUploadEvent(StartUploadEvent startUploadEvent) {
        this.startUploadEvent = startUploadEvent;
    }
    
    public void setEndUploadEvent(EndUploadEvent endUploadEvent) {
        this.endUploadEvent = endUploadEvent;
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
    
    public void setUploadEnabled(boolean uploadEnabled) {
        uploadFormPanel.remove(uploadPanel);
        if (uploadEnabled) {
            uploadFormPanel.add(uploadPanel);
        } else {
            uploadFormPanel.remove(uploadPanel);
        }
    }

    public void fireClickToFileUploadField() {
        this.fileUploadField.click();
    }

    public void deleteCurrentFile() {
        final String localUri = uri;
        deleteFileOnServer(localUri);
    }

    private void deleteFileOnServer(String localUri) {
        if (localUri == null || "".equals(localUri)) {
            return;
        }
        // use request object as form elements of dialog are already destroyed
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, "/sailingserver/api/v1/file?uri=" + localUri);
        try {
            request.sendRequest(null, new RequestCallback() {

                @Override
                public void onResponseReceived(Request request, Response response) {
                    GWT.log("successfully deleted " + localUri + " " + response.getText());
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    GWT.log("delete failed" + localUri, exception);
                }
            });
        } catch (RequestException e) {
            GWT.log("request exception when deleting file" + localUri, e);
        }
    }
}
