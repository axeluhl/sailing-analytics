package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import java.util.function.BiFunction;

import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.shared.partials.launchpad.AbstractLaunchPadController;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardModes;

public class RaceviewerLaunchPadController extends AbstractLaunchPadController<SimpleRaceMetadataDTO> {

    public static enum RenderingStyle {
        WATCH_LIVE_ONLY, WATCH_LIVE_OR_ANALYZE, ANALYZE, NOT_TRACKED
    }

    private final BiFunction<SimpleRaceMetadataDTO, String, String> raceboardUrlFactory;

    /**
     * Creates a new {@link RaceviewerLaunchPadController} instance with the given URL factory.
     * 
     * @param raceviewerUrlFactory
     *            {@link BiFunction Factory method} to get the raceboard URL for given {@link SimpleRaceMetadataDTO
     *            race} and {@link RaceBoardModes raceboard mode} information
     */
    public RaceviewerLaunchPadController(BiFunction<SimpleRaceMetadataDTO, String, String> raceboardUrlFactory) {
        super((data, panel) -> new RaceviewerLaunchPad(data, raceboardUrlFactory, panel));
        this.raceboardUrlFactory = raceboardUrlFactory;
    }

    @Override
    public String getDirectLinkUrl(SimpleRaceMetadataDTO data) {
        return raceboardUrlFactory.apply(data, RaceBoardModes.PLAYER.name());
    }

    @Override
    public boolean renderAsDirectLink(SimpleRaceMetadataDTO data) {
        return !data.isRunning() && !data.isFinished();
    }

    /**
     * Calculates the {@link RenderingStyle} to use for the given {@link SimpleRaceMetadataDTO race}.
     * 
     * @param data
     *            the {@link SimpleRaceMetadataDTO race} to get the {@link RenderingStyle} for
     * @return the calculated {@link RenderingStyle}
     */
    public <T extends SimpleRaceMetadataDTO> RenderingStyle getRenderingStyle(T data) {
        final RenderingStyle result;
        if (data.hasValidTrackingData()) {
            if (renderAsDirectLink(data)) {
                result = RenderingStyle.WATCH_LIVE_ONLY;
            } else {
                result = data.isFinished() ? RenderingStyle.ANALYZE : RenderingStyle.WATCH_LIVE_OR_ANALYZE;
            }
        } else {
            result = RenderingStyle.NOT_TRACKED;
        }
        return result;
    }

}
