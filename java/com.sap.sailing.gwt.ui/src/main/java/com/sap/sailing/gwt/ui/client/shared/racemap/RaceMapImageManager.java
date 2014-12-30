package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
     * Loads the map overlay icons The method can only be called after the map is loaded. The {@link #maneuverIconsForTypeAndTargetTack} map
     * is filled for all combinations of {@link ManeuverType maneuver types} and {@link Tack tacks} including the value <code>null</code> representing
     * the unknown tack.
     */
    public void loadMapIcons(MapWidget map) {
        if (map != null) {
            final Map<ManeuverType, Character> maneuverLetter = new HashMap<>();
            maneuverLetter.put(ManeuverType.TACK, 'T');
            maneuverLetter.put(ManeuverType.JIBE, 'J');
            maneuverLetter.put(ManeuverType.HEAD_UP, 'H');
            maneuverLetter.put(ManeuverType.BEAR_AWAY, 'B');
            maneuverLetter.put(ManeuverType.MARK_PASSING, 'M');
            maneuverLetter.put(ManeuverType.PENALTY_CIRCLE, 'P');
            maneuverLetter.put(ManeuverType.UNKNOWN, '?');
            final Map<Tack, String> tackColor = new HashMap<>();
            tackColor.put(Tack.STARBOARD, "71bf44");
            tackColor.put(Tack.PORT, "d95252");
            tackColor.put(null, "FFFFFF");
            for (Entry<ManeuverType, Character> m : maneuverLetter.entrySet()) {
                for (Entry<Tack, String> t : tackColor.entrySet()) {
                    final Marker icon = createMarker("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld="+m.getValue()+"|"+t.getValue()+"|000000", 10, 33);
                    maneuverIconsForTypeAndTargetTack.put(new Util.Pair<ManeuverType, Tack>(m.getKey(), t.getKey()), icon);
                }
            }
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
