package com.sap.sailing.domain.common;

import com.sap.sse.common.Color;

/**
 * Manages color assignments to objects.
 * 
 * @author Axel Uhl (d043530)
 * 
 * @param <T>
 *            the type of the objects to which a color is assigned
 */
public interface ColorMap<T> {
    Color getColorByID(T object);
    
    boolean addBlockedColor(Color color);
    boolean removeBlockedColor(Color color);
    void clearBlockedColors();
}
