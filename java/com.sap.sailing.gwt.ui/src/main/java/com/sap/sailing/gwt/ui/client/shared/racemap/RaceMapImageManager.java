package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerImage;
import com.google.gwt.maps.client.overlays.MarkerOptions;
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
    protected ImageTransformer windSensorIconTransformer;

    /**
     * The default image for a course mark
     */
    private MarkImageDescriptor defaultCourseMarkDescriptor;
 
    private final List<MarkImageDescriptor> markImageDescriptors;
    
    protected Map<Pair<ManeuverType, Tack>, Marker> maneuverIconsForTypeAndTargetTack;

    private static RaceMapResources resources = GWT.create(RaceMapResources.class);

    public RaceMapImageManager() {
        markImageDescriptors = new ArrayList<MarkImageDescriptor>();
        
        maneuverIconsForTypeAndTargetTack = new HashMap<Pair<ManeuverType, Tack>, Marker>();
        
        combinedWindIconTransformer = new ImageTransformer(resources.combinedWindIcon());
        windSensorIconTransformer = new ImageTransformer(resources.expeditionWindIcon());
    }

    public MarkImageDescriptor resolveMarkImage(MarkType type, String color, String shape, String pattern) {
        MarkImageDescriptor result = defaultCourseMarkDescriptor;
        int highestCompatibilityLevel = -1;
        
        for (MarkImageDescriptor imageDescriptor: markImageDescriptors) {
            int compatibilityLevel = imageDescriptor.getCompatibilityLevel(type, color, shape, pattern);
            if(compatibilityLevel > highestCompatibilityLevel) {
               result = imageDescriptor;
               highestCompatibilityLevel = compatibilityLevel;
               if(highestCompatibilityLevel == 3) {
                   break;
               }
            }
        }
        
        return result;
    }

    /**
     * Loads the map overlay icons
     * The method can only be called after the map is loaded  
     */
    public void loadMapIcons(MapWidget map) {
        if(map != null) {
            defaultCourseMarkDescriptor = createMarkImageDescriptor(resources.buoyIcon(), MarkType.BUOY, "undefined", null, null, 6, 20);

            createMarkImageDescriptor(resources.buoyRedIcon(), MarkType.BUOY, "red", null, null, 6, 20); 
            createMarkImageDescriptor(resources.buoyGreenIcon(), MarkType.BUOY, "green", null, null, 6, 20); 
            createMarkImageDescriptor(resources.buoyYellowIcon(), MarkType.BUOY, "yellow", null, null, 6, 20); 
            createMarkImageDescriptor(resources.buoyGreyIcon(), MarkType.BUOY, "grey", null, null, 6, 20);
            createMarkImageDescriptor(resources.buoyWhiteIcon(), MarkType.BUOY, "white", null, null, 6, 20);
            createMarkImageDescriptor(resources.buoyWhiteConeIcon(), MarkType.BUOY, "white", "conical", null, 6, 20); 
            createMarkImageDescriptor(resources.buoyBlackIcon(), MarkType.BUOY, "black", null, null, 6, 20);
            createMarkImageDescriptor(resources.buoyBlackConeIcon(), MarkType.BUOY, "black", "conical", null, 6, 20); 
            createMarkImageDescriptor(resources.buoyDarkOrangeIcon(), MarkType.BUOY, "orange", null, null, 6, 20);
            createMarkImageDescriptor(resources.buoyBlackFinishIcon(), MarkType.BUOY, "black", "cylinder", "checkered", 6, 20); 
            
            createMarkImageDescriptor(resources.cameraBoatIcon(), MarkType.CAMERABOAT, null, null, null, 35, 20);
            createMarkImageDescriptor(resources.umpireBoatIcon(), MarkType.UMPIREBOAT, null, null, null, 35, 20);
            createMarkImageDescriptor(resources.startBoatIcon(), MarkType.STARTBOAT, null, null, null, 35, 20);

            createMarkImageDescriptor(resources.landmarkIcon(), MarkType.LANDMARK, null, null, null, 6, 15);
            
            Marker tackToStarboardIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=T|00FF00|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.TACK, Tack.STARBOARD), tackToStarboardIcon);

            Marker tackToPortIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=T|FF0000|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.TACK, Tack.PORT), tackToPortIcon);

            Marker jibeToStarboardIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=J|00FF00|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.JIBE, Tack.STARBOARD), jibeToStarboardIcon);

            Marker jibeToPortIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=J|FF0000|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.JIBE, Tack.PORT), jibeToPortIcon);

            Marker headUpOnStarboardIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=H|00FF00|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.HEAD_UP, Tack.STARBOARD), headUpOnStarboardIcon);

            Marker headUpOnPortIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=H|FF0000|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.HEAD_UP, Tack.PORT), headUpOnPortIcon);

            Marker bearAwayOnStarboardIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=B|00FF00|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.BEAR_AWAY, Tack.STARBOARD), bearAwayOnStarboardIcon);
          
            Marker bearAwayOnPortIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=B|FF0000|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.BEAR_AWAY, Tack.PORT), bearAwayOnPortIcon);

            Marker markPassingToStarboardIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=M|00FF00|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.MARK_PASSING, Tack.STARBOARD), markPassingToStarboardIcon);
            
            Marker markPassingToPortIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=M|FF0000|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.MARK_PASSING, Tack.PORT), markPassingToPortIcon);

            Marker unknownManeuverIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=?|FFFFFF|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.UNKNOWN, Tack.STARBOARD), unknownManeuverIcon);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.UNKNOWN, Tack.PORT), unknownManeuverIcon);

            Marker penaltyCircleToStarboardIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|00FF00|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.PENALTY_CIRCLE, Tack.STARBOARD), penaltyCircleToStarboardIcon);
            
            Marker penaltyCircleToPortIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|FF0000|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Pair<ManeuverType, Tack>(ManeuverType.PENALTY_CIRCLE, Tack.PORT), penaltyCircleToPortIcon);
        }
    }
    
    private Marker createMarker(String iconUrl, int anchorX, int anchorY) {
        MarkerOptions options = MarkerOptions.newInstance();
        MarkerImage markerImage = MarkerImage.newInstance(iconUrl);
        markerImage.setAnchor(Point.newInstance(anchorX, anchorY));
        options.setIcon(markerImage);
        Marker marker = Marker.newInstance(options);
        return marker;
    }
    
    private MarkImageDescriptor createMarkImageDescriptor(ImageResource imgResource, MarkType type, String color, String shape, String pattern,
            int anchorPointX, int anchorPointY) {
        MarkImageDescriptor markIconDescriptor = new MarkImageDescriptor(imgResource, Point.newInstance(anchorPointX, anchorPointY),
                type, color, shape, pattern);
        markImageDescriptors.add(markIconDescriptor);
        
        return markIconDescriptor;
    }
    
    public ImageTransformer getCombinedWindIconTransformer() {
        return combinedWindIconTransformer;
    }

    public ImageTransformer getWindSensorIconTransformer() {
        return windSensorIconTransformer;
    }
}
