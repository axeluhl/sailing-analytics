package com.sap.sailing.simulator;



import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;

public interface WindFieldGenerator extends WindField {
    
    public void setPositionGrid(Position[][] positions);
    
    public void generate(TimePoint start, TimePoint end, TimePoint step);

    public Position[][] getPositionGrid();
}
