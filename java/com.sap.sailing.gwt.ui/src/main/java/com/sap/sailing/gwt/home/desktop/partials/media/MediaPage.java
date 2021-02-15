package com.sap.sailing.gwt.home.desktop.partials.media;

import java.util.UUID;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
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
import com.sap.sailing.gwt.home.communication.media.SailingVideoDTO;
import com.sap.sailing.gwt.home.desktop.partials.uploadpopup.DesktopMediaUploadPopup;
import com.sap.sailing.gwt.home.shared.partials.placeholder.InfoPlaceholder;
import com.sap.sailing.gwt.home.shared.partials.videoplayer.VideoWithLowerThird;
import com.sap.sailing.gwt.ui.client.SailingServiceHelper;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.GalleryImageHolder;
import com.sap.sailing.gwt.ui.client.media.VideoThumbnail;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.client.UserService;

/**
 * Desktop page to show videos and images as a gallery.
 */
public class MediaPage extends Composite {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private static MediaPageUiBinder uiBinder = GWT.create(MediaPageUiBinder.class);

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
    private final UserService userService;
    private final SailingServiceWriteAsync sailingServiceWrite;
    private final UUID eventId;
    private VideoWithLowerThird videoDisplayUi;

    @UiHandler("videoSettingsButton")
    public void handleVideoSettingsButtonClick(ClickEvent e) {
        manageVideos = !manageVideos;
        setVideosManaged(manageVideos);
    }
    
    private void setVideosManaged(boolean managed) {
        if (managed) {
            videoSettingsButton.addStyleName(local_res.css().active());
        } else {
            videoSettingsButton.removeStyleName(local_res.css().active());
        }
        for (int i = 0; i < videosListUi.getWidgetCount(); i++) {
            if (videosListUi.getWidget(i) instanceof VideoThumbnail) {
                VideoThumbnail thumb = (VideoThumbnail) videosListUi.getWidget(i);
                thumb.setManageable(managed);
            }
        }
    }

    @UiHandler("photoSettingsButton")
    public void handlePhotoSettingsButtonClick(ClickEvent e) {
        managePhotos = !managePhotos;
        setPhotosManaged(managePhotos);
    }
    
    private void setPhotosManaged(boolean managed) {
        if (managed) {
            photoSettingsButton.addStyleName(local_res.css().active());
        } else {
            photoSettingsButton.removeStyleName(local_res.css().active());
        }
        for (int i = 0; i < photoListOuterBoxUi.getWidgetCount(); i++) {
            if (photoListOuterBoxUi.getWidget(i) instanceof GalleryImageHolder) {
                GalleryImageHolder gih = (GalleryImageHolder) photoListOuterBoxUi.getWidget(i);
                gih.setManageable(managed);
            }
        }
    }

    @UiHandler("mediaAddButton")
    public void handleMediaAddButtonClick(ClickEvent e) {
        popupHolder.clear();
        DesktopMediaUploadPopup popup = new DesktopMediaUploadPopup(sailingServiceWrite, eventId);
        popupHolder.add(popup);
        popup.center();
    }
    
    public MediaPage(IsWidget initialView, EventBus eventBus, UserService userService, UUID eventId) {
        this.eventId = eventId;
        sailingServiceWrite =  SailingServiceHelper.createSailingServiceWriteInstance();
        MediaPageResources.INSTANCE.css().ensureInjected();
        this.userService = userService;
        contentPanel = new SimplePanel();
        contentPanel.setWidget(initialView);
        initWidget(contentPanel);
        popupHolder = new FlowPanel();
        
        eventBus.addHandler(AuthenticationContextEvent.TYPE, event->{
            // for some reason this event is only send after logout. Never the less it will also handle login.
            AuthenticationContext authContext = event.getCtx();
            if (authContext.getCurrentUser() != null && !authContext.getCurrentUser().getName().equals("Anonymous")) {
                setMediaManaged(true);
            } else {
                setMediaManaged(false);
            }
        });
    }

    public void setMedia(final MediaDTO media) {
        Widget mediaUi = uiBinder.createAndBindUi(this);
        int photosCount = media.getPhotos().size();
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

            for (final SailingImageDTO holder : media.getPhotos()) {
                if (holder.getSourceRef() != null) {

                    GalleryImageHolder gih = new GalleryImageHolder(holder);
                    gih.addStyleName(photoCss);
                    gih.addStyleName(res.mediaCss().columns());

                    photoListOuterBoxUi.add(gih);
                    gih.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            if (!managePhotos) {
                                new SailingFullscreenViewer().show(holder, media.getPhotos());
                            }
                        }
                    });
                }
            }
        }
        int videoCount = media.getVideos().size();
        logger.info("show vidoes: " + videoCount);
        if (videoCount > 0) {
            videoSectionUi.getStyle().clearDisplay();
            if (videoCount == 1) {
                videoDisplayOuterBoxUi.addClassName(res.mediaCss().large12());
                videoListOuterBoxUi.getStyle().setDisplay(Display.NONE);
            } else if (videoCount > 1) {
                videoDisplayOuterBoxUi.addClassName(res.mediaCss().large9());
                videoListOuterBoxUi.addClassName(res.mediaCss().large3());
            }
            boolean first = true;
            for (final SailingVideoDTO videoCandidateInfo : media.getVideos()) {
                if (first) {
                    putVideoOnDisplay(videoCandidateInfo, false);
                    first = false;
                }
                if (videoCount > 1) {
                    VideoThumbnail thumbnail = new VideoThumbnail(videoCandidateInfo);
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

        UserDTO currentUser = userService.getCurrentUser();
        if (currentUser != null && !currentUser.getName().equals("Anonymous")) {
            setMediaManaged(true);
        } else {
            setMediaManaged(false);
        }
    }

    /**
     * Shows a selected video in the big viewer.
     * 
     * @param video the video to show in the big viewer
     * @param autoplay true, if the video should play automatically, false otherwise
     */
    private void putVideoOnDisplay(final SailingVideoDTO video, boolean autoplay) {
        videoDisplayUi = new VideoWithLowerThird(true, autoplay);
        videoDisplayUi.setVideo(video);
        videoDisplayHolderUi.setWidget(videoDisplayUi);
    }
    
    private void setMediaManaged(boolean managed) {
        mediaAddButton.setVisible(managed);
        photoSettingsButton.setVisible(managed);
        videoSettingsButton.setVisible(managed);
        if (!managed) {
            managePhotos = false;
            setPhotosManaged(managePhotos);
            manageVideos = false;
            setVideosManaged(manageVideos);
        }
    }
}
