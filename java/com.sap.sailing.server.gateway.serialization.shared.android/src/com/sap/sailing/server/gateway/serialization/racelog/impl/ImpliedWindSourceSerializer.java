package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.orc.FixedSpeedImpliedWind;
import com.sap.sailing.domain.common.orc.ImpliedWindSource;
import com.sap.sailing.domain.common.orc.ImpliedWindSourceVisitor;
import com.sap.sailing.domain.common.orc.OtherRaceAsImpliedWindSource;
import com.sap.sailing.domain.common.orc.OwnMaxImpliedWind;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class ImpliedWindSourceSerializer implements JsonSerializer<ImpliedWindSource> {
    public static final String ORC_FIXED_IMPLIED_WIND_SPEED_IN_KNOTS = "impliedWindInKnots";
    public static final String ORC_OTHER_RACE_REGATTA_LIKE_NAME = "otherRaceRegattaLikeName";
    public static final String ORC_OTHER_RACE_RACE_COLUMN_NAME = "otherRaceRaceColumnName";
    public static final String ORC_OTHER_RACE_FLEET_NAME = "otherRaceFleetName";
    public static final String ORC_IMPLIED_WIND_SOURCE_TYPE = "impliedWindSourceType";

    @Override
    public JSONObject serialize(ImpliedWindSource impliedWindSource) {
        final JSONObject result = new JSONObject();
        if (impliedWindSource == null) {
            result.put(ORC_IMPLIED_WIND_SOURCE_TYPE, null);
        } else {
            impliedWindSource.accept(new ImpliedWindSourceVisitor<Void>() {
                @Override
                public Void visit(FixedSpeedImpliedWind impliedWindSource) {
                    result.put(ORC_IMPLIED_WIND_SOURCE_TYPE, FixedSpeedImpliedWind.class.getSimpleName());
                    result.put(ORC_FIXED_IMPLIED_WIND_SPEED_IN_KNOTS, impliedWindSource.getFixedImpliedWindSpeed() == null ? null : impliedWindSource.getFixedImpliedWindSpeed().getKnots());
                    return null;
                }
    
                @Override
                public Void visit(OtherRaceAsImpliedWindSource impliedWindSource) {
                    result.put(ORC_IMPLIED_WIND_SOURCE_TYPE, OtherRaceAsImpliedWindSource.class.getSimpleName());
                    result.put(ORC_OTHER_RACE_REGATTA_LIKE_NAME, impliedWindSource.getLeaderboardAndRaceColumnAndFleetOfDefiningRace().getA());
                    result.put(ORC_OTHER_RACE_RACE_COLUMN_NAME, impliedWindSource.getLeaderboardAndRaceColumnAndFleetOfDefiningRace().getB());
                    result.put(ORC_OTHER_RACE_FLEET_NAME, impliedWindSource.getLeaderboardAndRaceColumnAndFleetOfDefiningRace().getC());
                    return null;
                }
    
                @Override
                public Void visit(OwnMaxImpliedWind impliedWindSource) {
                    result.put(ORC_IMPLIED_WIND_SOURCE_TYPE, OwnMaxImpliedWind.class.getSimpleName());
                    return null;
                }
            });
        }
        return result;
    }
}
