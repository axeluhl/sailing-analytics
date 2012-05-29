package com.sap.sailing.simulator.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.WindControlParameters;
import com.sap.sailing.simulator.WindField;

public class WindFieldGenerator implements WindField {

    private Boundary boundary;
    private WindControlParameters windParameters;
    private Position[][] positions;
    private Map<Pair<Integer,Integer>, Position> indexPositionMap;
    
    private static Logger logger = Logger.getLogger("com.sap.sailing");
    
    private class LatLngComparator implements Comparator<Position> {

        @Override
        public int compare(Position p1, Position p2) {
       
           if (p1.getLatDeg() < p2.getLatDeg()) {
               return -1;
           } else if (p1.getLatDeg() == p2.getLngDeg()) {
                  if (p1.getLngDeg() < p2.getLngDeg()) {
                      return -1;
                  } else if (p1.getLngDeg() == p2.getLngDeg()) {
                      return 0;
                  } else {
                      return 1;
                  }
            } else {
                return 1;
            }
        }
        
    }
    
    public WindFieldGenerator(Boundary boundary, WindControlParameters windParameters) {
        this.boundary = boundary;
        this.windParameters = windParameters;
        this.positions = null;
        this.indexPositionMap = null;
    }

    public List<Position> extractLattice(int hPoints, int vPoints) {
        List<Position> positionList = boundary.extractLattice(hPoints, vPoints);
        Collections.sort(positionList, new LatLngComparator());
        assert(positionList.size() == hPoints*vPoints);
        positions = new Position[vPoints][hPoints];
        indexPositionMap = new HashMap<Pair<Integer,Integer>, Position>();
        int i = 0;
        int j = 0;
        for(Position p : positionList) {
            if (j < hPoints) {
                positions[i][j] = p;
                indexPositionMap.put(new Pair<Integer, Integer>(i,j), p);
                ++j;
            } else {
                j = 0;
                ++i;
                positions[i][j] = p;
                indexPositionMap.put(new Pair<Integer, Integer>(i,j), p);
                ++j;
            }
        }
        
        return positionList;
    }
    
    @Override
    public Wind getWind(TimedPosition coordinates) {
        KnotSpeedImpl knotSpeedImpl = new KnotSpeedImpl(windParameters.baseWindSpeed);
        
        double wBearing = windParameters.baseWindBearing *
                        (1 + coordinates.getPosition().getDistance(boundary.getCorners().get("NorthWest")).getMeters()/boundary.getHeight().getMeters());
        SpeedWithBearing wspeed = new KilometersPerHourSpeedWithBearingImpl(knotSpeedImpl.getKilometersPerHour(), new DegreeBearingImpl(wBearing));
        
        return new WindImpl(coordinates.getPosition(),coordinates.getTimePoint(), wspeed);
                
    }

    @Override
    public Boundary getBoundaries() {
        return boundary;
    }
    
    public Position[][] getPositionsGrid() {
        return positions;
    }
    
    public Position getPosition(int i, int j) {
        Pair<Integer,Integer> indexKey = new Pair<Integer,Integer>(i,j);
        return indexPositionMap.get(indexKey);
       
    }
}
