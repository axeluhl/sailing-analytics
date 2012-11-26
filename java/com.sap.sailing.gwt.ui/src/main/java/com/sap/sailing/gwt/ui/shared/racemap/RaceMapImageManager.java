package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.Util.Pair;

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
     * The default icon for a course mark
     */
    private Icon defaultCourseMarkIcon;
 
    private final List<MarkIconDescriptor> markIconDescriptors;
    
    protected Map<Pair<ManeuverType, Tack>, Icon> maneuverIconsForTypeAndTargetTack;

    private static RaceMapResources resources = GWT.create(RaceMapResources.class);

    public RaceMapImageManager() {
        markIconDescriptors = new ArrayList<MarkIconDescriptor>();
        
        maneuverIconsForTypeAndTargetTack = new HashMap<Pair<ManeuverType, Tack>, Icon>();
        
        combinedWindIconTransformer = new ImageTransformer(resources.combinedWindIcon());
        expeditionWindIconTransformer = new ImageTransformer(resources.expeditionWindIcon());
    }
    
    public Icon resolveMarkIcon(MarkType type, String color, String shape, String pattern) {
        Icon result = defaultCourseMarkIcon;
        
        for (MarkIconDescriptor iconDescriptor: markIconDescriptors) {
            if(iconDescriptor.isCompatible(type, color, shape, pattern)) {
                result = iconDescriptor.getIcon();
                break;
            }
        }
        
        return result;
    }
    
    /*
     * Loads the map overlay icons
     * The method can only be called after the map is loaded  
     */
    public void loadMapIcons(MapWidget map) {
        if(map != null) {
            defaultCourseMarkIcon = Icon.newInstance(resources.buoyIcon().getSafeUri().asString());
            defaultCourseMarkIcon.setIconSize(Size.newInstance(19, 28));
            defaultCourseMarkIcon.setIconAnchor(Point.newInstance(6, 15));

            createMarkIconDescriptor(resources.buoyRedIcon(), MarkType.BUOY, "red", null, null, 6, 20); 
            createMarkIconDescriptor(resources.buoyGreenIcon(), MarkType.BUOY, "green", null, null, 6, 20); 
            createMarkIconDescriptor(resources.buoyYellowIcon(), MarkType.BUOY, "yellow", null, null, 6, 20); 
            createMarkIconDescriptor(resources.buoyGreyIcon(), MarkType.BUOY, "grey", null, null, 6, 20);
            createMarkIconDescriptor(resources.buoyWhiteIcon(), MarkType.BUOY, "white", null, null, 6, 20);
            createMarkIconDescriptor(resources.buoyWhiteConeIcon(), MarkType.BUOY, "white", "conical", null, 6, 20); 
            createMarkIconDescriptor(resources.buoyBlackIcon(), MarkType.BUOY, "black", null, null, 6, 20);
            createMarkIconDescriptor(resources.buoyBlackConeIcon(), MarkType.BUOY, "black", "conical", null, 6, 20); 
            createMarkIconDescriptor(resources.buoyDarkOrangeIcon(), MarkType.BUOY, "orange", null, null, 6, 20);
            createMarkIconDescriptor(resources.buoyBlackFinishIcon(), MarkType.BUOY, "black", "cylinder", "checkered", 6, 20); 
            
            createMarkIconDescriptor(resources.cameraBoatIcon(), MarkType.CAMERABOAT, null, null, null, 6, 15);
            createMarkIconDescriptor(resources.umpireBoatIcon(), MarkType.UMPIREBOAT, null, null, null, 6, 15);
            createMarkIconDescriptor(resources.startBoatIcon(), MarkType.STARTBOAT, null, null, null, 6, 15);

            createMarkIconDescriptor(resources.landmarkIcon(), MarkType.LANDMARK, null, null, null, 6, 15);
            
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
    
    private void createMarkIconDescriptor(ImageResource imgResource, MarkType type, String color, String shape, String pattern,
            int anchorPointX, int anchorPointY) {
        Icon icon = Icon.newInstance(imgResource.getSafeUri().asString());
        //icon.setIconSize(Size.newInstance(19, 28));
        icon.setIconAnchor(Point.newInstance(anchorPointX, anchorPointY));
        MarkIconDescriptor markIconDescriptor = new MarkIconDescriptor(icon, type, color, shape, pattern);
        markIconDescriptors.add(markIconDescriptor);
    }
    
    public ImageTransformer getCombinedWindIconTransformer() {
        return combinedWindIconTransformer;
    }

    public ImageTransformer getExpeditionWindIconTransformer() {
        return expeditionWindIconTransformer;
    }
}
