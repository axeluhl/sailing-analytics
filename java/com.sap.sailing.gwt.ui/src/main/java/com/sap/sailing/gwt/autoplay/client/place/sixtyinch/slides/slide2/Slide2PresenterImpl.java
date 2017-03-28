package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2;

import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.client.media.ImageDTO;

public class Slide2PresenterImpl extends ConfiguredSlideBase<Slide2Place> implements Slide2View.Slide2Presenter {
    private Slide2View view;

    public Slide2PresenterImpl(Slide2Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide2View slide2ViewImpl) {
        super(place, clientFactory);
        this.view = slide2ViewImpl;
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        updateEventImage();
        view.startingWith(this, panel);
    }

    protected void updateEventImage() {
        if (getClientFactory().getSlideCtx() == null || getClientFactory().getSlideCtx().getEvent() == null) {
            return;
        }
        ImageDTO imageToUseDTO = null;
        for (ImageDTO imageDTO : getSlideCtx().getEvent().getImages()) {
            if (imageDTO.getTags().contains(MediaTagConstants.TEASER)) {
                imageToUseDTO = imageDTO;
                break;
            } else if (imageDTO.getTags().contains(MediaTagConstants.HIGHLIGHT)) {
                imageToUseDTO = imageDTO;
            } else if (imageToUseDTO == null) {
                imageToUseDTO = imageDTO;
            }
        }
        if (imageToUseDTO != null) {
            final StringBuilder thumbnailUrlBuilder = new StringBuilder("url('");
            thumbnailUrlBuilder.append(UriUtils.fromString(imageToUseDTO.getSourceRef()).asString());
            thumbnailUrlBuilder.append("')");
            view.setBackgroudImage(thumbnailUrlBuilder.toString());
        }
    }
}
