package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;

public abstract class AbstractSpeedImpl implements Speed {

    @Override
    public Distance travel(TimePoint t1, TimePoint t2) {
        return new NauticalMileDistance((t2.asMillis() - t1.asMillis()) / 1000. / 3600. * getKnots());
    }

}
