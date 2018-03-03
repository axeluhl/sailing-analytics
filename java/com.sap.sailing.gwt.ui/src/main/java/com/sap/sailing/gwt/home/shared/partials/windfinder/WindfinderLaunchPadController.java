package com.sap.sailing.gwt.home.shared.partials.windfinder;

import java.util.function.BiFunction;

import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sailing.gwt.home.shared.partials.launchpad.AbstractLaunchPadController;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

class WindfinderLaunchPadController extends AbstractLaunchPadController<Iterable<SpotDTO>> {

    private final BiFunction<SpotDTO, TimePoint, String> windfinderUrlFactory;

    /**
     * Creates a new {@link WindfinderLaunchPadController} instance with the given URL factory.
     * 
     * @param raceviewerUrlFactory
     *            {@link BiFunction Factory} to get the windfinder URL for given {@link SpotDTO spot} and
     *            {@link TimePoint time point} information
     */
    WindfinderLaunchPadController(final BiFunction<SpotDTO, TimePoint, String> windfinderUrlFactory) {
        super((data, panel) -> new WindfinderLaunchPad(data, windfinderUrlFactory, panel));
        this.windfinderUrlFactory = windfinderUrlFactory;
    }

    @Override
    public String getDirectLinkUrl(Iterable<SpotDTO> data) {
        final SpotDTO spot = data.iterator().next();
        return windfinderUrlFactory.apply(spot, MillisecondsTimePoint.now());
    }

    @Override
    public boolean renderAsDirectLink(Iterable<SpotDTO> data) {
        return Util.size(data) == 1;
    }

}
