package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import java.util.function.BiFunction;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardModes;

class RaceviewerLaunchPad extends Composite {

    private static RaceviewerLaunchPadUiBinder uiBinder = GWT.create(RaceviewerLaunchPadUiBinder.class);

    interface RaceviewerLaunchPadUiBinder extends UiBinder<Widget, RaceviewerLaunchPad> {
    }

    @UiField RaceviewerLaunchPadResources local_res;
    @UiField DivElement itemContainerUi;
    private final BiFunction<SimpleRaceMetadataDTO, String, String> raceboardUrlFactory;
    private final PopupPanel parent;

    RaceviewerLaunchPad(SimpleRaceMetadataDTO data,
            BiFunction<SimpleRaceMetadataDTO, String, String> raceboardUrlFactory, PopupPanel parent) {
        this.raceboardUrlFactory = raceboardUrlFactory;
        this.parent = parent;
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
            addItem(data, RaceviewerLaunchPadMenuItem.WINNING_LANES);
        }
        sinkEvents(Event.ONCLICK);
    }
    
    @Override
    public void onBrowserEvent(final Event event) {
        if (event.getTypeInt() == Event.ONCLICK) {
            parent.hide();
        }
        super.onBrowserEvent(event);
    }
    
    private void addItem(SimpleRaceMetadataDTO data, RaceviewerLaunchPadMenuItem item) {
        String raceViewerUrl = raceboardUrlFactory.apply(data, item.raceBoardMode);
        itemContainerUi.appendChild(new RaceviewerLaunchPadItem(item.label, item.icon, raceViewerUrl).getElement());
    }
    
    private enum RaceviewerLaunchPadMenuItem {
        REPLAY(StringMessages.INSTANCE.replay(), "launch-play", RaceBoardModes.PLAYER.name()),
        WATCH_LIVE(StringMessages.INSTANCE.watchLive(), "launch-play", RaceBoardModes.PLAYER.name()),
        RACE_ANALYSIS(StringMessages.INSTANCE.raceAnalysis(), "launch-loupe", RaceBoardModes.FULL_ANALYSIS.name()),
        START_ANALYSIS(StringMessages.INSTANCE.startAnalysis(), "launch-start", RaceBoardModes.START_ANALYSIS.name()),
        WINNING_LANES(StringMessages.INSTANCE.winningLanes(), "launch-winning-lanes", RaceBoardModes.WINNING_LANES.name());
        
        private String label, icon, raceBoardMode; 
        
        private RaceviewerLaunchPadMenuItem(String label, String iconKey, String raceBoardMode) {
            this.label = label;
            this.icon = "<svg><use xlink:href=\"#" + iconKey + "\"></use></svg>";
            this.raceBoardMode = raceBoardMode;
        }
    }
    
}
