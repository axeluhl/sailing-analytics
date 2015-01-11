package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.gwt.ui.shared.racemap.BoatClassVectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.Extreme40VectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.LaserVectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.SmallMultihullVectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap._49erVectorGraphics;

/**
 * A resolver utility for finding a suitable vector graphics for a given boat class
 * @author Frank Mittag (C5163874)
 */
public class BoatClassVectorGraphicsResolver {
    private static Map<String, BoatClassVectorGraphics> boatVectorGraphicsMap;
    private static Map<String, BoatClassVectorGraphics> compatibleBoatVectorGraphicsMap;
    private static BoatClassVectorGraphics defaultBoatVectorGraphics;
	
    static {
    	boatVectorGraphicsMap = new HashMap<String, BoatClassVectorGraphics>();
    	compatibleBoatVectorGraphicsMap = new HashMap<String, BoatClassVectorGraphics>();
    	
    	BoatClassVectorGraphics laser = new LaserVectorGraphics("470", "470er");
    	BoatClassVectorGraphics _49er = new _49erVectorGraphics("49erFX", "49FX", "29er");
    	BoatClassVectorGraphics extreme40 = new Extreme40VectorGraphics("Extreme", "D35");
        BoatClassVectorGraphics smallMultihull = new SmallMultihullVectorGraphics("Nacra 17", "Formula 16", "Formula 18", 
                "Hobie Wild Cat", "Hobie Tiger", "A-Catamaran", "Tornado");
    	
    	defaultBoatVectorGraphics = laser;
    	boatVectorGraphicsMap.put(laser.getMainBoatClassName(), laser);
    	boatVectorGraphicsMap.put(_49er.getMainBoatClassName(), _49er);
    	boatVectorGraphicsMap.put(extreme40.getMainBoatClassName(), extreme40);
        boatVectorGraphicsMap.put(smallMultihull.getMainBoatClassName(), smallMultihull);
    }
	
    public static BoatClassVectorGraphics resolveBoatClassVectorGraphics(String boatClassName) {
        BoatClassVectorGraphics result = defaultBoatVectorGraphics;
        if (boatVectorGraphicsMap.containsKey(boatClassName)) {
            result = boatVectorGraphicsMap.get(boatClassName);
        } else if (compatibleBoatVectorGraphicsMap.containsKey(boatClassName)) {
            result = compatibleBoatVectorGraphicsMap.get(boatClassName);
        } else {
            // now try to find compatible boat class images
            for (BoatClassVectorGraphics boatClassImageData : boatVectorGraphicsMap.values()) {
                if (boatClassImageData.isBoatClassNameCompatible(boatClassName)) {
                    result = boatClassImageData;
                    compatibleBoatVectorGraphicsMap.put(boatClassName, boatClassImageData);
                    break;
                }
            }
        }
        return result;
    }
}
