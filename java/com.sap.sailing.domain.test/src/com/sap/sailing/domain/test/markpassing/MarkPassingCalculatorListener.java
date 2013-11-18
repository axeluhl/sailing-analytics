package com.sap.sailing.domain.test.markpassing;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;

public class MarkPassingCalculatorListener extends AbstractRaceChangeListener {
    
    MarkPassingCalculator mpc = new MarkPassingCalculator();
    
    @Override
    public void markPositionChanged(GPSFix fix, Mark mark) {
    }
    
    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor item) {
    }


}
