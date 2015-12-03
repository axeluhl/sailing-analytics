package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.user.client.Timer;

class AdvantageLineAnimator extends Timer {
    private static final int ANIMATION_PERIOD = 100;
    
    private final Polyline advantageLine;
    private long lastTime;
    private long durationMillis = -1;
    private MVCArray<LatLng> nextPosition;
    
    public AdvantageLineAnimator(Polyline advantageLine) {
        this.advantageLine = advantageLine;
    }
    
    public void setNextPositionAndTransitionMillis(MVCArray<LatLng> nextPosition, long timeForPositionTransitionMillis) {
        this.nextPosition = nextPosition;
        scheduleRepeating(ANIMATION_PERIOD, timeForPositionTransitionMillis);
    }
    
    public void scheduleRepeating(int periodMillis, long durationMillis) {
        this.lastTime = System.currentTimeMillis();
        this.scheduleRepeating(periodMillis);
        this.durationMillis = durationMillis;
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        long deltaTime = time - lastTime;
        lastTime = time;
        double moveFactor = 1;
        if (durationMillis > 0) {
            long oldDurationMillis = durationMillis;
            durationMillis -= deltaTime;
            if (durationMillis < 0) {
                deltaTime = oldDurationMillis;
                durationMillis = 0;
            }
            moveFactor = (double)deltaTime / oldDurationMillis;
        } else if (durationMillis == 0) {
            this.cancel();
            return;
        }
        if (advantageLine != null && nextPosition != null) {
            final MVCArray<LatLng> currentPosition = this.advantageLine.getPath();
            
            LatLng currentLatLng;
            LatLng nextLatLng;
            for(int i = 0; i <= 1; i++) {
                currentLatLng = currentPosition.get(i);
                nextLatLng = nextPosition.get(i);
                currentPosition.setAt(i, LatLng.newInstance(currentLatLng.getLatitude() + (nextLatLng.getLatitude() - currentLatLng.getLatitude()) * moveFactor, 
                        currentLatLng.getLongitude() + (nextLatLng.getLongitude() - currentLatLng.getLongitude()) * moveFactor));
            }
        }
    }
}