package com.sap.sailing.gwt.ui.shared;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.security.SecuredDomainType.EventActions;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.home.communication.media.SailingVideoDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.media.AbstractMediaDTO;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.ui.client.UserService;

public class ManageMediaModel {

    private Logger logger = Logger.getLogger(getClass().getName());

    protected final SailingServiceWriteAsync sailingServiceWrite;
    protected final UserService userService;
    private final EventViewDTO eventViewDto;
    private final StringMessages i18n;
    private MediaDTO mediaDto;

    private Collection<ImageDTO> images = new LinkedHashSet<ImageDTO>();
    private Collection<VideoDTO> videos = new LinkedHashSet<VideoDTO>();

    public ManageMediaModel(SailingServiceWriteAsync sailingServiceWrite, UserService userService, 
            EventViewDTO eventViewDto, StringMessages i18n) {
        this.sailingServiceWrite = sailingServiceWrite;
        this.userService = userService;
        this.eventViewDto = eventViewDto;
        this.i18n = i18n;
    }

    private void setEventDto(EventDTO eventDto) {
        setVideos(eventDto.getVideos());
        setImages(eventDto.getImages());
    }

    public Collection<ImageDTO> getImages() {
        return images;
    }

    public Collection<VideoDTO> getVideos() {
        return videos;
    }
    
    public void setMedia(MediaDTO mediaDto) {
        this.mediaDto = mediaDto;
        setVideos(mediaDto.getVideos());
        setImages(mediaDto.getPhotos());
    }

    private void setVideos(Collection<? extends VideoDTO> videos) {
        this.videos = new LinkedHashSet<VideoDTO>(
                videos.stream()
                        .sorted(Comparator.comparing(AbstractMediaDTO::getCreatedAtDate).reversed())
                        .collect(Collectors.toList()));
    }

    private void setImages(Collection<? extends ImageDTO> images) {
        this.images = new LinkedHashSet<ImageDTO>(
                images.stream().filter(video -> video.hasTag(MediaTagConstants.GALLERY.getName()))
                        .sorted(Comparator.comparing(AbstractMediaDTO::getCreatedAtDate).reversed())
                        .collect(Collectors.toList()));
    }

    public void deleteImage(ImageDTO imageDto, Consumer<EventDTO> callback) {
        loadEventData(eventDto -> {
            Collection<ImageDTO> toRemove = eventDto.getImages().stream()
                    .filter(image -> image.getSourceRef().equals(imageDto.getSourceRef())
                            && image.getCreatedAtDate().equals(imageDto.getCreatedAtDate()))
                    .collect(Collectors.toList());
            eventDto.getImages().removeAll(toRemove);
            eventDto.getImages().removeAll(toRemove);
            updateEventDto(eventDto, callback);
            mediaDto.getPhotos().stream()
                    .filter(photo -> photo.getSourceRef().equals(imageDto.getSourceRef())
                            && photo.getCreatedAtDate().equals(imageDto.getCreatedAtDate()))
                    .forEach(photo -> mediaDto.removePhoto(photo));
        });
    }

    public void deleteVideo(VideoDTO videoDto, Consumer<EventDTO> callback) {
        loadEventData(eventDto -> {
            Collection<VideoDTO> toRemove = eventDto.getVideos().stream()
                    .filter(video -> video.getSourceRef().equals(videoDto.getSourceRef())
                            && video.getCreatedAtDate().equals(videoDto.getCreatedAtDate()))
                    .collect(Collectors.toList());
            eventDto.getVideos().removeAll(toRemove);
            updateEventDto(eventDto, callback);
            mediaDto.getVideos().stream()
                    .filter(video -> video.getSourceRef().equals(videoDto.getSourceRef())
                            && video.getCreatedAtDate().equals(videoDto.getCreatedAtDate()))
                    .forEach(video -> mediaDto.removeVideo(video));
        });
    }

    public void addImages(List<ImageDTO> imageList, Consumer<EventDTO> callback) {
        loadEventData(eventDto -> {
            for (ImageDTO image: imageList) {
                eventDto.getImages().add(image);
                mediaDto.addPhoto(new SailingImageDTO(null, image));
            }
            updateEventDto(eventDto, callback);
        });
    }

    public void addVideos(List<VideoDTO> videoList, Consumer<EventDTO> callback) {
        loadEventData(eventDto -> {
            for (VideoDTO video: videoList) {
                eventDto.getVideos().add(video);
                mediaDto.addVideo(new SailingVideoDTO(null, video));
            }
            updateEventDto(eventDto, callback);
        });
    }

    public void addImagesAndVideos(List<ImageDTO> imageList, List<VideoDTO> videoList, Consumer<EventDTO> callback) {
        loadEventData(eventDto -> {
            for (ImageDTO image: imageList) {
                eventDto.getImages().add(image);
                mediaDto.addPhoto(new SailingImageDTO(null, image));
            }
            for (VideoDTO video: videoList) {
                eventDto.getVideos().add(video);
                mediaDto.addVideo(new SailingVideoDTO(null, video));
            }
            updateEventDto(eventDto, callback);
        });
    }

    public void reloadMedia(Consumer<EventDTO> callback) {
        loadEventData(eventDto -> {
            setVideos(eventDto.getVideos());
            setImages(eventDto.getImages());
            callback.accept(eventDto);
        });
    }

    private void loadEventData(Consumer<EventDTO> callback) {
        sailingServiceWrite.getEventById(eventViewDto.getId(), true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(EventDTO eventDto) {
                callback.accept(eventDto);
            }

            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(i18n.errorWhileUpdatingEvent(), NotificationType.ERROR);
                logger.log(Level.SEVERE, "Cannot update event.", caught);
            }
        });
    }

    private void updateEventDto(EventDTO eventDto, Consumer<EventDTO> callback) {
        if (hasPermissions()) {
            sailingServiceWrite.updateEvent(eventDto, new AsyncCallback<EventDTO>() {
                @Override
                public void onSuccess(EventDTO eventDto) {
                    setEventDto(eventDto);
                    callback.accept(eventDto);
                    Notification.notify(i18n.updateEventSuccessfully(), NotificationType.SUCCESS);
                }

                @Override
                public void onFailure(Throwable caught) {
                    Notification.notify(i18n.error(), NotificationType.ERROR);
                    logger.log(Level.SEVERE, "Cannot update event. Video not added.", caught);
                }
            });
        }
    }

    /**
     * Check permission on default object (eventViewDTO from init).
     */
    public boolean hasPermissions() {
        final boolean hasPermission;
        if (userService.hasPermission(eventViewDto, HasPermissions.DefaultActions.UPDATE) || userService.hasPermission(eventViewDto, EventActions.UPLOAD_MEDIA)) {
            hasPermission = true;
        } else {
            hasPermission = false;
        }
        return hasPermission;
    }

    /**
     * Check permission on current EventDTO.
     */
    public boolean hasPermissions(EventDTO eventDto) {
        final boolean hasPermission;
        if (userService.hasPermission(eventDto, HasPermissions.DefaultActions.UPDATE) || userService.hasPermission(eventViewDto, EventActions.UPLOAD_MEDIA)) {
            hasPermission = true;
        } else {
            hasPermission = false;
        }
        return hasPermission;
    }

}
