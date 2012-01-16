package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Icon;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;

public class RaceMapResources {

    private final MapWidget map;
    
    /**
     * Two sails on downwind leg, wind from port (sails on starboard); no highlighting
     */
    protected ImageTransformer boatIconDownwindPortRotator;

    /**
     * Two sails on downwind leg, wind from port (sails on starboard); with highlighting
     */
    protected ImageTransformer boatIconHighlightedDownwindPortRotator;

    /**
     * Two sails on downwind leg, wind from starboard (sails on port); no highlighting
     */
    protected ImageTransformer boatIconDownwindStarboardRotator;

    /**
     * Two sails on downwind leg, wind from starboard (sails on port); with highlighting
     */
    protected ImageTransformer boatIconHighlightedDownwindStarboardRotator;

    /**
     * One sail, wind from port (sails on starboard); no highlighting
     */
    protected ImageTransformer boatIconPortRotator;

    /**
     * One sail, wind from port (sails on starboard); with highlighting
     */
    protected ImageTransformer boatIconHighlightedPortRotator;

    /**
     * One sail, wind from starboard (sails on port); no highlighting
     */
    protected ImageTransformer boatIconStarboardRotator;

    /**
     * One sail, wind from starboard (sails on port); with highlighting
     */
    protected ImageTransformer boatIconHighlightedStarboardRotator;

    protected Icon buoyIcon;
    protected Icon tackToStarboardIcon;
    protected Icon tackToPortIcon;
    protected Icon jibeToStarboardIcon;
    protected Icon jibeToPortIcon;
    protected Icon markPassingToStarboardIcon;
    protected Icon markPassingToPortIcon;
    protected Icon headUpOnStarboardIcon;
    protected Icon headUpOnPortIcon;
    protected Icon bearAwayOnStarboardIcon;
    protected Icon bearAwayOnPortIcon;
    protected Icon unknownManeuverIcon;
    protected Icon penaltyCircleToStarboardIcon;
    protected Icon penaltyCircleToPortIcon;

    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public RaceMapResources(final MapWidget map)
    {
        this.map = map;
        
        boatIconDownwindPortRotator = new ImageTransformer(resources.lowlightedBoatIconDW_Port());
        boatIconHighlightedDownwindPortRotator = new ImageTransformer(resources.highlightedBoatIconDW_Port());
        boatIconDownwindStarboardRotator = new ImageTransformer(resources.lowlightedBoatIconDW_Starboard());
        boatIconHighlightedDownwindStarboardRotator = new ImageTransformer(resources
                .highlightedBoatIconDW_Starboard());
        boatIconPortRotator = new ImageTransformer(resources.lowlightedBoatIcon_Port());
        boatIconHighlightedPortRotator = new ImageTransformer(resources.highlightedBoatIcon_Port());
        boatIconStarboardRotator = new ImageTransformer(resources.lowlightedBoatIcon_Starboard());
        boatIconHighlightedStarboardRotator = new ImageTransformer(resources.highlightedBoatIcon_Starboard());
        buoyIcon = Icon.newInstance(resources.buoyIcon().getSafeUri().asString());
        buoyIcon.setIconAnchor(Point.newInstance(4, 4));
        tackToStarboardIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=T|00FF00|000000");
        tackToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
        tackToPortIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=T|FF0000|000000");
        tackToPortIcon.setIconAnchor(Point.newInstance(10, 33));
        jibeToStarboardIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=J|00FF00|000000");
        jibeToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
        jibeToPortIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=J|FF0000|000000");
        jibeToPortIcon.setIconAnchor(Point.newInstance(10, 33));
        headUpOnStarboardIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=H|00FF00|000000");
        headUpOnStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
        headUpOnPortIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=H|FF0000|000000");
        headUpOnPortIcon.setIconAnchor(Point.newInstance(10, 33));
        bearAwayOnStarboardIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=B|00FF00|000000");
        bearAwayOnStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
        bearAwayOnPortIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=B|FF0000|000000");
        bearAwayOnPortIcon.setIconAnchor(Point.newInstance(10, 33));
        markPassingToStarboardIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=M|00FF00|000000");
        markPassingToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
        markPassingToPortIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=M|FF0000|000000");
        markPassingToPortIcon.setIconAnchor(Point.newInstance(10, 33));
        unknownManeuverIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=?|FFFFFF|000000");
        unknownManeuverIcon.setIconAnchor(Point.newInstance(10, 33));
        penaltyCircleToStarboardIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|00FF00|000000");
        penaltyCircleToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
        penaltyCircleToPortIcon = Icon
                .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|FF0000|000000");
        penaltyCircleToPortIcon.setIconAnchor(Point.newInstance(10, 33));
    }
    
