package com.sap.sailing.simulator.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.simulator.Boundary;

public class RectangularBoundary implements Boundary {

    private static final long serialVersionUID = 3598121983120213464L;
    private Position rcStart; // start position of race course
    private Position rcEnd; // end position of race course
    private double lngScale; // scale of distances longitude : latitude
    private double[] nvVrt; // vertical normal vector with suitable length
    private double[] nvHor; // horizontal normal vector with suitable length
    private int vPoints; // number of vertical steps
    private int hPoints; // number of horizontal steps
    private int borderY;
    private int borderX;

    private Position northWest;
    //private Position southEast;
    private Position southWest;
    private Position northEast;

    private Position appNorthWest;
    private Position appSouthEast;
    private Position appSouthWest;
    private Position appNorthEast;

    private Bearing north;
    private Bearing south;
    private Bearing east;
    private Bearing west;

    private Distance appWidth;
    private Distance appHeight;

    double tolerance;

    public RectangularBoundary(Position p1, Position p2, double tlr) {

        // System.out.println("Start:"+p1);
        // System.out.println("End  :"+p2);

        rcStart = p1;
        rcEnd = p2;

        tolerance = tlr;

        north = p1.getBearingGreatCircle(p2);
        south = north.reverse();
        east = north.add(TRUEEAST);
        west = north.add(TRUEWEST);

        appHeight = p1.getDistance(p2);
        appWidth = appHeight.scale(2);

        // make sure race course stays in the middle
        appNorthWest = p2.translateGreatCircle(west, appHeight);
        appNorthEast = p2.translateGreatCircle(east, appHeight);

        // the following test shows, that numerics appear to be instable:
        // translating appNorthEast 2xEast should be appNorthWest, but numerically its NOT
        // Position tstNorthEast = appNorthWest.translateGreatCircle(east, appHeight).translateGreatCircle(east,
        // appHeight);
        // appSouthEast = appNorthEast.translateGreatCircle(south, appHeight);
        // appSouthWest = appNorthWest.translateGreatCircle(south, appHeight);

        appSouthWest = p1.translateGreatCircle(west, appHeight);
        appSouthEast = p1.translateGreatCircle(east, appHeight);

        // System.out.println("southWest:"+appSouthWest);
        // System.out.println("southEast:"+appSouthEast);
        // System.out.println("northWest:"+appNorthWest);
        // System.out.println("northEast:"+appNorthEast);
        // System.out.println("testnEast:"+tstNorthEast);

        Distance diag = appNorthWest.getDistance(appSouthEast);

        Bearing diag1 = appSouthEast.getBearingGreatCircle(appNorthWest);
        northWest = appNorthWest.translateGreatCircle(diag1, diag.scale(tolerance));
        diag1 = diag1.reverse();
        //southEast = appSouthEast.translateGreatCircle(diag1, diag.scale(tolerance));
        Bearing diag2 = appSouthWest.getBearingGreatCircle(appNorthEast);
        northEast = appNorthEast.translateGreatCircle(diag2, diag.scale(tolerance));
        diag2 = diag2.reverse();
        southWest = appSouthWest.translateGreatCircle(diag2, diag.scale(tolerance));
    }

    public RectangularBoundary(Position p1, Position p2) {

        this(p1, p2, 0.1);

    }

    @Override
    public Map<String, Position> getCorners() {

        Map<String, Position> map = new HashMap<String, Position>();
        map.put("NorthWest", appNorthWest);
        map.put("SouthWest", appSouthWest);
        map.put("SouthEast", appSouthEast);
        map.put("NorthEast", appNorthEast);

        return map;

    }

    @Override
    public boolean isWithinBoundaries(Position p) {

        Position northProjection = p.projectToLineThrough(northWest, getEast());
        Position southProjection = p.projectToLineThrough(southWest, getEast());
        Position westProjection = p.projectToLineThrough(northWest, getNorth());
        Position eastProjection = p.projectToLineThrough(northEast, getNorth());

        Distance northSouth = northProjection.getDistance(southProjection);
        Distance eastWest = eastProjection.getDistance(westProjection);

        return (northSouth.compareTo(p.getDistance(northProjection)) >= 0)
                && (northSouth.compareTo(p.getDistance(southProjection)) >= 0)
                && (eastWest.compareTo(p.getDistance(eastProjection)) >= 0)
                && (eastWest.compareTo(p.getDistance(westProjection)) >= 0);

    }

