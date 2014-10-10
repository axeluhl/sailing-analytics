package com.sap.sailing.domain.common.racelog.tracking;

import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;

/**
 * Shared between GWT and Android. Used for creating and deciphering the URL
 * encoded by the QRCode, which gives the tracking app all necessary information
 * for creating the device mapping.
 * This includes the leaderboard name, race-column name and fleet name to identify
 * which RaceLog to add the {@code DeviceMappingEvent} to. Also, either a competitor
 * or mark ID is added, to which the device should be mapped. Timestamps for the
 * beginning and the end of the mapping are also included.
 * <p>
 * The URL points to the apk-file of the app deployed to the {@code www} directory
 * of the server, with additional {@code GET}-parameters:
 * <ul>
 * <li>{@link RaceLogServletConstants#PARAMS_LEADERBOARD_NAME}</li>
 * <li>{@link RaceLogServletConstants#PARAMS_RACE_COLUMN_NAME}</li>
 * <li>{@link RaceLogServletConstants#PARAMS_RACE_FLEET_NAME}</li>
 * <li>either {@link #COMPETITOR_ID_AS_STRING} or {@link #MARK_ID_AS_STRING}</li>
 * <li>{@link #FROM_MILLIS}</li>
 * <li>{@link #TO_MILLIS}</li>
 * </ul>
 * 
 * @author Fredrik Teschke
 */
public interface DeviceMappingConstants {
   static final String COMPETITOR_ID_AS_STRING = "competitor";
   static final String MARK_ID_AS_STRING = "mark";
   static final String FROM_MILLIS = "from";
   static final String TO_MILLIS = "to";
}
