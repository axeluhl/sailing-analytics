package com.sap.sailing.geocoding;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.geocoding.impl.ReverseGeocoderImpl;

public interface ReverseGeocoder {
    final ReverseGeocoder INSTANCE = new ReverseGeocoderImpl();
    final double POSITION_CACHE_DISTANCE_LIMIT_IN_KM = 5.0;

    /**
     * Returns the nearest {@link Placemark} towards the given {@link Position}.
     * 
     * @param position The position where to search
     * @return The nearest {@link Placemark} towards the given {@link Position} or null if there is none within 300km
     * @throws IOException
     * @throws ParseException
     */
    Placemark getPlacemarkNearest(Position position) throws IOException, ParseException;

    /**
     * Returns a list of {@link Placemark Placemarks} near the given {@link Position}.
     * 
     * @param position The position where to search
     * @param radius The search radius
     * @return A list of {@link Placemark Placemarks} near the given {@link Position} or null if there are none within 300km
     * @throws IOException
     * @throws ParseException
     */
    List<Placemark> getPlacemarksNear(Position position, double radius) throws IOException, ParseException;

    /**
     * Searches for {@link Placemark Placemarks} near the given {@link Position} via
     * {@link ReverseGeocoder#getPlacemarkNearest(double, double) getPlacemarkNearest} and sorts them with
     * <code>comp</code>. Returns the last element in the sorted list.<br /><br />
     * The interface {@link Placemark} contains some Comparators for Placemarks.
     * 
     * @param position The position where to search
     * @param radius The search radius
     * @param comp Sorts the list
     * @return The last element in the sorted list
     * @throws IOException
     * @throws ParseException
     */
    Placemark getPlacemarkLast(Position position, double radius, Comparator<Placemark> comp) throws IOException,
            ParseException;

    /**
     * Searches for {@link Placemark Placemarks} near the given {@link Position} via
     * {@link ReverseGeocoder#getPlacemarkNearest(double, double) getPlacemarkNearest} and sorts them with
     * <code>comp</code>. Returns the first element in the sorted list.<br /><br />
     * The interface {@link Placemark} contains some Comparators for Placemarks.
     * 
     * @param position The position where to search      
     * @param radius The search radius                   
     * @param comp Sorts the list                        
     * @return The first element in the sorted list
     * @throws IOException
     * @throws ParseException
     */
    Placemark getPlacemarkFirst(Position position, double radius, Comparator<Placemark> comp) throws IOException,
            ParseException;
}
