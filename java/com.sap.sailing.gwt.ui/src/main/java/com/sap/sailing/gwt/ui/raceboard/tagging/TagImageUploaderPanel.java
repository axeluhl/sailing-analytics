package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanelResources.TagPanelStyle;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

/**
 * Used to upload images of a {@link TagDTO tag}. Contains a hidden {@link FileUpload} inside a {@link FormPanel} which
 * gets triggered by the {@code browseAndUploadImageButton} and opens the browser specific file chooser. After a file
 * was chosen, the form gets submitted and so the the selected image gets uploaded. Submitting the form returns the new
 * URL of the file which is now placed inside the {@link TextBox imagePathTextBox}. If a User wants to use an image
 * already uploaded, he can just put this URL inside of the TextBox. Each time the value of this TextBox is changed the
 * image width and height are calculated which will be needed later for adding a resized image.
 * 
 * @author D067890
 */
public class TagImageUploaderPanel extends FlowPanel {

    private final TagPanelStyle style = TaggingPanelResources.INSTANCE.style();
    private final SailingServiceAsync sailingService;

    private final TextBox imagePathTextBox;

    private int imageWidth = -1;
    private int imageHeight = -1;

    public TagImageUploaderPanel(TaggingPanel taggingPanel, SailingServiceAsync sailingService,
            StringMessages stringMessages) {
        this.sailingService = sailingService;

        imagePathTextBox = new TextBox();
        imagePathTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelImageURL());
        imagePathTextBox.setStyleName(style.tagInputPanelImageTextBox());
        imagePathTextBox.addStyleName("gwt-TextBox");
        imagePathTextBox.addValueChangeHandler(event -> {
            calculateImageWidthAndHeight(imagePathTextBox.getText());
        });

        // the upload panel
        FormPanel uploadFormPanel = new FormPanel();
        uploadFormPanel.setStyleName(style.tagInputPanelImageFormPanel());
        add(uploadFormPanel);

        uploadFormPanel.setAction("/sailingserver/fileupload");
        uploadFormPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        uploadFormPanel.setMethod(FormPanel.METHOD_POST);
        FileUpload fileUploadField = new FileUpload();
        uploadFormPanel.add(fileUploadField);
        fileUploadField.setVisible(false);
        InputElement inputElement = fileUploadField.getElement().cast();
        inputElement.setName("file");
        fileUploadField.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (fileUploadField.getFilename() != null && !fileUploadField.getFilename().isEmpty()) {
                    uploadFormPanel.submit();
                }
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
                        calculateImageWidthAndHeight(
                                resultJson.get(0).isObject().get("file_uri").isString().stringValue());
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

        Button browseAndUploadImageButton = new Button(stringMessages.tagBrowseAndUploadImage());
        browseAndUploadImageButton.setStyleName(style.tagInputPanelImageButton());
        browseAndUploadImageButton.addStyleName("gwt-Button");
        browseAndUploadImageButton.addClickHandler(event -> {
            fileUploadField.click();
        });

        add(imagePathTextBox);
        add(uploadFormPanel);
        add(browseAndUploadImageButton);
    }

    private JSONValue parseAfterReplacingSurroundingPreElement(String jsonString) {
        return JSONParser.parseStrict(jsonString.replaceFirst("<pre[^>]*>(.*)</pre>", "$1"));
    }

    private void calculateImageWidthAndHeight(String imageURL) {
        if (imageURL == null || imageURL.isEmpty()) {
            imageWidth = -1;
            imageHeight = -1;
        } else {
            sailingService.resolveImageDimensions(imageURL, new AsyncCallback<Util.Pair<Integer, Integer>>() {
                @Override
                public void onSuccess(Pair<Integer, Integer> imageSize) {
                    if (imageSize != null) {
                        imageWidth = imageSize.getA();
                        imageHeight = imageSize.getB();
                    } else {
                        setImageURL("");
                        // no valid image?
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                }
            });
        }
    }

    public String getImageURL() {
        return imagePathTextBox.getText();
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

    private void setImageURL(String imageURL) {
        imagePathTextBox.setText(imageURL);
    }
}