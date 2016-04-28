package com.sap.sailing.gwt.home.mobile.partials.imagegallery;

import java.util.Collection;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ImageGallery extends Composite {

    private static ImageGalleryUiBinder uiBinder = GWT.create(ImageGalleryUiBinder.class);
    
    interface ImageGalleryUiBinder extends UiBinder<MobileSection, ImageGallery> {
    }

    @UiField SectionHeaderContent sectionHeaderUi;
    @UiField FlowPanel firstColumnUi;
    @UiField FlowPanel secondColumnUi;
    private final MobileSection mobileSection;
    
    public ImageGallery() {
        ImageGalleryResources.INSTANCE.css().ensureInjected();
        initWidget(mobileSection = uiBinder.createAndBindUi(this));
        sectionHeaderUi.setSectionTitle(StringMessages.INSTANCE.images());
        sectionHeaderUi.initCollapsibility(mobileSection.getContentContainerElement(), true);
    }
    
    public void setImages(final Collection<SailingImageDTO> images) {
        sectionHeaderUi.setInfoText(StringMessages.INSTANCE.photosCount(images.size()));
        firstColumnUi.clear();
        secondColumnUi.clear();
        int imageCount = 0;
        for (final SailingImageDTO image : images) {
            FlowPanel container = ++imageCount % 2 != 0 ? firstColumnUi : secondColumnUi;
            ImageGalleryItem imageGalleryItem = new ImageGalleryItem(image);
            imageGalleryItem.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    new MobileFullscreenGallery().show(image, images);
                }
            });
            container.add(imageGalleryItem);
        }
    }
    
}
