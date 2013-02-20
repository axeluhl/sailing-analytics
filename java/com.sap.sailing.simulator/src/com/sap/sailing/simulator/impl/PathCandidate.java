package com.sap.sailing.simulator.impl;

import com.sap.sailing.simulator.TimedPosition;

public class PathCandidate implements Comparable<PathCandidate> {

    public PathCandidate(TimedPosition pos, double vrt, double hrz, int trn, String path, char sid) {
        this.pos = pos;
        this.vrt = vrt;
        this.hrz = hrz;
        this.trn = trn;
        this.path = path;
        this.sid = sid;
    }

    TimedPosition pos;
    double vrt;
    double hrz;
    int trn;
    String path;
    char sid;

    @Override
    // sort descending by length, width, height
    public int compareTo(PathCandidate other) {
        if (this.path.length() == other.path.length()) {
            if (Math.abs(this.hrz) == Math.abs(other.hrz)) {
                if (this.vrt == other.vrt) {
                    return 0;
                } else {
                    return (this.vrt > other.vrt ? -1 : +1);
                }
            } else {
                return (Math.abs(this.hrz) < Math.abs(other.hrz) ? -1 : +1);
            }
        } else {
            return (this.path.length() < other.path.length() ? -1 : +1);            
        }
    }

}
