package com.sap.sailing.simulator;

import java.util.List;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;

public interface WindFieldGenerator extends WindField {

    public List<Position> extractLattice(int hSteps, int vSteps);

    public void generate(TimePoint start, TimePoint end, TimePoint step);
}
