package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;

public class SimulatorField implements VectorField {
    private boolean swarmDebug = false;

    private Position rcStart;
    private Position rcEnd;

    private int resY;
    private int resX;

    private int borderY;
    private int borderX;

    private double bdXi;
    private double bdPhi;

    private Position bdA;
    private Position bdB;
    private Position bdC;

    private double xScale;

    private Position visSW;
    private Position visNE;
    private boolean visFull = false;

    private double maxLength;
    private double particleFactor;

    private double lngScale;

    private Position nvY;
    private Position nvX;
    private Position gvX;

    private double[][][] data;
    private int step;

    public SimulatorField(WindFieldDTO windData, WindFieldGenParamsDTO windParams) {
        this.step = 0;
        String parseString = windData.windDataJSON.substring(18, windData.windDataJSON.length() - 1) + "}";
        JSONObject baseData = JSONParser.parseLenient(parseString).isObject();
        this.rcStart = new DegreePosition(baseData.get("rcStart").isObject().get("lat").isNumber().doubleValue(),
                baseData.get("rcStart").isObject().get("lng").isNumber().doubleValue());
        this.rcEnd = new DegreePosition(baseData.get("rcEnd").isObject().get("lat").isNumber().doubleValue(), baseData
                .get("rcEnd").isObject().get("lng").isNumber().doubleValue());
        this.resY = (int) baseData.get("resY").isNumber().doubleValue();
        this.resX = (int) baseData.get("resX").isNumber().doubleValue();
        this.borderY = (int) baseData.get("borderY").isNumber().doubleValue();
        this.borderX = (int) baseData.get("borderX").isNumber().doubleValue();
        this.bdXi = (this.borderY + 0.5) / (this.resY - 1);
        this.bdPhi = 1.0 + 2 * this.bdXi;
        this.bdA = new DegreePosition(this.rcEnd.getLatDeg() + (this.rcEnd.getLatDeg() - this.rcStart.getLatDeg())
                * this.bdXi, this.rcEnd.getLngDeg() + (this.rcEnd.getLngDeg() - this.rcStart.getLngDeg()) * this.bdXi);
        this.bdB = new DegreePosition((this.rcStart.getLatDeg() - this.rcEnd.getLatDeg()) * this.bdPhi,
                (this.rcStart.getLngDeg() - this.rcEnd.getLngDeg()) * this.bdPhi);
        this.xScale = baseData.get("xScale").isNumber().doubleValue();
        this.visSW = new DegreePosition(0.0, 0.0);
        this.visNE = new DegreePosition(0.0, 0.0);
        List<SimulatorWindDTO> gridData = windData.getMatrix();
        int p = 0;
        int imax = windParams.getyRes() + 2 * windParams.getBorderY();
        int jmax = windParams.getxRes() + 2 * windParams.getBorderX();
        int steps = gridData.size() / (imax * jmax);
        this.data = new double[steps][imax][2 * jmax];
        double maxWindSpeed = 0;
        double minWindSpeed = 100;
        for (int s = 0; s < steps; s++) {
            for (int i = 0; i < imax; i++) {
                for (int j = 0; j < jmax; j++) {
                    SimulatorWindDTO wind = gridData.get(p);
                    p++;
                    if (wind.trueWindSpeedInKnots > maxWindSpeed) {
                        maxWindSpeed = wind.trueWindSpeedInKnots;
                    }
                    if (wind.trueWindSpeedInKnots < minWindSpeed) {
                        minWindSpeed = wind.trueWindSpeedInKnots;
                    }
                    this.data[s][i][2 * j + 1] = wind.trueWindSpeedInKnots
                            * Math.cos(wind.trueWindBearingDeg * Math.PI / 180.0);
                    this.data[s][i][2 * j] = wind.trueWindSpeedInKnots
                            * Math.sin(wind.trueWindBearingDeg * Math.PI / 180.0);
                }
            }
        }
        this.maxLength = maxWindSpeed;
        this.particleFactor = 2.0;
        double latAvg = (this.rcEnd.getLatDeg() + this.rcStart.getLatDeg()) / 2.;
        this.lngScale = Math.cos(latAvg * Math.PI / 180.0);
        double difLat = this.rcEnd.getLatDeg() - this.rcStart.getLatDeg();
        double difLng = (this.rcEnd.getLngDeg() - this.rcStart.getLngDeg()) * this.lngScale;
        double difLen = Math.sqrt(difLat * difLat + difLng * difLng);
        this.nvY = new DegreePosition(difLat / difLen / difLen * (this.resY - 1), difLng / difLen / difLen
                * (this.resY - 1));
        double nrmLat = -difLng / difLen;
        double nrmLng = difLat / difLen;
        this.nvX = new DegreePosition(nrmLat / this.xScale / difLen * (this.resX - 1), nrmLng / this.xScale / difLen
                * (this.resX - 1));
        this.gvX = new DegreePosition(nrmLat * this.xScale * difLen, nrmLng / this.lngScale * this.xScale * difLen);
        this.bdC = new DegreePosition(this.gvX.getLatDeg() * (this.resX + 2 * this.borderX - 1) / (this.resX - 1),
                this.gvX.getLngDeg() * (this.resX + 2 * this.borderX - 1) / (this.resX - 1));
    }

