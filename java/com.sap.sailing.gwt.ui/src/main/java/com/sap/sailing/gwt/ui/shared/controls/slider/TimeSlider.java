package com.sap.sailing.gwt.ui.shared.controls.slider;

import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.sap.sailing.gwt.ui.shared.controls.slider.TimeTicksCalculator.NormalizedInterval;
import com.sap.sailing.gwt.ui.shared.controls.slider.TimeTicksCalculator.TickPosition;

public class TimeSlider extends SliderBar {

    private final TimeTicksCalculator calc = new TimeTicksCalculator();

    /**
     * Draw the tick along the line.
     */
    protected void drawTicks() {
        if (!isAttached() || !isMinMaxInitialized())
            return;

        NormalizedInterval normalizedTimeTickInterval = calc.normalizeTimeTickInterval((maxValue.longValue() - minValue.longValue()) / 8);
        List<TickPosition> calculatedTimeTicks = calc.calculateTimeTicks(normalizedTimeTickInterval, minValue.longValue(), maxValue.longValue(), 1);
        
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
                long pos = (tickPosition.time.getTime() - minValue.longValue()) * lineWidth / (maxValue.longValue() - minValue.longValue());
                int tickLeftOffset = lineLeftOffset + (int) pos - (tickWidth / 2);
                tickLeftOffset = Math.min(tickLeftOffset, lineLeftOffset + lineWidth - tickWidth);
                DOM.setStyleAttribute(tick, "left", tickLeftOffset + "px");
                DOM.setStyleAttribute(tick, "visibility", "visible");
            }

            // Hide unused ticks
            for (int i = (calculatedTimeTicks.size() + 1); i < tickElements.size(); i++) {
                DOM.setStyleAttribute(tickElements.get(i), "display", "none");
            }
    }


    /**
     * Draw the labels along the line.
     */
    protected void drawTickLabels() {
        if (!isAttached() || !isMinMaxInitialized())
            return;

        NormalizedInterval normalizedTimeTickInterval = calc.normalizeTimeTickInterval((maxValue.longValue() - minValue.longValue()) / 8);
        List<TickPosition> calculatedTimeTicks = calc.calculateTimeTicks(normalizedTimeTickInterval, minValue.longValue(), maxValue.longValue(), 1);

        // Draw the tick labels
        int lineWidth = lineElement.getOffsetWidth();
            // Create the labels or make them visible
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
                double value = tickPosition.time.getTime();
                DOM.setStyleAttribute(label, "visibility", "hidden");
                DOM.setStyleAttribute(label, "display", "");
                DOM.setElementProperty(label, "innerHTML", formatTickLabel(value));

                // Move to the left so the label width is not clipped by the
                // shell
                DOM.setStyleAttribute(label, "left", "0px");

                // Position the label and make it visible
                int labelWidth = label.getOffsetWidth();
                long pos = (tickPosition.time.getTime() - minValue.longValue()) * lineWidth / (maxValue.longValue() - minValue.longValue());
                int labelLeftOffset = lineLeftOffset + (int) pos - (labelWidth / 2);
                labelLeftOffset = Math.min(labelLeftOffset, lineLeftOffset + lineWidth - labelWidth);
                labelLeftOffset = Math.max(labelLeftOffset, lineLeftOffset);
                DOM.setStyleAttribute(label, "left", labelLeftOffset + "px");
                DOM.setStyleAttribute(label, "visibility", "visible");
            }

            // Hide unused labels
            for (int i = (calculatedTimeTicks.size() + 1); i < tickLabelElements.size(); i++) {
                DOM.setStyleAttribute(tickLabelElements.get(i), "display", "none");
            }
    }

}
