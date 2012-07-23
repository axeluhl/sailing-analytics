package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;

public class RaceMapImageManager {
    /**
     * Two sails on downwind leg, wind from port (sails on starboard); no highlighting
     */
    protected ImageTransformer boatIconDownwindPortTransformer;

    /**
     * Two sails on downwind leg, wind from port (sails on starboard); with highlighting
     */
    protected ImageTransformer boatIconHighlightedDownwindPortTransformer;

    /**
     * Two sails on downwind leg, wind from starboard (sails on port); no highlighting
     */
    protected ImageTransformer boatIconDownwindStarboardTransformer;

    /**
     * Two sails on downwind leg, wind from starboard (sails on port); with highlighting
     */
    protected ImageTransformer boatIconHighlightedDownwindStarboardTransformer;

    /**
     * One sail, wind from port (sails on starboard); no highlighting
     */
    protected ImageTransformer boatIconPortTransformer;

    /**
     * One sail, wind from port (sails on starboard); with highlighting
     */
    protected ImageTransformer boatIconHighlightedPortTransformer;

    /**
     * One sail, wind from starboard (sails on port); no highlighting
     */
    protected ImageTransformer boatIconStarboardTransformer;

    /**
     * One sail, wind from starboard (sails on port); with highlighting
     */
    protected ImageTransformer boatIconHighlightedStarboardTransformer;

    /**
     * An arrow showing the combined wind depending on the wind direction 
     */
    protected ImageTransformer combinedWindIconTransformer;

    /**
     * An arrow showing the wind provided by a wind sensor on a boat 
     */
    protected ImageTransformer expeditionWindIconTransformer;

    protected Icon buoyIcon;

    protected Map<Pair<ManeuverType, Tack>, Icon> maneuverIconsForTypeAndTargetTack;

    private static RaceMapResources resources = GWT.create(RaceMapResources.class);

    public RaceMapImageManager() {
        maneuverIconsForTypeAndTargetTack = new HashMap<Pair<ManeuverType, Tack>, Icon>();
        
        boatIconDownwindPortTransformer = new ImageTransformer(resources.lowlightedBoatIconDW_Port());
        boatIconHighlightedDownwindPortTransformer = new ImageTransformer(resources.highlightedBoatIconDW_Port());
        boatIconDownwindStarboardTransformer = new ImageTransformer(resources.lowlightedBoatIconDW_Starboard());
        boatIconHighlightedDownwindStarboardTransformer = new ImageTransformer(resources.highlightedBoatIconDW_Starboard());
        boatIconPortTransformer = new ImageTransformer(resources.lowlightedBoatIcon_Port());
        boatIconHighlightedPortTransformer = new ImageTransformer(resources.highlightedBoatIcon_Port());
        boatIconStarboardTransformer = new ImageTransformer(resources.lowlightedBoatIcon_Starboard());
        boatIconHighlightedStarboardTransformer = new ImageTransformer(resources.highlightedBoatIcon_Starboard());
        combinedWindIconTransformer = new ImageTransformer(resources.combinedWindIcon());
        expeditionWindIconTransformer = new ImageTransformer(resources.expeditionWindIcon());
    }
    
    /*
     * Loads the map overlay icons
     * The method can only be called after the map is loaded  
     */
    public void loadMapIcons(MapWidget map) {
        if(map != null) {
            buoyIcon = Icon.newInstance(resources.buoyIcon().getSafeUri().asString());
            buoyIcon.setIconSize(Size.newInstance(19, 28));
            buoyIcon.setIconAnchor(Point.newInstance(6, 15));

            Icon tackToStarboardIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=T|00FF00|000000");
            tackToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.TACK, Tack.STARBOARD), tackToStarboardIcon);
            Icon tackToPortIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=T|FF0000|000000");
            tackToPortIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.TACK, Tack.PORT), tackToPortIcon);
            Icon jibeToStarboardIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=J|00FF00|000000");
            jibeToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.JIBE, Tack.STARBOARD), jibeToStarboardIcon);
            Icon jibeToPortIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=J|FF0000|000000");
            jibeToPortIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.JIBE, Tack.PORT), jibeToPortIcon);
            Icon headUpOnStarboardIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=H|00FF00|000000");
            headUpOnStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.HEAD_UP, Tack.STARBOARD), headUpOnStarboardIcon);
            Icon headUpOnPortIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=H|FF0000|000000");
            headUpOnPortIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.HEAD_UP, Tack.PORT), headUpOnPortIcon);
            Icon bearAwayOnStarboardIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=B|00FF00|000000");
            bearAwayOnStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.BEAR_AWAY, Tack.STARBOARD), bearAwayOnStarboardIcon);
            Icon bearAwayOnPortIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=B|FF0000|000000");
            bearAwayOnPortIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.BEAR_AWAY, Tack.PORT), bearAwayOnPortIcon);
            Icon markPassingToStarboardIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=M|00FF00|000000");
            markPassingToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.MARK_PASSING, Tack.STARBOARD), markPassingToStarboardIcon);
            Icon markPassingToPortIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=M|FF0000|000000");
            markPassingToPortIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.MARK_PASSING, Tack.PORT), markPassingToPortIcon);
            Icon unknownManeuverIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=?|FFFFFF|000000");
            unknownManeuverIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.UNKNOWN, Tack.STARBOARD), unknownManeuverIcon);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.UNKNOWN, Tack.PORT), unknownManeuverIcon);
            Icon penaltyCircleToStarboardIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|00FF00|000000");
            penaltyCircleToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.PENALTY_CIRCLE, Tack.STARBOARD), penaltyCircleToStarboardIcon);
            Icon penaltyCircleToPortIcon = Icon
                    .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|FF0000|000000");
            penaltyCircleToPortIcon.setIconAnchor(Point.newInstance(10, 33));
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.PENALTY_CIRCLE, Tack.PORT), penaltyCircleToPortIcon);
        }
    }
    
    public ImageTransformer getBoatImageTransformer(GPSFixDTO boatFix, boolean highlighted) {
        if (boatFix.tack == Tack.PORT) {
            if (LegType.DOWNWIND == boatFix.legType) {
                if (highlighted) {
                    return boatIconHighlightedDownwindStarboardTransformer;
                } else {
                    return boatIconDownwindStarboardTransformer;
                }
            } else {
                if (highlighted) {
                    return boatIconHighlightedStarboardTransformer;
                } else {
                    return boatIconStarboardTransformer;
                }
            }
        } else {
            if (LegType.DOWNWIND == boatFix.legType) {
                if (highlighted) {
                    return boatIconHighlightedDownwindPortTransformer;
                } else {
                    return boatIconDownwindPortTransformer;
                }
            } else {
                if (highlighted) {
                    return boatIconHighlightedPortTransformer;
                } else {
                    return boatIconPortTransformer;
                }
            }
        }
    }

    public ImageTransformer getCombinedWindIconTransformer() {
        return combinedWindIconTransformer;
    }

    public ImageTransformer getExpeditionWindIconTransformer() {
        return expeditionWindIconTransformer;
    }
}