    @Override
    public Position[][] extractGrid(int hPoints, int vPoints, int borderY, int borderX) {

        this.vPoints = vPoints;
        this.hPoints = hPoints;

        this.borderY = borderY;
        this.borderX = borderX;
        
        double xscale = 1.5;

        double alat = (rcEnd.getLatDeg() + rcStart.getLatDeg()) / 2.;
        lngScale = Math.cos(alat * Math.PI / 180.);

        double[] dVrt = new double[2];
        dVrt[0] = rcEnd.getLatDeg() - rcStart.getLatDeg();
        dVrt[1] = rcEnd.getLngDeg() - rcStart.getLngDeg();

        double[] dscVrt = new double[2];
        dscVrt[0] = dVrt[0];
        dscVrt[1] = dVrt[1] * lngScale;

        double lscVrt = Math.sqrt(dscVrt[0] * dscVrt[0] + dscVrt[1] * dscVrt[1]);

        nvVrt = new double[2];
        nvVrt[0] = dscVrt[0] / lscVrt / lscVrt * (vPoints - 1);
        nvVrt[1] = dscVrt[1] / lscVrt / lscVrt * (vPoints - 1);

        double[] nscHor = new double[2];
        nscHor[0] = -dscVrt[1] / lscVrt;
        nscHor[1] = dscVrt[0] / lscVrt;

        nvHor = new double[2];
        nvHor[0] = nscHor[0] / xscale / lscVrt * (hPoints - 1);
        nvHor[1] = nscHor[1] / xscale / lscVrt * (hPoints - 1);

        double[] nHor = new double[2];
        nHor[0] = nscHor[0];
        nHor[1] = nscHor[1] / lngScale;

        // System.out.println("LngScale:"+lngScale+", Diff.Vrt:"+dVrt);
        Position[][] grid = new Position[vPoints+2*borderY][hPoints+2*borderX];
        Position pv;
        for (int i = -borderY; i < (vPoints+borderY); i++) {
            // System.out.print("nrow "+i+": ");

            if (i == 0) {
                pv = rcStart;
            } else if (i == vPoints - 1) {
                pv = rcEnd;
            } else {
                pv = new DegreePosition(rcStart.getLatDeg() + i / (vPoints - 1.0) * dVrt[0], rcStart.getLngDeg() + i
                        / (vPoints - 1.0) * dVrt[1]);
            }

            int j = 0;
            // left side
            while (j < (hPoints+2*borderX) / 2) {
                grid[i+borderY][j] = new DegreePosition(pv.getLatDeg() - ((hPoints + 2*borderX - 1) / 2. - j) / (hPoints - 1) * nHor[0]
                        * xscale * lscVrt, pv.getLngDeg() - ((hPoints + 2*borderX - 1) / 2. - j) / (hPoints - 1) * nHor[1] * xscale
                        * lscVrt);
                // System.out.print(""+grid[i][j]+"("+((hPoints-1)/2.-j)+"), ");
                j++;
            }

            // middle
            if ((hPoints+2*borderX) % 2 == 1) {
                grid[i+borderY][j] = pv;
                // System.out.print(""+grid[i][j]+", ");
                j++;
            }

            // right side
            while (j < (hPoints+2*borderX)) {
                grid[i+borderY][j] = new DegreePosition(pv.getLatDeg() + (j - (hPoints + 2*borderX - 1) / 2.) / (hPoints - 1) * nHor[0]
                        * xscale * lscVrt, pv.getLngDeg() + (j - (hPoints + 2*borderX - 1) / 2.) / (hPoints - 1) * nHor[1] * xscale
                        * lscVrt);
                // System.out.print(""+grid[i][j]+"("+(j-(hPoints-1)/2.)+"), ");
                j++;
            }
            // System.out.println();
        }

        /*
         * System.out.println("Grid Index Test:"); for (int i =0; i<vPoints; i++) { for (int j=0; j<hPoints; j++) {
         * System.out.print(""+this.getGridIndex(grid[i][j])+", "); } System.out.println(); }
         */
        return grid;

    }

