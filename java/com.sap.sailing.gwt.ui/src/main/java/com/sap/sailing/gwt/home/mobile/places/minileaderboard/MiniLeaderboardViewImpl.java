package com.sap.sailing.gwt.home.mobile.places.minileaderboard;

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
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderbordAction;

public class MiniLeaderboardViewImpl extends Composite implements MiniLeaderboardView {
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    interface MyBinder extends UiBinder<Widget, MiniLeaderboardViewImpl> {
    }

    private Presenter currentPresenter;

    @UiField(provided = true) EventHeader eventHeaderUi;
    @UiField MinileaderboardBox minileaderboardUi;

    public MiniLeaderboardViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;

        eventHeaderUi = new EventHeader(presenter.getCtx().getEventDTO());
        
        initWidget(uiBinder.createAndBindUi(this));
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        EventContext ctx = presenter.getCtx();
        UUID uuid = UUID.fromString(ctx.getEventId());
        refreshManager.add(minileaderboardUi, new GetMiniLeaderbordAction(uuid, presenter.getCtx().getRegattaId()));

    }
}
