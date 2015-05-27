package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.RowingBoatClassMasterdata;
import com.sap.sailing.gwt.ui.shared.racemap.RowingBoatClassVectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.RowingBoatVectorGraphics;

/**
 * A resolver utility for finding a suitable vector graphics for a given rowing boat class
 * @author Frank Mittag (C5163874)
 */
public class RowingBoatClassVectorGraphicsResolver {
    private static Map<RowingBoatClassMasterdata, RowingBoatClassVectorGraphics> compatibleBoatVectorGraphicsMap;
    private static RowingBoatVectorGraphics defaultRowingBoatVectorGraphics;
	
    static {
    	compatibleBoatVectorGraphicsMap = new HashMap<>();
    	
        RowingBoatVectorGraphics rowingBoat = new RowingBoatVectorGraphics(RowingBoatClassMasterdata.ROWING_BOAT);

        defaultRowingBoatVectorGraphics = rowingBoat;
        for (RowingBoatClassVectorGraphics g : new RowingBoatClassVectorGraphics[] {rowingBoat}) {
            for (RowingBoatClassMasterdata b : g.getCompatibleRowingBoatClasses()) {
                compatibleBoatVectorGraphicsMap.put(b, g);
            }
        }
    }
	
    public static RowingBoatClassVectorGraphics resolveRowingBoatClassVectorGraphics(String rowingBoatClassName) {
        RowingBoatClassMasterdata resolvedBoatClass = RowingBoatClassMasterdata.resolveBoatClass(rowingBoatClassName);
        final RowingBoatClassVectorGraphics result;
        if (resolvedBoatClass != null && compatibleBoatVectorGraphicsMap.containsKey(resolvedBoatClass)) {
            result = compatibleBoatVectorGraphicsMap.get(resolvedBoatClass);
        } else {
            result = defaultRowingBoatVectorGraphics;
        }
        return result;
    }
}
