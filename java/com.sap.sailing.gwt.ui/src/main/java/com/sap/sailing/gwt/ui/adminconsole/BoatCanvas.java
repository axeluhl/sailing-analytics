package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;

public class BoatCanvas {

    private final CompetitorDTO competitorDTO;

    private final RaceMapResources raceMapResources;
    
    private final Canvas canvas;
    
    public BoatCanvas(final CompetitorDTO competitorDTO, RaceMapResources raceMapResources) {
        this.competitorDTO = competitorDTO;
        this.raceMapResources = raceMapResources;
        canvas = Canvas.createIfSupported();
        if(canvas != null) {
            canvas.getElement().getStyle().setZIndex(150);
            canvas.getElement().getStyle().setPosition(Position.ABSOLUTE);
        }
    }
    
    public void updateBoat(GPSFixDTO boatFix, boolean highlighted) {
        ImageTransformer boatImageTransformer = raceMapResources.getBoatImageTransformer(boatFix, highlighted);
        double realBoatSizeScaleFactor = raceMapResources.getRealBoatSizeScaleFactor(boatImageTransformer.getImageSize());        
        ImageData imageData = boatImageTransformer.getTransformedImageData(boatFix.speedWithBearing.bearingInDegrees, realBoatSizeScaleFactor);
        
        canvas.setWidth(imageData.getWidth() + "px");
        canvas.setHeight(imageData.getWidth() + "px");
        canvas.getElement().getStyle().setLeft(10., Unit.PX);
        canvas.getElement().getStyle().setTop(10., Unit.PX);
        canvas.getContext2d().putImageData(imageData, 0, 0);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public boolean isVisible() {
        if(canvas == null)
            return false;
        
        return canvas.isVisible();
    }

    public void setVisible(boolean isVisible) {
        if(canvas != null)
            canvas.setVisible(isVisible);
    }
}
