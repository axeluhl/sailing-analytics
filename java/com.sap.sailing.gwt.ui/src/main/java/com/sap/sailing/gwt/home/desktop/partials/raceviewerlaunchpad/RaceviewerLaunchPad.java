package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class RaceviewerLaunchPad extends Composite {

    private static RaceviewerLaunchPadUiBinder uiBinder = GWT.create(RaceviewerLaunchPadUiBinder.class);

    interface RaceviewerLaunchPadUiBinder extends UiBinder<Widget, RaceviewerLaunchPad> {
    }

    @UiField RaceviewerLaunchPadResources local_res;
    @UiField DivElement itemContainerUi;

    public RaceviewerLaunchPad(SimpleRaceMetadataDTO data) {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.css().ensureInjected();
        if (!data.isFinished()) {
            addStyleName(local_res.css().raceviewerlaunchpadlive());
            addItem(data, RaceviewerLaunchPadMenuItem.WATCH_LIVE);
        } else {
            addItem(data, RaceviewerLaunchPadMenuItem.REPLAY);
            addItem(data, RaceviewerLaunchPadMenuItem.RACE_ANALYSIS);
        }
        if (data.isRunning() || data.isFinished()) {
            addItem(data, RaceviewerLaunchPadMenuItem.START_ANALYSIS);
        }
        addItem(data, RaceviewerLaunchPadMenuItem.WINNING_LANES);
    }
    
    private void addItem(SimpleRaceMetadataDTO data, RaceviewerLaunchPadMenuItem item) {
        String raceViewerUrl = getRaceViewerURL(data, item.raceBoardMode);
        itemContainerUi.appendChild(new RaceviewerLaunchPadItem(item.label, item.icon, raceViewerUrl).getElement());
    }
    
    private enum RaceviewerLaunchPadMenuItem {
        REPLAY(StringMessages.INSTANCE.replay(), "launch-replay", "PLAYER"),
        WATCH_LIVE(StringMessages.INSTANCE.watchLive(), "launch-play", "PLAYER"),
        RACE_ANALYSIS(StringMessages.INSTANCE.raceAnalysis(), "launch-loupe", "FULL_ANALYSIS"),
        START_ANALYSIS(StringMessages.INSTANCE.startAnalysis(), "launch-start", "START_ANALYSIS"),
        WINNING_LANES(StringMessages.INSTANCE.winningLanes(), "launch-winning-lanes", "WINNING_LANES");
        
        private String label, icon, raceBoardMode; 
        
        private RaceviewerLaunchPadMenuItem(String label, String iconKey, String raceBoardMode) {
            this.label = label;
            this.icon = "<svg><use xlink:href=\"#" + iconKey + "\"></use></svg>";
            this.raceBoardMode = raceBoardMode;
        }
    }
    
    protected abstract String getRaceViewerURL(SimpleRaceMetadataDTO data, String raceBoardMode);

}
