/**
 * 
 */
package com.sap.sailing.gwt.ui.simulator;

import com.sap.sailing.gwt.ui.client.TimeListener;

/**
 * A time listener that can be asked whether the timer shall be stopped.
 * 
 * @author Nidhi Sawhney (D054070)
 *
 */
public interface TimeListenerWithStoppingCriteria extends TimeListener {
   
   /**
    * @return whether the Listener signals a stop request for the timer.
    */
   public boolean shallStop();

}
