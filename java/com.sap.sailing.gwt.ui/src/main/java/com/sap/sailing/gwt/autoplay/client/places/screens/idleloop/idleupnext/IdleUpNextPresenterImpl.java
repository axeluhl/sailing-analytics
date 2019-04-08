package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.idleupnext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayPresenterConfigured;
import com.sap.sailing.gwt.common.client.DateUtil;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.client.media.ImageDTO;

public class IdleUpNextPresenterImpl extends AutoPlayPresenterConfigured<IdleUpNextPlace>
        implements IdleUpNextView.IdleUpNextPresenter {
    private static final int SHOW_RACES_STARTED = 1000 * 60 * 30;
    private IdleUpNextView view;
    private Timer updateData;

    public IdleUpNextPresenterImpl(IdleUpNextPlace place, AutoPlayClientFactory clientFactory,
            IdleUpNextView slide2ViewImpl) {
        super(place, clientFactory);
        this.view = slide2ViewImpl;
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        updateEventImage();
        view.startingWith(this, panel);

        updateData = new Timer() {

            @Override
            public void run() {
                updateData();
            }
        };
        updateData.scheduleRepeating(1000);

        updateData();
    }

    protected void updateData() {
        ArrayList<Pair<RegattaAndRaceIdentifier, Date>> data = getPlace().getRaceToStartOfRace();
        if (data == null) {
            view.setData(null);
            return;
        }
        ArrayList<Pair<RegattaAndRaceIdentifier, Date>> filteredData = new ArrayList<>();
        for (Pair<RegattaAndRaceIdentifier, Date> raw : data) {
            if (DateUtil.isToday(raw.getB())) {
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
        updateData.cancel();
    }

    protected void updateEventImage() {
        List<SafeUri> teaserHighlight = new ArrayList<>();
        List<SafeUri> bigScreenImages = new ArrayList<>();
        for (ImageDTO imageDTO : getSlideCtx().getEvent().getImages()) {
            final List<String> tags = imageDTO.getTags();
            if (tags.contains(MediaTagConstants.BIGSCREEN.getName())) {
                bigScreenImages.add(UriUtils.fromString(imageDTO.getSourceRef()));
            } else if (tags.contains(MediaTagConstants.TEASER.getName()) || tags.contains(MediaTagConstants.HIGHLIGHT.getName())) {
                teaserHighlight.add(UriUtils.fromString(imageDTO.getSourceRef()));
            }
        }
        List<SafeUri> usedImages;
        if (bigScreenImages.isEmpty()) {
            if (teaserHighlight.isEmpty()) {
                usedImages = Collections
                        .singletonList(SharedHomeResources.INSTANCE.defaultStageEventTeaserImage().getSafeUri());
            } else {
                usedImages = teaserHighlight;
            }
        } else {
            usedImages = bigScreenImages;
        }

        if (!usedImages.isEmpty()) {
            int selected = 0;
            if (usedImages.size() > 1) {
                Random r = new Random();
                selected = r.nextInt(usedImages.size());
            }
            SafeUri imageToUseDTO = usedImages.get(selected);
            if (imageToUseDTO != null) {
                final StringBuilder thumbnailUrlBuilder = new StringBuilder("url('");
                thumbnailUrlBuilder.append(imageToUseDTO.asString());
                thumbnailUrlBuilder.append("')");
                view.setBackgroudImage(thumbnailUrlBuilder.toString());
            }
        }
    }
}
