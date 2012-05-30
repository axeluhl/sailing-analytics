package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.simulator.WindLattice;

public class WindLatticeImpl implements WindLattice {

    public class LatticePoint extends DegreePosition {
        
        /**
         * Generate Serial version ID
         */
        private static final long serialVersionUID = 6171186870433644107L;
        public LatticePoint previous;
        public LatticePoint left;
        public LatticePoint right;
        
        public LatticePoint(double lat, double lng) {
            super(lat, lng);
            previous = null;
            left = null;
            right = null;
        }

        public LatticePoint(Position startPoint) {
            super(startPoint.getLatDeg(), startPoint.getLngDeg());
            previous = null;
            left = null;
            right = null;
        }

    }
    
    private int hPoints;
    private int vPoints;
    private Distance hStep;
    private Distance vStep;
    private LatticePoint[][] points;
    
    public WindLatticeImpl(int hPoints, int vPoints, Distance hStep, Distance vStep) {
        this.sethStep(hStep);
        this.setvStep(vStep);
        this.sethPoints(hPoints);
        this.setvPoints(vPoints);
        points = new LatticePoint[hPoints][vPoints];      
        for (int i = 0; i < hPoints; ++i) {
            points[i] = new LatticePoint[vPoints];
        }
    }

   

    @Override
    public Position[][] getPoints() {

        return points;
    }



    public int gethPoints() {
        return hPoints;
    }



    public void sethPoints(int hPoints) {
        this.hPoints = hPoints;
    }



    public int getvPoints() {
        return vPoints;
    }



    public void setvPoints(int vPoints) {
        this.vPoints = vPoints;
    }



    public Distance gethStep() {
        return hStep;
    }



    public void sethStep(Distance hStep) {
        this.hStep = hStep;
    }



    public Distance getvStep() {
        return vStep;
    }



    public void setvStep(Distance vStep) {
        this.vStep = vStep;
    }



    public void addPoint(LatticePoint latticePoint, int rowIndex, int colIndex) {
       points[rowIndex][colIndex] = latticePoint;
    }

}
