package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sse.gwt.client.player.Timer.PlayStates;

/**
 * Puts the race viewer into player mode, setting the timer to 10s before start and into {@link PlayStates#Playing} state if it's
 * not a live race where auto-play is started anyway.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class PlayerMode implements RaceBoardMode {
    @Override
    public void applyTo(RaceBoardPanel raceBoardPanel) {
        // TODO
    }
}
