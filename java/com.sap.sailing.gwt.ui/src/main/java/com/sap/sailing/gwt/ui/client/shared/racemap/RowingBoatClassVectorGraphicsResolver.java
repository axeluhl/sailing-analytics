package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.RowingBoatClassMasterdata;
import com.sap.sailing.gwt.ui.shared.racemap.RowingBoatClassVectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.RowingBoatVectorGraphics;

/**
 * A resolver utility for finding a suitable vector graphics for a given boat class
 * @author Frank Mittag (C5163874)
 */
public class RowingBoatClassVectorGraphicsResolver {
    private static Map<RowingBoatClassMasterdata, RowingBoatClassVectorGraphics> compatibleBoatVectorGraphicsMap;
    private static RowingBoatVectorGraphics defaultBoatVectorGraphics;
	
    static {
    	compatibleBoatVectorGraphicsMap = new HashMap<>();
    	
        RowingBoatVectorGraphics rowingBoat = new RowingBoatVectorGraphics(RowingBoatClassMasterdata.ROWING_BOAT);

        defaultBoatVectorGraphics = rowingBoat; // TODO see bug 2571; this should be a slup-rigged icon working for 470, 505, J/70 etc.
        for (RowingBoatClassVectorGraphics g : new RowingBoatClassVectorGraphics[] {rowingBoat}) {
            for (RowingBoatClassMasterdata b : g.getCompatibleRowingBoatClasses()) {
                compatibleBoatVectorGraphicsMap.put(b, g);
            }
        }
    }
	
    public static RowingBoatClassVectorGraphics resolveRowingBoatClassVectorGraphics(String boatClassName) {
        RowingBoatClassMasterdata resolvedBoatClass = RowingBoatClassMasterdata.resolveBoatClass(boatClassName);
        final RowingBoatClassVectorGraphics result;
        if (resolvedBoatClass != null && compatibleBoatVectorGraphicsMap.containsKey(resolvedBoatClass)) {
            result = compatibleBoatVectorGraphicsMap.get(resolvedBoatClass);
        } else {
            result = defaultBoatVectorGraphics;
        }
        return result;
    }
}
