package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.user.client.Timer;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;

class AdvantageLineAnimator extends Timer {
    private static final double MILLIS_TO_HOURS_FACTOR = 3600000;
    
    private final Polyline advantageLine;
    private GPSFixDTO lastBoatFix;
    private long lastTime;
    private int repetitions = -1;
    
    public AdvantageLineAnimator(Polyline advantageLine) {
        this.advantageLine = advantageLine;
    }
    
    public void setLastFix(GPSFixDTO lastBoatFix) {
        this.lastBoatFix = lastBoatFix;
        scheduleRepeating(100, 10);
    }
    
    public void scheduleRepeating(int periodMillis, int repetitions) { // TODO Jonas: use duration instead of repetitions and check time as we go
        this.lastTime = System.currentTimeMillis();
        this.scheduleRepeating(periodMillis);
        this.repetitions = repetitions;
    }

    @Override
    public void run() {
        if (repetitions > 0) {
            repetitions--;
        } else if (repetitions == 0) { // TODO Jonas: see above, use duration / target time
            this.cancel();
            return;
        }
        if (advantageLine != null) {
            long time = System.currentTimeMillis();
            long deltaTime = time - lastTime;
            lastTime = time;
            if (lastBoatFix != null) {
                Position oldPosition = lastBoatFix.position;
                Position newPosition = oldPosition.translateRhumb( // TODO Jonas: use great circle navigation
                        new DegreeBearingImpl(lastBoatFix.speedWithBearing.bearingInDegrees), 
                        new NauticalMileDistance(deltaTime / MILLIS_TO_HOURS_FACTOR * lastBoatFix.speedWithBearing.speedInKnots));
                
                final double latDelta = newPosition.getLatDeg() - oldPosition.getLatDeg(); // FIXME Jonas: this would be a delta in "logical coordinates" but not Google Map coordinates
                final double lngDelta = newPosition.getLngDeg() - oldPosition.getLngDeg();
                
                final MVCArray<LatLng> path = this.advantageLine.getPath();
                LatLng pos1 = path.pop();
                LatLng pos2 = path.pop();
                path.push(LatLng.newInstance(pos2.getLatitude() + latDelta, pos2.getLongitude() + lngDelta)); // FIXME Jonas: must not use Position's lat/lng to draw on map; always use coordinateSystem to map
                path.push(LatLng.newInstance(pos1.getLatitude() + latDelta, pos1.getLongitude() + lngDelta));
            }
        }
    }
}