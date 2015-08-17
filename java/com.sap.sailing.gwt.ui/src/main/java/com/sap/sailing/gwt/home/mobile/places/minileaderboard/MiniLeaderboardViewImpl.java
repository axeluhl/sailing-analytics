package com.sap.sailing.gwt.home.mobile.places.minileaderboard;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public class MiniLeaderboardViewImpl extends Composite implements MiniLeaderboardView {
    private static final StringMessages MSG = StringMessages.INSTANCE;
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    interface MyBinder extends UiBinder<Widget, MiniLeaderboardViewImpl> {
    }

    private Presenter currentPresenter;
    @UiField(provided = true)
    EventHeader eventHeaderUi;
    @UiField(provided = true)
    MinileaderboardBox minileaderboardUi;
    @UiField
    Quickfinder quickFinderUi;

    public MiniLeaderboardViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        String regattaDisplayName = null;
        if(presenter.getCtx().getEventDTO().getType() == EventType.MULTI_REGATTA) {
            regattaDisplayName = presenter.getCtx().getRegatta().getDisplayName();
        }
        minileaderboardUi = new MinileaderboardBox(false);
        eventHeaderUi = new EventHeader(presenter.getCtx().getEventDTO(), regattaDisplayName, presenter.getEventNavigation());
        initWidget(uiBinder.createAndBindUi(this));
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        EventContext ctx = presenter.getCtx();
        UUID uuid = UUID.fromString(ctx.getEventId());
        refreshManager.add(minileaderboardUi, new GetMiniLeaderbordAction(uuid, presenter.getCtx().getRegattaId()));
        minileaderboardUi.setAction(MSG.details(), presenter.getRegattaLeaderboardNavigation(presenter.getCtx().getRegattaId()));
    }

    @Override
    public void setQuickFinderValues(Collection<RegattaMetadataDTO> regattaMetadatas) {
        QuickfinderPresenter.getRegattaLeaderboardsQuickfinder(quickFinderUi, currentPresenter, regattaMetadatas);
    }
    
    @Override
    public void setQuickFinderValues(String seriesName, Collection<EventReferenceDTO> eventsOfSeries) {
        new QuickfinderPresenter(quickFinderUi, currentPresenter, seriesName, eventsOfSeries);
    }
    
    @Override
    public void hideQuickfinder() {
        quickFinderUi.removeFromParent();
    }
}
