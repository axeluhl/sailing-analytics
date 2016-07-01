package com.sap.sailing.gwt.ui.raceboard;

/**
 * Puts the race viewer in a mode where the user can see what may be called the "Winning Lanes." For this,
 * the timer is set to the point in time when the first competitor finishes the race, or, for live races,
 * to the current point in time. The tail length is chosen such that it covers the full track of the
 * competitor farthest ahead.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WinningLanesMode implements RaceBoardMode {
    @Override
    public void applyTo(RaceBoardPanel raceBoardPanel) {
        // TODO Auto-generated method stub
    }
}
