package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import java.util.function.BiFunction;
import java.util.function.Function;

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

class RaceviewerLaunchPad<T extends SimpleRaceMetadataDTO> extends Composite {

    private static RaceviewerLaunchPadUiBinder uiBinder = GWT.create(RaceviewerLaunchPadUiBinder.class);

    interface RaceviewerLaunchPadUiBinder extends UiBinder<Widget, RaceviewerLaunchPad<?>> {
    }

    @UiField RaceviewerLaunchPadResources local_res;
    @UiField DivElement itemContainerUi;
    private final BiFunction<? super T, String, String> raceboardUrlFactory;
    private final Function<? super T, String> mapAndWindChartUrlFactory;

    private final PopupPanel parent;

    RaceviewerLaunchPad(final T data, final BiFunction<? super T, String, String> raceboardUrlFactory,
            final Function<? super T, String> mapAndWindChartUrlFactory, final PopupPanel parent) {
        this.raceboardUrlFactory = raceboardUrlFactory;
        this.mapAndWindChartUrlFactory = mapAndWindChartUrlFactory;
        this.parent = parent;
        initWidget(uiBinder.createAndBindUi(this));
        local_res.css().ensureInjected();
        initItems(data);
        initStyles(data);
        sinkEvents(Event.ONCLICK);
    }

    private void initItems(final T data) {
        if (!data.isFinished()) {
            addItem(data, RaceviewerLaunchPadMenuItem.WATCH_LIVE);
        } else {
            addItem(data, RaceviewerLaunchPadMenuItem.REPLAY);
            addItem(data, RaceviewerLaunchPadMenuItem.RACE_ANALYSIS);
        }
        if (data.isRunning() || data.isFinished()) {
            addItem(data, RaceviewerLaunchPadMenuItem.START_ANALYSIS);
            addItem(data, RaceviewerLaunchPadMenuItem.WINNING_LANES);
        }
        if (!data.isFinished()) {
            addItem(data, RaceviewerLaunchPadMenuItem.WIND_AND_COURSE);
        }
    }

    private void initStyles(final T data) {
        if (!data.isRunning() && !data.isFinished()) {
            addStyleName(local_res.css().raceviewerlaunchpadplanned());
        } else if (data.isRunning()) {
            addStyleName(local_res.css().raceviewerlaunchpadlive());
        }
    }

    @Override
    public void onBrowserEvent(final Event event) {
        if (event.getTypeInt() == Event.ONCLICK) {
            parent.hide();
        }
        super.onBrowserEvent(event);
    }

    private void addItem(final T data, final RaceviewerLaunchPadMenuItem item) {
        final String url = RaceviewerLaunchPadMenuItem.WIND_AND_COURSE == item ? mapAndWindChartUrlFactory.apply(data)
                : raceboardUrlFactory.apply(data, item.raceBoardMode);
        itemContainerUi.appendChild(new RaceviewerLaunchPadItem(item.label, item.icon, url).getElement());
    }

    private enum RaceviewerLaunchPadMenuItem {
        REPLAY(StringMessages.INSTANCE.replay(), "launch-play", RaceBoardModes.PLAYER.name()),
        WATCH_LIVE(StringMessages.INSTANCE.watchLive(), "launch-play", RaceBoardModes.PLAYER.name()),
        RACE_ANALYSIS(StringMessages.INSTANCE.raceAnalysis(), "launch-loupe", RaceBoardModes.FULL_ANALYSIS.name()),
        START_ANALYSIS(StringMessages.INSTANCE.startAnalysis(), "launch-start", RaceBoardModes.START_ANALYSIS.name()),
        WINNING_LANES(StringMessages.INSTANCE.winningLanes(), "launch-winning-lanes", RaceBoardModes.WINNING_LANES.name()),
        WIND_AND_COURSE(StringMessages.INSTANCE.windAndCourse(), "launch-wind-course", null);

        private String label, icon, raceBoardMode;

        private RaceviewerLaunchPadMenuItem(final String label, final String iconKey, final String raceBoardMode) {
            this.label = label;
            this.icon = "<svg><use xlink:href=\"#" + iconKey + "\"></use></svg>";
            this.raceBoardMode = raceBoardMode;
        }
    }

}
