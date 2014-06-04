package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.gwt.ui.simulator.util.LineSegment;
import com.sap.sailing.gwt.ui.simulator.util.LineSegment.Point;

public class LineSegmentTest {

    @Test
    public void test1() {
        LineSegment s1 = new LineSegment(0,0,10,10);
        LineSegment s2 = new LineSegment(10,0,0,10);
        
        Point intersectionPoint = s1.intersect(s2);
        if (intersectionPoint != null) {
            System.out.println("The lines interect at " + intersectionPoint);
        }
        assertEquals("Lines do intersect", 5.0, intersectionPoint.getX(),1e-6);
        assertEquals("Lines do intersect", 5.0, intersectionPoint.getY(),1e-6);
    }
    
    @Test
    public void test2() {
        LineSegment s1 = new LineSegment(0,0,0,10);
        LineSegment s2 = new LineSegment(10,0,10,10);
        
        Point intersectionPoint = s1.intersect(s2);
       
        assertEquals("Lines do not intersect", null, intersectionPoint);
    }

    @Test
    public void test3() {
       
        LineSegment s1 = new LineSegment(53.987292187570304, 10.944955638570837,53.991202136256135, 10.951666848382969);
   
        LineSegment b1 = new LineSegment(53.977189863832564, 10.886260271572164,53.96560719314814, 10.925463437534281);
        LineSegment b2 = new LineSegment(53.96560719314814, 10.925463437534281,53.991220287799436, 10.947350263095805);
        LineSegment b3 = new LineSegment(53.991220287799436, 10.947350263095805,54.00280295848386, 10.90814709713369);
        LineSegment b4 = new LineSegment(54.00280295848386, 10.90814709713369,53.977189863832564, 10.886260271572164);
        
        s1.intersect(b1);
        s1.intersect(b2);
        s1.intersect(b3);
        s1.intersect(b4);
        Point intersectionPoint = s1.intersect(b1);
        assertEquals("Lines s1 and b1 do not intersect", null, intersectionPoint);
        intersectionPoint = s1.intersect(b2);
        assertEquals("Lines s and b2 do not intersect", null, intersectionPoint);
        intersectionPoint = s1.intersect(b3);
        assertEquals("Lines s3 and b3 do not intersect", null, intersectionPoint);
        intersectionPoint = s1.intersect(b4);
        assertEquals("Lines s4 and b4 do not intersect", null, intersectionPoint);
        
    }
    
}
