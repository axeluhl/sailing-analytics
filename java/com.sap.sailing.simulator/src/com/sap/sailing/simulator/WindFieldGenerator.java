package com.sap.sailing.simulator;



import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;

public interface WindFieldGenerator extends WindField {
    
    public void setPositionGrid(Position[][] positions);
    
    public void generate(TimePoint start, TimePoint end, TimePoint step);

    public Position[][] getPositionGrid();
    
    /**
     * The first time for which we have a timed wind from the wind field
     * @return
     */
    public TimePoint getStartTime();
    
    /**
     * Returns the time steps if the wind field is generated at these time uints
     * from the start time
     * @return
     */
    public TimePoint getTimeStep();
    
    /**
     * The last TimePoint for which the wind field is generated, could be null if no
     * such time is defined.
     * @return
     */
    public TimePoint getEndTime();
}
