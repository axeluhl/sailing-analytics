package com.sap.sailing.gwt.home.mobile.partials.imagegallery;

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

public class ImageGallery extends Composite {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private static ImageGalleryUiBinder uiBinder = GWT.create(ImageGalleryUiBinder.class);
    
    interface ImageGalleryUiBinder extends UiBinder<MobileSection, ImageGallery> {
    }

    @UiField SectionHeaderContent sectionHeaderUi;
    @UiField FlowPanel firstColumnUi;
    @UiField FlowPanel secondColumnUi;
    private final MobileSection mobileSection;
    private boolean managed;
    
    public ImageGallery() {
        //photoUploadUi.getElement().setAttribute("capture", "camera");
        ImageGalleryResources.INSTANCE.css().ensureInjected();
        initWidget(mobileSection = uiBinder.createAndBindUi(this));
        sectionHeaderUi.setSectionTitle(StringMessages.INSTANCE.images());
        sectionHeaderUi.initCollapsibility(mobileSection.getContentContainerElement(), true);
        sectionHeaderUi.addManageButtonClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                setMediaManaged(!managed);
                event.stopPropagation();
            }
        });
    }
    
    public void setImages(final Collection<SailingImageDTO> images, final UUID eventId, 
            final SailingServiceWriteAsync sailingServiceWrite) {
        sectionHeaderUi.setInfoText(StringMessages.INSTANCE.photosCount(images.size()));
        firstColumnUi.clear();
        secondColumnUi.clear();
        int imageCount = 0;
        for (final SailingImageDTO image : images) {
            FlowPanel container = ++imageCount % 2 != 0 ? firstColumnUi : secondColumnUi;
            
            ImageGalleryItem imageGalleryItem = new ImageGalleryItem(image, createDeleteHandler(image, eventId, sailingServiceWrite));
            imageGalleryItem.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    new MobileFullscreenGallery().show(image, images);
                }
            });
            container.add(imageGalleryItem);
        }
    }
    
    public void setManageButtonsVisible(boolean visible) {
        sectionHeaderUi.setManageButtonVisible(visible);
    }
    
    public void setMediaManaged(boolean managed) {
        this.managed = managed;
        for (int i = 0; i < firstColumnUi.getWidgetCount(); i++) {
            ImageGalleryItem item = (ImageGalleryItem) firstColumnUi.getWidget(i);
            item.manageMedia(managed);
        }
        for (int i = 0; i < secondColumnUi.getWidgetCount(); i++) {
            ImageGalleryItem item = (ImageGalleryItem) secondColumnUi.getWidget(i);
            item.manageMedia(managed);
        }
        sectionHeaderUi.setManageButtonActive(managed);
    }
    
    private ClickHandler createDeleteHandler(final SailingImageDTO image, final UUID eventId, 
            final SailingServiceWriteAsync sailingServiceWrite) {
        ClickHandler clickHandler =  new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                if (Window.confirm("Do you really want to delete the image")) {
                    sailingServiceWrite.getEventById(eventId, true, new AsyncCallback<EventDTO>() {
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
            }
        };
        return clickHandler;
    }
}
