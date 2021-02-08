package com.sap.sailing.gwt.ui.shared;

import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.home.shared.SharedHomeResources;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingServiceWrite;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.fileupload.FileUploadConstants;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

public abstract class AbstractMediaUploadPopup extends DialogBox {
    
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    
    private final static String UPLOAD_URL = "/sailingserver/fileupload";
    private final static String DELETE_URL = "/sailingserver/api/v1/file?uri=";
    private final static String STATUS_OK = "OK";
    private final static String STATUS_NOT_OK = "NOK";
    private final static String EMPTY_MESSAGE = "-";
    protected final StringMessages i18n = StringMessages.INSTANCE;
    protected final SharedHomeResources sharedHomeResources = SharedHomeResources.INSTANCE;

    protected final FileUpload upload;
    protected final FlowPanel content;
    protected final TextBox fileNameInput;
    private final FlowPanel fileExistingPanel;
    private final Button saveButton;
    private final SailingServiceWriteAsync sailingService;
    private String uri;
    
    public AbstractMediaUploadPopup() {
        sharedHomeResources.sharedHomeCss().ensureInjected();
        addStyleName(sharedHomeResources.sharedHomeCss().popup());
        setTitle(i18n.upload());
        setText(i18n.upload().toUpperCase());
        setGlassEnabled(true);
        setAnimationEnabled(true);
        
        // init media service
        sailingService = GWT.create(SailingServiceWrite.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath, HEADER_FORWARD_TO_MASTER);
        
        //sailingService.updateEvent(eventId, eventName, eventDescription, startDate, endDate, venue, isPublic, leaderboardGroupIds, officialWebsiteURL, baseURL, sailorsInfoWebsiteURLsByLocaleName, images, videos, windFinderReviewedSpotCollectionIds, callback);
        
        upload = new FileUpload();
        upload.setVisible(false);
        upload.setName("file");
        this.setModal(false);
        
        content = new FlowPanel();
        fileNameInput = new TextBox();
        
        // Upload form
        final FormPanel uploadForm = new FormPanel();
        // Because we're going to add a FileUpload widget, we'll need to set the
        // form to use the POST method, and multipart MIME encoding.
        uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        uploadForm.setMethod(FormPanel.METHOD_POST);

        uploadForm.setAction(UPLOAD_URL);
        uploadForm.add(upload);
        // Add an event handler to the form.
        uploadForm.addSubmitHandler(new SubmitHandler());
        uploadForm.addSubmitCompleteHandler(new SubmitCompleteHandler());
        content.add(uploadForm);

        final FormPanel metaDataForm = new FormPanel();

        VerticalPanel metaDataPanel = new VerticalPanel();
        metaDataForm.add(metaDataPanel);
        
        Label fileNameLabel = new Label(i18n.name());
        fileNameLabel.addStyleName(sharedHomeResources.sharedHomeCss().label());
        metaDataPanel.add(fileNameLabel);

        FlowPanel fileInputGroup = new FlowPanel();
        fileInputGroup.addStyleName(sharedHomeResources.sharedHomeCss().inputGroup());
        final Button uploadButton = new Button();
        uploadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                upload.click();
            }
        });
        uploadButton.addStyleName(sharedHomeResources.sharedHomeCss().uploadButton());
        // Create a TextBox, giving it a name so that it will be submitted.
        fileNameInput.setName("textBoxFormElement");
        fileNameInput.addStyleName(sharedHomeResources.sharedHomeCss().input());
        fileNameInput.setEnabled(false);
        
        fileInputGroup.add(fileNameInput);
        fileInputGroup.add(uploadButton);
        metaDataPanel.add(fileInputGroup);
        
        fileExistingPanel = new FlowPanel();
        // TODO: i18n
        fileExistingPanel.add(new Label("-- no media selected --"));
        metaDataPanel.add(fileExistingPanel);
        
        FlowPanel buttonGroup = new FlowPanel();
        buttonGroup.addStyleName(sharedHomeResources.sharedHomeCss().buttonGroup());
        buttonGroup.addStyleName(sharedHomeResources.sharedHomeCss().right());
        metaDataPanel.add(buttonGroup);
        // Add a 'submit' button.
        Button cancelButton = new Button(i18n.cancel(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                fileNameInput.setValue("");
                cleanupTempFileUpload();
                hide();
            }
        });
        buttonGroup.add(cancelButton);
        // Add a 'submit' button.
        saveButton = new Button(i18n.save(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                metaDataForm.submit();
            }
        });
        saveButton.addStyleName(sharedHomeResources.sharedHomeCss().primary());
        saveButton.setEnabled(false);
        buttonGroup.add(saveButton);
        metaDataForm.addSubmitHandler(new FormPanel.SubmitHandler() {
            
            @Override
            public void onSubmit(SubmitEvent event) {
                fileNameInput.setValue("");
                addMediaTrack();
                hide();
            }
        });
        content.add(metaDataForm);
        

        upload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                cleanupTempFileUpload();
                uploadForm.submit();
            }
        });
        
        add(content);
        
    }
    
    protected class SubmitHandler implements FormPanel.SubmitHandler {

        @Override
        public void onSubmit(SubmitEvent event) {
            // This event is fired just before the form is submitted. We can take
            // this opportunity to perform validation.
            if (!upload.getFilename().toLowerCase().endsWith(".png") && !upload.getFilename().endsWith(".svg")
                    && !upload.getFilename().endsWith(".tiff") && !upload.getFilename().endsWith(".jpg")
                    && !upload.getFilename().endsWith(".jpeg")) {
                logger.log(Level.SEVERE, "File type is not supported. Supported file types are PNG, SVG, JPG, JPEG and TIFF.");
                Notification.notify("File type is not supported. Supported file types are PNG, SVG, JPG, JPEG and TIFF.", NotificationType.WARNING);
                event.cancel();
            }
        }
        
    }
    
    private class SubmitCompleteHandler implements FormPanel.SubmitCompleteHandler {

        @Override
        public void onSubmitComplete(SubmitCompleteEvent event) {
            // When the form submission is successfully completed, this event is
            // fired. Assuming the service returned a response of type text/html,
            // we can get the result text here (see the FormPanel documentation for
            // further explanation).
            String result = event.getResults();
            JSONArray resultJson = parseAfterReplacingSurroundingPreElement(result).isArray();
            if (resultJson != null) {
                if (resultJson.get(0).isObject().get(FileUploadConstants.FILE_URI) != null) {
                    String uri = resultJson.get(0).isObject().get(FileUploadConstants.FILE_URI).isString()
                            .stringValue();
                    String fileName = resultJson.get(0).isObject().get(FileUploadConstants.FILE_NAME).isString()
                            .stringValue();
                    updateUri(uri, fileName);
                    Notification.notify("File " + fileName + " can be used.", NotificationType.INFO);

                } else {
                    String status = resultJson.get(0).isObject().get(FileUploadConstants.STATUS).isString().stringValue();
                    String message = resultJson.get(0).isObject().get(FileUploadConstants.MESSAGE).isString().stringValue();
                    Notification.notify(i18n.fileUploadResult(
                            status,
                            message),
                            NotificationType.ERROR);
                    logger.log(Level.SEVERE, "Submit file failed. Status: " + status + ", message: " + message);
                }
            }
        }
        
    }
    
    public void openFileUpload() {
        upload.click();
    }

    /**
     * Finally add a new MediaTrack.
     */
    private void addMediaTrack() {
        // TODO: not working. Try to get correct initialized media service.
        Notification.notify("Would save media track, but it's currently not working.", NotificationType.ERROR);
        
    }
    
    private void cleanupTempFileUpload() {
        if (uri != null) {
            String url = DELETE_URL + uri;
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.DELETE, url);
            requestBuilder.setCallback(new RequestCallback() {

                @Override
                public void onResponseReceived(Request request, Response response) {
                    String status = STATUS_NOT_OK;
                    String message = EMPTY_MESSAGE;
                    JSONValue jsonValue = parseAfterReplacingSurroundingPreElement(response.getText());
                    JSONArray resultJson = jsonValue.isArray();
                    if (resultJson != null) {
                        if (resultJson.get(0).isObject().get(FileUploadConstants.STATUS) != null) {
                            status = resultJson.get(0).isObject().get(FileUploadConstants.STATUS).isString()
                                    .stringValue();
                            if (!STATUS_OK.equals(status)) {
                                message = resultJson.get(0).isObject().get(FileUploadConstants.MESSAGE).isString()
                                        .stringValue();
                            }
                        }
                    } else {
                        status = jsonValue.isObject().get(FileUploadConstants.STATUS).isString().stringValue();
                    }
                    if (STATUS_OK.equals(status)) {
                        logger.log(Level.INFO, "Cleanup file successful. URL: " + uri);
                        updateUri(null, "");
                    } else if (EMPTY_MESSAGE.equals(message)) {
                        // No further message from service. Probably the file is not existing any more.
                        Notification.notify(i18n.error(), NotificationType.ERROR);
                        logger.log(Level.SEVERE, "Cleanup file failed. " + status + ": File with URI could not be removed. URI: " + uri);
                    } else {
                        Notification.notify(i18n.fileUploadResult(status, message), NotificationType.ERROR);
                        logger.log(Level.SEVERE, "Cleanup file failed. Status: " + status + ", message: " + message);
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Notification.notify(i18n.error(), NotificationType.ERROR);
                    logger.log(Level.SEVERE, "Cleanup file failed. Callback returned with error: " + exception.getMessage(), exception);
                }
            });
            try {
                requestBuilder.send();
            } catch (RequestException e) {
                Notification.notify(i18n.error(), NotificationType.ERROR);
                logger.log(Level.SEVERE, "Cleanup file failed. Sending caused an error: " + e.getMessage(), e);
            }
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
    
    private void updateUri(String uri, String fileName) {
        this.uri = uri;
        if (uri == null) {
            fileExistingPanel.setVisible(true);
            saveButton.setEnabled(false);
            fileNameInput.setEnabled(false);
        } else {
            fileExistingPanel.setVisible(false);
            saveButton.setEnabled(true);
            fileNameInput.setEnabled(true);
        }
        fileNameInput.setValue(fileName);
    }

}