    public Position getRandomPosition() {
        final Position result;
        if (this.visFull) {
            double rndY = Math.random();
            double rndX = Math.random();
            double latDeg = rndY * this.visSW.getLatDeg() + (1 - rndY) * this.visNE.getLatDeg();
            double lngDeg = rndX * this.visSW.getLngDeg() + (1 - rndX) * this.visNE.getLngDeg();
            result = new DegreePosition(latDeg, lngDeg);
        } else {
            double rndY = Math.random();
            double rndX = Math.random() - 0.5;
            result = this.getInnerPosition(rndX, rndY);
        }
        return result;
    }

    public Position getInnerPosition(double factX, double factY) {
        double latDeg = this.bdA.getLatDeg() + factY * this.bdB.getLatDeg() + factX * this.bdC.getLatDeg();
        double lngDeg = this.bdA.getLngDeg() + factY * this.bdB.getLngDeg() + factX * this.bdC.getLngDeg();
        Position result = new DegreePosition(latDeg, lngDeg);
        if (swarmDebug && (!this.inBounds(result))) {
            GWT.log("random-position: out of bounds");
        }
        return result;
    }

    public boolean inBounds(Position p) {
        Index idx = this.getIndex(p);
        boolean inBool = (idx.x >= 0) && (idx.x < (this.resX + 2 * this.borderX)) && (idx.y >= 0)
                && (idx.y < (this.resY + 2 * this.borderY));
        return inBool;
    }

    private Vector interpolate(Position p) {
        Neighbors idx = this.getNeighbors(p);
        if (swarmDebug
                && ((idx.xTop >= (this.resX + 2 * this.borderX)) || (idx.yTop >= (this.resY + 2 * this.borderY)))) {
            GWT.log("interpolate: out of range: " + idx.xTop + "  " + idx.yTop);
        }
        double avgX = this.data[this.step][idx.yBot][2 * idx.xBot] * (1 - idx.yMod) * (1 - idx.xMod)
                + this.data[this.step][idx.yTop][2 * idx.xBot] * idx.yMod * (1 - idx.xMod)
                + this.data[this.step][idx.yBot][2 * idx.xTop] * (1 - idx.yMod) * idx.xMod
                + this.data[this.step][idx.yTop][2 * idx.xTop] * idx.yMod * idx.xMod;
        double avgY = this.data[this.step][idx.yBot][2 * idx.xBot + 1] * (1 - idx.yMod) * (1 - idx.xMod)
                + this.data[this.step][idx.yTop][2 * idx.xBot + 1] * idx.yMod * (1 - idx.xMod)
                + this.data[this.step][idx.yBot][2 * idx.xTop + 1] * (1 - idx.yMod) * idx.xMod
                + this.data[this.step][idx.yTop][2 * idx.xTop + 1] * idx.yMod * idx.xMod;
        return new Vector(avgX / this.lngScale, avgY);
    }

    public void setStep(int step) {
        if (step < 0) {
            this.step = 0;
        } else if (step >= this.data.length) {
            this.step = this.data.length - 1;
        } else {
            this.step = step;
        }
    }

    public void nextStep() {
        if (this.step < (this.data.length - 1)) {
            this.step++;
        }
    }

    public void prevStep() {
        if (this.step > 0) {
            this.step--;
        }
    }

    public Vector getVector(Position p) {
        return this.interpolate(p);
    }

    public Index getIndex(Position p) {
        // calculate grid indexes
        Position posR = new DegreePosition(p.getLatDeg() - this.rcStart.getLatDeg(),
                (p.getLngDeg() - this.rcStart.getLngDeg()) * this.lngScale);

        // closest grid point
        long yIdx = Math.round(posR.getLatDeg() * this.nvY.getLatDeg() + posR.getLngDeg() * this.nvY.getLngDeg())
                + this.borderY;
        long xIdx = Math.round(posR.getLatDeg() * this.nvX.getLatDeg() + posR.getLngDeg() * this.nvX.getLngDeg()
                + (this.resX - 1) / 2.)
                + this.borderX;

        if (this.visFull) {
            if (yIdx >= (this.resY + 2 * this.borderY)) {
                yIdx = this.resY + 2 * this.borderY - 1;
            }
            if (yIdx < 0) {
                yIdx = 0;
            }
            if (xIdx >= (this.resX + 2 * this.borderX)) {
                xIdx = this.resX + 2 * this.borderX - 1;
            }
            if (xIdx < 0) {
                xIdx = 0;
            }
        }
        return new Index(xIdx, yIdx);
    }

