package com.sap.sailing.geocoding.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.PlacemarkImpl;
import com.sap.sailing.domain.common.quadtree.QuadTree;
import com.sap.sailing.geocoding.ReverseGeocoder;
import com.sap.sse.common.Util;

public class ReverseGeocoderImpl implements ReverseGeocoder {

    private final String NEARBY_PLACE_SERVICE = "http://api.geonames.org/findNearbyPlaceNameJSON?";
    /**
     * Maximal distance in degree for the cache.<br />
     * The first number is the distance in kilometers and the second number is a needed calculation factor and mustn't
     * be changed! To change the distance just change the first number!
     */
    private final double POSITION_CACHE_DISTANCE_LIMIT = ReverseGeocoder.POSITION_CACHE_DISTANCE_LIMIT_IN_KM * 0.00899928005759539236861051115911;
    private final int XKM_RADIUS = 5;
    private final int ROWS_PER_XKM_RADIUS = 15;
    
    private final int MAX_ROW_NUMBER = 1000;
    private final int MAX_RADIUS = 300;

    private QuadTree<Util.Triple<Position, Double, List<Placemark>>> cache = new QuadTree<Util.Triple<Position,Double,List<Placemark>>>();;

    @Override
    public Placemark getPlacemarkNearest(Position position) throws IOException, ParseException {
        Placemark p = null;
        Util.Triple<Position, Double, List<Placemark>> cachedPlacemarks = checkCache(position);

        if (cachedPlacemarks != null && cachedPlacemarks.getC() != null && !cachedPlacemarks.getC().isEmpty()) {
            p = cachedPlacemarks.getC().get(0);
        } else {
            JSONArray geonames = callNearestService(position);
            if (geonames != null && !geonames.isEmpty()) {
                p = JSONToPlacemark((JSONObject) geonames.get(0));

                if (p != null) {
                    List<Placemark> placemarks = new ArrayList<Placemark>();
                    placemarks.add(p);
                    cachePlacemarks(position, 0.0, placemarks);
                }
            }
        }

        return p;
    }

    @Override
    public List<Placemark> getPlacemarksNear(Position position, double radius) throws IOException, ParseException {
        List<Placemark> placemarks = null;
        Util.Triple<Position, Double, List<Placemark>> cachedPlacemarks = checkCache(position);
        
        //Calculating the search radius and the maximum number of returning Placemarks
        double limitedRadius = Math.min(radius, MAX_RADIUS);
        int radiusInt = (int) limitedRadius;
        int xKmRadius = radiusInt / XKM_RADIUS;
        int maxRows = (int) (ROWS_PER_XKM_RADIUS * Math.pow(2, xKmRadius));
        maxRows = Math.min(maxRows, MAX_ROW_NUMBER);

        if (cachedPlacemarks != null && cachedPlacemarks.getB() >= limitedRadius) {
            if (cachedPlacemarks.getC().size() > maxRows) {
                placemarks = cachedPlacemarks.getC().subList(0, maxRows);
            } else {
                placemarks = new ArrayList<Placemark>(cachedPlacemarks.getC());
            }
        } else {
            //Recalculating the radius and resetting the search position to keep the cache correct
            Position searchPosition = null;
            if (cachedPlacemarks != null) {
                searchPosition = cachedPlacemarks.getA();
                double distance = position.getDistance(searchPosition).getKilometers();
                limitedRadius = Math.min(MAX_RADIUS, limitedRadius + distance); 
            } else {
                searchPosition = position;
            }
            
            JSONArray geonames = callNearbyService(searchPosition, limitedRadius, maxRows);
            if (geonames != null) {
                Iterator<Object> iterator = geonames.iterator();
                placemarks = iterator.hasNext() ? new ArrayList<Placemark>() : null;
                while (iterator.hasNext()) {
                    JSONObject object = (JSONObject) iterator.next();
                    Placemark place = JSONToPlacemark(object);
                    if (place != null) {
                        placemarks.add(JSONToPlacemark(object));
                    }
                }
            }
            // If there are no cached placemarks for the requested Position just cache them, otherwise update the cache
            if (cachedPlacemarks == null && placemarks != null) {
                cachePlacemarks(searchPosition, limitedRadius, placemarks);
            } else if (placemarks != null) {
                updateCachedPlacemarks(searchPosition, limitedRadius, placemarks);
            }
        }

        return placemarks;
    }

    @Override
    public Placemark getPlacemarkLast(Position position, double radius, Comparator<Placemark> comp) throws IOException,
            ParseException {
        List<Placemark> placemarks = getPlacemarksNearSorted(position, radius, comp);
        return placemarks == null ? null : placemarks.get(placemarks.size() - 1);
    }

    @Override
    public Placemark getPlacemarkFirst(Position position, double radius, Comparator<Placemark> comp)
            throws IOException, ParseException {
        List<Placemark> placemarks = getPlacemarksNearSorted(position, radius, comp);
        return placemarks == null ? null : placemarks.get(0);
    }

