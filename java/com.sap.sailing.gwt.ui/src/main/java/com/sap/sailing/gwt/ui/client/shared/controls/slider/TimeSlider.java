package com.sap.sailing.gwt.ui.client.shared.controls.slider;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.dom.client.Element;
import com.sap.sailing.gwt.ui.client.shared.controls.slider.TimeTicksCalculator.NormalizedInterval;
import com.sap.sailing.gwt.ui.client.shared.controls.slider.TimeTicksCalculator.TickPosition;

public class TimeSlider extends SliderBar {

    private final TimeTicksCalculator calc = new TimeTicksCalculator();

    private List<TickPosition> calculatedTimeTicks;
    
    private boolean isZoomed;

    private final int TICKCOUNT = 10;

    public TimeSlider() {
        calculatedTimeTicks = new ArrayList<TickPosition>();
        isZoomed = false;
    }
    
    private void calculateTicks() {
        if (!isMinMaxInitialized())
            return;

        calculatedTimeTicks.clear();
        
        long minMaxDiffInMs = maxValue.longValue() - minValue.longValue();
        long tickInterval = minMaxDiffInMs / TICKCOUNT; 
        NormalizedInterval normalizedTimeTickInterval = calc.normalizeTimeTickInterval(tickInterval);
        calculatedTimeTicks = calc.calculateTimeTicks(normalizedTimeTickInterval, minValue.longValue(), maxValue.longValue(), 1);
    }
    
    /**
     * Draw the ticks along the line.
     */
    protected void drawTicks() {
        if (!isAttached() || !isMinMaxInitialized())
            return;

        // Draw the ticks
        int lineWidth = lineElement.getOffsetWidth();
        // Create the ticks or make them visible
        for (int i = 0; i < calculatedTimeTicks.size(); i++) {
            TickPosition tickPosition = calculatedTimeTicks.get(i);
            
            Element tick = null;
            if (i < tickElements.size()) {
                tick = tickElements.get(i);
            } else { // Create the new tick
                tick = DOM.createDiv();
                DOM.setStyleAttribute(tick, "position", "absolute");
                DOM.setStyleAttribute(tick, "display", "none");
                DOM.appendChild(getElement(), tick);
                tickElements.add(tick);
            }
            if (enabled) {
                DOM.setElementProperty(tick, "className", "gwt-SliderBar-tick");
            } else {
                DOM.setElementProperty(tick, "className", "gwt-SliderBar-tick gwt-SliderBar-tick-disabled");
            }
            // Position the tick and make it visible
            DOM.setStyleAttribute(tick, "visibility", "hidden");
            DOM.setStyleAttribute(tick, "display", "");
            int tickWidth = tick.getOffsetWidth();
            long pos = (tickPosition.getPosition().getTime() - minValue.longValue()) * lineWidth / (maxValue.longValue() - minValue.longValue());
            int tickLeftOffset = lineLeftOffset + (int) pos - (tickWidth / 2);
            tickLeftOffset = Math.min(tickLeftOffset, lineLeftOffset + lineWidth - tickWidth);
            DOM.setStyleAttribute(tick, "left", tickLeftOffset + "px");
            DOM.setStyleAttribute(tick, "visibility", "visible");
        }

        // Hide unused ticks
        for (int i = calculatedTimeTicks.size(); i < tickElements.size(); i++) {
            DOM.setStyleAttribute(tickElements.get(i), "display", "none");
        }
    }