    public Neighbors getNeighbors(Position p) {

        // calculate grid indexes
        Position posR = new DegreePosition(p.getLatDeg() - this.rcStart.getLatDeg(),
                (p.getLngDeg() - this.rcStart.getLngDeg()) * this.lngScale);

        // surrounding grid points
        double yFlt = posR.getLatDeg() * this.nvY.getLatDeg() + posR.getLngDeg() * this.nvY.getLngDeg() + this.borderY;
        double xFlt = posR.getLatDeg() * this.nvX.getLatDeg() + posR.getLngDeg() * this.nvX.getLngDeg()
                + (this.resX - 1) / 2. + this.borderX;
        double yBot = Math.floor(yFlt);
        double xBot = Math.floor(xFlt);
        double yTop = Math.ceil(yFlt);
        double xTop = Math.ceil(xFlt);

        if (xBot < 0) {
            xBot = 0;
            xFlt = xBot;
            xTop = 1;
        }

        if (yBot < 0) {
            yBot = 0;
            yFlt = yBot;
            yTop = 1;
        }

        if (xTop >= (this.resX + 2 * this.borderX)) {
            xTop = this.resX + 2 * this.borderX - 1;
            xFlt = xTop;
            xBot = xTop - 1;
        }

        if (yTop >= (this.resY + 2 * this.borderY)) {
            yTop = this.resY + 2 * this.borderY - 1;
            yFlt = yTop;
            yBot = yTop - 1;
        }

        double yMod = yFlt - yBot;
        double xMod = xFlt - xBot;

        // System.out.println("neighbors:"+xFlt+","+xBot+","+xTop+","+yFlt+","+yBot+","+yTop);

        return new Neighbors((int) xTop, (int) yTop, (int) xBot, (int) yBot, xMod, yMod);
    }

    public double getMaxLength() {
        return maxLength;
    }

    public double motionScale(int zoomLevel) {
        return 0.07 * Math.pow(1.6, Math.min(1.0, 6.0 - zoomLevel));
    }

    public double particleWeight(Position p, Vector v) {
        return v.length() / this.maxLength + 0.1;
    }

    public String[] getColors() {
        String[] colors = new String[256];
        double alphaMin = 0.0;
        double alphaMax = 1.0;
        int greyValue = 255;
        for (int i = 0; i < 256; i++) {
            colors[i] = "rgba(" + (greyValue) + "," + (greyValue) + "," + (greyValue) + ","
                    + (alphaMin + (alphaMax - alphaMin) * i / 255.0) + ")";
        }
        return colors;
    }

    public int getIntensity(double speed) {
        /*
         * normalized intensity: speed == average wind speed => intensity 0.5 speed <= minimum wind speed => intensity
         * 0.0 speed between 0.0 and 1.0 double s; if (minLength == maxLength) { s = 0.5; } else if (speed <= minLength)
         * { s = 0.0; } else { s = (speed - minLength) / (maxLength - minLength); }
         */

        /*
         * absolute intensity speed == 12kn => intensity 0.7 speed == 20kn => intensity 1.0 speed == 0kn => intensity
         * 0.25
         */
        double s = 0.7 + 0.0375 * (speed - 12.0);
        return (int) Math.max(0, Math.min(255, Math.round(255 * s)));
    }

    public double lineWidth(double speed) {
        /*
         * absolute linewidth speed == 12kn => linewidth 1.5 speed == 24kn => linewidth 3.0 speed == 6kn => linewidth
         * 0.75
         */
        return Math.round(speed / 8.0 * 100.0) / 100.0;
    }

    public Position[] getFieldCorners() {
        Position fieldNE = this.getInnerPosition(+0.5, 1.0);
        Position fieldSW = this.getInnerPosition(-0.5, 0.0);
        Position fieldSE = this.getInnerPosition(+0.5, 0.0);
        Position fieldNW = this.getInnerPosition(-0.5, 1.0);

        DegreePosition[] result = new DegreePosition[2];

        double minLat = Math.min(Math.min(fieldNE.getLatDeg(), fieldSW.getLatDeg()),
                Math.min(fieldNW.getLatDeg(), fieldSE.getLatDeg()));
        double minLng = Math.min(Math.min(fieldNE.getLngDeg(), fieldSW.getLngDeg()),
                Math.min(fieldNW.getLngDeg(), fieldSE.getLngDeg()));
        result[0] = new DegreePosition(minLat, minLng);

        double maxLat = Math.max(Math.max(fieldNE.getLatDeg(), fieldSW.getLatDeg()),
                Math.max(fieldNW.getLatDeg(), fieldSE.getLatDeg()));
        double maxLng = Math.max(Math.max(fieldNE.getLngDeg(), fieldSW.getLngDeg()),
                Math.max(fieldNW.getLngDeg(), fieldSE.getLngDeg()));
        result[1] = new DegreePosition(maxLat, maxLng);

        return result;
    }

    public void setVisNE(Position visNE) {
        this.visNE = visNE;
    }

    public void setVisSW(Position visSW) {
        this.visSW = visSW;
    }

    public void setVisFullCanvas(boolean full) {
        if (this.visFull != full) {
            System.out.println("visFull: " + full);
            this.visFull = full;
        }
    }

    public double getParticleFactor() {
        return this.particleFactor;
    }
}
