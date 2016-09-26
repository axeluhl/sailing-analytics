package com.sap.sse.datamining.components.management;

/**
 * This defines an action to perform, when the free memory in percent is below
 * a threshold. Extending {@link Comparable} to determine the action with the highest
 * priority, if the threshold of multiple actions is exceeded.
 * 
 * @author Lennart Hensler (D054527)
 */
public interface MemoryMonitorAction extends Comparable<MemoryMonitorAction> {
    
    /**
     * @return the threshold of this action in free memory in percent.
     */
    double getThreshold();
    
    /**
     * Checks if the given argument is below the {@link #getThreshold() threshold}
     * and performs the action if yes.
     * 
     * @param freeMemoryRatio The current free memory as a ratio of the total memory, between 0..1
     * @return <code>true</code>, if the action has been performed.
     */
    boolean checkMemoryAndPerformAction(double freeMemoryRatio);
    
    /**
     * Compares this action to the given action by its importance.
     * 
     * @return <code>0</code>, if the actions are equally important<br>
     *         <code>1</code>, if this action is more important than the other action<br>
     *         <code>-1</code>, if this action is less important than the other action
     */
    @Override
    public int compareTo(MemoryMonitorAction other);

}
