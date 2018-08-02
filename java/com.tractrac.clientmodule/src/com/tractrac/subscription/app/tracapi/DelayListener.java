package com.tractrac.subscription.app.tracapi;

import com.tractrac.model.lib.api.data.*;
import com.tractrac.model.lib.api.event.*;
import com.tractrac.model.lib.api.route.IControl;
import com.tractrac.util.lib.api.TimeUtils;

import java.util.Date;

/**
 * @author <a href="mailto:jorge@tractrac.dk">Jorge Piera Llodr&aacute;</a>
 */
public class DelayListener  extends AbstractListener {

    @Override
    public void gotControlPointPosition(IControl control, IPosition position, int markNumber) {
       //printDelay(position.getTimestamp(), control.getName());
    }

    @Override
    public void gotPosition(IRaceCompetitor raceCompetitor, IPosition position) {
       if (!raceCompetitor.getCompetitor().isNonCompeting()) {
           printDelay(position.getTimestamp(), raceCompetitor.getCompetitor().getName());
       }
    }

    private void printDelay(long sampleTime, String name) {
        long now = new Date().getTime();
        long diff = now - sampleTime;
        int steps = Math.round(diff/100);
        StringBuilder delay = new StringBuilder();
        for (int i = 0 ; i<steps ; i++) {
            delay.append("*");
        }

        System.out.println(
                String.valueOf(TimeUtils.formatDateInMillis(now)) + ": " +
                String.valueOf(TimeUtils.formatDateInMillis(sampleTime)) + ": " +
                fillString(name, 25) + "-> " +
                delay.toString()
        );
    }

    public String fillString(String string, int count){
       if (string.length() > count) {
           return string.substring(0, count);
       }
       String out = string;
       for (int i=string.length() ;i<count ; i++) {
           out = out + "-";
       }
       return out;
    }
}

