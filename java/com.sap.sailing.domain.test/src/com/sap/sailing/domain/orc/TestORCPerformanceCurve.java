package com.sap.sailing.domain.orc;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveCourseImpl;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveImpl;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveLegImpl;
import com.sap.sse.common.Distance;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class TestORCPerformanceCurve {

    private static ORCPerformanceCurveCourse course = null;
    
    @BeforeClass
    public static void initialize() {
        List<ORCPerformanceCurveLeg> legs = new ArrayList<>();
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(2.23), new DegreeBearingImpl(10)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(2.00), new DegreeBearingImpl(170)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(0.97), new DegreeBearingImpl(0)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.03), new DegreeBearingImpl(15)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.03), new DegreeBearingImpl(165)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1.17), new DegreeBearingImpl(180)));
        course = new ORCPerformanceCurveCourseImpl(legs);
    }
    
    @Test
    public void testLagrangeInterpolation() {
        //TODO Moqutio? Or use just a normal Certificate from Import?
        //ORCPerformanceCurve = new ORCPerformanceCurveImpl(twaAllowances, beatAngles, gybeAngles);
    }
    
}
