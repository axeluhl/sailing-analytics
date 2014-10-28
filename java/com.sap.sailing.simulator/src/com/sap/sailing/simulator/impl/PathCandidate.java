package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.simulator.TimedPosition;

public class PathCandidate implements Comparable<PathCandidate> {

    public PathCandidate(TimedPosition pos, boolean reached, double vrt, double hrz, int trn, String path, char sid, Wind wind) {
        this.pos = pos;   // time and position
        this.wind = wind;
        this.reached = reached;
        this.vrt = vrt;   // height of target projected onto wind
        this.hrz = hrz;   // distance from middle line
        this.trn = trn;   // number of turns
        this.path = path; // path as sequence of steps from start to pos
        this.sid = sid;   // side of wind of step reaching pos
    }

    TimedPosition pos;
    Wind wind;
    boolean reached;
    double vrt;
    double hrz;
    int trn;
    String path;
    char sid;

    public int getIndexOfTurnLR() {
    	return path.indexOf("LR");
    }

    public int getIndexOfTurnRL() {
    	return path.indexOf("RL");
    }

    @Override
    // sort descending by time, -#turns, width
    public int compareTo(PathCandidate other) {
        if (Math.abs(this.pos.getTimePoint().asMillis() - other.pos.getTimePoint().asMillis()) <= 1000) {
            if (this.trn == other.trn) {
                if (Math.abs(this.hrz) == Math.abs(other.hrz)) {
                    return 0;
                } else {
                    return (Math.abs(this.hrz) < Math.abs(other.hrz) ? -1 : +1);
                }
            } else {
                return (this.trn < other.trn ? -1 : +1);
            }
        } else {
            return (this.pos.getTimePoint().asMillis() < other.pos.getTimePoint().asMillis() ? -1 : +1);            
        }
    }

}
