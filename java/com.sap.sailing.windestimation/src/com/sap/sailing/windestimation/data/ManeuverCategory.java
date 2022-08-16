package com.sap.sailing.windestimation.data;

/**
 * Category for maneuvers which helps to filter out irrelevant maneuver for wind estimation. The wind estimation ingores
 * all maneuvers which are not {@link #REGULAR} and {@link #MARK_PASSING}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public enum ManeuverCategory {

    SMALL, REGULAR, MARK_PASSING, WIDE, _180, _360

}
