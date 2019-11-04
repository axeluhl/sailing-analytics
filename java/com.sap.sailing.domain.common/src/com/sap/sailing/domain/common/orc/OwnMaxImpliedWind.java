package com.sap.sailing.domain.common.orc;

public interface OwnMaxImpliedWind extends ImpliedWindSource {
    @Override
    default <T> T accept(ImpliedWindSourceVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
