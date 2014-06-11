package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;

public class SimulatorField implements VectorField {
    private boolean swarmDebug = false;

    private Position rcStart;
    private Position rcEnd;

    private final int resY;
    private final int resX;

    private final int borderY;
    private final int borderX;

    private final Position bdA;
    private final Position bdB;
    private final Position bdC;

    private final double xScale;
    private final double maxLength;
    private final double particleFactor;

    /**
     * The cosine of the field's average latitude (arithmetic average of NW's latitude and SE's latitude) which represents
     * the average ratio of pixels used per longitude and latitude angle in the Mercator projection. At the equator, this
     * ration is 1; towards the poles it gets less.
     */
    private final double lngScale;

    private final Position nvY;
    private final Position nvX;

    private final double[][][] data;
    private final String[] colorsForSpeeds;
    private int step;

    public SimulatorField(WindFieldDTO windData, WindFieldGenParamsDTO windParams) {
        this.colorsForSpeeds = createColorsForSpeeds();
        this.step = 0;
        this.rcStart = new DegreePosition(windData.windData.rcStart.latDeg, windData.windData.rcStart.lngDeg);
        this.rcEnd = new DegreePosition(windData.windData.rcEnd.latDeg, windData.windData.rcEnd.lngDeg);
        this.resX = windData.windData.resX;
        this.resY = windData.windData.resY;
        this.borderX = windData.windData.borderX;
        this.borderY = windData.windData.borderY;
        final double bdXi = (this.borderY + 0.5) / (this.resY - 1);
        final double bdPhi = 1.0 + 2 * bdXi;
        this.bdA = new DegreePosition(this.rcEnd.getLatDeg() + (this.rcEnd.getLatDeg() - this.rcStart.getLatDeg())
                * bdXi, this.rcEnd.getLngDeg() + (this.rcEnd.getLngDeg() - this.rcStart.getLngDeg()) * bdXi);
        this.bdB = new DegreePosition((this.rcStart.getLatDeg() - this.rcEnd.getLatDeg()) * bdPhi,
                (this.rcStart.getLngDeg() - this.rcEnd.getLngDeg()) * bdPhi);
        this.xScale = windData.windData.xScale;
        List<SimulatorWindDTO> gridData = windData.getMatrix();
        int p = 0;
        int imax = windParams.getyRes() + 2 * windParams.getBorderY();
        int jmax = windParams.getxRes() + 2 * windParams.getBorderX();
        int steps = gridData.size() / (imax * jmax);
        this.data = new double[steps][imax][2 * jmax];
        double maxWindSpeed = 0;
        double minWindSpeed = Double.MAX_VALUE;
        // copy the wind data from WindFieldDTO.getMatrix() to the data array, determining min/max wind speed
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
        final double latAvg = (this.rcEnd.getLatDeg() + this.rcStart.getLatDeg()) / 2.;
        this.lngScale = Math.cos(latAvg * Math.PI / 180.0);
        final double difLat = this.rcEnd.getLatDeg() - this.rcStart.getLatDeg();
        final double difLng = (this.rcEnd.getLngDeg() - this.rcStart.getLngDeg()) * this.lngScale;
        final double difLen = Math.sqrt(difLat * difLat + difLng * difLng);
        this.nvY = new DegreePosition(difLat / difLen / difLen * (this.resY - 1), difLng / difLen / difLen
                * (this.resY - 1));
        double nrmLat = -difLng / difLen;
        double nrmLng = difLat / difLen;
        this.nvX = new DegreePosition(nrmLat / this.xScale / difLen * (this.resX - 1), nrmLng / this.xScale / difLen
                * (this.resX - 1));
        final Position gvX = new DegreePosition(nrmLat * this.xScale * difLen, nrmLng / this.lngScale * this.xScale * difLen);
        this.bdC = new DegreePosition(gvX.getLatDeg() * (this.resX + 2 * this.borderX - 1) / (this.resX - 1),
                gvX.getLngDeg() * (this.resX + 2 * this.borderX - 1) / (this.resX - 1));
    }

    private Position getInnerPosition(double factX, double factY, boolean visFull) {
        double latDeg = this.bdA.getLatDeg() + factY * this.bdB.getLatDeg() + factX * this.bdC.getLatDeg();
        double lngDeg = this.bdA.getLngDeg() + factY * this.bdB.getLngDeg() + factX * this.bdC.getLngDeg();
        Position result = new DegreePosition(latDeg, lngDeg);
        if (swarmDebug && (!this.inBounds(result, visFull))) {
            GWT.log("random-position: out of bounds");
        }
        return result;
    }

