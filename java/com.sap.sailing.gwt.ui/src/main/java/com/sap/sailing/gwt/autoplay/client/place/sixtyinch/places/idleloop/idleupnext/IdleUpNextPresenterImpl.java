package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.idleloop.idleupnext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredPresenter;
import com.sap.sailing.gwt.common.client.DateUtil;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.client.media.ImageDTO;

public class IdleUpNextPresenterImpl extends ConfiguredPresenter<IdleUpNextPlace>
        implements IdleUpNextView.IdleUpNextPresenter {
    private static final int SHOW_RACES_STARTED = 1000 * 60 * 30;
    private IdleUpNextView view;
    private Timer updateImage;
    private Timer updateData;

    public IdleUpNextPresenterImpl(IdleUpNextPlace place, AutoPlayClientFactorySixtyInch clientFactory,
            IdleUpNextView slide2ViewImpl) {
        super(place, clientFactory);
        this.view = slide2ViewImpl;
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        updateEventImage();
        view.startingWith(this, panel);
        updateImage = new Timer() {

            @Override
            public void run() {
                updateEventImage();
            }
        };
        updateImage.scheduleRepeating(30000);

        updateData = new Timer() {

            @Override
            public void run() {
                updateData();
            }
        };
        updateImage.scheduleRepeating(60000);
        updateData.scheduleRepeating(1000);

        updateData();
    }

    protected void updateData() {
        ArrayList<Pair<RegattaAndRaceIdentifier, Date>> data = getPlace().getRaceToStartOfRace();
        if (data == null) {
            return;
        }
        ArrayList<Pair<RegattaAndRaceIdentifier, Date>> filteredData = new ArrayList<>();
        for (Pair<RegattaAndRaceIdentifier, Date> raw : data) {
            if (DateUtil.isToday(raw.getB()) || DateUtil.daysFromNow(raw.getB()) < 2) {
                Date now = new Date();
                long diffInMillis = raw.getB().getTime() - now.getTime();
                if (diffInMillis > 0 || diffInMillis < SHOW_RACES_STARTED) {
                    filteredData.add(raw);
                }
            }
        }

        // filter past events here
        view.setData(filteredData);
    }

    @Override
    public void onStop() {
        super.onStop();
        updateImage.cancel();
        updateData.cancel();
    }

    protected void updateEventImage() {
        if (getClientFactory().getSlideCtx() == null || getClientFactory().getSlideCtx().getEvent() == null) {
            return;
        }
        List<ImageDTO> images = new ArrayList<>();
        for (ImageDTO imageDTO : getSlideCtx().getEvent().getImages()) {
            if (imageDTO.getTags().contains(MediaTagConstants.TEASER)) {
                images.add(imageDTO);
            } else if (imageDTO.getTags().contains(MediaTagConstants.HIGHLIGHT)) {
                images.add(imageDTO);
            }
        }
        if (images.isEmpty()) {
            // add any image that might work in this case
            images.addAll(getSlideCtx().getEvent().getImages());
        }
        if (!images.isEmpty()) {
            int selected = 0;
            if (images.size() > 1) {
                Random r = new Random();
                selected = r.nextInt(images.size() - 1);
            }
            GWT.log("Selecting " + selected + " from " + images.size());
            ImageDTO imageToUseDTO = images.get(selected);
            if (imageToUseDTO != null) {
                final StringBuilder thumbnailUrlBuilder = new StringBuilder("url('");
                thumbnailUrlBuilder.append(UriUtils.fromString(imageToUseDTO.getSourceRef()).asString());
                thumbnailUrlBuilder.append("')");
                view.setBackgroudImage(thumbnailUrlBuilder.toString());
            }
        }
    }
}
