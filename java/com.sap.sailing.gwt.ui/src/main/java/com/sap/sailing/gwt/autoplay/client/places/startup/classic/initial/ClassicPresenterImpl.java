package com.sap.sailing.gwt.autoplay.client.places.startup.classic.initial;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.classic.AutoPlayClientFactoryClassic;
import com.sap.sailing.gwt.autoplay.client.app.classic.ClassicPresenterBase;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.client.media.ImageDTO;

public class ClassicPresenterImpl extends ClassicPresenterBase<ClassicInitialPlace>
        implements ClassicInitialView.Presenter {
    private ClassicInitialView view;

    public ClassicPresenterImpl(ClassicInitialPlace place, AutoPlayClientFactoryClassic clientFactory,
            ClassicInitialView slideinitViewImpl) {
        super(place, clientFactory);
        this.view = slideinitViewImpl;
        updateEventImage();
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        // TODO
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
            view.setImage(thumbnailUrlBuilder.toString());
        }
    }
}
