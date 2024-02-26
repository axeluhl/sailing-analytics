package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.shared.partials.launchpad.AbstractLaunchPadController;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardModes;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public class RaceviewerLaunchPadController<T extends SimpleRaceMetadataDTO> extends AbstractLaunchPadController<T> {

    public static enum RenderingStyle {
        WATCH_LIVE_OR_PRE_START, WATCH_LIVE_OR_ANALYZE, ANALYZE, NOT_TRACKED
    }

    private final BiFunction<? super T, String, String> raceboardUrlFactory;

    /**
     * Creates a new {@link RaceviewerLaunchPadController} instance with the given URL factory.
     *
     * @param raceviewerUrlFactory
     *            {@link BiFunction Factory method} to get the raceboard URL for given {@link SimpleRaceMetadataDTO
     *            race} and {@link RaceBoardModes raceboard mode} information
     * @param mapAndWindChartUrlFactory
     *            {@link Function Factory method} to get the EmbeddedMapAndWindChart URL for the given
     *            {@link SimpleRaceMetadataDTO race}
     */
    public RaceviewerLaunchPadController(final BiFunction<? super T, String, String> raceboardUrlFactory,
            final Function<? super T, String> mapAndWindChartUrlFactory, PaywallResolver paywallResolver) {
        super((data, panel) -> new RaceviewerLaunchPad<>(data, raceboardUrlFactory, mapAndWindChartUrlFactory, panel,
                paywallResolver));
        this.raceboardUrlFactory = raceboardUrlFactory;
    }

    @Override
    public String getDirectLinkUrl(final T data) {
        return raceboardUrlFactory.apply(data, RaceBoardModes.PLAYER.name());
    }

    @Override
    public boolean renderAsDirectLink(final T data) {
        return false;
    }

    /**
     * Calculates the {@link RenderingStyle} to use for the given {@link SimpleRaceMetadataDTO race}.
     *
     * @param data
     *            the {@link SimpleRaceMetadataDTO race} to get the {@link RenderingStyle} for
     * @return the calculated {@link RenderingStyle}
     */
    public RenderingStyle getRenderingStyle(final T data) {
        final RenderingStyle result;
        if (data.hasValidTrackingData()) {
            if (!data.isRunning() && !data.isFinished()) {
                result = RenderingStyle.WATCH_LIVE_OR_PRE_START;
            } else {
                result = data.isFinished() ? RenderingStyle.ANALYZE : RenderingStyle.WATCH_LIVE_OR_ANALYZE;
            }
        } else {
            result = RenderingStyle.NOT_TRACKED;
        }
        return result;
    }

}