    public ImageTransformer getBoatImageTransformer(GPSFixDAO boatFix, boolean highlighted) {
        if (boatFix.tack == Tack.PORT) {
            if (LegType.DOWNWIND == boatFix.legType) {
                if (highlighted) {
                    return boatIconHighlightedDownwindStarboardRotator;
                } else {
                    return boatIconDownwindStarboardRotator;
                }
            } else {
                if (highlighted) {
                    return boatIconHighlightedStarboardRotator;
                } else {
                    return boatIconStarboardRotator;
                }
            }
        } else {
            if (LegType.DOWNWIND == boatFix.legType) {
                if (highlighted) {
                    return boatIconHighlightedDownwindPortRotator;
                } else {
                    return boatIconDownwindPortRotator;
                }
            } else {
                if (highlighted) {
                    return boatIconHighlightedPortRotator;
                } else {
                    return boatIconPortRotator;
                }
            }
        }
    }

    public String getBoatImageURL(GPSFixDAO boatFix, boolean highlighted) {
        return getBoatImageURL(getBoatImageTransformer(boatFix, highlighted), boatFix);
    }

    public String getBoatImageURL(ImageTransformer boatImageTransformer, GPSFixDAO boatFix) {
        double realBoatSizeScaleFactor = getRealBoatSizeScaleFactor();
        return boatImageTransformer.getTransformedImageURL(boatFix.speedWithBearing.bearingInDegrees, realBoatSizeScaleFactor);
    }

    public double getRealBoatSizeScaleFactor() {
        // the possible zoom level range is 0 to 21 (zoom level 0 would show the whole world)
        int zoomLevel = map.getZoomLevel();
        double minScaleFactor = 0.33;
        double realBoatSizeScaleFactor = minScaleFactor;
        double boatLengthInMeter = 5.0; 
        if(zoomLevel > 5) {
            int boatSizeXInPixel = 41; 
            LatLngBounds bounds = map.getBounds();
            LatLng upperRight = bounds.getNorthEast();
            LatLng bottomLeft = bounds.getSouthWest();
            LatLng upperLeft = LatLng.newInstance(upperRight.getLatitude(), bottomLeft.getLongitude());
            double distXInMeters = upperLeft.distanceFrom(upperRight);
            int widthInPixel = map.getSize().getWidth();
            double realBoatSizeInPixel  = (widthInPixel * boatLengthInMeter) / distXInMeters;
            realBoatSizeScaleFactor = realBoatSizeInPixel / (double) boatSizeXInPixel;
            if(realBoatSizeScaleFactor < minScaleFactor) {
                realBoatSizeScaleFactor = minScaleFactor;
            }
        }
        return realBoatSizeScaleFactor;
    }

    public Icon getBoatImageIcon(GPSFixDAO boatFix, boolean highlighted) {
        ImageTransformer boatImageTransformer = getBoatImageTransformer(boatFix, highlighted);
        Icon icon = Icon.newInstance(getBoatImageURL(boatImageTransformer, boatFix));
        icon.setIconAnchor(boatImageTransformer.getAnchor(getRealBoatSizeScaleFactor()));
        return icon;
    }
}
