package com.sap.sailing.gwt.home.mobile.partials.imagegallery;

import java.util.Collection;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
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
    private final MobileSection mobileSection;
    
    public ImageGallery() {
        ImageGalleryResources.INSTANCE.css().ensureInjected();
        initWidget(mobileSection = uiBinder.createAndBindUi(this));
        sectionHeaderUi.setSectionTitle(StringMessages.INSTANCE.images());
    }
    
    public void setImages(Collection<ImageDTO> images) {
        sectionHeaderUi.setInfoText(StringMessages.INSTANCE.imagesCount(images.size()));
        mobileSection.clearContent();
        if (images.isEmpty()) {
            mobileSection.addContent(getNoImagesInfoWidget());
        } else {
            int imageCount = 0;
            for (ImageDTO image : images) {
                DivElement container = ++imageCount % 2 != 0 ? firstColumnUi : secondColumnUi;
                container.appendChild(new ImageGalleryItem(image).getElement());
            }
        }
    }
    
    private Widget getNoImagesInfoWidget() {
        Label label = new Label(StringMessages.INSTANCE.noImages());
        label.getElement().getStyle().setPadding(1, Unit.EM);
        label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        label.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
        return label;
    }

}