    public Pair<Integer, Integer> getGridIndex(Position x) {

        double[] scX = new double[2];
        scX[0] = x.getLatDeg() - rcStart.getLatDeg();
        scX[1] = (x.getLngDeg() - rcStart.getLngDeg()) * lngScale;

        double sPrd = scX[0] * nvHor[0] + scX[1] * nvHor[1];

        int vIdx = Math.min(Math.max(-this.borderY, (int) Math.round(scX[0] * nvVrt[0] + scX[1] * nvVrt[1])), vPoints - 1 + this.borderY);
        int hIdx = Math.min(Math.max(-this.borderX, (int) Math.round(sPrd + (hPoints - 1) / 2.)), hPoints - 1 + this.borderX);

        // System.out.println("getGridIndex: "+vIdx+","+hIdx+"("+vPoints+","+hPoints+")");
        return new Pair<Integer, Integer>(vIdx, hIdx);

    }

    /*
     * @Override public List<Position> extractLattice(int hPoints, int vPoints) {
     * 
     * Position[][] grid = extractGrid(hPoints, vPoints); List<Position> lst = new ArrayList<Position>(); for(Position[]
     * line : grid) { for(Position p : line) { lst.add(p); } } return lst; }
     * 
     * @Override //may not return a rectangular lattice! public List<Position> extractLattice(Distance hStep, Distance
     * vStep) {
     * 
     * Position startPoint = appSouthWest;
     * 
     * Bearing vBearing = getNorth(); Bearing hBearing = getEast(); boolean hMode = true;
     * 
     * List<Position> lst = new ArrayList<Position>();
     * 
     * Position current = startPoint; lst.add(current); Position next;
     * 
     * while (true) {
     * 
     * if (hMode) next = current.translateGreatCircle(hBearing, hStep); else next =
     * current.translateGreatCircle(vBearing, vStep);
     * 
     * if (isWithinBoundaries(next)) { current = next; lst.add(current); if (!hMode) { hMode = true; hBearing =
     * hBearing.reverse(); } } else { if(hMode) hMode = false; else break; }
     * 
     * }
     * 
     * return lst; }
     */

    @Override
    public int getResY() {
    	return this.vPoints;
    }
    
    @Override
    public int getResX() {
    	return this.hPoints;
    }
    
    @Override
    public int getBorderY() {
    	return this.borderY;
    }
    
    @Override
    public int getBorderX() {
    	return this.borderX;
    }
    
    @Override
    public Bearing getNorth() {
        return north;
    }

    @Override
    public Bearing getSouth() {
        return south;
    }

    @Override
    public Bearing getEast() {
        return east;
    }

    @Override
    public Bearing getWest() {
        return west;
    }

    @Override
    public Distance getWidth() {
        return appWidth;
    }

    @Override
    public Distance getHeight() {
        return appHeight;
    }

    @Override
    public Map<String, Double> getRelativeCoordinates(Position p) {
        if (!isWithinBoundaries(p))
            return null;

        Map<String, Double> map = new HashMap<String, Double>();
        Position px = p.projectToLineThrough(appSouthWest, getEast());
        Position py = p.projectToLineThrough(appSouthWest, getNorth());

        map.put("X", px.getDistance(appSouthWest).getMeters() / getWidth().getMeters());
        map.put("Y", py.getDistance(appSouthWest).getMeters() / getHeight().getMeters());
        return map;
    }

    @Override
    public Position getRelativePoint(double x, double y) {

        Position point = appSouthWest;
        point = point.translateGreatCircle(getEast(), getWidth().scale(x));
        point = point.translateGreatCircle(getNorth(), getHeight().scale(y));
        return point;
    }

    @Override
    public double getTolerance() {
        return tolerance;
    }

}
