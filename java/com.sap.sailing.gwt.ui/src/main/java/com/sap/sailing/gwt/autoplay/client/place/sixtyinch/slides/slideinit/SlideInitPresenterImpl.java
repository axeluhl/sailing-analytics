package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideBase;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;

public class SlideInitPresenterImpl extends SlideBase<SlideInitPlace> implements SlideInitView.SlideInitPresenter {

    private SlideInitView view;

    public SlideInitPresenterImpl(SlideInitPlace place, AutoPlayClientFactorySixtyInch clientFactory,
            SlideInitView slideinitViewImpl) {
        super(place, clientFactory);
        this.view = slideinitViewImpl;
        updateEventImage();
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        eventBus.fireEvent(
                new SlideHeaderEvent(getSlideCtx().getSettings().getLeaderBoardName(), "Loading event data"));

        eventBus.addHandler(EventChanged.TYPE, new EventChanged.Handler() {
            @Override
            public void onEventChanged(EventChanged e) {
                updateEventImage();

            }
        });

        view.startingWith(this, panel);
    }

    protected void updateEventImage() {
        GWT.log("Updating image");
        String thumbnailImageUrl = null;
        if (getSlideCtx().getEvent() != null) {
            thumbnailImageUrl = getSlideCtx().getEvent().getImages().get(0).getSourceRef();
        }
        final StringBuilder thumbnailUrlBuilder = new StringBuilder("url('");
        if (thumbnailImageUrl == null || thumbnailImageUrl.isEmpty()) {
            thumbnailUrlBuilder
                    .append(SharedHomeResources.INSTANCE.defaultStageEventTeaserImage().getSafeUri().asString());
        } else {
            thumbnailUrlBuilder.append(UriUtils.fromString(thumbnailImageUrl).asString());
        }
        thumbnailUrlBuilder.append("')");
        view.setImage(thumbnailUrlBuilder.toString());
    }
}
