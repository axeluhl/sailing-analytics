/**
 * 
 */
package com.sap.sailing.dashboards.gwt.shared;


/**
 * Used to calculate the average value for a certain amount of renewing values.
 * The amount of values is defined by {@link #NUMBER_OF_VALUES_AVERAGE_CALCULATED}.
 * It uses a doubleEnededQueue to delete old values, when the queue reached the size 
 * of {@link #NUMBER_OF_FIEXES_AVERAGE_CALCULATED} and populate it with new ones.
 * 
 * @author Alexander Ries
 *
 */
public class MovingAverage {
    
    private int NUMBER_OF_VALUES_AVERAGE_CALCULATED;
    private DoubleEndedQueue values;
    private double sumFromAllValuesInQueue;
    
    public MovingAverage(int numberOfValuesAverageCalculated) {
        super();
        NUMBER_OF_VALUES_AVERAGE_CALCULATED = numberOfValuesAverageCalculated;
        values = new DoubleEndedQueue();
    }
    
    public void add(double newValue){
        values.insertRear(newValue);
        sumFromAllValuesInQueue = sumFromAllValuesInQueue+newValue;
        if(values.getSize()>NUMBER_OF_VALUES_AVERAGE_CALCULATED){
            sumFromAllValuesInQueue = sumFromAllValuesInQueue - values.removeFront();    
        }
    }
    
    public double getAverage(){
        return sumFromAllValuesInQueue/values.getSize();
    }
}
