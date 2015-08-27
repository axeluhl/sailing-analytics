package com.sap.sailing.gwt.home.mobile.partials.imagegallery;

import java.util.Collection;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.media.ImageDTO;

public class ImageGallery extends Composite {

    private static ImageGalleryUiBinder uiBinder = GWT.create(ImageGalleryUiBinder.class);
    
    interface ImageGalleryUiBinder extends UiBinder<MobileSection, ImageGallery> {
    }

    @UiField SectionHeaderContent sectionHeaderUi;
    @UiField DivElement firstColumnUi;
    @UiField DivElement secondColumnUi;
    
    public ImageGallery() {
        ImageGalleryResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        sectionHeaderUi.setSectionTitle(StringMessages.INSTANCE.images());
    }
    
    public void setImages(Collection<? extends ImageDTO> images) {
        sectionHeaderUi.setInfoText(StringMessages.INSTANCE.photosCount(images.size()));
        firstColumnUi.removeAllChildren();
        secondColumnUi.removeAllChildren();
        int imageCount = 0;
        for (ImageDTO image : images) {
            DivElement container = ++imageCount % 2 != 0 ? firstColumnUi : secondColumnUi;
            container.appendChild(new ImageGalleryItem(image).getElement());
        }
    }
    
}
