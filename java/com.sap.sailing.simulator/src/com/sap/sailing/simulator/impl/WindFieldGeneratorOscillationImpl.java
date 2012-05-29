package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.WindControlParameters;
import com.sap.sailing.simulator.WindFieldGenerator;
import com.sap.sailing.simulator.impl.WindFieldGeneratorImpl;

public class WindFieldGeneratorOscillationImpl extends WindFieldGeneratorImpl implements WindFieldGenerator {

    public WindFieldGeneratorOscillationImpl(Boundary boundary, WindControlParameters windParameters) {
        super(boundary, windParameters);
    }

    @Override
    public void generate(TimePoint start, TimePoint end, TimePoint step) {
        // TODO Auto-generated method stub

    }

}
