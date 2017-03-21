package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.TimedTransitionSimpleNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.RaceMapHelper;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.Slide7Place;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;

public class Slide7Node extends TimedTransitionSimpleNode {
    private final AutoPlayClientFactorySixtyInch cf;

    public Slide7Node(AutoPlayClientFactorySixtyInch cf) {
        super("slide7", 10000);
        this.cf = cf;

    }

    public void onStart() {
        RaceMapHelper.create(cf.getSailingService(), new AsyncActionsExecutor(), cf.getErrorReporter(),
                cf.getSlideCtx().getSettings().getLeaderBoardName(), cf.getSlideCtx().getSettings().getEventId(),
                cf.getSlideCtx().getEvent(), cf.getEventBus(), cf.getDispatch(), new AsyncCallback<RaceMap>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Slide7Place place = new Slide7Place();
                        place.setError(caught);
                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                    }

                    @Override
                    public void onSuccess(RaceMap result) {
                        Slide7Place place = new Slide7Place();
                        place.setRaceMap(result);
                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                    }
                });
    };
}
