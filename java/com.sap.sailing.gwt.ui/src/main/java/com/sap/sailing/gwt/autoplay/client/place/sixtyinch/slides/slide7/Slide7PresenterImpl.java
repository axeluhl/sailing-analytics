package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.RaceMapHelper;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;

public class Slide7PresenterImpl extends ConfiguredSlideBase<Slide7Place> implements Slide7View.Slide7Presenter {
    private Slide7View view;
    protected RaceMap raceMap;
    protected Throwable error;

    public Slide7PresenterImpl(Slide7Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide7View slide7ViewImpl) {
        super(place, clientFactory);
        this.view = slide7ViewImpl;

    }

    @Override
    protected void init(AcceptsOneWidget panel, Command whenReady) {
        RaceMapHelper.create(getClientFactory().getSailingService(), new AsyncActionsExecutor(),
                getClientFactory().getErrorReporter(),
                getClientFactory().getSlideCtx().getSettings().getLeaderBoardName(),
                getClientFactory().getSlideCtx().getSettings().getEventId(),
                getClientFactory().getSlideCtx().getEvent(), getEventBus(), getClientFactory().getDispatch(),
                new AsyncCallback<RaceMap>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        error = caught;
                        whenReady.execute();
                    }

                    @Override
                    public void onSuccess(RaceMap result) {
                        raceMap = result;
                        whenReady.execute();
                    }
                });
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        if (error != null) {
            view.showErrorNoLive(this, panel, error);
        } else {
            view.startingWith(this, panel, raceMap);
        }
    }
}
