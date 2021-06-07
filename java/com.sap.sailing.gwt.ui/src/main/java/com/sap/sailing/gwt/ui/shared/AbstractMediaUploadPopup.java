package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;
import java.util.Date;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.home.shared.SharedHomeResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.fileupload.FileUploadConstants;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MediaType;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;
import com.sap.sse.gwt.client.shared.components.CollapsablePanel;

public abstract class AbstractMediaUploadPopup extends DialogBox {
    
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    
    private final static String UPLOAD_URL = "/sailingserver/fileupload";
    private final static String DELETE_URL = "/sailingserver/api/v1/file?uri=";
    private final static String STATUS_OK = "OK";
    private final static String STATUS_NOT_OK = "NOK";
    private final static String EMPTY_MESSAGE = "-";
    private final static String YOUTUBE_REGEX = "http(?:s?):\\/\\/(?:www\\.)?youtu(?:be\\.com\\/watch\\?v=|\\.be\\/)([\\w\\-\\_]*)(&(amp;)?‌​[\\w\\?‌​=]*)?";
    protected final StringMessages i18n = StringMessages.INSTANCE;
    protected final SharedHomeResources sharedHomeResources = SharedHomeResources.INSTANCE;
    
    private final Consumer<VideoDTO> updateVideo;
    private final Consumer<ImageDTO> updateImage;

    protected final FileUpload upload;
    protected final Button uploadButton;
    protected final FlowPanel content;
    protected final TextBox fileNameInput;
    protected final TextBox urlInput;
    protected final TextBox titleTextBox;
    protected final TextBox subtitleTextBox;
    protected final TextBox copyrightTextBox;
    protected final ListBox mimeTypeListBox;
    private final FlowPanel fileExistingPanel;
    private final Button saveButton;
    private FlowPanel progressOverlay;
    private String uri;
    
