package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;

/**
 * A google map overlay based on a HTML5 canvas for drawing a wind sensor (as an rotating arrow)
 * The wind sensor symbol will be rotated according to the wind data.
 */
public class WindSensorOverlay extends CanvasOverlay {

    private final RaceMapImageManager raceMapImageManager;

    /**
     * The current wind track used to draw the wind sensor.
     */
    private WindTrackInfoDTO windTrackInfoDTO;

    /**
     * The current wind source used to draw the wind sensor.
     */
    private WindSource windSource;

    private final ImageTransformer transformer;

    private final StringMessages stringMessages;
    private int canvasWidth;
    private int canvasHeight;

    private final NumberFormat numberFormat = NumberFormat.getFormat("0.0");
    
    public WindSensorOverlay(RaceMapImageManager raceMapImageManager, StringMessages stringMessages) {
        super();
        this.raceMapImageManager = raceMapImageManager;
        this.stringMessages = stringMessages;
        canvasWidth = 28;
        canvasHeight = 28;

        if(getCanvas() != null) {
            getCanvas().setWidth(String.valueOf(canvasWidth));
            getCanvas().setHeight(String.valueOf(canvasHeight));
            getCanvas().setCoordinateSpaceWidth(canvasWidth);
            getCanvas().setCoordinateSpaceHeight(canvasHeight);
        }
        transformer = raceMapImageManager.getExpeditionWindIconTransformer();
    }
    
    @Override
    protected Overlay copy() {
      return new WindSensorOverlay(raceMapImageManager, stringMessages);
    }

    @Override
    protected void redraw(boolean force) {
        boolean hasValidWind = false;
        if (windTrackInfoDTO != null && windTrackInfoDTO.windFixes.size() > 0) {
            WindDTO windDTO = windTrackInfoDTO.windFixes.get(0);
            PositionDTO position = windDTO.position;
            // Attention: sometimes there is no valid position for the wind source available -> ignore the wind in this case
            if (position != null) {
                double rotationDegOfWindSymbol = windDTO.dampenedTrueWindBearingDeg;
                transformer.drawToCanvas(getCanvas(), rotationDegOfWindSymbol, 1.0);
                setLatLngPosition(LatLng.newInstance(windDTO.position.latDeg, windDTO.position.lngDeg));
                Point sensorPositionInPx = getMap().convertLatLngToDivPixel(getLatLngPosition());
                getPane().setWidgetPosition(getCanvas(), sensorPositionInPx.getX() - canvasWidth / 2, sensorPositionInPx.getY() - canvasHeight / 2);
                String title = stringMessages.wind() + " ("+ WindSourceTypeFormatter.format(windSource, stringMessages) + "): "; 
                title += Math.round(windDTO.dampenedTrueWindFromDeg) + " " + stringMessages.degreesShort()+ ",  ";
                title += numberFormat.format(windDTO.dampenedTrueWindSpeedInKnots) + " " + stringMessages.averageSpeedInKnotsUnit();
                
                getCanvas().setTitle(title);
                hasValidWind = true;
            }
        }
        if (!hasValidWind) {
            setLatLngPosition(null);
        }
        getCanvas().setVisible(hasValidWind);
    }

    public WindTrackInfoDTO getWindTrackInfoDTO() {
        return windTrackInfoDTO;
    }

    public void setWindInfo(WindTrackInfoDTO windTrackInfoDTO, WindSource windSource) {
        this.windTrackInfoDTO = windTrackInfoDTO;
        this.windSource = windSource;
    }

    public WindSource getWindSource() {
        return windSource;
    }
}
