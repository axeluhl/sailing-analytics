package com.sap.sailing.domain.base;


/**
 * A speed, convertable in various units of measure. Can be negative.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Speed extends Comparable<Speed> {
    final static Speed NULL = new Speed() {
        @Override
        public int compareTo(Speed o) {
            return getMetersPerSecond() > o.getMetersPerSecond() ? 1 : getMetersPerSecond() == o.getMetersPerSecond() ? 0 : -1;
        }

        @Override
        public double getKnots() {
            return 0;
        }

        @Override
        public double getMetersPerSecond() {
            return 0;
        }

        @Override
        public double getKilometersPerHour() {
            return 0;
        }

        @Override
        public double getBeaufort() {
            return 0;
        }

        @Override
        public Distance travel(TimePoint from, TimePoint to) {
            return Distance.NULL;
        }
        
    };
    
    double getKnots();

    double getMetersPerSecond();

    double getKilometersPerHour();
    
    double getBeaufort();

    /**
     * Traveling at this speed starting at time <code>from</code> until time </code>to</code>, how far have we traveled?
     * If <code>to</code> is before </code>from</code>, the speed will be applied in reverse. If this speed has a negative
     * amount then so will the resulting distance.
     */
    Distance travel(TimePoint from, TimePoint to);

}
