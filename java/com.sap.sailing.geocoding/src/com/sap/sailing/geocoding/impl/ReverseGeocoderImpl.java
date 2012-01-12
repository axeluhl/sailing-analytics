package com.sap.sailing.geocoding.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.PlacemarkImpl;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.quadtree.QuadTree;
import com.sap.sailing.geocoding.ReverseGeocoder;

public class ReverseGeocoderImpl implements ReverseGeocoder {

    private final String NEARBY_PLACE_SERVICE = "http://api.geonames.org/findNearbyPlaceNameJSON?";
    private final double POSITION_CACHE_DISTANCE_LIMIT = 10.0;
    private final int XKM_RADIUS = 5;
    private final int ROWS_PER_XKM_RADIUS = 20;

    private QuadTree<Triple<Position, Double, List<Placemark>>> cache = new QuadTree<Triple<Position,Double,List<Placemark>>>();;

    @Override
    public Placemark getPlacemarkNearest(Position position) throws IOException, ParseException {
        Placemark p = null;
        Triple<Position, Double, List<Placemark>> cachedPlacemarks = checkCache(position);

        if (cachedPlacemarks != null) {
            p = cachedPlacemarks.getC().get(0);
        } else {
            StringBuilder url = new StringBuilder(NEARBY_PLACE_SERVICE);
            url.append("&lat=" + Double.toString(position.getLatDeg()));
            url.append("&lng=" + Double.toString(position.getLngDeg()));
            url.append("&username=" + GEONAMES_USER);

            URL request = new URL(url.toString());
            URLConnection connection = request.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(in);
            JSONArray geonames = (JSONArray) obj.get("geonames");
            p = JSONToPlacemark((JSONObject) geonames.get(0));

            List<Placemark> placemarks = new ArrayList<Placemark>();
            placemarks.add(p);
            cachePlacemarks(position, 0.0, placemarks);
        }

        return p;
    }

    @Override
    public List<Placemark> getPlacemarksNear(Position position, double radius) throws IOException, ParseException {
        List<Placemark> placemarks = null;
        Triple<Position, Double, List<Placemark>> cachedPlacemarks = checkCache(position);
        int radiusInt = (int) radius;
        int xKmRadius = radiusInt / XKM_RADIUS;
        int maxRows = (int) (ROWS_PER_XKM_RADIUS * Math.pow(2, xKmRadius));

        if (cachedPlacemarks != null && cachedPlacemarks.getB() >= radius) {
            placemarks = cachedPlacemarks.getC().subList(0, maxRows);
        } else {
            placemarks = new ArrayList<Placemark>();
            StringBuilder url = new StringBuilder(NEARBY_PLACE_SERVICE);
            url.append("&lat=" + Double.toString(position.getLatDeg()));
            url.append("&lng=" + Double.toString(position.getLngDeg()));
            url.append("&radius=" + Double.toString(radius));
            url.append("&maxRows=" + Integer.toString(maxRows));
            url.append("&username=" + GEONAMES_USER);

            URL request = new URL(url.toString());
            URLConnection connection = request.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(in);
            JSONArray geonames = (JSONArray) obj.get("geonames");
            Iterator<Object> iterator = geonames.iterator();
            while (iterator.hasNext()) {
                JSONObject object = (JSONObject) iterator.next();
                placemarks.add(JSONToPlacemark(object));
            }

            // If there are no cached placemarks for the requested Position just cache them, otherwise update the cache
            if (cachedPlacemarks == null) {
                cachePlacemarks(position, radius, placemarks);
            } else {
                if (maxRows < placemarks.size() - 1) {
                    updateCachedPlacemarks(position, radius, placemarks.subList(maxRows, placemarks.size() - 1));
                }
            }
        }

        return placemarks;
    }

    @Override
    public Placemark getPlacemarkLast(Position position, double radius, Comparator<Placemark> comp) throws IOException,
            ParseException {
        List<Placemark> placemarks = getPlacemarksNearSorted(position, radius, comp);
        return placemarks.get(placemarks.size() - 1);
    }

    @Override
    public Placemark getPlacemarkFirst(Position position, double radius, Comparator<Placemark> comp)
            throws IOException, ParseException {
        return getPlacemarksNearSorted(position, radius, comp).get(0);
    }

    private List<Placemark> getPlacemarksNearSorted(Position position, double radius, Comparator<Placemark> comp)
            throws IOException, ParseException {
        List<Placemark> placemarks = getPlacemarksNear(position, radius);
        Collections.sort(placemarks, comp);

        return placemarks;
    }

    private Placemark JSONToPlacemark(JSONObject json) {
        String name = (String) json.get("toponymName");
        String countryCode = (String) json.get("countryCode");

        // Tries are necessary, because some latitude or longitude values delivered by Geonames have no decimal places
        // and are interpreted as Long
        // Casting a Long to a Double raises a ClassCastException
        Double latDeg = null;
        try {
            latDeg = (Double) json.get("lat");
        } catch (ClassCastException e) {
            latDeg = ((Long) json.get("lat")).doubleValue();
        }
        Double lngDeg = null;
        try {
            lngDeg = (Double) json.get("lng");
        } catch (ClassCastException e) {
            lngDeg = ((Long) json.get("lng")).doubleValue();
        }
        Position p = new DegreePosition(latDeg, lngDeg);

        long population = (Long) json.get("population");

        if (name != null && lngDeg != null && latDeg != null) {
            return new PlacemarkImpl(name, countryCode, p, population);
        } else {
            return null;
        }
    }

    private void cachePlacemarks(Position p, Double radius, List<Placemark> placemarks) {
        Collections.sort(placemarks, new Placemark.ByDistance(p));
        cache.put(p, new Triple<Position, Double, List<Placemark>>(p, radius, placemarks));
    }
    
    private void updateCachedPlacemarks(Position p, Double newRadius, List<Placemark> newPlacemarks) {
        Triple<Position, Double, List<Placemark>> cachedPlacemarks = checkCache(p);
        if (cachedPlacemarks != null) {
            cachedPlacemarks.setB(newRadius);
            cachedPlacemarks.getC().addAll(newPlacemarks);
            Collections.sort(cachedPlacemarks.getC(), new Placemark.ByDistance(cachedPlacemarks.getA()));
        }
    }

    private Triple<Position, Double, List<Placemark>> checkCache(Position p) {
        return cache.get(p, POSITION_CACHE_DISTANCE_LIMIT);
    }

}