    public AbstractMediaUploadPopup(Consumer<VideoDTO> updateVideo, Consumer<ImageDTO> updateImage) {
        this.updateVideo = updateVideo;
        this.updateImage = updateImage;
        
        sharedHomeResources.sharedHomeCss().ensureInjected();
        addStyleName(sharedHomeResources.sharedHomeCss().popup());
        setTitle(i18n.upload());
        setText(i18n.upload());
        setGlassEnabled(true);
        setAnimationEnabled(true);
        
        upload = new FileUpload();
        upload.getElement().setAttribute("accept", "image/*;capture=camera");
        upload.setVisible(false);
        upload.setName("file");
        this.setModal(false);
        
        content = new FlowPanel();
        fileNameInput = new TextBox();
        urlInput = new TextBox();
        
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
        
        Label fileNameLabel = new Label(i18n.fileUpload());
        fileNameLabel.addStyleName(sharedHomeResources.sharedHomeCss().label());
        metaDataPanel.add(fileNameLabel);

        FlowPanel fileInputGroup = new FlowPanel();
        fileInputGroup.addStyleName(sharedHomeResources.sharedHomeCss().inputGroup());
        uploadButton = new Button();
        uploadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
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
        
        metaDataPanel.add(new Label("-- " + i18n.or() + " --"));
        
        Label urlLabel = new Label(i18n.url());
        urlLabel.addStyleName(sharedHomeResources.sharedHomeCss().label());
        metaDataPanel.add(urlLabel);
        urlInput.addStyleName(sharedHomeResources.sharedHomeCss().input());
        urlInput.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                checkSaveButton();
            }
        });
        urlInput.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                selectMimeTypeInBox(getMimeType(urlInput.getValue()));
                checkSaveButton();
            }
        });
        metaDataPanel.add(urlInput);
        
        fileExistingPanel = new FlowPanel();
        fileExistingPanel.add(new Label("-- " + i18n.noMediaSelected() + " --"));
        metaDataPanel.add(fileExistingPanel);
        
        Label detailsSubTitle = new Label(i18n.details());
        detailsSubTitle.addStyleName(sharedHomeResources.sharedHomeCss().subTitle());
        metaDataPanel.add(detailsSubTitle);
        
        Label titleLabel = new Label(i18n.title());
        titleLabel.addStyleName(sharedHomeResources.sharedHomeCss().label());
        metaDataPanel.add(titleLabel);
        titleTextBox = new TextBox();
        titleTextBox.addStyleName(sharedHomeResources.sharedHomeCss().input());
        metaDataPanel.add(titleTextBox);

        FlowPanel advancedContent = new FlowPanel();
        CollapsablePanel collapsableAdvancedPanel = new CollapsablePanel(i18n.advanced(), true);
        collapsableAdvancedPanel.setContent(advancedContent);
        collapsableAdvancedPanel.setWidth("100%");
        metaDataPanel.add(collapsableAdvancedPanel);
        
        Label subtitleLabel = new Label(i18n.subtitle());
        subtitleLabel.addStyleName(sharedHomeResources.sharedHomeCss().label());
        advancedContent.add(subtitleLabel);
        subtitleTextBox = new TextBox();
        subtitleTextBox.addStyleName(sharedHomeResources.sharedHomeCss().input());
        advancedContent.add(subtitleTextBox);

        Label copyrightLabel = new Label(i18n.copyright());
        copyrightLabel.addStyleName(sharedHomeResources.sharedHomeCss().label());
        advancedContent.add(copyrightLabel);
        copyrightTextBox = new TextBox();
        copyrightTextBox.addStyleName(sharedHomeResources.sharedHomeCss().input());
        advancedContent.add(copyrightTextBox);

        Label mimeTypeListLabel = new Label(i18n.mimeType());
        mimeTypeListLabel.addStyleName(sharedHomeResources.sharedHomeCss().label());
        advancedContent.add(mimeTypeListLabel);
        mimeTypeListBox = new ListBox();
        mimeTypeListBox.addStyleName(sharedHomeResources.sharedHomeCss().select());
        initMediaTypes();
        advancedContent.add(mimeTypeListBox);

        FlowPanel buttonGroup = new FlowPanel();
        buttonGroup.addStyleName(sharedHomeResources.sharedHomeCss().buttonGroup());
        buttonGroup.addStyleName(sharedHomeResources.sharedHomeCss().right());
        metaDataPanel.add(buttonGroup);
        // Add a 'submit' button.
        Button cancelButton = new Button(i18n.cancel(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                fileNameInput.setValue("");
                cleanupTempFileUpload();
                hide();
            }
        });
        buttonGroup.add(cancelButton);
        // Add a 'submit' button.
        saveButton = new Button(i18n.save(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                event.stopPropagation();
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
                addMedia();
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
        
        progressOverlay = new FlowPanel();
        progressOverlay.ensureDebugId("ProgressOverlay");
        progressOverlay.addStyleName(sharedHomeResources.sharedHomeCss().progressOverlay());
        FlowPanel progressSpinner = new FlowPanel();
        progressSpinner.addStyleName(sharedHomeResources.sharedHomeCss().progressSpinner());
        progressOverlay.add(progressSpinner);
        progressOverlay.setVisible(false);
        content.add(progressOverlay);
        
        add(content);
        
    }
    
    private void initMediaTypes() {
        mimeTypeListBox.addItem(MimeType.unknown.name());
        for (MimeType mimeType: MimeType.values()) {
            if (mimeType != MimeType.unknown) {
                mimeTypeListBox.addItem(mimeType.name());
            }
        }
        mimeTypeListBox.setSelectedIndex(0);
    }
    
    private void selectMimeTypeInBox(MimeType mimeType) {
        for (int i=0; i<mimeTypeListBox.getItemCount(); i++) {
            if (mimeType.name().equals(mimeTypeListBox.getValue(i))) {
                mimeTypeListBox.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private MimeType getMimeType(String urlParam) {
        String url;
        if (urlParam == null) {
            url = "";
        } else {
            url = urlParam.trim();
        }
        logger.info("Check url: " + url);
        final MimeType mimeType;
        if (matches(url, YOUTUBE_REGEX)) {
            mimeType = MimeType.youtube;
        } else if (isVimeoUrl(url)) {
            mimeType = MimeType.vimeo;
        } else {
            mimeType = detectMimeTypeFromUrl(url);
        }
        return mimeType;
    }
    
    private MimeType detectMimeTypeFromUrl(String url) {
        MimeType result = MimeType.unknown;
        if (url != null) {
            for (MimeType mimeType: MimeType.values()) {
                if (mimeType.endingPattern.length() > 0) {
                    String regex = "[a-z\\-_0-9\\/\\:\\.]*\\.(" + mimeType.getEndingPattern() + ")";
                    if (matches(url, regex)) {
                        result = mimeType;
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    private boolean matches(String matcher, String pattern) {
        return RegExp.compile(pattern, "i").test(matcher);
    }
    
    private boolean isVimeoUrl(String url) {
        try {
            RegExp urlPattern = RegExp.compile("^(.*:)//([A-Za-z0-9\\-\\.]+)(:[0-9]+)?(.*)$");
            MatchResult matchResult = urlPattern.exec(url);
            String host = matchResult.getGroup(2);
            return host.contains("vimeo.com");
        } catch (Exception e) {
            return false;
        }
    }
    
    protected class SubmitHandler implements FormPanel.SubmitHandler {
        @Override
        public void onSubmit(SubmitEvent event) {
            // This event is fired just before the form is submitted. We can take
            // this opportunity to perform validation.
            progressOverlay.setVisible(true);
            fileNameInput.setValue(i18n.loading());
            if (getMimeType(upload.getFilename()) == MimeType.unknown) {
                logger.log(Level.SEVERE, "File type is not supported.");
                progressOverlay.setVisible(false);
                fileNameInput.setValue(i18n.fileTypeNotSupported());
                Notification.notify(i18n.fileTypeNotSupported(), NotificationType.WARNING);
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
            progressOverlay.setVisible(false);
            fileNameInput.setValue("");
            String result = event.getResults().trim();
            JSONValue resultJsonValue = parseAfterReplacingSurroundingPreElement(result);
            JSONArray resultJson = resultJsonValue.isArray();
            if (resultJson != null) {
                if (resultJson.get(0).isObject().get(FileUploadConstants.FILE_URI) != null) {
                    cleanFormElements();
                    String uri = resultJson.get(0).isObject().get(FileUploadConstants.FILE_URI).isString()
                            .stringValue();
                    String fileName = resultJson.get(0).isObject().get(FileUploadConstants.FILE_NAME).isString()
                            .stringValue();
                    updateUri(uri, fileName);
                    fileNameInput.setValue(i18n.uploadSuccessful());
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
    
    @Override
    public void show() {
        // reset all fields
        updateUri(null, null);
        cleanFormElements();
        super.show();
    }
    
    private void cleanFormElements() {
        fileNameInput.setValue("");
        urlInput.setValue("");
        titleTextBox.setValue("");
        subtitleTextBox.setValue("");
        copyrightTextBox.setValue("");
        mimeTypeListBox.setSelectedIndex(0);
        //progressOverlay.setVisible(false);
    }

    /**
     * Finally add a new MediaTrack.
     */
    private void addMedia() {

        final String uploadUrl;
        if (uri == null) {
            uploadUrl = "";
        } else if (!UriUtils.isSafeUri(uri.trim()))  {
            logger.warning("Upload url is not valid: " + uri + ". Ignore upload url.");
            uploadUrl = "";
        } else {
            uploadUrl = uri.trim();
        }
        final String inputUrl;
        if (urlInput.getValue() == null) {
            inputUrl = "";
        } else if (!UriUtils.isSafeUri(urlInput.getValue())) {
            logger.warning("Upload url is not valid: " + uri + ". Ignore upload url.");
            inputUrl = "";
        } else {
            inputUrl = urlInput.getValue();
        }
        final String url;
        if (uploadUrl.isEmpty()) {
            url = inputUrl;
        } else {
            url = uploadUrl;
        }
        
        if (!url.isEmpty()) {
            final String mimeTypeName = mimeTypeListBox.getSelectedValue();
            final MimeType mimeType;
            if (mimeTypeName != null) {
                mimeType = MimeType.byName(mimeTypeListBox.getSelectedValue());
            } else {
                mimeType = MimeType.unknown;
            }
            hide();
            if (mimeType.mediaType == MediaType.image) {
                updateImage.accept(createImage(url));
            } else if (mimeType.mediaType == MediaType.video || mimeType.mediaType == MediaType.audio) {
                updateVideo.accept(createVideo(url, null, mimeType));
            } else {
                logger.warning("No image nor video detected. Nothing will be saved.");
                Notification.notify(i18n.noImageOrVideoDetected(), NotificationType.WARNING);
            }
            
        } else {
            Notification.notify(i18n.invalidURL(), NotificationType.ERROR);
        }
    }
    
    private ImageDTO createImage(String url) {
        final ImageDTO image = new ImageDTO(url, new Date());
        image.setTitle(titleTextBox.getValue());
        image.setSubtitle(subtitleTextBox.getValue());
        image.setCopyright(copyrightTextBox.getValue());
        Iterable<String> defaultTags = Collections.singletonList(MediaTagConstants.GALLERY.getName());
        image.setTags(defaultTags);
        logger.info("Image ready: " + image);
        return image;
    }
    
    private VideoDTO createVideo(String url, String thumbnailUrl, MimeType mimeType) {
        final VideoDTO video = new VideoDTO(url, mimeType, new Date());
        video.setTitle(titleTextBox.getValue());
        video.setSubtitle(subtitleTextBox.getValue());
        video.setCopyright(copyrightTextBox.getValue());
        video.setThumbnailRef(thumbnailUrl);
        Iterable<String> defaultTags = Collections.singletonList(MediaTagConstants.GALLERY.getName());
        video.setTags(defaultTags);
        logger.info("video created. " + url + ", " + mimeType);
        return video;
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
                updateUri(null, "");
                cleanFormElements();
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
            //fileNameInput.setEnabled(false);
            urlInput.setEnabled(true);
        } else {
            fileExistingPanel.setVisible(false);
            saveButton.setEnabled(true);
            //fileNameInput.setEnabled(true);
            urlInput.setEnabled(false);
            selectMimeTypeInBox(getMimeType(uri));
        }
        updateFileName(fileName);
        checkSaveButton();
    }
    
    abstract protected void updateFileName(String fileName);

    private void checkSaveButton() {
        boolean urlInputNotEmpty = urlInput.getValue() != null && !urlInput.getValue().trim().isEmpty();
        boolean uriNotEmpty = uri != null && !uri.trim().isEmpty();
        saveButton.setEnabled(urlInputNotEmpty || uriNotEmpty);
    }

}
