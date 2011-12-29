package com.sap.sailing.gwt.ui.client;

import java.util.HashMap;

/**
 * Manages color assignments to objects.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T> the type of the objects to which a color is assigned
 */
public class ColorMap<T> {
    private final HashMap<T, String> idColor;
    
    private int colorCounter;
    
    public ColorMap() {
        idColor = new HashMap<T, String>();
    }

    /**
     * Returns a color that is computed once by using {@link ChartsPanel#createHexColor(int)} and then cached.
     * 
     * @param object
     *            An ID unique for a competitor.
     * @return A color in hex/html-format (e.g. #ff0000)
     */
    public String getColorByID(T object){
        String color = idColor.get(object);
        if (color == null || color.isEmpty()){
                color = createHexColor(colorCounter++);
                idColor.put(object, color);
        }
        return color;
    }
    
    /**
     * Only use this if you don't want the color to be cached. You can use {@link ChartsPanel#getColorByID(int)}
     * instead.
     * 
     * @param index
     *            The index of e.g. a competitor. Make sure, that each competitor has a unique index.
     * @return A color computed using the {@code index}.
     */
    private String createHexColor(int index){
        String rs, gs, bs;
        int r = 0, g = 0, b = 0;
        double factor = 1 - ((index/6)/6.0);
        if (index%6 < 2 || index%6 > 4){
            r = (int) (255*factor);
        }
        rs = Integer.toHexString(r);
        while(rs.length() < 2){
            rs = "0" + rs;
        }
        if (index%6 > 0 && index%6 < 4){
            g = (int) (220*factor);
        }
        gs = Integer.toHexString(g);
        while(gs.length() < 2){
            gs = "0" + gs;
        }
        if (index%6 > 2){
            b = (int) (255*factor);
        }
        bs = Integer.toHexString(b);
        while(bs.length() < 2){
            bs = "0" + bs;
        }
        return "#" + rs + gs + bs;
    }
    
}
