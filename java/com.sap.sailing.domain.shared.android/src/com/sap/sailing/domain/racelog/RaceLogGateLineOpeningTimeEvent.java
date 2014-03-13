package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.common.impl.Util.Pair;


public interface RaceLogGateLineOpeningTimeEvent extends RaceLogEvent {
    
    public class GateLineOpeningTimes extends Pair<Long, Long> {
        private static final long serialVersionUID = -3262303047719105275L;
        
        public GateLineOpeningTimes(long gateLaunchStopTime, long golfDownTime) {
            super(gateLaunchStopTime, golfDownTime);
        }

        public final long getGateLaunchStopTime() { return this.getA(); }
        public final long getGolfDownTime() { return this.getB(); }
    }
    
    GateLineOpeningTimes getGateLineOpeningTimes();
}
