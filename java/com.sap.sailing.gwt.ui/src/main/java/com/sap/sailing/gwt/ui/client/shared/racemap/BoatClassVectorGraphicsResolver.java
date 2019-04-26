package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.ui.shared.racemap.BoatClassVectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.CircleVectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.DinghyWithSpinnakerVectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.Extreme40VectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.GC32VectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.KeelBoatWithGennakerVectorGraphics;
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
    	
        BoatClassVectorGraphics laser = new LaserVectorGraphics(BoatClassMasterdata.LASER_INT,
                BoatClassMasterdata.LASER_RADIAL, BoatClassMasterdata.LASER_4_7, BoatClassMasterdata.LASER_2,
                BoatClassMasterdata.CONTENDER, BoatClassMasterdata.HANSA_303,
                BoatClassMasterdata.FINN, BoatClassMasterdata.MUSTO_SKIFF, BoatClassMasterdata.OPEN_BIC,
                BoatClassMasterdata.OPTIMIST, BoatClassMasterdata.PWA,
                BoatClassMasterdata.RS_AERO, BoatClassMasterdata.RS_X,
                BoatClassMasterdata.SPLASH_BLUE, BoatClassMasterdata.SPLASH_RED, BoatClassMasterdata.SPLASH_GREEN,
                BoatClassMasterdata.STAR, BoatClassMasterdata.TECHNO_293, BoatClassMasterdata.TECHNO_293_PLUS);
        BoatClassVectorGraphics _49er = new _49erVectorGraphics(BoatClassMasterdata._49ER, BoatClassMasterdata._49ERFX,
                BoatClassMasterdata._29ER, BoatClassMasterdata._18Footer);
        BoatClassVectorGraphics extreme40 = new Extreme40VectorGraphics(BoatClassMasterdata.EXTREME_40,
                BoatClassMasterdata.D_35, BoatClassMasterdata.SKUD_18, BoatClassMasterdata.WETA);
        BoatClassVectorGraphics gc32 = new GC32VectorGraphics(BoatClassMasterdata.GC_32, BoatClassMasterdata.M32);
        BoatClassVectorGraphics smallMultihull = new SmallMultihullVectorGraphics(BoatClassMasterdata.NACRA_15, BoatClassMasterdata.NACRA_17,
                BoatClassMasterdata.NACRA_17_FOIL, BoatClassMasterdata.F_16, BoatClassMasterdata.F_18,
                BoatClassMasterdata.HOBIE_WILD_CAT, BoatClassMasterdata.HOBIE_16, BoatClassMasterdata.HOBIE_TIGER,
                BoatClassMasterdata.A_CAT, BoatClassMasterdata.TORNADO, BoatClassMasterdata.FLYING_PHANTOM);
        BoatClassVectorGraphics keelBoatWithGennaker = new KeelBoatWithGennakerVectorGraphics(BoatClassMasterdata.J70,
                BoatClassMasterdata.B_ONE, BoatClassMasterdata.J80, BoatClassMasterdata.LASER_SB3, BoatClassMasterdata.LONGTZE,
                BoatClassMasterdata.RS_FEVA, BoatClassMasterdata.RS_TERA, BoatClassMasterdata.RS100, BoatClassMasterdata.TP52,
                BoatClassMasterdata.CLUB_SWAN_50, BoatClassMasterdata.BAVARIA_CRUISER_41S, BoatClassMasterdata.BAVARIA_CRUISER_45,
                BoatClassMasterdata.BAVARIA_CRUISER_46);
        BoatClassVectorGraphics dinghyWithSpinnaker = new DinghyWithSpinnakerVectorGraphics(BoatClassMasterdata._420,
                BoatClassMasterdata._470, BoatClassMasterdata._5O5, BoatClassMasterdata.CADET, BoatClassMasterdata.FLYING_DUTCHMAN,
                BoatClassMasterdata.FOLKBOAT, BoatClassMasterdata.DYAS, BoatClassMasterdata.DRAGON_INT,
                BoatClassMasterdata.ELLIOTT_6M, BoatClassMasterdata.H_BOAT, BoatClassMasterdata.ALBIN_EXPRESS,
                BoatClassMasterdata.FARR_30, BoatClassMasterdata.J24, BoatClassMasterdata.PLATU_25,
                BoatClassMasterdata.TOM_28_MAX, BoatClassMasterdata.DELPHIA_24,
                BoatClassMasterdata.RS200, BoatClassMasterdata.RS400, BoatClassMasterdata.RS500, BoatClassMasterdata.RS800,
                BoatClassMasterdata.STREAMLINE, BoatClassMasterdata.SWAN_45, BoatClassMasterdata.TEENY, BoatClassMasterdata.X_99,
                BoatClassMasterdata.TRIAS, BoatClassMasterdata.VENT_D_OUEST, BoatClassMasterdata.FLYING_JUNIOR, BoatClassMasterdata.VAURIEN, BoatClassMasterdata.VARIANTA);
        BoatClassVectorGraphics circle = new CircleVectorGraphics(BoatClassMasterdata.RUNNING);

        defaultBoatVectorGraphics = dinghyWithSpinnaker; // TODO see bug 2571; this should be a slup-rigged icon working for 470, 505, J/70 etc.
        for (BoatClassVectorGraphics g : new BoatClassVectorGraphics[] { laser, _49er, extreme40, smallMultihull, keelBoatWithGennaker, dinghyWithSpinnaker, gc32, circle}) {
            for (BoatClassMasterdata b : g.getCompatibleBoatClasses()) {
                compatibleBoatVectorGraphicsMap.put(b, g);
            }
        }
    }
	
    public static BoatClassVectorGraphics resolveBoatClassVectorGraphics(String boatClassName) {
        BoatClassMasterdata resolvedBoatClass = BoatClassMasterdata.resolveBoatClass(boatClassName);
        final BoatClassVectorGraphics result;
        if (resolvedBoatClass != null && compatibleBoatVectorGraphicsMap.containsKey(resolvedBoatClass)) {
            result = compatibleBoatVectorGraphicsMap.get(resolvedBoatClass);
        } else {
            result = defaultBoatVectorGraphics;
        }
        return result;
    }
}
