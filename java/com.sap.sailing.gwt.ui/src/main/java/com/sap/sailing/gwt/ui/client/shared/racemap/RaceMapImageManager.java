package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerImage;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sse.common.Util;

public class RaceMapImageManager {

    /**
     * An arrow showing the combined wind depending on the wind direction 
     */
    protected ImageTransformer combinedWindIconTransformer;

    /**
     * An arrow showing the wind provided by a wind sensor on a boat 
     */
    protected ImageTransformer windSensorIconTransformer;

    protected Map<Util.Pair<ManeuverType, Tack>, Marker> maneuverIconsForTypeAndTargetTack;

    private static RaceMapResources resources = GWT.create(RaceMapResources.class);

    public RaceMapImageManager() {
        maneuverIconsForTypeAndTargetTack = new HashMap<Util.Pair<ManeuverType, Tack>, Marker>();
        
        combinedWindIconTransformer = new ImageTransformer(resources.combinedWindIcon());
        windSensorIconTransformer = new ImageTransformer(resources.expeditionWindIcon());
    }

    /**
     * Loads the map overlay icons
     * The method can only be called after the map is loaded  
     */
    public void loadMapIcons(MapWidget map) {
        if(map != null) {
            Marker tackToStarboardIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=T|71bf44|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.TACK, Tack.STARBOARD), tackToStarboardIcon);

            Marker tackToPortIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=T|d95252|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.TACK, Tack.PORT), tackToPortIcon);

            Marker jibeToStarboardIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=J|71bf44|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.JIBE, Tack.STARBOARD), jibeToStarboardIcon);

            Marker jibeToPortIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=J|d95252|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.JIBE, Tack.PORT), jibeToPortIcon);

            Marker headUpOnStarboardIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=H|71bf44|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.HEAD_UP, Tack.STARBOARD), headUpOnStarboardIcon);

            Marker headUpOnPortIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=H|d95252|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.HEAD_UP, Tack.PORT), headUpOnPortIcon);

            Marker bearAwayOnStarboardIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=B|71bf44|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.BEAR_AWAY, Tack.STARBOARD), bearAwayOnStarboardIcon);
          
            Marker bearAwayOnPortIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=B|d95252|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.BEAR_AWAY, Tack.PORT), bearAwayOnPortIcon);

            Marker markPassingToStarboardIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=M|71bf44|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.MARK_PASSING, Tack.STARBOARD), markPassingToStarboardIcon);
            
            Marker markPassingToPortIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=M|d95252|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.MARK_PASSING, Tack.PORT), markPassingToPortIcon);

            Marker unknownManeuverIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=?|FFFFFF|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.UNKNOWN, Tack.STARBOARD), unknownManeuverIcon);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.UNKNOWN, Tack.PORT), unknownManeuverIcon);

            Marker penaltyCircleToStarboardIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|71bf44|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.PENALTY_CIRCLE, Tack.STARBOARD), penaltyCircleToStarboardIcon);
            
            Marker penaltyCircleToPortIcon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|d95252|000000", 10, 33);
            maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(ManeuverType.PENALTY_CIRCLE, Tack.PORT), penaltyCircleToPortIcon);
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
    
    public ImageTransformer getCombinedWindIconTransformer() {
        return combinedWindIconTransformer;
    }

    public ImageTransformer getWindSensorIconTransformer() {
        return windSensorIconTransformer;
    }
}
