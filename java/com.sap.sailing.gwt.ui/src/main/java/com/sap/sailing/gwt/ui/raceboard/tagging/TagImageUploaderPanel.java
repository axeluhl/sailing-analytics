package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

/**
 * Used to upload images of a {@link TagDTO tag}
 * 
 * @author D067890
 */
public class TagImageUploaderPanel extends FlowPanel {

    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();
    private final SailingServiceAsync sailingService;

    private final TextBox imagePathTextBox;
    private final FormPanel uploadFormPanel;
    private final FlowPanel uploadPanel;
    private final FileUpload fileUploadField;
    private final Button browseImageButton;
    private final Button removeImageButton;

    private String imageURL;
    private int imageWidth = -1;
    private int imageHeight = -1;

    public TagImageUploaderPanel(TaggingPanel taggingPanel, SailingServiceAsync sailingService) {
        final StringMessages stringMessages = taggingPanel.getStringMessages();
        this.sailingService = sailingService;

        imagePathTextBox = new TextBox();
        imagePathTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelImageURL());
        imagePathTextBox.setStyleName(style.tagInputPanelImageTextBox());
        imagePathTextBox.addStyleName("gwt-TextBox");
        imagePathTextBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (imagePathTextBox.getText().length() > 0) {
                    // submitButton.setVisible(true);
                } else {
                    // submitButton.setVisible(false);
                }
            }
        });

        // the upload panel
        uploadFormPanel = new FormPanel();
        uploadFormPanel.setStyleName(style.tagInputPanelImageFormPanel());
        add(uploadFormPanel);
        uploadPanel = new FlowPanel();

        uploadFormPanel.add(uploadPanel);
        uploadFormPanel.setAction("/sailingserver/fileupload");
        uploadFormPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        uploadFormPanel.setMethod(FormPanel.METHOD_POST);
        fileUploadField = new FileUpload();
        fileUploadField.setVisible(false);
        final InputElement inputElement = fileUploadField.getElement().cast();
        inputElement.setName("file");
        final SubmitButton submitButton = new SubmitButton(stringMessages.tagSendImage());
        submitButton.setStyleName(style.tagInputPanelImageButton());
        submitButton.addStyleName("gwt-Button");
        submitButton.setEnabled(false);
        fileUploadField.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (fileUploadField.getFilename() != null && !fileUploadField.getFilename().isEmpty()) {
                    submitButton.setEnabled(true);
                    imagePathTextBox.setText(fileUploadField.getFilename());
                }
            }
        });
        uploadPanel.add(submitButton);
        uploadFormPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
            public void onSubmit(SubmitEvent event) {
                // This event is fired just before the form is submitted. We can take
                // this opportunity to perform validation.
                Window.alert(event.toString());
            }
        });
        uploadFormPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                String result = event.getResults();
                JSONArray resultJson = parseAfterReplacingSurroundingPreElement(result).isArray();
                if (resultJson != null) {
                    if (resultJson.get(0).isObject().get("file_uri") != null) {
                        setImageURL(resultJson.get(0).isObject().get("file_uri").isString().stringValue());
                        Notification.notify(stringMessages.uploadSuccessful(), NotificationType.SUCCESS);
                    } else {
                        Notification.notify(
                                stringMessages.fileUploadResult(
                                        resultJson.get(0).isObject().get("status").isString().stringValue(),
                                        resultJson.get(0).isObject().get("message").isString().stringValue()),
                                NotificationType.ERROR);
                    }
                }
            }
        });

        browseImageButton = new Button(stringMessages.tagBrowseImage());
        browseImageButton.setStyleName(style.tagInputPanelImageButton());
        browseImageButton.addStyleName("gwt-Button");
        browseImageButton.addClickHandler(event -> {
            fileUploadField.click();
        });

        removeImageButton = new Button(stringMessages.tagRemoveUploadedImage());
        removeImageButton.setStyleName(style.tagInputPanelImageButton());
        removeImageButton.addStyleName("gwt-Button");
        removeImageButton.addClickHandler(event -> {
            clearImageURL();
        });

        add(imagePathTextBox);
        add(uploadFormPanel);
        add(browseImageButton);
        add(removeImageButton);
        removeImageButton.setVisible(false);
    }

    private JSONValue parseAfterReplacingSurroundingPreElement(String jsonString) {
        return JSONParser.parseStrict(jsonString.replaceFirst("<pre[^>]*>(.*)</pre>", "$1"));
    }

    private void setImageURL(String url) {
        imageURL = url;
        if (imageURL == null || imageURL.isEmpty()) {
            imageWidth = -1;
            imageHeight = -1;
            clearImageURL();
        } else {
            imagePathTextBox.setText(url);
            imagePathTextBox.setEnabled(false);
            browseImageButton.setVisible(false);
            uploadFormPanel.setVisible(false);
            removeImageButton.setVisible(true);
            sailingService.resolveImageDimensions(imageURL, new AsyncCallback<Util.Pair<Integer, Integer>>() {
                @Override
                public void onSuccess(Pair<Integer, Integer> imageSize) {
                    if (imageSize != null) {
                        imageWidth = imageSize.getA();
                        imageHeight = imageSize.getB();
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                }
            });
        }
    }

    private void clearImageURL() {
        imageURL = "";
        imageWidth = -1;
        imageHeight = -1;
        imagePathTextBox.setText(imageURL);
        imagePathTextBox.setEnabled(true);
        browseImageButton.setVisible(true);
        uploadFormPanel.setVisible(true);
        removeImageButton.setVisible(false);
    }

    public String getImageURL() {
        return imageURL;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setValue(String imageURL) {
        setImageURL(imageURL);
    }
}