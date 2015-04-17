package com.sap.sailing.simulator.impl;

import java.util.BitSet;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.simulator.TimedPosition;

public class PathCandidateBitSet implements Comparable<PathCandidateBitSet> {

    public PathCandidateBitSet(TimedPosition pos, boolean reached, double vrt, double hrz, int trn, BitSet path, int length, boolean sid, Wind wind) {
        this.pos = pos;   // time and position
        this.reached = reached;
        this.vrt = vrt;   // height of target projected onto wind
        this.hrz = hrz;   // distance from middle line
        this.trn = trn;   // number of turns
        this.path = path; // path as sequence of steps from start to pos
        this.length = length; // length of path in steps
        this.sid = sid;   // side of wind of step reaching pos
    }

    TimedPosition pos;
    boolean reached;
    double vrt;
    double hrz;
    int trn;
    BitSet path;
    int length;
    boolean sid;

    public int getIndexOfTurnLR() {
    	for(int step=0; step<(length-1); step++) {
    		if ((path.get(step) == PathGeneratorTreeGrowBitSet.LEFT)&&(path.get(step+1) == PathGeneratorTreeGrowBitSet.RIGHT)) {
    			return step;
    		}
    	}
    	return -1;
    }

    public int getIndexOfTurnRL() {
    	for(int step=0; step<(length-1); step++) {
    		if ((path.get(step) == PathGeneratorTreeGrowBitSet.RIGHT)&&(path.get(step+1) == PathGeneratorTreeGrowBitSet.LEFT)) {
    			return step;
    		}
    	}
    	return -1;    	
    }

    @Override
    // sort descending by time, -#turns, width
    public int compareTo(PathCandidateBitSet other) {
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
