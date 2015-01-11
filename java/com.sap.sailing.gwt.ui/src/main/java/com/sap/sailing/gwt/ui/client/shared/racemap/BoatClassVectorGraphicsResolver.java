package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.BoatClassMasterdata;
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
    private static Map<BoatClassMasterdata, BoatClassVectorGraphics> compatibleBoatVectorGraphicsMap;
    private static BoatClassVectorGraphics defaultBoatVectorGraphics;
	
    static {
    	compatibleBoatVectorGraphicsMap = new HashMap<>();
    	
    	BoatClassVectorGraphics laser = new LaserVectorGraphics(BoatClassMasterdata.LASER_INT, BoatClassMasterdata.LASER_RADIAL);
    	BoatClassVectorGraphics _49er = new _49erVectorGraphics(BoatClassMasterdata._49ER, BoatClassMasterdata._49ERFX, BoatClassMasterdata._29ER);
    	BoatClassVectorGraphics extreme40 = new Extreme40VectorGraphics(BoatClassMasterdata.EXTREME_40, BoatClassMasterdata.D_35);
        BoatClassVectorGraphics smallMultihull = new SmallMultihullVectorGraphics(BoatClassMasterdata.NACRA_17,
                BoatClassMasterdata.F_16, BoatClassMasterdata.F_18, BoatClassMasterdata.HOBIE_WILD_CAT,
                BoatClassMasterdata.HOBIE_TIGER, BoatClassMasterdata.A_CAT, BoatClassMasterdata.TORNADO);

        defaultBoatVectorGraphics = laser; // TODO see bug 2571; this should be a slup-rigged icon working for 470, 505, J/70 etc.
        for (BoatClassVectorGraphics g : new BoatClassVectorGraphics[] { laser, _49er, extreme40, smallMultihull }) {
            for (BoatClassMasterdata b : g.getCompatibleBoatClasses()) {
                compatibleBoatVectorGraphicsMap.put(b, g);
            }
        }
    }
	
    public static BoatClassVectorGraphics resolveBoatClassVectorGraphics(String boatClassName) {
        BoatClassMasterdata resolvedBoatClass = BoatClassMasterdata.resolveBoatClass(boatClassName);
        final BoatClassVectorGraphics result;
        if (resolvedBoatClass != null && compatibleBoatVectorGraphicsMap.containsKey(boatClassName)) {
            result = compatibleBoatVectorGraphicsMap.get(boatClassName);
        } else {
            result = defaultBoatVectorGraphics;
        }
        return result;
    }
}
