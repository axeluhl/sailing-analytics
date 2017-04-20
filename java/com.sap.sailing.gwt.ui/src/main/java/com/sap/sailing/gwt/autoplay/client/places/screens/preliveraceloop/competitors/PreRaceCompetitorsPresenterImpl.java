package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.competitors;

import java.util.List;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredPresenter;

public class PreRaceCompetitorsPresenterImpl extends ConfiguredPresenter<AbstractPreRaceCompetitorsPlace>
        implements PreRaceCompetitorsView.PreRaceCompetitorsPresenter {
    static final int DELAY_NEXT = 2000;
    private PreRaceCompetitorsView view;
    private Timer selectionScroll;

    public PreRaceCompetitorsPresenterImpl(AbstractPreRaceCompetitorsPlace place, AutoPlayClientFactorySixtyInch clientFactory,
            PreRaceCompetitorsView slide4ViewImpl) {
        super(place, clientFactory);
        this.view = slide4ViewImpl;
        selectionScroll = new Timer() {

            @Override
            public void run() {
                view.move();
            }
        };
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        view.startingWith(this, panel);
        List<CompetitorDTO> data = null;
        view.setCompetitors(data);
        selectionScroll.scheduleRepeating(DELAY_NEXT);
    }
}
