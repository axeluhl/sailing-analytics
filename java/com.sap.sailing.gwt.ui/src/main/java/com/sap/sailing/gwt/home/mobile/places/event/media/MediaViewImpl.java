package com.sap.sailing.gwt.home.mobile.places.event.media;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.home.communication.media.SailingVideoDTO;
import com.sap.sailing.gwt.home.desktop.partials.media.MediaPageResources;
import com.sap.sailing.gwt.home.mobile.partials.imagegallery.ImageGallery;
import com.sap.sailing.gwt.home.mobile.partials.uploadpopup.MobileMediaUploadPopup;
import com.sap.sailing.gwt.home.mobile.partials.videogallery.VideoGallery;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;
import com.sap.sailing.gwt.ui.client.SailingServiceHelper;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class MediaViewImpl extends AbstractEventView<MediaView.Presenter> implements MediaView {
    
    private Logger logger = Logger.getLogger(getClass().getName());
    
    private static MediaViewImplUiBinder uiBinder = GWT.create(MediaViewImplUiBinder.class);

    interface MediaViewImplUiBinder extends UiBinder<Widget, MediaViewImpl> {
    }
    
    @UiField Label noContentInfoUi;
    @UiField VideoGallery videoGalleryUi;
    @UiField ImageGallery imageGalleryUi;
    @UiField Button addMediaButtonUi;
    
    private MobileMediaUploadPopup mobileMediaUploadPopup;
    private final SailingServiceWriteAsync sailingServiceWrite;
    
    private MediaDTO media;

    public MediaViewImpl(MediaView.Presenter presenter) {
        super(presenter, false, true, false);
        this.sailingServiceWrite = SailingServiceHelper.createSailingServiceWriteInstance();
        MediaViewResources.INSTANCE.css().ensureInjected();
        setViewContent(uiBinder.createAndBindUi(this));
        UserDTO currentUser = presenter.getUserService().getCurrentUser();
        if (currentUser != null && !currentUser.getName().equals("Anonymous")) {
            setMediaManaged(true);
        }
        
        presenter.getEventBus().addHandler(AuthenticationContextEvent.TYPE, event->{
            // for some reason this event is only send after logout. Never the less it will also handle login.
            AuthenticationContext authContext = event.getCtx();
            if (authContext.getCurrentUser() != null && !authContext.getCurrentUser().getName().equals("Anonymous")) {
                setMediaManaged(true);
            } else {
                setMediaManaged(false);
            }
        });

        SailingServiceWriteAsync sailingServiceWrite = SailingServiceHelper.createSailingServiceWriteInstance();
        MediaPageResources.INSTANCE.css().ensureInjected();
        mobileMediaUploadPopup = new MobileMediaUploadPopup(sailingServiceWrite, presenter.getEventDTO().getId(),
                video -> {
                    // SailingVideoDTO can be created without eventRef because this is not needed here. Later after reload this
                    // objects will be overwritten.
                    SailingVideoDTO sailingVideoDTO = new SailingVideoDTO(null, video);
                    media.getVideos().add(sailingVideoDTO);
                    setMedia(media);
                },
                image -> {
                    // SailingImageDTO can be created without eventRef because this is not needed here. Later after reload this
                    // objects will be overwritten.
                    SailingImageDTO imageSailingImageDTO = new SailingImageDTO(null, image.getSourceRef(),
                            image.getCreatedAtDate());
                    media.getPhotos().add(imageSailingImageDTO);
                    setMedia(media);
                });

        addMediaButtonUi.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                mobileMediaUploadPopup.show();
                mobileMediaUploadPopup.openFileUpload();
            }
        });

    }
    
    @Override
    public void setMedia(MediaDTO media) {
        this.media = media;
        noContentInfoUi.setVisible(media.getVideos().isEmpty() && media.getPhotos().isEmpty());
        videoGalleryUi.setVideos(media.getVideos(),
                video -> deleteVideo(video));
        videoGalleryUi.setVisible(!media.getVideos().isEmpty());
        imageGalleryUi.setImages(media.getPhotos(), 
                image -> deleteImage(image));
        imageGalleryUi.setVisible(!media.getPhotos().isEmpty());

        imageGalleryUi.setMediaManaged(false);
        videoGalleryUi.setMediaManaged(false);
    }
    
    private void setMediaManaged(boolean managed) {
        addMediaButtonUi.setVisible(managed);
        videoGalleryUi.setManageButtonsVisible(managed);
        imageGalleryUi.setManageButtonsVisible(managed);
        if (!managed) {
            videoGalleryUi.setMediaManaged(managed);
            imageGalleryUi.setMediaManaged(managed);
        }
    }
    
    private void deleteImage(ImageDTO image) {
        sailingServiceWrite.getEventById(getEventId(), true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(EventDTO result) {
                result.getImages().stream()
                        .filter(img 
                                -> img.getSourceRef().equals(image.getSourceRef()) 
                                        && img.getCreatedAtDate().equals(image.getCreatedAtDate()))
                        .forEach(matchImage -> result.removeImage(matchImage));
                sailingServiceWrite.updateEvent(result, new AsyncCallback<EventDTO>() {
                    
                    @Override
                    public void onSuccess(EventDTO result) {
                        SailingImageDTO imageSailingImageDTO = new SailingImageDTO(null, image);
                        media.getPhotos().remove(imageSailingImageDTO);
                        setMedia(media);
                        // TODO: translate
                        Notification.notify("Image removed.", NotificationType.SUCCESS);
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        // TODO: translate
                        Notification.notify("Error -> Image not removed. Error: " + caught.getMessage(), NotificationType.ERROR);
                        logger.log(Level.SEVERE, "Cannot update event. Image not removed.", caught);
                    }
                });
            }
            @Override
            public void onFailure(Throwable caught) {
                // TODO: translate
                Notification.notify("Error -> Image not removed. Error: " + caught.getMessage(), NotificationType.ERROR);
                logger.log(Level.SEVERE, "Cannot load event. Image not removed.", caught);
            }
        });
    }
    
    private void deleteVideo(VideoDTO video) {
        sailingServiceWrite.getEventById(getEventId(), true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(EventDTO result) {
                result.getVideos().stream()
                        .filter(img 
                                -> img.getSourceRef().equals(video.getSourceRef()) 
                                        && img.getCreatedAtDate().equals(video.getCreatedAtDate()))
                        .forEach(matchVideo -> result.removeVideo(matchVideo));
                sailingServiceWrite.updateEvent(result, new AsyncCallback<EventDTO>() {
                    
                    @Override
                    public void onSuccess(EventDTO result) {
                        SailingVideoDTO sailingVideoDTO = new SailingVideoDTO(null, video);
                        media.getVideos().remove(sailingVideoDTO);
                        setMedia(media);
                        // TODO: translate
                        Notification.notify("Image removed.", NotificationType.SUCCESS);
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        // TODO: translate
                        Notification.notify("Error -> Video not removed. Error: " + caught.getMessage(), NotificationType.ERROR);
                        logger.log(Level.SEVERE, "Cannot update event. Video not removed.", caught);
                    }
                });
            }
            @Override
            public void onFailure(Throwable caught) {
                // TODO: translate
                Notification.notify("Error -> Video not removed. Error: " + caught.getMessage(), NotificationType.ERROR);
                logger.log(Level.SEVERE, "Cannot load event. Video not removed.", caught);
            }
        });
    }

}
