package com.sap.sailing.domain.common.orc;

public interface ImpliedWindSourceVisitor<T> {
    T visit(FixedSpeedImpliedWind impliedWindSource);

    T visit(OtherRaceAsImpliedWindSource impliedWindSource);

    T visit(OwnMaxImpliedWind impliedWindSource);
}
