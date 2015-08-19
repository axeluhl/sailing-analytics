package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.impressions.Impressions;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.home.mobile.partials.regattaStatus.RegattaStatus;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.StatisticsBox;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.UpdatesBox;
import com.sap.sailing.gwt.home.mobile.places.event.overview.EventOverviewStage;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewNewsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewStageAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventStatisticsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattasAndLiveRacesForEventAction;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;

public class EventViewImpl extends AbstractEventView<EventView.Presenter> implements EventView {
    
    private static final StringMessages MSG = StringMessages.INSTANCE;
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, EventViewImpl> {
    }

    @UiField(provided = true) EventOverviewStage overviewStageUi;
    @UiField SimplePanel listContentUi;
    @UiField Impressions impressionsUi;
    @UiField(provided = true) StatisticsBox statisticsBoxUi;
    @UiField(provided = true) UpdatesBox updatesBoxUi;

    public EventViewImpl(EventView.Presenter presenter) {
        super(presenter, false, false);
        this.setupOverviewStage();
        this.setupUpdateBox();
        this.setupStatisticsBox();
        setViewContent(uiBinder.createAndBindUi(this));
        this.setupListContent();
        impressionsUi.getElement().getStyle().setDisplay(Display.NONE);
    }
    
    private void setupOverviewStage() {
        overviewStageUi = new EventOverviewStage(currentPresenter);
        refreshManager.add(overviewStageUi, new GetEventOverviewStageAction(getEventId()));
    }
    
    private void setupListContent() {
        if (isMultiRegattaEvent()) {
            RegattaStatus regattaStatus = new RegattaStatus(currentPresenter);
            listContentUi.add(regattaStatus);
            refreshManager.add(regattaStatus, new GetRegattasAndLiveRacesForEventAction(getEventId()));
        } else {
            MinileaderboardBox miniLeaderboard = new MinileaderboardBox(false);
            miniLeaderboard.setAction(MSG.showAll(), currentPresenter.getRegattaMiniLeaderboardNavigation(getRegattaId()));
            listContentUi.add(miniLeaderboard);
            refreshManager.add(miniLeaderboard, new GetMiniLeaderbordAction(getEventId(), getRegattaId(), 3));
        }
    }
    
    private void setupUpdateBox() {
        updatesBoxUi = new UpdatesBox(currentPresenter, refreshManager);
        if (currentPresenter.getCtx().getEventDTO().getState() == EventState.RUNNING) {
            refreshManager.add(updatesBoxUi, new GetEventOverviewNewsAction(getEventId(), 2));
        } else {
            updatesBoxUi.removeFromParent();
        }
    }
    
    private void setupStatisticsBox() {
        statisticsBoxUi = new StatisticsBox(isMultiRegattaEvent());
        refreshManager.add(statisticsBoxUi, new GetEventStatisticsAction(getEventId()));
    }

    @Override
    public void setMediaForImpressions(int nrOfImages, int nrOfVideos, Collection<SailingImageDTO> images) {
        impressionsUi.getElement().getStyle().setDisplay(Display.BLOCK);
        impressionsUi.setStatistis(nrOfImages, nrOfVideos);
        impressionsUi.addImages(images);
        // TODO: desktop media navigation
        impressionsUi.setClickDestinaton(currentPresenter.getMediaPageNavigation());
    }
}
