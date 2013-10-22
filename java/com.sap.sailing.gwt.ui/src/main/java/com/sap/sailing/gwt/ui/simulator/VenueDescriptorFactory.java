package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;

import com.google.gwt.maps.client.base.LatLng;
import com.sap.sailing.simulator.util.SailingSimulatorConstants;

public class VenueDescriptorFactory {
    public static VenueDescriptor createVenue(char event) {
        VenueDescriptor result = null;
        switch (event) {
            case SailingSimulatorConstants.EventKielerWoche: 
                result = createKielVenue();
                break;
            case SailingSimulatorConstants.EventTravemuenderWoche:
                result = createTravemuendeVenue();
                break;
        }
        return result;
    }
    
    private static VenueDescriptor createKielVenue() {
        LatLng cPos;
        ArrayList<CourseAreaDescriptor> courseAreas = new ArrayList<CourseAreaDescriptor>();

        VenueDescriptor venue = new VenueDescriptor("Kieler Bucht", courseAreas);
        // Middle of Echo and Klio
        venue.setCenterPos(LatLng.newInstance(54.477245795, 10.220622225));
        
        // TV
        cPos = LatLng.newInstance(54.43450, 10.19559167);
        CourseAreaDescriptor defaultCourseArea = new CourseAreaDescriptor("TV", cPos, 0.5, "#EAB75A");
        courseAreas.add(defaultCourseArea);

        // Golf
        cPos = LatLng.newInstance(54.41985556, 10.19454167);
        courseAreas.add(new CourseAreaDescriptor("Golf", cPos, 0.35, "#F1F3EF", "silver"));

        // Foxtrot
        cPos = LatLng.newInstance(54.445775, 10.29223889);
        courseAreas.add(new CourseAreaDescriptor("Foxtrot", cPos, 0.65, "#B4287C"));

        // India
        cPos = LatLng.newInstance(54.44803611, 10.20863611);
        courseAreas.add(new CourseAreaDescriptor("India", cPos, 0.40, "#774741"));

        // Juliett
        cPos = LatLng.newInstance(54.46183611, 10.2239);
        courseAreas.add(new CourseAreaDescriptor("Juliett", cPos, 0.55, "#818585"));

        // Echo
        cPos = LatLng.newInstance(54.47640278, 10.20090556);
        courseAreas.add(new CourseAreaDescriptor("Echo", cPos, 0.60, "#1CADD9"));

        // Kilo
        cPos = LatLng.newInstance(54.47808889, 10.24033889);
        courseAreas.add(new CourseAreaDescriptor("Kilo", cPos, 0.55, "#9FC269"));

        // Charlie
        cPos = LatLng.newInstance(54.49327222, 10.17525833);
        courseAreas.add(new CourseAreaDescriptor("Charlie", cPos, 0.70, "#0A5998"));

        // Delta
        cPos = LatLng.newInstance(54.49706111, 10.21921944);
        courseAreas.add(new CourseAreaDescriptor("Delta", cPos, 0.75, "#179E8B"));

        // Bravo
        cPos = LatLng.newInstance(54.50911667, 10.13973333);
        courseAreas.add(new CourseAreaDescriptor("Bravo", cPos, 0.80, "#CE3032"));

        // Alfa
        cPos = LatLng.newInstance(54.52905, 10.18515278);
        courseAreas.add(new CourseAreaDescriptor("Alfa", cPos, 1.00, "#D9699B"));
        
        venue.setDefaultCourseArea(defaultCourseArea);
        return venue;
    }

    private static VenueDescriptor createTravemuendeVenue() {
        LatLng cPos;
        ArrayList<CourseAreaDescriptor> courseAreas = new ArrayList<CourseAreaDescriptor>();

        VenueDescriptor venue = new VenueDescriptor("Kieler Bucht", courseAreas);
        venue.setCenterPos(LatLng.newInstance(54.01583, 10.92583));

        // Alfa
        cPos = LatLng.newInstance(54.015, 10.835);
        CourseAreaDescriptor defaultCourseArea = new CourseAreaDescriptor("Alfa", cPos, 0.9, "#FF8030");
        courseAreas.add(defaultCourseArea);

        // Bravo
        cPos = LatLng.newInstance(54.01666667, 11.01666667);
        courseAreas.add(new CourseAreaDescriptor("Bravo", cPos, 0.75, "#179E8B"));

        // Charlie
        cPos = LatLng.newInstance(54.0025, 10.98333333);
        courseAreas.add(new CourseAreaDescriptor("Charlie", cPos, 0.6, "#CE3032"));

        // Delta
        cPos = LatLng.newInstance(54.02166667, 10.92083333);
        courseAreas.add(new CourseAreaDescriptor("Delta", cPos, 0.75, "#B4287C"));

        // Foxtrot
        cPos = LatLng.newInstance(54.00833333, 10.88333333);
        courseAreas.add(new CourseAreaDescriptor("Foxtrot", cPos, 0.75, "#FFFFFF", "silver"));

        // Golf
        cPos = LatLng.newInstance(53.98333333, 10.9);
        courseAreas.add(new CourseAreaDescriptor("Golf", cPos, 0.50, "#1CADD9"));

        // Hotel
        cPos = LatLng.newInstance(53.99, 10.95666667);
        courseAreas.add(new CourseAreaDescriptor("Hotel", cPos, 0.50, "#FFFF30", "silver"));

        // See
        cPos = LatLng.newInstance(54.04333333, 10.875);
        courseAreas.add(new CourseAreaDescriptor("See", cPos, 1.25, "#818585"));
      
        venue.setDefaultCourseArea(defaultCourseArea);
        return venue;
    }
}