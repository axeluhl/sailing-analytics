/**
 * 
 */
package com.sap.sailing.gwt.ui.client;

/**
 * @author Nidhi Sawhney (D054070)
 *
 */
public interface TimeListenerWithStoppingCriteria extends TimeListener{
   
   /**
    * 
    * @return 0 when the Listener signals a stop for the timer.
    */
   public int stop();

}