    private List<Placemark> getPlacemarksNearSorted(Position position, double radius, Comparator<Placemark> comp)
            throws IOException, ParseException {
        List<Placemark> placemarks = getPlacemarksNear(position, radius);
        if (placemarks != null) {
            Collections.sort(placemarks, comp);
        }
        return placemarks;
    }

    /**
     * Returns a {@link Placemark} for a compatible JSONObject.
     * 
     * @param json
     *            The object to be converted
     * @return A {@link Placemark} or <code>null</code>, if the object doesn't contain a name, a postion or the
     *         if the population is 0
     */
    private Placemark JSONToPlacemark(JSONObject json) {
        String name = (String) json.get("toponymName");
        String countryCode = (String) json.get("countryCode");

        // Tries are necessary, because some latitude or longitude values delivered by Geonames have no decimal places
        // and are interpreted as Long
        // Casting a Long to a Double raises a ClassCastException
        Double latDeg = null;
        Object jsonLat = json.get("lat");
        if (jsonLat instanceof String) {
            latDeg = Double.valueOf((String) jsonLat);
        } else if (jsonLat instanceof Number) {
            latDeg = ((Number) jsonLat).doubleValue();
        }
        Double lngDeg = null;
        Object jsonLng = json.get("lng");
        if (jsonLng instanceof String) {
            lngDeg = Double.valueOf((String) jsonLng);
        } else if (jsonLng instanceof Number) {
            lngDeg = ((Number) jsonLng).doubleValue();
        }
        Position position = new DegreePosition(latDeg, lngDeg);
        long population = (Long) json.get("population");
        if (name != null && lngDeg != null && latDeg != null) {
            return new PlacemarkImpl(name, countryCode, position, population);
        } else {
            return null;
        }
    }

    /**
     * Caches the <code>position</code>, the <code>radius</code> and the <code>placemarks</code> at the Position
     * <code>p</code> in the cache.
     * 
     * @param position
     *            The position in the cache and the point of the search
     * @param radius
     *            The radius of the search
     * @param placemarks
     *            The results of the search
     */
    private void cachePlacemarks(Position position, Double radius, List<Placemark> placemarks) {
        Collections.sort(placemarks, new Placemark.ByDistance(position));
        if (position != null) {
            synchronized (cache) {
                cache.put(position, new Util.Triple<Position, Double, List<Placemark>>(position, radius, placemarks));
            }
        }
    }
    
    /**
     * Replaces the data at <code>cachedPoint</code> with the <code>newRadius</code> and <code>newPlacemarks</code>.
     * 
     * @param cachedPoint
     *            The position of the data which should be replaced. Has to be a Position which was cached before.<br />
     *            The parameter is as best a value from a Triple in the cache, like <code>cachedData.getA()</code>.
     * @param newRadius
     *            The new radius of the cached search results
     * @param newPlacemarks
     *            The new search results
     */
    private void updateCachedPlacemarks(Position cachedPoint, Double newRadius, List<Placemark> newPlacemarks) {
        if (cachedPoint != null) {
            synchronized (cache) {
                cache.put(cachedPoint, new Util.Triple<Position, Double, List<Placemark>>(cachedPoint, newRadius, newPlacemarks));
            }
        }
    }

    /**
     * @param position
     *            The position to be checked by the cache
     * @return The cached placemarks (with meta-data) sorted by distance towards <code>position</code> or
     *         <code>null</code>, if there's nothing cached around <code>position</code> within
     *         {@link ReverseGeocoderImpl#POSITION_CACHE_DISTANCE_LIMIT the distance limit}
     */
    private Util.Triple<Position, Double, List<Placemark>> checkCache(Position position) {
        synchronized (cache) {
            return cache.get(position, POSITION_CACHE_DISTANCE_LIMIT);
        }
    }

    private JSONArray callNearestService(Position position) throws MalformedURLException, IOException, ParseException {
        StringBuilder url = new StringBuilder(NEARBY_PLACE_SERVICE);
        url.append("&lat=" + Double.toString(position.getLatDeg()));
        url.append("&lng=" + Double.toString(position.getLngDeg()));
        url.append("&username=" + getGeonamesUser());

        URL request = new URL(url.toString());
        URLConnection connection = request.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(in);
        JSONArray geonames = (JSONArray) obj.get("geonames");
        return geonames;
    }

    private JSONArray callNearbyService(Position position, double radius, int maxRows) throws MalformedURLException,
            IOException, ParseException {
        StringBuilder url = new StringBuilder(NEARBY_PLACE_SERVICE);
        url.append("&lat=" + Double.toString(position.getLatDeg()));
        url.append("&lng=" + Double.toString(position.getLngDeg()));
        url.append("&radius=" + Double.toString(radius));
        url.append("&maxRows=" + Integer.toString(maxRows));
        url.append("&username=" + getGeonamesUser());

        URL request = new URL(url.toString());
        URLConnection connection = request.openConnection();
        connection.setRequestProperty("User-Agent", "");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(in);
        JSONArray geonames = (JSONArray) obj.get("geonames");
        return geonames;
    }

    final String GEONAMES_USER = "sailtracking";
    private String getGeonamesUser() {
        return GEONAMES_USER+new Random().nextInt(10);
    }

}
