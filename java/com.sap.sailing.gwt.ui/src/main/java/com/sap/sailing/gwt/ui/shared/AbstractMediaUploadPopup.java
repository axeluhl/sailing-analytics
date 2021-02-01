package com.sap.sailing.gwt.ui.shared;

import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.MediaServiceWrite;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.fileupload.FileUploadConstants;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

public abstract class AbstractMediaUploadPopup extends DialogBox {
    
    private final static String UPLOAD_URL = "/sailingserver/fileupload";
    private final StringMessages i18n = StringMessages.INSTANCE;

    protected final FileUpload upload;
    protected final FlowPanel content;
    protected final TextBox fileNameInput;
    private final MediaServiceWriteAsync mediaServiceWrite;
    private String uri;
    private String fileName;
    
    public AbstractMediaUploadPopup() {
        // TODO: fix this. It will not work.
        mediaServiceWrite = GWT.create(MediaServiceWrite.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaServiceWrite, RemoteServiceMappingConstants.mediaServiceRemotePath, HEADER_FORWARD_TO_MASTER);
        
        upload = new FileUpload();
        upload.setVisible(false);
        upload.setName("file");
        
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
        final Button uploadButton = new Button(i18n.upload());
        uploadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                upload.click();
            }
        });
        content.add(uploadButton);
        content.add(uploadForm);

        final FormPanel metaDataForm = new FormPanel();

        VerticalPanel metaDataPanel = new VerticalPanel();
        metaDataForm.add(metaDataPanel);

        // Create a TextBox, giving it a name so that it will be submitted.
        fileNameInput.setName("textBoxFormElement");
        metaDataPanel.add(fileNameInput);

        // Add a 'submit' button. TODO: translated label needed
        metaDataPanel.add(new Button(i18n.save(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                metaDataForm.submit();
            }
        }));
        // Add a 'submit' button.
        metaDataPanel.add(new Button(i18n.cancel(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                fileNameInput.setValue("");
                cleanupTempFileUpload();
                hide();
            }
        }));
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
            if (!upload.getFilename().endsWith(".png") && !upload.getFilename().endsWith(".svg")
                    && !upload.getFilename().endsWith(".tiff") && !upload.getFilename().endsWith(".jpg")
                    && !upload.getFilename().endsWith(".jpeg")) {
                Window.alert("File typ is not supported.");
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
                    uri = resultJson.get(0).isObject().get(FileUploadConstants.FILE_URI).isString()
                            .stringValue();
                    fileName = resultJson.get(0).isObject().get(FileUploadConstants.FILE_NAME).isString()
                            .stringValue();
                    fileNameInput.setValue(fileName);

                    Window.alert("Uri: " + uri + ", file name: " + fileName);

                } else {
                    Notification.notify(i18n.fileUploadResult(
                            resultJson.get(0).isObject().get(FileUploadConstants.STATUS).isString().stringValue(),
                            resultJson.get(0).isObject().get(FileUploadConstants.MESSAGE).isString().stringValue()),
                            NotificationType.ERROR);
                    Window.alert("Error!!!");
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
        Window.alert("would save media track now, but it's not working.");
        /*Set<RegattaAndRaceIdentifier> regattaAndRaceIdentifiers = new HashSet<RegattaAndRaceIdentifier>();
        RegattaAndRaceIdentifier regattaAndRaceIdentifier = new RegattaNameAndRaceName(regattaId, "R1");
        regattaAndRaceIdentifiers.add(regattaAndRaceIdentifier);
        MediaTrack mediaTrack = new MediaTrack(uri, uri, null, null, MimeType.image, regattaAndRaceIdentifiers);
        mediaServiceWrite.addMediaTrack(mediaTrack, new AsyncCallback<MediaTrackWithSecurityDTO>() {

            @Override
            public void onSuccess(MediaTrackWithSecurityDTO result) {
                Window.alert("Uri: " + uri + ", file name: " + fileName);
                Notification.notify(i18n.uploadSuccessful(), NotificationType.SUCCESS);
            }

            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(i18n.error(), NotificationType.ERROR);
            }
        });*/
    }
    
    private void cleanupTempFileUpload() {
        if (uri != null) {
            Window.alert("would cleanup temp file now.");
            // TODO implement delete file by uri from storage, if canceled or overwritten by new file selection.
            uri = null;
            fileName = null;
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
