package com.sap.sse.gwt.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
public class URLFieldWithFileUpload extends Composite implements HasValue<List<String>> {
    private static final URLFieldWithFileUploadResources RESOURCES = URLFieldWithFileUploadResources.INSTANCE;
    private static final String URI_DELIMITER = " ";
    private static final String URI_DELIMITER_REGEX = "\\s+";

    private final TextBox urlTextBox;

    private final FileUpload fileUploadField;

    private final List<String> uriList;
    private boolean multiFileUpload;

    private final Button removeButton;

    private boolean valueChangeHandlerInitialized = false;

    private final FormPanel uploadFormPanel;

    private final FlowPanel uploadPanel;

    public URLFieldWithFileUpload(final StringMessages stringMessages) {
        this(stringMessages, true, false);
    }

    public URLFieldWithFileUpload(final StringMessages stringMessages, boolean initiallyEnableUpload,
            boolean multiFileUpload) {
        RESOURCES.urlFieldWithFileUploadStyle().ensureInjected();
        this.uriList = new ArrayList<>();
        this.multiFileUpload = multiFileUpload;
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
                final JSONObject resultJson = parseAfterReplacingSurroundingPreElement(event.getResults()).isObject();
                Notification.notify(stringMessages.removeResult(resultJson.get(FileUploadConstants.STATUS).isString().stringValue(),
                        resultJson.get(FileUploadConstants.MESSAGE) == null ? "" : resultJson.get(FileUploadConstants.MESSAGE).isString().stringValue()), NotificationType.INFO);
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
                for (String uri : uriList) {
                    removePanel.setAction("/sailingserver/api/v1/file?uri="+uri);
                    removePanel.submit();
                }
                uriList.clear();
            }
        });
        removePanel.add(removeButton);
        urlTextBox = new TextBox();
        urlTextBox.getElement().addClassName("url-textbox");
        urlTextBox.setWidth("400px");
        imageUrlPanel.add(urlTextBox);
        imageUrlPanel.add(removePanel);

        // the upload panel
        uploadFormPanel = new FormPanel();
        mainPanel.add(uploadFormPanel);
        uploadPanel = new FlowPanel();
        uploadPanel.setStylePrimaryName(RESOURCES.urlFieldWithFileUploadStyle().spaceDirectChildrenClass());
        if(initiallyEnableUpload) {
            uploadFormPanel.add(uploadPanel);
        }
        uploadFormPanel.setAction("/sailingserver/fileupload");
        uploadFormPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        uploadFormPanel.setMethod(FormPanel.METHOD_POST);
        fileUploadField = new FileUpload();
        if (multiFileUpload) {
            fileUploadField.getElement().setAttribute("multiple", "multiple");
        }
        final Label uploadLabel = new Label(stringMessages.upload()+ ":");
        uploadLabel.setStylePrimaryName(RESOURCES.urlFieldWithFileUploadStyle().inlineClass());
        uploadPanel.add(uploadLabel);
        uploadPanel.add(fileUploadField);
        final InputElement inputElement = fileUploadField.getElement().cast();
        inputElement.setName("file");
        final SubmitButton submitButton = new SubmitButton(stringMessages.send());
        submitButton.setEnabled(false);
        fileUploadField.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (fileUploadField.getFilename() != null && !fileUploadField.getFilename().isEmpty()) {
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
                    List<String> uris = new ArrayList<>(resultJson.size());
                    for (int i = 0; i < resultJson.size(); i++) {
                        if (resultJson.get(i).isObject() != null) {
                            if (resultJson.get(i).isObject().get(FileUploadConstants.FILE_URI) != null) {
                                uris.add(resultJson.get(i).isObject().get(FileUploadConstants.FILE_URI).isString()
                                        .stringValue());
                            } else {
                                Notification.notify(stringMessages.fileUploadResult(
                                        resultJson.get(i).isObject().get(FileUploadConstants.STATUS).isString()
                                                .stringValue(),
                                        resultJson.get(i).isObject().get(FileUploadConstants.MESSAGE).isString()
                                                .stringValue()),
                                        NotificationType.ERROR);
                            }
                        }
                    }
                    setValue(uris, true);
                    if (!uris.isEmpty()) {
                        removeButton.setEnabled(true);
                        Notification.notify(stringMessages.uploadSuccessful(), NotificationType.SUCCESS);
                    }
                }
            }
        });
        initWidget(mainPanel);
    }
    
    /**
     * @return <code>null</code> if the trimmed URL field contents are empty. Even if {@link #multiFileUpload} is
     * enabled this method will only return the first URL.
     * @see {@link #getURLs()}
     */
    public String getURL() {
        final List<String> urls = getURLs();
        return urls.isEmpty() ? null : urls.get(0);
    }

    /**
     * @return an empty List or a List with the first URL if {@link #multiFileUpload} is disabled. Otherwise an empty List
     * or a List of all URLs in the URL field.
     */
    public List<String> getURLs() {
        String[] urls = urlTextBox.getValue().trim().split(URI_DELIMITER_REGEX);
        if (urls.length == 0) {
            return Collections.emptyList();
        }
        if (multiFileUpload) {
            return Arrays.asList(urls);
        } else {
            return Arrays.asList(urls[0]);
        }
    }

    /**
     * @see {@link #setURLs(List)}
     */
    public void setURL(String imageURL) {
        uriList.clear();
        if (imageURL == null) {
            urlTextBox.setValue("");
            removeButton.setEnabled(false);
        } else {
            urlTextBox.setValue(imageURL);
            uriList.add(imageURL);
            removeButton.setEnabled(true);
        }
    }

    public void setURLs(List<String> imageURLs) {
        uriList.clear();
        if (imageURLs == null || imageURLs.isEmpty()) {
            urlTextBox.setValue("");
            removeButton.setEnabled(false);
        } else {
            urlTextBox.setValue(String.join(URI_DELIMITER, imageURLs));
            uriList.addAll(imageURLs);
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
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<String>> handler) {
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
    public List<String> getValue() {
        return getURLs();
    }

    @Override
    public void setValue(List<String> value) {
        setURLs(value);
    }

    @Override
    public void setValue(List<String> value, boolean fireEvents) {
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
    
    public void setUploadEnabled(boolean uploadEnabled) {
        if (uploadEnabled) {
            uploadFormPanel.add(uploadPanel);
        } else {
            uploadFormPanel.remove(uploadPanel);
        }
    }
}
