package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;
import com.sap.sailing.gwt.home.communication.event.sixtyinch.GetSixtyInchStatisticAction;
import com.sap.sailing.gwt.home.communication.event.sixtyinch.GetSixtyInchStatisticDTO;

public class PreRaceRacemapPresenterImpl extends ConfiguredSlideBase<PreRaceRacemapPlace>
        implements PreRaceRacemapView.Slide7Presenter {
    private PreRaceRacemapView view;
    private Timer updateStatistics;

    public PreRaceRacemapPresenterImpl(PreRaceRacemapPlace place, AutoPlayClientFactorySixtyInch clientFactory,
            PreRaceRacemapView slide7ViewImpl) {
        super(place, clientFactory);
        this.view = slide7ViewImpl;

    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        if (getPlace().getError() != null) {
            view.showErrorNoLive(this, panel, getPlace().getError());
            return;
        }

        updateStatistics = new Timer() {

            @Override
            public void run() {
                getClientFactory().getDispatch()
                        .execute(
                                new GetSixtyInchStatisticAction(getPlace().getRace().getRaceName(),
                                        getPlace().getRace().getRegattaName()),
                        new AsyncCallback<GetSixtyInchStatisticDTO>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                caught.printStackTrace();
                            }

                            @Override
                            public void onSuccess(GetSixtyInchStatisticDTO result) {
                                view.updateStatistic(result, getSlideCtx().getEvent().getOfficialWebsiteURL());
                            }
                        });

            }
        };
        updateStatistics.scheduleRepeating(1000);
        view.startingWith(this, panel, getPlace().getRaceMap());
    }

    @Override
    public void onStop() {
        super.onStop();
        updateStatistics.cancel();
    }

}
