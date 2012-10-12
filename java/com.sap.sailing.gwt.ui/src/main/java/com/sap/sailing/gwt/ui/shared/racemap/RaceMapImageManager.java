package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.shared.BuoyDTO;

public class RaceMapImageManager {

    /**
     * An arrow showing the combined wind depending on the wind direction 
     */
    protected ImageTransformer combinedWindIconTransformer;

    /**
     * An arrow showing the wind provided by a wind sensor on a boat 
     */
    protected ImageTransformer expeditionWindIconTransformer;

    /**
     * An icon for a buoy
     */
    private Icon defaultBuoyIcon;

    /**
     * Contains buoy icons for display color names as obtained from {@link BuoyDTO#displayColor}, then converted to all lower case
     */
    private final Map<String, Icon> buoyIcons;
    
    protected Map<Pair<ManeuverType, Tack>, Icon> maneuverIconsForTypeAndTargetTack;

    private static RaceMapResources resources = GWT.create(RaceMapResources.class);

    public RaceMapImageManager() {
        buoyIcons = new HashMap<String, Icon>();
        maneuverIconsForTypeAndTargetTack = new HashMap<Pair<ManeuverType, Tack>, Icon>();
        
        combinedWindIconTransformer = new ImageTransformer(resources.combinedWindIcon());
        expeditionWindIconTransformer = new ImageTransformer(resources.expeditionWindIcon());
    }
    
    public Icon getIconForDisplayColor(String displayColor) {
        Icon result;
        if (displayColor != null) {
            result = buoyIcons.get(displayColor.toLowerCase());
            if (result == null) {
                result = defaultBuoyIcon;
            }
        } else {
            result = defaultBuoyIcon;
        }
        return result;
    }
    
    /*
     * Loads the map overlay icons
     * The method can only be called after the map is loaded  
     */
    public void loadMapIcons(MapWidget map) {
        if(map != null) {
            defaultBuoyIcon = Icon.newInstance(resources.buoyIcon().getSafeUri().asString());
            defaultBuoyIcon.setIconSize(Size.newInstance(19, 28));
            defaultBuoyIcon.setIconAnchor(Point.newInstance(6, 15));

            buoyIcons.put("red", Icon.newInstance(resources.buoyRedIcon().getSafeUri().asString()));
            buoyIcons.put("green", Icon.newInstance(resources.buoyGreenIcon().getSafeUri().asString()));
            buoyIcons.put("yellow", Icon.newInstance(resources.buoyYellowIcon().getSafeUri().asString()));
            buoyIcons.put("white", Icon.newInstance(resources.buoyWhiteIcon().getSafeUri().asString()));
            buoyIcons.put("black", Icon.newInstance(resources.buoyBlackIcon().getSafeUri().asString()));
            buoyIcons.put("black conical checkered", Icon.newInstance(resources.buoyBlackConeIcon().getSafeUri().asString()));
            buoyIcons.put("orange", Icon.newInstance(resources.buoyDarkOrangeIcon().getSafeUri().asString()));
            buoyIcons.put("white conical", Icon.newInstance(resources.buoyWhiteConeIcon().getSafeUri().asString()));
            buoyIcons.put("black cylinder checkered", Icon.newInstance(resources.buoyBlackFinishIcon().getSafeUri().asString()));
            buoyIcons.put("committee vessel", Icon.newInstance(resources.juryBoatIcon().getSafeUri().asString()));
            for (Icon buoyIcon : buoyIcons.values()) {
                buoyIcon.setIconSize(Size.newInstance(19, 28));
                buoyIcon.setIconAnchor(Point.newInstance(6, 15));
            }
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
    
    public ImageTransformer getCombinedWindIconTransformer() {
        return combinedWindIconTransformer;
    }

    public ImageTransformer getExpeditionWindIconTransformer() {
        return expeditionWindIconTransformer;
    }
}
