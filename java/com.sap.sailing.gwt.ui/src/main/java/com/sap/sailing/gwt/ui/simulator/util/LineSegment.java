package com.sap.sailing.gwt.ui.simulator.util;

/**
 * Implementation based on demo @link http://bloggingmath.wordpress.com/2009/05/29/line-segment-intersection/
 * @author Nidhi Sawhney D054070
 *
 */

public class LineSegment {

    enum IntersectionType {
       DONT_INTERSECT,
       PARALLEL_DONT_INTERSECT,
       COLINEAR_DONT_INTERSECT,
       INTERSECT,
       COLINEAR_INTERSECT 
    };
    
    private final static double EPSILON = 10e-6;
    
    public class Point {
        private double x;
        private double y;
       
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        public Point add(Point p) {
            return new Point(this.x+p.x, this.y+p.y);
        }
        
        public Point subtract(Point p) {
            return new Point(this.x-p.x, this.y-p.y);
        }
        
        public Point scalarMult(double s) {
            return new Point(s*this.x, s*this.y);
        }
        
        public double cross(Point p) {
            return this.x * p.y - p.x * this.y;
        }
        
        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }

        public double getX() {
            return x;
        }
        
        public double getY() {
            return y;
        }
    }
   
    private Point p1;
    private Point p2;
 
    /**
     * Create a line segment between point p1 and p2
     * @param p1
     * @param p2
     */
    public LineSegment(Point p1, Point p2) {
        this.setP1(p1);
        this.setP2(p2);
    }
    
    /**
     * Create a line segment between points (x1,y1) and (x2,y2)
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public LineSegment(double x1, double y1, double x2, double y2) {
        Point p1 = new Point(x1,y1);
        Point p2 = new Point(x2,y2);
        this.setP1(p1);
        this.setP2(p2);
    }
    
    public IntersectionType getIntersectionType(final LineSegment ls) {
        /*
        p = seg1.p1;
        r = seg1.p2.subtract(seg1.p1);
        q = seg2.p1;
        s = seg2.p2.subtract(seg2.p1);
        rCrossS = cross(r, s);
        if(rCrossS <= epsilon && rCrossS >= -1 * epsilon){
                return PARALLEL_DONT_INTERSECT;
        }
        t = cross(q.subtract(p), s)/rCrossS;
        u = cross(q.subtract(p), r)/rCrossS;
        if(0 <= u && u <= 1 && 0 <= t && t <= 1){
                intPoint = p.add(r.scalarMult(t));
                intersectionPoint.x = intPoint.x;
                intersectionPoint.y = intPoint.y;
                return INTERSECT;
        }else{
                return DONT_INTERSECT;
        }
        */
        Point p = this.p1;
        Point r = this.p2.subtract(this.p1);
        Point q = ls.p1;
        Point s = ls.p2.subtract(ls.p1);
        
        double rCrossS = r.cross(s);
        if (rCrossS <= EPSILON && rCrossS >= -1*EPSILON) {
  
            return IntersectionType.PARALLEL_DONT_INTERSECT;
        }
        Point qMinusp = q.subtract(p);
        
        double t = qMinusp.cross(s) / rCrossS;
        double u = qMinusp.cross(r) / rCrossS;
        
        if(0 <= u && u <= 1 && 0 <= t && t <= 1){
            Point intersectionPoint = p.add(r.scalarMult(t));
            
            return IntersectionType.INTERSECT;
        }
        return IntersectionType.DONT_INTERSECT;
    }
    
    /**
     * Intersect this line with LineSegment s
     * @param s the LineSegment to intersect
     * @return the point of intersection or null if they do not intersect
     */
    public Point intersect(final LineSegment ls) {
        
      
        Point p = this.p1;
        Point r = this.p2.subtract(this.p1);
        Point q = ls.p1;
        Point s = ls.p2.subtract(ls.p1);
        
        double rCrossS = r.cross(s);
        if (rCrossS <= EPSILON && rCrossS >= -1*EPSILON) {
            return null;
        }
        Point qMinusp = q.subtract(p);
        
        double t = qMinusp.cross(s) / rCrossS;
        double u = qMinusp.cross(r) / rCrossS;
        
        if(0 <= u && u <= 1 && 0 <= t && t <= 1){
            Point intersectionPoint = p.add(r.scalarMult(t));
            
            return intersectionPoint;
        }
        return null;
    }
    
    public Point getP1() {
        return p1;
    }

    public void setP1(Point p1) {
        this.p1 = p1;
    }

    public Point getP2() {
        return p2;
    }

    public void setP2(Point p2) {
        this.p2 = p2;
    }
}
