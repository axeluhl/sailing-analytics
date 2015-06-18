package com.sap.sailing.gwt.home.mobile.places.minileaderboard;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;

public class MiniLeaderboardViewImpl extends Composite implements MiniLeaderboardView {
    private static final boolean showLeaderboard = true;
    
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    interface MyBinder extends UiBinder<Widget, MiniLeaderboardViewImpl> {
    }

    private Presenter currentPresenter;

    @UiField(provided = true) EventHeader eventHeaderUi;
    @UiField MinileaderboardBox minileaderboardUi;
    @UiField DivElement buttonsUi;
    @UiField AnchorElement leaderboardLinkUi;
    @UiField AnchorElement dektopLinkUi;

    public MiniLeaderboardViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;

        RegattaMetadataDTO regatta = presenter.getCtx().getRegatta();
        // TODO check if regatta is valid!
        eventHeaderUi = new EventHeader(presenter.getCtx().getEventDTO(), regatta.getDisplayName());
        
        initWidget(uiBinder.createAndBindUi(this));
        
        if(showLeaderboard) {
            RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
            EventContext ctx = presenter.getCtx();
            UUID uuid = UUID.fromString(ctx.getEventId());
            refreshManager.add(minileaderboardUi, new GetMiniLeaderbordAction(uuid, presenter.getCtx().getRegattaId()));
            
            buttonsUi.removeFromParent();
        } else {
            leaderboardLinkUi.setHref(constructExternalLeaderboardURL(presenter.getCtx().getRegattaId(), presenter.getCtx().getRegatta().getDisplayName()));
            
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    String link = "HomeDesktop.html" + Window.Location.getQueryString() + Window.Location.getHash();
                    dektopLinkUi.setHref(link);
                }
            });
            
            minileaderboardUi.removeFromParent();
        }
        
    }
    
    private String constructExternalLeaderboardURL(String leaderboardId, String leaderboardDisplayName) {
        return "Leaderboard.html?name="+ leaderboardId +"&displayName="+leaderboardDisplayName+"&embedded=true&hideToolbar=true&refreshIntervalMillis=3000&legDetail=AVERAGE_SPEED_OVER_GROUND_IN_KNOTS&legDetail=DISTANCE_TRAVELED&legDetail=RANK_GAIN&overallDetail=REGATTA_RANK&maneuverDetail=TACK&maneuverDetail=JIBE&maneuverDetail=PENALTY_CIRCLE&lastN=1&showAddedScores=false";
    }
    
}
