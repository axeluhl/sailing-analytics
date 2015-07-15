package com.sap.sailing.dashboards.gwt.client.visualeffects;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;

/**
 * Animates number values changes in the UI as ticking animation. Requires a method to change the value of the corresponding UI element.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public abstract class NumberTickingAnimation implements NumberValueSetter {

    private double previousNumber = 0;
    private Timer timer;
    int counter = 0;
    List<String> animationNumberStrings;
    private final int ANIMATION_TIME_IN_MILLISECONDS = 1000;
    
    public void execute(double number) { 
        animationNumberStrings = getValuesBetweenDoubleOldAndNewNumber(number);
        int animationTimePerNumber = ANIMATION_TIME_IN_MILLISECONDS/animationNumberStrings.size();
            timer = new Timer() {
                @Override
                public void run() {
                            if(animationNumberStrings.size() > counter) {
                             String value = animationNumberStrings.get(counter);
                            setValueInUI(value);
                            counter++;
                            }else {
                                timer.cancel();
                                counter = 0;
                            }
                        }                    
            };
            timer.scheduleRepeating(animationTimePerNumber);
        }
    
    private List<String> getValuesBetweenDoubleOldAndNewNumber(double number) {
        List<String> result = new ArrayList<String>();
        double counter = previousNumber;
        if (previousNumber < number) {
            while (counter <= number) {
                counter = counter + 0.1;
                result.add(NumberFormat.getFormat("#0.0").format(counter));
            }
        } else {
            while (counter >= number) {
                counter = counter - 0.1;
                result.add(NumberFormat.getFormat("#0.0").format(counter));
            }
        }
        previousNumber = number;
        return result;
    }
    
}
