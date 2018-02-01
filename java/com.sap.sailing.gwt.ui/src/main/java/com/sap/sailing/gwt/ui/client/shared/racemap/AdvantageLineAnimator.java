package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.user.client.Timer;

/**
 * Moves a {@link Polyline} from its current position to a target position in a given duration with a fixed
 * {@link #ANIMATION_PERIOD_MILLIS animation interval}. Since the timer may be triggered at non-exact intervals, the
 * offset per animation tick is calculated based on the current time for each iteration.
 * <p>
 * 
 * If an animation is still going on, it is permissible for clients to invoke
 * {@link #setNextPositionAndTransitionMillis(MVCArray, long)} which will have the effect that from its current position
 * reached so far the {@link Polyline} will then be animated to the next target position in the specified time interval.
 * 
 * @author Jonas Dann
 *
 */
public class AdvantageLineAnimator extends Timer {
    /**
     * Time between two animation steps
     */
    private static final int ANIMATION_PERIOD_MILLIS = 100;
    
    /**
     * The polyline to animate by updating its {@link Polyline#getPath() path}
     */
    private final Polyline advantageLine;
    
    /**
     * The last time the coordinates in the {@link #advantageLine}'s path were updated
     */
    private long lastTime;
    
    /**
     * The remaining time for the animation to move the {@link #advantageLine} to its {@link #nextPosition target
     * position}. If <code>-1</code>, the {@link #advantageLine} will be moved to its {@link #nextPosition next target
     * position} in one step.
     */
    private long durationMillis = -1;
    
    /**
     * The "to-be" / target position to which to move the {@link #advantageLine}'s path gradually during the remaining
     * {@link #durationMillis}.
     */
    private MVCArray<LatLng> nextPosition;
    
    public AdvantageLineAnimator(Polyline advantageLine) {
        if (advantageLine == null) {
            throw new NullPointerException("Advantage line must not be null");
        }
        this.advantageLine = advantageLine;
    }
    
    /**
     * @param timeForPositionTransitionMillis if <code>-1</code> then no animation is requested and the {@link #advantageLine} will
     * be moved to its {@link #nextPosition next target position} in one step.
     * 
     * @param nextPosition the next target position for the {@link #advantageLine}; if <code>null</code>, no animation will happen at all
     */
    public void setNextPositionAndTransitionMillis(MVCArray<LatLng> nextPosition, long timeForPositionTransitionMillis) {
        if (nextPosition.getLength() != advantageLine.getPath().getLength()) {
            throw new IllegalArgumentException("The next animation position for a polyline with "+advantageLine.getPath().getLength()+
                    " elements must have the same number of positions but did have "+nextPosition.getLength());
        }
        this.nextPosition = nextPosition;
        this.lastTime = System.currentTimeMillis();
        this.durationMillis = timeForPositionTransitionMillis;
        this.scheduleRepeating(ANIMATION_PERIOD_MILLIS);
    }
    
    @Override
    public void run() {
        final long time = System.currentTimeMillis();
        final double moveFactor; // how much of the delta to the nextPosition shall this tick move the advantage line?
        if (durationMillis > 0) {
            long deltaTime = time - lastTime;
            moveFactor = Math.min(1.0, (double) deltaTime / (double) durationMillis); // don't overshoot
            durationMillis -= deltaTime;
        } else {
            moveFactor = 1;
        }
        if (nextPosition != null) {
            final MVCArray<LatLng> currentPosition = this.advantageLine.getPath();
            for (int i = 0; i < nextPosition.getLength(); i++) {
                final LatLng currentLatLng = currentPosition.get(i);
                final LatLng nextLatLng = nextPosition.get(i);
                currentPosition.setAt(i, LatLng.newInstance(currentLatLng.getLatitude() + (nextLatLng.getLatitude() - currentLatLng.getLatitude()) * moveFactor, 
                        currentLatLng.getLongitude() + (nextLatLng.getLongitude() - currentLatLng.getLongitude()) * moveFactor));
            }
        }
        if (durationMillis <= 0) {
            cancel();
        }
        lastTime = time;
    }

    public void removeAnimation() {
        this.durationMillis = -1;        
    }
}