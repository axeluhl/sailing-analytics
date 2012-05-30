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
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.WindControlParameters;
import com.sap.sailing.simulator.WindFieldGenerator;

public abstract class WindFieldGeneratorImpl implements WindFieldGenerator {

    protected Boundary boundary;
    protected WindControlParameters windParameters;
    protected List<Position> sortedPositionList;
    protected Position[][] positions;
    protected Map<Pair<Integer, Integer>, Position> indexPositionMap;
    protected Map<TimePoint, SpeedWithBearing[][]> timeSpeedWithBearingMap;

    @SuppressWarnings("unused")
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

    public WindFieldGeneratorImpl(Boundary boundary, WindControlParameters windParameters) {
        this.boundary = boundary;
        this.windParameters = windParameters;
        this.positions = null;
        this.indexPositionMap = new HashMap<Pair<Integer, Integer>, Position>();
    }

    private List<Position> extractLattice(int hPoints, int vPoints) {
        sortedPositionList = boundary.extractLattice(hPoints, vPoints);
        Collections.sort(sortedPositionList, new LatLngComparator());
        assert (sortedPositionList.size() == hPoints * vPoints);
        positions = new Position[vPoints][hPoints];
        indexPositionMap = new HashMap<Pair<Integer, Integer>, Position>();
        int i = 0;
        int j = 0;
        for (Position p : sortedPositionList) {
            if (j < hPoints) {
                positions[i][j] = p;
                indexPositionMap.put(new Pair<Integer, Integer>(i, j), p);
                ++j;
            } else {
                j = 0;
                ++i;
                positions[i][j] = p;
                indexPositionMap.put(new Pair<Integer, Integer>(i, j), p);
                ++j;
            }
        }

        return sortedPositionList;
    }

    @Override
    public Wind getWind(TimedPosition coordinates) {
        KnotSpeedImpl knotSpeedImpl = new KnotSpeedImpl(windParameters.baseWindSpeed);

        double wBearing = windParameters.baseWindBearing
                * (1 + coordinates.getPosition().getDistance(boundary.getCorners().get("NorthWest")).getMeters()
                        / boundary.getHeight().getMeters());
        SpeedWithBearing wspeed = new KilometersPerHourSpeedWithBearingImpl(knotSpeedImpl.getKilometersPerHour(),
                new DegreeBearingImpl(wBearing));

        return new WindImpl(coordinates.getPosition(), coordinates.getTimePoint(), wspeed);

    }

    @Override
    public Boundary getBoundaries() {
        return boundary;
    }

    public Position[][] getPositionsGrid() {
        return positions;
    }

    @Override
    public void setPositionGrid(Position[][] positions) {
        this.positions = positions;
        indexPositionMap.clear();
        if (positions == null || positions.length < 1) {
            return;
        }
        for (int i = 0; i < positions.length; ++i) {
            for (int j = 0; j < positions[0].length; ++j) {
                indexPositionMap.put(new Pair<Integer, Integer>(i, j), positions[i][j]);
            }
        }
        
    }

    public Position getPosition(int i, int j) {
        Pair<Integer, Integer> indexKey = new Pair<Integer, Integer>(i, j);
        return indexPositionMap.get(indexKey);

    }
}
