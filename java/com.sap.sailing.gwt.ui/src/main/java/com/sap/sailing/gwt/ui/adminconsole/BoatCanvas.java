package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class BoatCanvas {

    protected final CompetitorDTO competitorDTO;

    protected final RaceMapResources raceMapResources;
    
    protected final Canvas canvas;

    protected int posX;
    
    protected int posY;
    
    protected boolean isSelected;
    
    public BoatCanvas(final CompetitorDTO competitorDTO, RaceMapResources raceMapResources) {
        this.competitorDTO = competitorDTO;
        this.raceMapResources = raceMapResources;
        canvas = Canvas.createIfSupported();
        if(canvas != null) {
            canvas.getElement().getStyle().setZIndex(100);
            canvas.getElement().getStyle().setPosition(Position.ABSOLUTE);
        }
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

    public void setPosition(int newPosX, int newPosY) {
        this.posX = newPosX;
        this.posY = newPosY;
        
        canvas.getElement().getStyle().setLeft(posX, Unit.PX);
        canvas.getElement().getStyle().setTop(posY, Unit.PX);
    }

     public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
