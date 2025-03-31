package com.sap.sailing.dashboards.gwt.client.visualeffects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;


/**
 * Animates number values changes in the UI as ticking animation. Requires a method to change the value of the corresponding UI element.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public abstract class NumberTickingAnimation implements NumberValueSetter, TimeListener {

    private double previousNumber = 0;
    int counter = 0;
    Timer timer;
    List<String> animationNumberStrings;
    private final int ANIMATION_TIME_IN_MILLISECONDS = 500;
    
    public NumberTickingAnimation(){
        animationNumberStrings = new ArrayList<String>();
        timer = new Timer(PlayModes.Live);
    }
    
    public void execute(String numberString) {
            double numberAsDouble = Double.parseDouble(numberString);
            animationNumberStrings.clear();
            animationNumberStrings = getValuesBetweenDoubleOldAndNewNumber(numberAsDouble);
            int animationTimePerNumber = ANIMATION_TIME_IN_MILLISECONDS / animationNumberStrings.size();
            timer.setRefreshInterval(animationTimePerNumber);
            timer.addTimeListener(this);
            timer.play();
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
    
    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        if (animationNumberStrings.size() > counter) {
            String value = animationNumberStrings.get(counter);
            setValueInUI(value);
            counter++;
        } else {
            timer.pause();
            timer.removeTimeListener(this);
            counter = 0;
        }
    }
}
