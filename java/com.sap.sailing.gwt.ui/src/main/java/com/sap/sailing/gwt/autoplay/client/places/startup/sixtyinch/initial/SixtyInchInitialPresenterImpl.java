package com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.initial;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.SixtyInchPresenterBase;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.config.SixtyInchConfigPlace;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.client.media.ImageDTO;

public class SixtyInchInitialPresenterImpl extends SixtyInchPresenterBase<SixtyInchInitialPlace> implements SixtyInchInitialView.Presenter {
    private SixtyInchInitialView view;

    public SixtyInchInitialPresenterImpl(SixtyInchInitialPlace place, AutoPlayClientFactorySixtyInch clientFactory,
            SixtyInchInitialView slideinitViewImpl) {
        super(place, clientFactory);
        this.view = slideinitViewImpl;
        updateEventImage();
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {

        if (getClientFactory().getSlideCtx() == null || getClientFactory().getSlideCtx().getEvent() != null) {
            getClientFactory().getPlaceController().goTo(new SixtyInchConfigPlace());
            return;
        }
        eventBus.addHandler(EventChanged.TYPE, new EventChanged.Handler() {
            @Override
            public void onEventChanged(EventChanged e) {
                if (getClientFactory().getSlideCtx() != null) {
                    updateEventImage();
                }
            }
        });
        if (getSlideCtx() != null && getSlideCtx().getSettings() != null) {
            eventBus.fireEvent(
                    new AutoPlayHeaderEvent(getSlideCtx().getSettings().getLeaderboardName(), "Loading event data"));
        }
        view.startingWith(this, panel);
        if (getPlace().getFailureEvent() != null) {
            view.showFailure(getPlace().getFailureEvent(), new Command() {
                @Override
                public void execute() {
                    if (getPlace().getCurrentSlideConfig() != null) {
                        GWT.log("sideinit presenter: doContinue");

                    }
                }
            }, new Command() {
                @Override
                public void execute() {
                    GWT.log("sideinit presenter: doContinue");
                    getClientFactory().getPlaceController().goTo(new SixtyInchConfigPlace());
                }
            });
        }
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
