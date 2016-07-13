package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;

public abstract class RaceviewerLaunchPad extends Composite {

    private static RaceviewerLaunchPadUiBinder uiBinder = GWT.create(RaceviewerLaunchPadUiBinder.class);

    interface RaceviewerLaunchPadUiBinder extends UiBinder<Widget, RaceviewerLaunchPad> {
    }

    @UiField RaceviewerLaunchPadResources local_res;
    @UiField DivElement itemContainerUi;

    public RaceviewerLaunchPad(SimpleRaceMetadataDTO data) {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.css().ensureInjected();
        if (!data.isFinished()) addStyleName(local_res.css().raceviewerlaunchpadlive());
        addItem(data, RaceviewerModes.REPLAY);
        addItem(data, RaceviewerModes.RACE_ANALYSIS);
        addItem(data, RaceviewerModes.START_ANALYSIS);
        addItem(data, RaceviewerModes.WINNING_LANES);
    }
    
    private void addItem(SimpleRaceMetadataDTO data, RaceviewerModes mode) {
        String raceViewerUrl = getRaceViewerURL(data); // TODO send mode
        itemContainerUi.appendChild(new RaceviewerLaunchPadItem(mode.label, mode.icon, raceViewerUrl).getElement());
    }
    
    private enum RaceviewerModes {
        // TODO i18n
        REPLAY("TODO Replay", "<svg><use xlink:href=\"#launch-replay\"></use></svg>"),
        WATCH_LIVE("TODO Watch live", "<svg><use xlink:href=\"#launch-play\"></use></svg>"),
        RACE_ANALYSIS("TODO Race analysis", "<svg><use xlink:href=\"#launch-loupe\"></use></svg>"),
        START_ANALYSIS("TODO Start analysis", "<svg><use xlink:href=\"#launch-start\"></use></svg>"),
        WINNING_LANES("TODO Winning lanes", "<svg><use xlink:href=\"#launch-winning-lanes\"></use></svg>");
        
        private String label, icon; 
        
        private RaceviewerModes(String label, String icon) {
            this.label = label;
            this.icon = icon;
        }
    }
    
    protected abstract String getRaceViewerURL(SimpleRaceMetadataDTO data);

}
