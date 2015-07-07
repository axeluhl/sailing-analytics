package com.sap.sailing.gwt.home.mobile.places.series;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.recents.EventsOverviewRecentYearEvent;
import com.sap.sailing.gwt.home.mobile.partials.seriesheader.SeriesHeader;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniOverallLeaderbordAction;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;

public class SeriesViewImpl extends Composite implements SeriesView {
    private static final StringMessages MSG = StringMessages.INSTANCE;
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, SeriesViewImpl> {
    }

    @UiField(provided = true) SeriesHeader eventHeaderUi;
    @UiField Quickfinder quickFinderUi;
    @UiField MinileaderboardBox leaderboardUi;
    @UiField FlowPanel eventsUi;
//    @UiField(provided = true) StatisticsBox statisticsBoxUi;

    private final Presenter currentPresenter;
    private final RefreshManager refreshManager;

    public SeriesViewImpl(SeriesView.Presenter presenter) {
        this.currentPresenter = presenter;
        this.refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        EventSeriesViewDTO series = currentPresenter.getCtx().getSeriesDTO();
        eventHeaderUi = new SeriesHeader(series);
//        this.setupStatisticsBox(event);
        initWidget(uiBinder.createAndBindUi(this));
        this.setupListContent(series);
        this.setupEventListContent(series);
    }
    
    private void setupEventListContent(EventSeriesViewDTO series) {
        boolean first = true;
        for(EventMetadataDTO eventOfSeries : series.getEvents()) {
            if(eventOfSeries.getState() == EventState.PLANNED) {
                continue;
            }
            boolean teaser = first || eventOfSeries.getState() == EventState.UPCOMING || eventOfSeries.getState() == EventState.RUNNING;
            EventsOverviewRecentYearEvent eventTeaser = new EventsOverviewRecentYearEvent(currentPresenter.getEventNavigation(eventOfSeries.getId().toString()), eventOfSeries, eventOfSeries.getState().getStateMarker(), teaser );
            eventsUi.add(eventTeaser);
            first = false;
        }
    }
    
    @Override
    public void setQuickFinderValues(String seriesName, Collection<EventMetadataDTO> eventsOfSeries) {
        new QuickfinderPresenter(quickFinderUi, currentPresenter, seriesName, eventsOfSeries);
    }

    private void setupListContent(EventSeriesViewDTO event) {
        leaderboardUi.setAction(MSG.showAll(), currentPresenter.getMiniOverallLeaderboardNavigation());
        refreshManager.add(leaderboardUi, new GetMiniOverallLeaderbordAction(event.getId(), 3));
    }
//    
//    private void setupStatisticsBox(EventViewDTO event) {
//        statisticsBoxUi = new StatisticsBox(event.getType() == EventType.MULTI_REGATTA);
//        refreshManager.add(statisticsBoxUi, new GetEventStatisticsAction(event.getId()));
//    }
}