    /**
     * Draw the labels along the line.
     */
    protected void drawTickLabels() {
        if (!isAttached() || !isMinMaxInitialized())
            return;

        // Draw the tick labels
        int lineWidth = lineElement.getOffsetWidth();
            // Create the labels or make them visible
            Double previousValue = null;
            for (int i = 0; i < calculatedTimeTicks.size(); i++) {
                TickPosition tickPosition = calculatedTimeTicks.get(i);
                Element label = null;
                if (i < tickLabelElements.size()) {
                    label = tickLabelElements.get(i);
                } else { // Create the new label
                    label = DOM.createDiv();
                    DOM.setStyleAttribute(label, "position", "absolute");
                    DOM.setStyleAttribute(label, "display", "none");
                    if (enabled) {
                        DOM.setElementProperty(label, "className", "gwt-SliderBar-ticklabel");
                    } else {
                        DOM.setElementProperty(label, "className", "gwt-SliderBar-ticklabel-disabled");
                    }
                    DOM.appendChild(getElement(), label);
                    tickLabelElements.add(label);
                }

                // Set the label text
                double value = tickPosition.getPosition().getTime();
                DOM.setStyleAttribute(label, "visibility", "hidden");
                DOM.setStyleAttribute(label, "display", "");
                DOM.setElementProperty(label, "innerHTML", formatTickLabel(value, previousValue));

                // Move to the left so the label width is not clipped by the
                // shell
                DOM.setStyleAttribute(label, "left", "0px");

                // Position the label and make it visible
                int labelWidth = label.getOffsetWidth();
                long pos = (tickPosition.getPosition().getTime() - minValue.longValue()) * lineWidth / (maxValue.longValue() - minValue.longValue());
                int labelLeftOffset = lineLeftOffset + (int) pos - (labelWidth / 2);
                labelLeftOffset = Math.min(labelLeftOffset, lineLeftOffset + lineWidth - labelWidth);
                labelLeftOffset = Math.max(labelLeftOffset, lineLeftOffset);
                DOM.setStyleAttribute(label, "left", labelLeftOffset + "px");
                DOM.setStyleAttribute(label, "visibility", "visible");
                
                previousValue = value;
            }

            // Hide unused labels
            for (int i = calculatedTimeTicks.size(); i < tickLabelElements.size(); i++) {
                DOM.setStyleAttribute(tickLabelElements.get(i), "display", "none");
            }
    }

    public void clearMarkersAndLabelsAndTicks() {
        clearMarkers();
        for (Element elem : tickLabelElements) {
            DOM.setStyleAttribute(elem, "display", "none");
            DOM.setStyleAttribute(elem, "visibility", "hidden");
        }
        for (Element elem : tickElements) {
            DOM.setStyleAttribute(elem, "display", "none");
            DOM.setStyleAttribute(elem, "visibility", "hidden");
        }

        for (Element elem : markerLabelElements) {
            DOM.setStyleAttribute(elem, "display", "none");
            DOM.setStyleAttribute(elem, "visibility", "hidden");
        }
        for (Element elem : markerElements) {
            DOM.setStyleAttribute(elem, "display", "none");
            DOM.setStyleAttribute(elem, "visibility", "hidden");
        }
    }
    
    /**
     * Draw the knob where it is supposed to be relative to the line.
     */
    protected void drawKnob() {
        if (!isAttached() || !isMinMaxInitialized())
            return;
        
        Element knobElement = knobImage.getElement();
        if(curValue >= minValue && curValue <= maxValue) {
            // Move the knob to the correct position
            int lineWidth = lineElement.getOffsetWidth();
            int knobWidth = knobElement.getOffsetWidth();
            int knobLeftOffset = (int) (lineLeftOffset + (getKnobPercent() * lineWidth) - (knobWidth / 2));
            knobLeftOffset = Math.min(knobLeftOffset, lineLeftOffset + lineWidth - (knobWidth / 2) - 1);
            DOM.setStyleAttribute(knobElement, "left", knobLeftOffset + "px");
            DOM.setStyleAttribute(knobElement, "visibility", "visible");
            DOM.setStyleAttribute(knobElement, "display", "");
        } else {
            DOM.setStyleAttribute(knobElement, "display", "none");
            DOM.setStyleAttribute(knobElement, "visibility", "hidden");
        }
    }
    
    public void setMaxValue(Double maxValue, boolean fireEvent) {
        if(!isZoomed) {
            super.setMaxValue(maxValue, fireEvent);
        } else {
            this.maxValue = maxValue;
        }
        calculateTicks();
    }

    public void setMinValue(Double minValue, boolean fireEvent) {
        if(!isZoomed) {
            super.setMinValue(minValue, fireEvent);
        } else {
            this.minValue = minValue;
        }
        calculateTicks();
    }

    public void setStepSize(double stepSize, boolean fireEvent) {
        if(!isZoomed) {
            super.setStepSize(stepSize, fireEvent);
        } else {
            this.stepSize = stepSize;
        }
    }

    public boolean isZoomed() {
        return isZoomed;
    }

    public void setZoomed(boolean isZoomed) {
        this.isZoomed = isZoomed;
    }
}
