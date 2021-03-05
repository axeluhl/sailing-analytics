package com.sap.sailing.gwt.home.desktop.partials.media;

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.home.desktop.partials.uploadpopup.DesktopMediaUploadPopup;
import com.sap.sailing.gwt.home.shared.partials.placeholder.InfoPlaceholder;
import com.sap.sailing.gwt.home.shared.partials.videoplayer.VideoWithLowerThird;
import com.sap.sailing.gwt.ui.client.SailingServiceHelper;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.GalleryImageHolder;
import com.sap.sailing.gwt.ui.client.media.VideoThumbnail;
import com.sap.sailing.gwt.ui.shared.ManageMediaContainer;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.client.UserService;

/**
 * Desktop page to show videos and images as a gallery.
 */
public class MediaPage extends Composite {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private static MediaPageUiBinder uiBinder = GWT.create(MediaPageUiBinder.class);
    
    private final ManageMediaContainer container;

    interface MediaPageUiBinder extends UiBinder<Widget, MediaPage> {
    }

    @UiField
    SharedResources res;
    @UiField
    MediaPageResources local_res;
    @UiField
    DivElement videoSectionUi;
    @UiField
    DivElement videoDisplayOuterBoxUi;
    @UiField
    DivElement videoListOuterBoxUi;
    @UiField
    FlowPanel videosListUi;
    @UiField
    SimplePanel videoDisplayHolderUi;
    @UiField
    DivElement photoSectionUi;
    @UiField
    FlowPanel photoListOuterBoxUi;
    
    @UiField
    Button videoSettingsButton;
    @UiField
    Button photoSettingsButton;
    @UiField
    Button mediaAddButton;
    @UiField
    StringMessages i18n;
    
    private boolean manageVideos;
    private boolean managePhotos;
    private final SimplePanel contentPanel;
    private final FlowPanel popupHolder;
    private VideoWithLowerThird videoDisplayUi;

    @UiHandler("videoSettingsButton")
    public void handleVideoSettingsButtonClick(ClickEvent e) {
        manageVideos = !manageVideos;
        setVideosManaged(manageVideos);
    }
    
    private void setVideosManaged(boolean managed) {
        if (videoSettingsButton != null) {
            if (managed) {
                videoSettingsButton.addStyleName(local_res.css().active());
            } else {
                videoSettingsButton.removeStyleName(local_res.css().active());
            }
        }
        if (videosListUi != null) {
            for (int i = 0; i < videosListUi.getWidgetCount(); i++) {
                if (videosListUi.getWidget(i) instanceof VideoThumbnail) {
                    VideoThumbnail thumb = (VideoThumbnail) videosListUi.getWidget(i);
                    thumb.setManageable(managed);
                }
            }
        }
    }

    @UiHandler("photoSettingsButton")
    public void handlePhotoSettingsButtonClick(ClickEvent e) {
        managePhotos = !managePhotos;
        setPhotosManaged(managePhotos);
    }
    
    private void setPhotosManaged(boolean managed) {
        if (photoSettingsButton != null) {
            if (managed) {
                photoSettingsButton.addStyleName(local_res.css().active());
            } else {
                photoSettingsButton.removeStyleName(local_res.css().active());
            }
        }
        if (photoListOuterBoxUi != null) {
            for (int i = 0; i < photoListOuterBoxUi.getWidgetCount(); i++) {
                if (photoListOuterBoxUi.getWidget(i) instanceof GalleryImageHolder) {
                    GalleryImageHolder gih = (GalleryImageHolder) photoListOuterBoxUi.getWidget(i);
                    gih.setManageable(managed);
                }
            }
        }
    }

    @UiHandler("mediaAddButton")
    public void handleMediaAddButtonClick(ClickEvent e) {
        popupHolder.clear();
        DesktopMediaUploadPopup popup = new DesktopMediaUploadPopup(video -> {
            container.addVideo(video, eventDto -> updateMedia());
        }, image -> {
            container.addImage(image, eventDto -> updateMedia());
        });
        popupHolder.add(popup);
        popup.center();
    }
    
    public MediaPage(IsWidget initialView, EventBus eventBus, UserService userService, UUID eventId) {
        SailingServiceWriteAsync sailingServiceWrite = SailingServiceHelper.createSailingServiceWriteInstance();
        MediaPageResources.INSTANCE.css().ensureInjected();
        container = new ManageMediaContainer(sailingServiceWrite, userService, eventId);
        contentPanel = new SimplePanel();
        contentPanel.setWidget(initialView);
        initWidget(contentPanel);
        popupHolder = new FlowPanel();
        
        eventBus.addHandler(AuthenticationContextEvent.TYPE, event->{
            logger.info("Sign out");
            // for some reason this event is only send after logout. Never the less it will also handle login.
            container.checkCurrentUserPermission(permitted -> setMediaManaged(permitted));
        });
    }
    
    public void setMedia(final MediaDTO media) {
        container.setVideos(media.getVideos());
        container.setImages(media.getPhotos());
        updateMedia();
    }
    
