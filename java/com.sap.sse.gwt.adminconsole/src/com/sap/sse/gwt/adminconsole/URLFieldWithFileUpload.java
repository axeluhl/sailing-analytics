package com.sap.sse.gwt.adminconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.sap.sse.gwt.client.fileupload.FileUploadUtil;

/**
 * A {@link TextBox} together with an upload button that can use the file storage service to
 * save the file and produce a URL for it which is then taken over into the text field.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class URLFieldWithFileUpload extends Composite implements HasValue<Map<String, String>> {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private static final URLFieldWithFileUploadResources RESOURCES = URLFieldWithFileUploadResources.INSTANCE;
    private static final String UPLOAD_URL = "/sailingserver/fileupload";
    private static final String URI_DELIMITER = " ";

    private final TextBox urlTextBox;

    private final FileUpload fileUploadField;

    private Map<String, String> uriList;
    private String name;

    private boolean valueChangeHandlerInitialized = false;
    private StartUploadEvent startUploadEvent;
    private EndUploadEvent endUploadEvent;

    private final Button removeButton;
    private final FormPanel uploadFormPanel;
    private final FlowPanel uploadPanel;

    public URLFieldWithFileUpload(final StringMessages stringMessages, String acceptedFileTypes) {
        this(stringMessages, false, true, true, acceptedFileTypes);
    }

    public URLFieldWithFileUpload(final StringMessages stringMessages, boolean multiFileUpload, boolean initiallyEnableUpload, boolean showUrlAfterUpload, String acceptedFileTypes) {
        RESOURCES.urlFieldWithFileUploadStyle().ensureInjected();
        this.uriList = new LinkedHashMap<>();
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
        mainPanel.ensureDebugId("URLFieldWithFileUpload");
        final FlowPanel imageUrlPanel = new FlowPanel();
        imageUrlPanel.addStyleName(RESOURCES.urlFieldWithFileUploadStyle().spaceDirectChildrenClass());
        mainPanel.add(new Label(stringMessages.pleaseOnlyUploadContentYouHaveAllUsageRightsFor()));
        mainPanel.add(imageUrlPanel);
        removeButton = new Button();
        removeButton.setStyleName(RESOURCES.urlFieldWithFileUploadStyle().deleteButtonClass(), true);
        removeButton.addStyleName("btn-primary");
        removeButton.setEnabled(false); // the button shall only be enabled as long as we know the URI for removal
        removeButton.ensureDebugId("RemoveButton");
        removeButton.setTitle(stringMessages.resetToDefault());
        removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (String uri : uriList.keySet()) {
                    deleteFileOnServer(uri);
                }
                setUri(null, true);
                urlTextBox.setEnabled(true);
            }
        });
        urlTextBox = new TextBox();
        urlTextBox.ensureDebugId("urlTextBox");
        urlTextBox.getElement().addClassName("url-textbox");
        urlTextBox.addStyleName(RESOURCES.urlFieldWithFileUploadStyle().urlTextboxClass());
        urlTextBox.addValueChangeHandler(valueChangedEvent->{
            setUri(valueChangedEvent.getValue(), true);
        });
        imageUrlPanel.add(urlTextBox);
        final Button selectUploadButton = new Button();
        selectUploadButton.setStyleName(RESOURCES.urlFieldWithFileUploadStyle().uploadButtonClass(), true);
        selectUploadButton.addStyleName("btn-primary");
        imageUrlPanel.add(selectUploadButton);
        imageUrlPanel.add(removeButton);
        // the upload panel
        uploadFormPanel = new FormPanel();
        uploadPanel = new FlowPanel();
        uploadPanel.setStylePrimaryName(RESOURCES.urlFieldWithFileUploadStyle().spaceDirectChildrenClass());
        uploadFormPanel.setAction(UPLOAD_URL);
        uploadFormPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        uploadFormPanel.setMethod(FormPanel.METHOD_POST);
        fileUploadField = new FileUpload();
        if (multiFileUpload) {
            fileUploadField.getElement().setAttribute("multiple", "multiple");
        }
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
                for (String localUri : uriList.keySet()) {
                    if (localUri != null && !"".equals(localUri)) {
                        GWT.log("(1) remove uploaded file " + localUri);
                        deleteFileOnServer(localUri);
                    }
                }
                selectUploadButton.removeStyleName(RESOURCES.urlFieldWithFileUploadStyle().loadingClass());
                endUploadEvent.endUpload();
                JSONArray resultJson = JSONParser.parseStrict(FileUploadUtil.getApplicationJsonContent(event)).isArray();
                if (resultJson != null) {
                    Map<String, String> uris = new HashMap<>(resultJson.size());
                    List<String> titleStrings = new ArrayList<>(resultJson.size());
                    List<String> valueStrings = new ArrayList<>(resultJson.size());
                    for (int i = 0; i < resultJson.size(); i++) {
                        if (resultJson.get(i).isObject() != null) {
                            if (resultJson.get(i).isObject().get(FileUploadConstants.FILE_URI) != null) {
                                String uri = resultJson.get(i).isObject().get(FileUploadConstants.FILE_URI).isString().stringValue();
                                // special handling of double underscore in JSON. Double underscores were encoded with hex representation.
                                // In some cases the JSON parser of Apples Safari on mobile devices cannot parse JSON with __. See also bug5127
                                String fileNameUnderscoreEncoded = resultJson.get(i).isObject().get(FileUploadConstants.FILE_NAME).isString().stringValue();
                                String fileName = fileNameUnderscoreEncoded.replace("%5f%5f", "__");
                                uris.put(uri, fileName);
                                titleStrings.add(fileName);
                                if (showUrlAfterUpload) {
                                    valueStrings.add(uri);
                                } else {
                                    String textShown = "";
                                    int dotPos = fileName.lastIndexOf('.');
                                    if (dotPos >= 0) {
                                        String fileEnding = fileName.substring(dotPos + 1);
                                        textShown = fileEnding;
                                    }
                                    valueStrings.add(textShown);
                                }
                                name = getFileNameWithoutEnding(fileName); //TODO Only contains the last value
                            } else {
                                urlTextBox.setValue(stringMessages.error());
                                Notification.notify(stringMessages.fileUploadResult(resultJson.get(0).isObject().get(FileUploadConstants.STATUS).isString().stringValue(),
                                resultJson.get(0).isObject().get(FileUploadConstants.MESSAGE).isString().stringValue()), NotificationType.ERROR);
                            }
                        }
                    }
                    if (!showUrlAfterUpload) {
                        valueStrings.add("- " + stringMessages.uploadSuccessful());
                    }
                    setUris(uris, true);
                    urlTextBox.setTitle(String.join(" ", titleStrings));
                    urlTextBox.setValue(String.join(URI_DELIMITER, valueStrings));
                    urlTextBox.setEnabled(showUrlAfterUpload);
                    removeButton.setEnabled(true);
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

    public String getName() {
        return name;
    }

    public List<String> getUris() {
        return new ArrayList<>(uriList.keySet());
    }

    private void setUris(Map<String, String> uris, boolean fireEvent) {
        if (uris != null) {
            this.uriList = uris;
        } else {
            this.uriList.clear();
        }
        if (uris == null || uris.size() == 0) {
            urlTextBox.setValue("");
            urlTextBox.setTitle("");
        } else {
            urlTextBox.setValue(String.join(URI_DELIMITER, uriList.keySet()));
        }
        removeButton.setEnabled(uriList != null);
        if (fireEvent) {
            ValueChangeEvent.fire(this, uriList);
        }
    }

    /**
     * Returns <code>null</code> if the trimmed URI field contents are empty
     * enabled this method will only return the first URI.
     * @see {@link #getUris()}
     */
    public String getUri() {
        String result = null;
        if (uriList != null && uriList.size() > 0 && uriList.keySet().iterator().next() != null) {
            result = uriList.keySet().iterator().next().trim();
        }
        return result;
    }

    public void setUri(String uri) {
        setUri(uri, false);
    }

    public void setUri(String uri, boolean fireEvent) {
        this.uriList.clear();
        if (uri == null) {
            urlTextBox.setValue("");
            urlTextBox.setTitle("");
        } else {
            this.uriList.put(uri, extractFileName(uri));
            urlTextBox.setValue(uri);
        }
        removeButton.setEnabled(uri != null);
        if (fireEvent) {
            ValueChangeEvent.fire(this, uriList);
        }
    }
    
    private String extractFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public FocusWidget getInitialFocusWidget() {
        return urlTextBox;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Map<String, String>> handler) {
        if (!valueChangeHandlerInitialized) {
            valueChangeHandlerInitialized = true;
            addDomHandler(new ChangeHandler() {
                public void onChange(ChangeEvent event) {
                    ValueChangeEvent.fire(URLFieldWithFileUpload.this, getValue());
                }
            }, ChangeEvent.getType());
        }
        return addHandler(handler, ValueChangeEvent.getType());
      }

    @Override
    public Map<String, String> getValue() {
        return uriList;
    }

    @Override
    public void setValue(Map<String, String> value) {
        setUris(value, false);
    }

    @Override
    public void setValue(Map<String, String> value, boolean fireEvents) {
        setUris(value, fireEvents);
    }

    public void setStartUploadEvent(StartUploadEvent startUploadEvent) {
        this.startUploadEvent = startUploadEvent;
    }
    
    public void setEndUploadEvent(EndUploadEvent endUploadEvent) {
        this.endUploadEvent = endUploadEvent;
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
        for (final String localUri : uriList.keySet()) {
            deleteFileOnServer(localUri);
        }
    }

    private void deleteFileOnServer(String localUri) {
        if (localUri == null || "".equals(localUri)) {
            return;
        }
        // use request object as form elements of dialog are already destroyed
        RequestBuilder request = new RequestBuilder(RequestBuilder.DELETE, "/sailingserver/api/v1/file?uri=" + localUri);
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
