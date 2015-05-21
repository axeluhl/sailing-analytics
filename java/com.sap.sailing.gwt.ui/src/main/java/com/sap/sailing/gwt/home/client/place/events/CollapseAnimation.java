package com.sap.sailing.gwt.home.client.place.events;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;

public class CollapseAnimation extends Animation {
    
    private Element elementToAnimate;
    
    private boolean show = true;
    private int effectiveHeight;

    public CollapseAnimation(Element elementToAnimate) {
        this(elementToAnimate, true);
    }
    
    public CollapseAnimation(Element elementToAnimate, int effectiveHeight) {
        this(elementToAnimate, false);
        this.effectiveHeight = effectiveHeight;
    }
    
    public CollapseAnimation(Element elementToAnimate, boolean showInitial) {
        this.elementToAnimate = elementToAnimate;
        this.show = showInitial;
        elementToAnimate.getStyle().setOverflow(Overflow.HIDDEN);
        if(!showInitial) {
            elementToAnimate.getStyle().setDisplay(Display.NONE);
        }
    }
    
    @Override
    protected void onComplete() {
        if(!show) {
            elementToAnimate.getStyle().setDisplay(Display.NONE);
        } else {
            elementToAnimate.getStyle().clearHeight();
        }
    }

    @Override
    protected void onUpdate(double progress) {
        if(!show) {
            progress = 1.0 - progress;
        }
        elementToAnimate.getStyle().setHeight(effectiveHeight * progress, Unit.PX);
    }
    
    public void animate(boolean show) {
        if(show && effectiveHeight == 0) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    effectiveHeight = elementToAnimate.getScrollHeight();
                }
            });
        }
        elementToAnimate.getStyle().clearDisplay();
        this.show = show;
        run(500);
    }
}
