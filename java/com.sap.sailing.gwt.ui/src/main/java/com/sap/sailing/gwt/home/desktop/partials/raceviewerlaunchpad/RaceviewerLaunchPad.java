package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardModes;

public abstract class RaceviewerLaunchPad extends Composite {

    private static RaceviewerLaunchPadUiBinder uiBinder = GWT.create(RaceviewerLaunchPadUiBinder.class);

    interface RaceviewerLaunchPadUiBinder extends UiBinder<Widget, RaceviewerLaunchPad> {
    }

    @UiField StringMessages i18n;
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
        }
        addItem(data, RaceviewerLaunchPadMenuItem.RACE_ANALYSIS);
        addItem(data, RaceviewerLaunchPadMenuItem.START_ANALYSIS);
        addItem(data, RaceviewerLaunchPadMenuItem.WINNING_LANES);
    }
    
    private void addItem(SimpleRaceMetadataDTO data, RaceviewerLaunchPadMenuItem item) {
        String raceViewerUrl = getRaceViewerURL(data, item.raceBoardMode);
        itemContainerUi.appendChild(new RaceviewerLaunchPadItem(item.label, item.icon, raceViewerUrl).getElement());
    }
    
    private enum RaceviewerLaunchPadMenuItem {
        // TODO i18n
        REPLAY("TODO Replay", "<svg><use xlink:href=\"#launch-replay\"></use></svg>", RaceBoardModes.PLAYER),
        WATCH_LIVE("TODO Watch live", "<svg><use xlink:href=\"#launch-play\"></use></svg>", RaceBoardModes.PLAYER),
        RACE_ANALYSIS("TODO Race analysis", "<svg><use xlink:href=\"#launch-loupe\"></use></svg>", RaceBoardModes.FULL_ANALYSIS),
        START_ANALYSIS("TODO Start analysis", "<svg><use xlink:href=\"#launch-start\"></use></svg>", RaceBoardModes.START_ANALYSIS),
        WINNING_LANES("TODO Winning lanes", "<svg><use xlink:href=\"#launch-winning-lanes\"></use></svg>", RaceBoardModes.WINNING_LANES);
        
        private String label, icon; 
        private RaceBoardModes raceBoardMode; 
        
        private RaceviewerLaunchPadMenuItem(String label, String icon, RaceBoardModes raceBoardMode) {
            this.label = label;
            this.icon = icon;
            this.raceBoardMode = raceBoardMode;
        }
    }
    
    protected abstract String getRaceViewerURL(SimpleRaceMetadataDTO data, RaceBoardModes mode);

}
