package com.sap.sailing.domain.common.orc;

import com.sap.sse.common.Util.Triple;

public interface OtherRaceAsImpliedWindSource extends ImpliedWindSource {
    @Override
    default <T> T accept(ImpliedWindSourceVisitor<T> visitor) {
        return visitor.visit(this);
    }
    
    Triple<String, String, String> getLeaderboardAndRaceColumnAndFleetOfDefiningRace();
}
