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

import com.sap.sailing.domain.common.DegreePosition;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.geocoding.Placemark;
import com.sap.sailing.geocoding.ReverseGeocoder;

public class ReverseGeocoderImpl implements ReverseGeocoder {

    private final String NEARBY_PLACE_SERVICE = "http://api.geonames.org/findNearbyPlaceNameJSON?";
    private int maxRows = 500;

    @Override
    public Placemark getPlacemarkNearest(double latDeg, double lngDeg) throws IOException, ParseException {
        Placemark p = null;

        StringBuilder url = new StringBuilder(NEARBY_PLACE_SERVICE);
        url.append("&lat=" + Double.toString(latDeg));
        url.append("&lng=" + Double.toString(lngDeg));
        url.append("&username=" + GEONAMES_USER);

        URL request = new URL(url.toString());
        URLConnection connection = request.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(in);
        JSONArray geonames = (JSONArray) obj.get("geonames");
        p = JSONToPlacemark((JSONObject) geonames.get(0));

        return p;
    }

    @Override
    public Placemark getPlacemarkNearest(Position position) throws IOException, ParseException {
        return getPlacemarkNearest(position.getLatDeg(), position.getLngDeg());
    }

    @Override
    public List<Placemark> getPlacemarksNear(double latDeg, double lngDeg, float radius) throws IOException,
            ParseException {
        List<Placemark> placemarks = new ArrayList<Placemark>();

        StringBuilder url = new StringBuilder(NEARBY_PLACE_SERVICE);
        url.append("&lat=" + Double.toString(latDeg));
        url.append("&lng=" + Double.toString(lngDeg));
        url.append("&radius=" + Float.toString(radius));
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

        return placemarks;
    }

    @Override
    public List<Placemark> getPlacemarksNear(Position position, float radius) throws IOException, ParseException {
        return getPlacemarksNear(position.getLatDeg(), position.getLngDeg(), radius);
    }

    @Override
    public Placemark getPlacemarkLast(double latDeg, double lngDeg, float radius, Comparator<Placemark> comp)
            throws IOException, ParseException {
        List<Placemark> placemarks = getPlacemarksNearSorted(latDeg, lngDeg, radius, comp);
        return placemarks.get(placemarks.size() - 1);
    }

    @Override
    public Placemark getPlacemarkLast(Position position, float radius, Comparator<Placemark> comp) throws IOException,
            ParseException {
        return getPlacemarkLast(position.getLatDeg(), position.getLngDeg(), radius, comp);
    }

    @Override
    public Placemark getPlacemarkFirst(double latDeg, double lngDeg, float radius, Comparator<Placemark> comp)
            throws IOException, ParseException {
        return getPlacemarksNearSorted(latDeg, lngDeg, radius, comp).get(0);
    }

    @Override
    public Placemark getPlacemarkFirst(Position position, float radius, Comparator<Placemark> comp) throws IOException,
            ParseException {
        return getPlacemarkFirst(position.getLatDeg(), position.getLngDeg(), radius, comp);
    }

    private List<Placemark> getPlacemarksNearSorted(double latDeg, double lngDeg, float radius,
            Comparator<Placemark> comp) throws IOException, ParseException {
        List<Placemark> placemarks = getPlacemarksNear(latDeg, lngDeg, radius);
        Collections.sort(placemarks, comp);

        return placemarks;
    }

    private Placemark JSONToPlacemark(JSONObject json) {
        String name = (String) json.get("toponymName");
        String countryCode = (String) json.get("countryCode");
        String countryName = (String) json.get("countryName");

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
            return new PlacemarkImpl(name, countryCode, countryName, p, population);
        } else {
            return null;
        }
    }

}
