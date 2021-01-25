package com.sap.sailing.gwt.home.mobile.partials.uploadpopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;
import com.sap.sailing.gwt.home.mobile.places.event.media.MediaViewResources;
import com.sap.sailing.gwt.ui.client.MediaServiceWrite;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.fileupload.FileUploadConstants;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

public class MediaUploadPopup extends DialogBox {

    private final FileUpload upload;
    private final StringMessages i18n = StringMessages.INSTANCE;
    private final MediaServiceWriteAsync mediaServiceWrite = GWT.create(MediaServiceWrite.class);

    private String fileName;
    private String uri;

    public MediaUploadPopup() {
        super();
        MediaViewResources.INSTANCE.css().ensureInjected();
        addStyleName(MediaViewResources.INSTANCE.css().popup());

        final FlowPanel content = new FlowPanel();
        add(content);
        final TextBox tb = new TextBox();

        // Upload
        final FormPanel uploadForm = new FormPanel();
        uploadForm.setAction("/sailingserver/fileupload");

        // Because we're going to add a FileUpload widget, we'll need to set the
        // form to use the POST method, and multipart MIME encoding.
        uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        uploadForm.setMethod(FormPanel.METHOD_POST);

        // Create a panel to hold all of the form widgets.
        VerticalPanel uploadPanel = new VerticalPanel();
        uploadForm.setWidget(uploadPanel);

        // Create a FileUpload widget.
        upload = new FileUpload();
        upload.getElement().setAttribute("accept", "image/*;capture=camera");
        upload.setName("file");
        uploadPanel.add(upload);

        upload.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                cleanupTempFileUpload();
                uploadForm.submit();
            }
        });

        // Add an event handler to the form.
        uploadForm.addSubmitHandler(new FormPanel.SubmitHandler() {
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
        });

        uploadForm.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
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
                        tb.setValue(fileName);
                        Notification.notify(i18n.uploadSuccessful(), NotificationType.SUCCESS);

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
        });
        content.add(uploadForm);
        
        // Meta Data
        final FormPanel metaDataForm = new FormPanel();
        VerticalPanel metaDataPanel = new VerticalPanel();

        // Create a TextBox, giving it a name so that it will be submitted.
        tb.setName("textBoxFormElement");
        metaDataPanel.add(tb);

        // Add a 'submit' button. TODO: translated label needed
        metaDataPanel.add(new Button(i18n.save(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                metaDataForm.submit();
            }
        }));
        // Add a 'submit' button.
        metaDataPanel.add(new Button(i18n.cancel(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                tb.setValue("");
                cleanupTempFileUpload();
                hide();
            }
        }));
        metaDataForm.addSubmitHandler(new FormPanel.SubmitHandler() {
            
            @Override
            public void onSubmit(SubmitEvent event) {
                tb.setValue("");
                addMediaTrack();
                hide();
            }
        });
        metaDataForm.add(metaDataPanel);
        content.add(metaDataForm);
    }

    public void openFileUpload() {
        upload.click();
    }

    /**
     * Finally add a new MediaTrack.
     */
    private void addMediaTrack() {
        // TODO: not working. Try to get correct initialized media service.
        MediaTrack mediaTrack = new MediaTrack(uri, uri, null, null, MimeType.image, null);
        mediaServiceWrite.addMediaTrack(mediaTrack, new AsyncCallback<MediaTrackWithSecurityDTO>() {

            @Override
            public void onSuccess(MediaTrackWithSecurityDTO result) {
                Window.alert("Uri: " + uri + ", file name: " + fileName);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Error!!!");
            }
        });
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
