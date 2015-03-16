package com.sap.sailing.gwt.home.client.place.events;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;

public class CollapseAnimation extends Animation {
    
    private Element elementToAnimate;
    
    private boolean show = true;
    private int effectiveHeight;

    public CollapseAnimation(Element elementToAnimate) {
        this.elementToAnimate = elementToAnimate;
    }
    
    public CollapseAnimation(Element elementToAnimate, int effectiveHeight) {
        this(elementToAnimate);
        this.effectiveHeight = effectiveHeight;
        show = false;
    }

    @Override
    protected void onUpdate(double progress) {
        if(!show) {
            progress = 1.0 - progress;
        }
        elementToAnimate.getStyle().setHeight(effectiveHeight * progress, Unit.PX);
    }
    
    protected void onStart() {
        super.onStart();
        elementToAnimate.getStyle().setOverflow(Overflow.HIDDEN);
    }
    
    public void animate(boolean show) {
        if(this.show && effectiveHeight == 0) {
            effectiveHeight = elementToAnimate.getOffsetHeight();
        }
        this.show = show;
        run(500);
    }

}
