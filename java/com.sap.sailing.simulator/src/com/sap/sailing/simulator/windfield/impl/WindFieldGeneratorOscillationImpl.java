package com.sap.sailing.simulator.windfield.impl;

import java.util.logging.Logger;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.windfield.WindControlParameters;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;

public class WindFieldGeneratorOscillationImpl extends WindFieldGeneratorImpl implements WindFieldGenerator {

    private static final long serialVersionUID = -7005970781594631010L;

    /* Currently the speed is time and vertical step invariant, it only changes along the horizontal 
     * direction*/
    private Speed[] speed;
   
    private double timeScale = 0.0;
    
    private static Logger logger = Logger.getLogger(WindFieldGeneratorOscillationImpl.class.getName());

    public WindFieldGeneratorOscillationImpl(Boundary boundary, WindControlParameters windParameters) {
        super(boundary, windParameters);
    }

    @Override
    public void generate(TimePoint start, TimePoint end, TimePoint step) {
        super.generate(start,end,step);
        if (positions == null || positions.length < 1) {
            return;
        }
        int vPoints = positions.length;
        Distance vStep = boundary.getHeight().scale(1.0/(vPoints-1));
        if (windParameters.baseWindSpeed > 0) {
            timeScale = vStep.getNauticalMiles()/windParameters.baseWindSpeed;
        } else {
            timeScale = 0;
        }
        initializeSpeed();
    }
    
    private void initializeSpeed() {
        if (positions == null || positions.length < 1) {
            return;
        }
       
        int ncol = positions[0].length;
   
        speed = new Speed[ncol];
        double leftSpeed = windParameters.baseWindSpeed*windParameters.leftWindSpeed/100.0;
        double middleSpeed = windParameters.baseWindSpeed*windParameters.middleWindSpeed/100.0;
        double rightSpeed = windParameters.baseWindSpeed*windParameters.rightWindSpeed/100.0;
        double midPoint = (ncol-1.)/2.;
        for (int i = 0; i < ncol/2; ++i) {
            speed[i] = new KnotSpeedImpl(leftSpeed + i*(middleSpeed-leftSpeed)/midPoint);
        }
        if (ncol%2==1) {
            speed[ncol/2] = new KnotSpeedImpl(middleSpeed);
        }
        for (int i = 0; i < ncol/2; ++i) {
            speed[ncol-1-i] = new KnotSpeedImpl(rightSpeed + i*(middleSpeed-rightSpeed)/midPoint);
        }
        /*System.out.println("wspeed:");
        for(int i = 0; i<ncol; i++) {
            System.out.print(""+speed[i]+", ");
        }
        System.out.println("");*/
    
    }
    
    private Speed getSpeed(TimedPosition timedPosition) {
        Position p = timedPosition.getPosition();
        Pair<Integer,Integer> positionIndex = getPositionIndex(p);
        if (positionIndex != null) {
            int colIndex = positionIndex.getB();
            return speed[colIndex];
        } else {
            logger.severe("Error finding position " + p);
        }
        return null;
        
    }
    
    private Bearing getBearing(TimedPosition timedPosition) {
        Position p = timedPosition.getPosition();
        Pair<Integer,Integer> positionIndex = getPositionIndex(p);
        if (positionIndex != null) {
            int rowIndex = positionIndex.getA();
            TimePoint timePoint = timedPosition.getTimePoint();
            int timeIndex = getTimeIndex(timePoint);
            Bearing phi0 = new DegreeBearingImpl(windParameters.baseWindBearing);
            //double vStep = 1.0/((positions.length-1)*timeScale);
            //double t = (timeIndex+(timeIndex+rowIndex)*vStep);
            double vStep = 1.0/(positions.length-1);
            //System.out.println("SliderTime: "+timeIndex*timeStep.asMillis()/1000./60./60.+" RowDeltaTime:"+rowIndex*vStep*timeScale);
            double t = timeIndex*timeStep.asMillis()/3600000.+rowIndex*vStep*timeScale;
            double oAngle = Math.sin(2*Math.PI*t*windParameters.frequency)*windParameters.amplitude;
            Bearing angle = new DegreeBearingImpl(oAngle);
            //logger.severe("timeIndex: "+timeIndex+" rowIndex: "+rowIndex+" timeScale: "+timeScale+" vStep: "+vStep+" t: "+t);
            //System.out.print("timeIndex: "+timeIndex+" rowIndex: "+rowIndex+" timeScale: "+timeScale+" vStep: "+vStep+" t: "+t+"\n");
            //System.out.print("oAngle: "+oAngle+" angle: "+angle.getDegrees()+"\n");
            angle = angle.add(phi0);
            return angle;
        } else {
            logger.severe("Error finding position " + p);
        }
        return null;
    }
    
    @Override
    public Wind getWind(TimedPosition timedPosition) {

        Speed speed = getSpeed(timedPosition);
        SpeedWithBearing wspeed = new KnotSpeedWithBearingImpl(speed.getKnots(),
                getBearing(timedPosition));

        return new WindImpl(timedPosition.getPosition(), timedPosition.getTimePoint(), wspeed);

    }
    
    public double getTimeScale() {
        return timeScale;
    }
    
    public void setTimeScale(double timeScale) {
        this.timeScale = timeScale;
    }
}
