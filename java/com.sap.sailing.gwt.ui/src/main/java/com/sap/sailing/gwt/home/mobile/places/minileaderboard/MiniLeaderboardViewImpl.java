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
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;

public class MiniLeaderboardViewImpl extends Composite implements MiniLeaderboardView {
    private static final StringMessages MSG = StringMessages.INSTANCE;
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    interface MyBinder extends UiBinder<Widget, MiniLeaderboardViewImpl> {
    }

    private Presenter currentPresenter;
    @UiField(provided = true)
    EventHeader eventHeaderUi;
    @UiField
    MinileaderboardBox minileaderboardUi;
    @UiField(provided = true)
    Quickfinder quickFinderUi;

    public MiniLeaderboardViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        RegattaMetadataDTO regatta = presenter.getCtx().getRegatta();
        // TODO check if regatta is valid!
        eventHeaderUi = new EventHeader(presenter.getCtx().getEventDTO(), regatta.getDisplayName());
        quickFinderUi = new Quickfinder(currentPresenter);
        initWidget(uiBinder.createAndBindUi(this));
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        EventContext ctx = presenter.getCtx();
        UUID uuid = UUID.fromString(ctx.getEventId());
        refreshManager.add(minileaderboardUi, new GetMiniLeaderbordAction(uuid, presenter.getCtx().getRegattaId()));
        minileaderboardUi.setAction(MSG.details(), presenter.getRegattaLeaderboardNavigation(regatta.getId()));
    }

    @Override
    public void setQuickFinderValues(Collection<RegattaMetadataDTO> regattaMetadatas) {
        quickFinderUi.addPlaceholderItem(MSG.resultsQuickfinder(), null);
        for (RegattaMetadataDTO regattaMetadata : regattaMetadatas) {
            quickFinderUi.addItemToGroup(regattaMetadata.getBoatCategory(), regattaMetadata.getDisplayName(),
                    regattaMetadata.getId());
        }
    }
}
