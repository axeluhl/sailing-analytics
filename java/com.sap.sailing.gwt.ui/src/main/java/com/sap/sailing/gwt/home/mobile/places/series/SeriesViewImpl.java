package com.sap.sailing.gwt.home.mobile.places.series;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.seriesheader.SeriesHeader;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.StatisticsBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventStatisticsAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;

public class SeriesViewImpl extends Composite implements SeriesView {
    private static final StringMessages MSG = StringMessages.INSTANCE;
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, SeriesViewImpl> {
    }

    @UiField(provided = true) SeriesHeader eventHeaderUi;
    @UiField SimplePanel listContentUi;
//    @UiField(provided = true) StatisticsBox statisticsBoxUi;

    private final Presenter currentPresenter;
    private final RefreshManager refreshManager;

    public SeriesViewImpl(SeriesView.Presenter presenter) {
        this.currentPresenter = presenter;
        this.refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        EventSeriesViewDTO event = currentPresenter.getCtx().getSeriesDTO();
        eventHeaderUi = new SeriesHeader(event);
//        this.setupStatisticsBox(event);
        initWidget(uiBinder.createAndBindUi(this));
//        this.setupListContent(event);
    }
//    
//    private void setupListContent(EventViewDTO event) {
//        String regattaId = currentPresenter.getCtx().getRegattaId();
//        if (event.getType() == EventType.MULTI_REGATTA) {
//            RegattaStatus regattaStatus = new RegattaStatus(currentPresenter);
//            listContentUi.add(regattaStatus);
//            refreshManager.add(regattaStatus, new GetRegattasAndLiveRacesForEventAction(event.getId()));
//        } else {
//            MinileaderboardBox miniLeaderboard = new MinileaderboardBox();
//            miniLeaderboard.setAction(MSG.showAll(), currentPresenter.getRegattaMiniLeaderboardNavigation(regattaId));
//            listContentUi.add(miniLeaderboard);
//            refreshManager.add(miniLeaderboard, new GetMiniLeaderbordAction(event.getId(), regattaId, 3));
//        }
//    }
//    
//    private void setupStatisticsBox(EventViewDTO event) {
//        statisticsBoxUi = new StatisticsBox(event.getType() == EventType.MULTI_REGATTA);
//        refreshManager.add(statisticsBoxUi, new GetEventStatisticsAction(event.getId()));
//    }
}
