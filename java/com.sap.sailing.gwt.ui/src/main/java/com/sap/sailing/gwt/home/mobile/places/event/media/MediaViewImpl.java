package com.sap.sailing.gwt.home.mobile.places.event.media;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import com.sap.sailing.gwt.home.desktop.partials.media.MediaPageResources;
import com.sap.sailing.gwt.home.mobile.partials.imagegallery.ImageGallery;
import com.sap.sailing.gwt.home.mobile.partials.uploadpopup.MobileMediaUploadPopup;
import com.sap.sailing.gwt.home.mobile.partials.videogallery.VideoGallery;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;
import com.sap.sailing.gwt.ui.client.SailingServiceHelper;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.media.AbstractMediaDTO;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.client.UserService;

public class MediaViewImpl extends AbstractEventView<MediaView.Presenter> implements MediaView {
    
    private Logger logger = Logger.getLogger(getClass().getName());
    
    private static MediaViewImplUiBinder uiBinder = GWT.create(MediaViewImplUiBinder.class);
    
    private Collection<ImageDTO> images = new LinkedHashSet<ImageDTO>();
    private Collection<VideoDTO> videos = new LinkedHashSet<VideoDTO>();

    interface MediaViewImplUiBinder extends UiBinder<Widget, MediaViewImpl> {
    }
    
    @UiField Label noContentInfoUi;
    @UiField VideoGallery videoGalleryUi;
    @UiField ImageGallery imageGalleryUi;
    @UiField Button addMediaButtonUi;
    
    private MobileMediaUploadPopup mobileMediaUploadPopup;
    private final SailingServiceWriteAsync sailingServiceWrite;
    private final UserService userService;