    @Override
    public boolean inBounds(Position p, boolean visFull) {
        Index idx = this.getIndex(p, visFull);
        boolean inBool = (idx.x >= 0) && (idx.x < (this.resX + 2 * this.borderX)) && (idx.y >= 0)
                && (idx.y < (this.resY + 2 * this.borderY));
        return inBool;
    }

    private Vector interpolate(Position p) {
        Neighbors idx = getNeighbors(p);
        if (swarmDebug
                && ((idx.xTop >= (this.resX + 2 * this.borderX)) || (idx.yTop >= (this.resY + 2 * this.borderY)))) {
            GWT.log("interpolate: out of range: " + idx.xTop + "  " + idx.yTop);
        }
        final double[][] dataAtStep = this.data[this.step];
        double avgX = dataAtStep[idx.yBot][2 * idx.xBot] * (1 - idx.yMod) * (1 - idx.xMod)
                + dataAtStep[idx.yTop][2 * idx.xBot] * idx.yMod * (1 - idx.xMod)
                + dataAtStep[idx.yBot][2 * idx.xTop] * (1 - idx.yMod) * idx.xMod
                + dataAtStep[idx.yTop][2 * idx.xTop] * idx.yMod * idx.xMod;
        double avgY = dataAtStep[idx.yBot][2 * idx.xBot + 1] * (1 - idx.yMod) * (1 - idx.xMod)
                + dataAtStep[idx.yTop][2 * idx.xBot + 1] * idx.yMod * (1 - idx.xMod)
                + dataAtStep[idx.yBot][2 * idx.xTop + 1] * (1 - idx.yMod) * idx.xMod
                + dataAtStep[idx.yTop][2 * idx.xTop + 1] * idx.yMod * idx.xMod;
        return new Vector(avgX / this.lngScale, avgY);
    }

    @Override
    public void setStep(int step) {
        if (step < 0) {
            this.step = 0;
        } else if (step >= this.data.length) {
            this.step = this.data.length - 1;
        } else {
            this.step = step;
        }
    }

    @Override
    public void nextStep() {
        if (this.step < (this.data.length - 1)) {
            this.step++;
        }
    }

    @Override
    public void prevStep() {
        if (this.step > 0) {
            this.step--;
        }
    }

    @Override
    public Vector getVector(Position p) {
        return this.interpolate(p);
    }

    private Index getIndex(Position p, boolean visFull) {
        // calculate grid indexes
        final double latOffset = p.getLatDeg() - this.rcStart.getLatDeg();
        final double lngOffset = (p.getLngDeg() - this.rcStart.getLngDeg()) * this.lngScale;
        // closest grid point
        long yIdx = Math.round(latOffset * this.nvY.getLatDeg() + lngOffset * this.nvY.getLngDeg())
                + this.borderY;
        long xIdx = Math.round(latOffset * this.nvX.getLatDeg() + lngOffset * this.nvX.getLngDeg()
                + (this.resX - 1) / 2.)
                + this.borderX;

        if (visFull) {
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

    private Neighbors getNeighbors(Position p) {
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
        return new Neighbors((int) xTop, (int) yTop, (int) xBot, (int) yBot, xMod, yMod);
    }

    @Override
    public double getMaxLength() {
        return maxLength;
    }

    @Override
    public double motionScale(int zoomLevel) {
        return 0.07 * Math.pow(1.6, Math.min(1.0, 6.0 - zoomLevel));
    }

    @Override
    public double particleWeight(Position p, Vector v) {
        return v.length() / this.maxLength + 0.1;
    }

    private String[] createColorsForSpeeds() {
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
    
    @Override
    public String getColor(double speed) {
        return colorsForSpeeds[getIntensity(speed)];
    }

    private int getIntensity(double speed) {
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

    @Override
    public double lineWidth(double speed) {
        /*
         * absolute linewidth speed == 12kn => linewidth 1.5 speed == 24kn => linewidth 3.0 speed == 6kn => linewidth
         * 0.75
         */
        return Math.round(speed / 8.0 * 100.0) / 100.0;
    }

    @Override
    public Position[] getFieldCorners(boolean visFull) {
        Position fieldNE = this.getInnerPosition(+0.5, 1.0, visFull);
        Position fieldSW = this.getInnerPosition(-0.5, 0.0, visFull);
        Position fieldSE = this.getInnerPosition(+0.5, 0.0, visFull);
        Position fieldNW = this.getInnerPosition(-0.5, 1.0, visFull);

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

    @Override
    public double getParticleFactor() {
        return this.particleFactor;
    }
}
