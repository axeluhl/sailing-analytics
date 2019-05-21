package com.sap.sailing.domain.orc;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveCourseImpl;
import com.sap.sailing.domain.orc.impl.ORCPerformanceCurveLegImpl;

public class TestORCPerformanceCurveCourse {

    @Test
    public void testSubcourseOfSimpleORCCourse () {
        double accuracy = 0.000000001;
        List<ORCPerformanceCurveLeg> legs = new ArrayList<>();
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1), new DegreeBearingImpl(0)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1), new DegreeBearingImpl(90)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1), new DegreeBearingImpl(120)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1), new DegreeBearingImpl(180)));
        legs.add(new ORCPerformanceCurveLegImpl(new NauticalMileDistance(1), new DegreeBearingImpl(0)));
        ORCPerformanceCurveCourse course = new ORCPerformanceCurveCourseImpl(legs);
        
        // case 0: no leg finished, 40.0% of current leg
        ORCPerformanceCurveCourse subcourse0 = course.subcourse(0, 0.4);
        assertEquals(0.4, subcourse0.getTotalLength().getNauticalMiles(), accuracy);
        
        // case 1: first leg finished, 0.0% of current leg
        ORCPerformanceCurveCourse subcourse1 = course.subcourse(1, 0);
        assertEquals(1, subcourse1.getTotalLength().getNauticalMiles(), accuracy);
        
        // case 2: first leg finished, 12.5% of current leg
        ORCPerformanceCurveCourse subcourse2 = course.subcourse(1, 0.125);
        assertEquals(1.125, subcourse2.getTotalLength().getNauticalMiles(), accuracy);
        
        // special case: didn't start, equals to no legs finished and 0.0% of current leg
        ORCPerformanceCurveCourse subcourseSpecial1 = course.subcourse(0, 0);
        assertEquals(0, subcourseSpecial1.getTotalLength().getNauticalMiles(), accuracy);
        
        // special case: number of finished legs is higher then number of actual legs
        ORCPerformanceCurveCourse subcourseSpecial2 = course.subcourse(10,0);
        assertEquals(5, subcourseSpecial2.getTotalLength().getNauticalMiles(), accuracy);
    }
    
    @Test
    public void testSubcourseOfComplexCourse () {
        
    }
    
}