    private void updateMedia() {
        container.checkCurrentUserPermission(permitted -> setMediaManaged(permitted));
        logger.info("updateMedia");
        Widget mediaUi = uiBinder.createAndBindUi(this);
        int photosCount = container.getImages().size();
        photoListOuterBoxUi.clear();
        if (photosCount > 0) {
            photoSectionUi.getStyle().clearDisplay();
            String photoCss = null;

            // To make the image gallery look good, we use different styling if there are only few (<7) images
            // available. If there are more images, always 4 images are shown in a row.
            switch (photosCount) {
            case 1:
                photoCss = res.mediaCss().medium12();
                break;
            case 2:
                photoCss = res.mediaCss().medium6();
                break;
            case 3:
                photoCss = res.mediaCss().medium4();
                break;
            case 4:
                photoCss = res.mediaCss().medium6();
                break;
            case 5:
            case 6:
                photoCss = res.mediaCss().medium4();
                break;
            default:
                photoCss = res.mediaCss().medium3();
                break;
            }

            for (final ImageDTO holder : container.getImages()) {
                if (holder.getSourceRef() != null) {

                    GalleryImageHolder gih = new GalleryImageHolder(holder, getDeleteImageHandler(holder));
                    gih.addStyleName(photoCss);
                    gih.addStyleName(res.mediaCss().columns());

                    photoListOuterBoxUi.add(gih);
                    gih.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            if (!managePhotos) {
                                Collection<SailingImageDTO> sailingImageDTOs = container.getImages().stream().map(imageDto -> {
                                    if (imageDto instanceof SailingImageDTO) {
                                        return (SailingImageDTO) imageDto;
                                    }
                                   return new SailingImageDTO(null, imageDto);
                                }).collect(Collectors.toList());
                                final SailingImageDTO showImage = sailingImageDTOs.stream()
                                        .filter(sailingImageDto -> sailingImageDto.compareTo(holder) == 0)
                                        .findFirst().orElse(new SailingImageDTO(null, holder));
                                new SailingFullscreenViewer().show(showImage, sailingImageDTOs);
                            }
                        }
                    });
                }
            }
        }
        int videoCount = container.getVideos().size();
        videosListUi.clear();
        if (videoCount > 0) {
            videoSectionUi.getStyle().clearDisplay();
            videoListOuterBoxUi.removeClassName(res.mediaCss().large3());
            videoListOuterBoxUi.getStyle().clearDisplay();
            if (videoCount == 1) {
                videoDisplayOuterBoxUi.addClassName(res.mediaCss().large12());
                videoListOuterBoxUi.getStyle().setDisplay(Display.NONE);
            } else if (videoCount > 1) {
                videoDisplayOuterBoxUi.addClassName(res.mediaCss().large9());
                videoListOuterBoxUi.addClassName(res.mediaCss().large3());
            }
            boolean first = true;
            for (final VideoDTO videoCandidateInfo : container.getVideos()) {
                if (first) {
                    putVideoOnDisplay(videoCandidateInfo, false);
                    first = false;
                }
                if (videoCount > 1) {
                    VideoThumbnail thumbnail = new VideoThumbnail(videoCandidateInfo, getDeleteVideoHandler(videoCandidateInfo), null);
                    thumbnail.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            if (!manageVideos) {
                                putVideoOnDisplay(videoCandidateInfo, true);
                            }
                        }
                    });
                    videosListUi.add(thumbnail);
                }
            }
        }
        if (photosCount == 0 && videoCount == 0) {
            contentPanel.setWidget(new InfoPlaceholder(i18n.mediaNoContent()));
        } else {
            contentPanel.setWidget(mediaUi);
        }
    }
    
    private ClickHandler getDeleteVideoHandler(VideoDTO videoCandidateInfo) {
        return new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if (Window.confirm("Do you really want to delete the video")) {
                    container.deleteVideo(videoCandidateInfo, eventDto -> updateMedia());
                    
                }
            }
        };
    }
    
    private ClickHandler getDeleteImageHandler(ImageDTO imageCandidateInfo) {
        return new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                // TODO: translation
                if (Window.confirm("Do you really want to delete the image")) {
                    container.deleteImage(imageCandidateInfo, eventDto -> updateMedia());
                }
            }
        };
    }

    /**
     * Shows a selected video in the big viewer.
     * 
     * @param video the video to show in the big viewer
     * @param autoplay true, if the video should play automatically, false otherwise
     */
    private void putVideoOnDisplay(final VideoDTO video, boolean autoplay) {
        videoDisplayUi = new VideoWithLowerThird(true, autoplay);
        videoDisplayUi.setVideo(video);
        videoDisplayHolderUi.setWidget(videoDisplayUi);
    }
    
    private void setMediaManaged(boolean managed) {
        logger.info("setMediaManaged " + managed);
        if (mediaAddButton != null) {
            logger.info("mediaAddButton != null");
            mediaAddButton.setVisible(managed);
            photoSettingsButton.setVisible(managed);
            videoSettingsButton.setVisible(managed);
            managePhotos = false;
            setPhotosManaged(managePhotos);
            manageVideos = false;
            setVideosManaged(manageVideos);
        }
    }
}