    public MediaViewImpl(MediaView.Presenter presenter) {
        super(presenter, false, true, false);
        this.sailingServiceWrite = SailingServiceHelper.createSailingServiceWriteInstance();
        this.userService = presenter.getUserService();
        MediaViewResources.INSTANCE.css().ensureInjected();
        setViewContent(uiBinder.createAndBindUi(this));
        UserDTO currentUser = presenter.getUserService().getCurrentUser();
        if (currentUser != null && !currentUser.getName().equals("Anonymous")) {
            loadEventData(eventDto -> {
                setMediaManaged(hasPermissions(eventDto));
                setEventDto(eventDto);
            });
        }
        
        presenter.getEventBus().addHandler(AuthenticationContextEvent.TYPE, event->{
            // for some reason this event is only send after logout. Never the less it will also handle login.
            AuthenticationContext authContext = event.getCtx();
            if (authContext.getCurrentUser() != null && !authContext.getCurrentUser().getName().equals("Anonymous")) {
                // only if user is logged in check permissions to update event and set "manage media" status accordingly
                loadEventData(eventDto -> {
                    setMediaManaged(hasPermissions(eventDto));
                    setEventDto(eventDto);
                });
            } else {
                setMediaManaged(false);
            }
        });

        MediaPageResources.INSTANCE.css().ensureInjected();
        mobileMediaUploadPopup = new MobileMediaUploadPopup(
                video -> {
                    addVideo(video);
                },
                image -> {
                    addImage(image);
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
        setVideos(media.getVideos());
        setImages(media.getPhotos());
        updateMedia();
    }
    
    public void setEventDto(EventDTO eventDto) {
        logger.info("setEventDto " + eventDto);
        setVideos(eventDto.getVideos());
        setImages(eventDto.getImages());
        updateMedia();
    }
    
    private void setVideos(Collection<? extends VideoDTO> videos) {
        this.videos = new LinkedHashSet<VideoDTO>(videos.stream()
                .filter(video -> video.hasTag(MediaTagConstants.GALLERY.getName()))
                .sorted(Comparator.comparing(AbstractMediaDTO::getCreatedAtDate).reversed())
                .collect(Collectors.toList()));
    }
    
    private void setImages(Collection<? extends ImageDTO> images) {
        this.images = new LinkedHashSet<ImageDTO>(images.stream()
                .filter(video -> video.hasTag(MediaTagConstants.GALLERY.getName()))
                .sorted(Comparator.comparing(AbstractMediaDTO::getCreatedAtDate).reversed())
                .collect(Collectors.toList()));
    }
    
    public void updateMedia() {
        noContentInfoUi.setVisible(videos.isEmpty() && images.isEmpty());
        videoGalleryUi.setVideos(videos,
                video -> deleteVideo(video));
        videoGalleryUi.setVisible(!videos.isEmpty());
        imageGalleryUi.setImages(images, 
                image -> deleteImage(image));
        imageGalleryUi.setVisible(!images.isEmpty());

        imageGalleryUi.setMediaManaged(false);
        videoGalleryUi.setMediaManaged(false);
    }
    
    private boolean hasPermissions(EventDTO eventDto) {
        final boolean hasPermission;
        if (userService.hasPermission(eventDto, HasPermissions.DefaultActions.UPDATE)) {
            hasPermission = true;
        } else {
            hasPermission = false;
        }
        logger.info("Check permission: " + hasPermission);
        return hasPermission;
    }
    
    private void setMediaManaged(boolean managed) {
        logger.info("Set manage media to: " + managed);
        addMediaButtonUi.setVisible(managed);
        videoGalleryUi.setManageButtonsVisible(managed);
        imageGalleryUi.setManageButtonsVisible(managed);
        if (!managed) {
            videoGalleryUi.setMediaManaged(managed);
            imageGalleryUi.setMediaManaged(managed);
        }
    }
    
    private void deleteImage(ImageDTO imageDto) {
        loadEventData(eventDto -> {
            Collection<ImageDTO> toRemove = eventDto.getImages().stream()
                    .filter(image -> image.getSourceRef().equals(imageDto.getSourceRef()) 
                            && image.getCreatedAtDate().equals(imageDto.getCreatedAtDate()))
                    .collect(Collectors.toList());
            eventDto.getImages().removeAll(toRemove);
            updateEventDto(eventDto);
        });
    }
    
    private void deleteVideo(VideoDTO videoDto) {
        loadEventData(eventDto -> {
            Collection<VideoDTO> toRemove = eventDto.getVideos().stream()
                    .filter(video -> video.getSourceRef().equals(videoDto.getSourceRef()) 
                            && video.getCreatedAtDate().equals(videoDto.getCreatedAtDate()))
                    .collect(Collectors.toList());
            eventDto.getVideos().removeAll(toRemove);
            updateEventDto(eventDto);
        });
    }
    
    private void addImage(ImageDTO image) {
        loadEventData(eventDto -> {
            eventDto.getImages().add(image);
            updateEventDto(eventDto);
        });
    }
    
    private void addVideo(VideoDTO video) {
        loadEventData(eventDto -> {
            eventDto.getVideos().add(video);
            updateEventDto(eventDto);
        });
    }
    
    public void loadEventData(Consumer<EventDTO> callback) {
        sailingServiceWrite.getEventById(getEventId(), true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(EventDTO eventDto) {
                callback.accept(eventDto);
            }
            @Override
            public void onFailure(Throwable caught) {
                // TODO: translate
                Notification.notify("Error while updating event data.",  NotificationType.ERROR);
                logger.log(Level.SEVERE, "Cannot update event.", caught);
            }
        });
    }
    
    private void updateEventDto(EventDTO eventDto) {
        if (hasPermissions(eventDto)) {
            sailingServiceWrite.updateEvent(eventDto, new AsyncCallback<EventDTO>() {
                
                @Override
                public void onSuccess(EventDTO eventDto) {
                    setEventDto(eventDto);
                    // TODO: translate
                    Notification.notify("Updated event successfully.", NotificationType.SUCCESS);
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    // TODO: translate
                    Notification.notify("Error -> Video not added. Error: " + caught.getMessage(), NotificationType.ERROR);
                    logger.log(Level.SEVERE, "Cannot update event. Video not added.", caught);
                }
            });
        }
    }

}
