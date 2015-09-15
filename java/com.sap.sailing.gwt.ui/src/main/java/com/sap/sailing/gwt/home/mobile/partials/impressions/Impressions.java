package com.sap.sailing.gwt.home.mobile.partials.impressions;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.imagegallery.MobileFullscreenGallery;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.StatisticsBoxResources;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;
import com.sap.sse.gwt.client.controls.carousel.ImageCarousel;

public class Impressions extends Composite {
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    interface MyBinder extends UiBinder<Widget, Impressions> {
    }

    @UiField
    MobileSection mobileSectionUi;
    @UiField
    SectionHeaderContent headerUi;
    @UiField
    StringMessages i18n;

    public Impressions() {
        StatisticsBoxResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setClickDestinaton(final PlaceNavigation<?> placeNavigation) {
        headerUi.setClickAction(placeNavigation);
    }

    public void setStatistis(int nrOfImages, int nrOfVideos) {
        StringBuilder sb = new StringBuilder();
        if (nrOfImages > 0) {
            sb.append(i18n.photosCount(nrOfImages));
            if (nrOfVideos > 0) {
                sb.append(" | ");
            }
        }
        if (nrOfVideos > 0) {
            sb.append(i18n.videosCount(nrOfVideos));
        }

        headerUi.setSubtitle(sb.toString());
    }

    public void addImages(Collection<SailingImageDTO> images) {
        if (images.isEmpty()) {
            return;
        }
        GWT.log("Got " + images.size() + " images");
        ImageCarousel<SailingImageDTO> imageCarousel = new ImageCarousel<SailingImageDTO>();
        imageCarousel.registerFullscreenViewer(new MobileFullscreenGallery());
        int count = 0;
        for (SailingImageDTO imageDTO : images) {
            if (imageDTO.getHeightInPx() == null || imageDTO.getWidthInPx() == null) {
                GWT.log("Ignore image without size ");
                continue;
            }
            GWT.log("Adding " + imageDTO.getSourceRef());
            count++;
            imageCarousel.addImage(imageDTO);
        }
        if (count > 1) {
            mobileSectionUi.clearContent();
            mobileSectionUi.addContent(imageCarousel);
        }
    }

}
