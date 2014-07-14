package com.sap.sailing.domain.tracking;

/**
 * Wind data may be requested for one or more objects that have a position. For example, when requesting the windward
 * distance that a competitor still has to go to reach the next mark, the wind in the middle between the competitor's
 * position and the end mark of the leg can be requested. When asking the relative windward distances that several
 * competitors still have to go, it is more useful to use a position for querying the wind field that remains constant
 * across the competitors to avoid inconsistencies. Here, the leg's middle between the leg start control point and the
 * leg end control point can be used to query the wind at that position.<p>
 * 
 * When showing a single wind direction for the entire race course area, instead of using a specific position it may be
 * more adequate to request a "general average" across the various wind sources.<p>
 * 
 * These different types of positioned wind field querying are identified by the literals of this enumeration. 
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public enum WindPositionMode {
    EXACT, LEG_MIDDLE, GLOBAL_AVERAGE;
}
