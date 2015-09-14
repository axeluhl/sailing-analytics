package com.sap.sailing.gwt.home.mobile.places.series.minileaderboard;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.client.shared.refresh.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.seriesheader.SeriesHeader;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniOverallLeaderbordAction;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;

public class SeriesMiniOverallLeaderboardViewImpl extends Composite implements SeriesMiniOverallLeaderboardView {
    private static final StringMessages MSG = StringMessages.INSTANCE;
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    interface MyBinder extends UiBinder<Widget, SeriesMiniOverallLeaderboardViewImpl> {
    }

    private Presenter currentPresenter;
    @UiField(provided = true)
    SeriesHeader eventHeaderUi;
    @UiField Quickfinder quickFinderUi;
    @UiField(provided = true)
    MinileaderboardBox minileaderboardUi;

    public SeriesMiniOverallLeaderboardViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        minileaderboardUi = new MinileaderboardBox(true);
        minileaderboardUi.setShowIfEmpty(true);
        eventHeaderUi = new SeriesHeader(presenter.getCtx().getSeriesDTO(), presenter.getSeriesNavigation());
        initWidget(uiBinder.createAndBindUi(this));
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        SeriesContext ctx = presenter.getCtx();
        refreshManager.add(minileaderboardUi, new GetMiniOverallLeaderbordAction(UUID.fromString(ctx.getSeriesId())));
        minileaderboardUi.setAction(MSG.details(), presenter.getOverallLeaderboardNavigation());
    }
    
    @Override
    public void setQuickFinderValues(String seriesName, Collection<EventMetadataDTO> eventsOfSeries) {
        QuickfinderPresenter.getForSeriesLeaderboards(quickFinderUi, seriesName, currentPresenter, eventsOfSeries);
    }
}
