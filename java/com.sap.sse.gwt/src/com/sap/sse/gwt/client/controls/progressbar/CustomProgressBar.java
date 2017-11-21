package com.sap.sse.gwt.client.controls.progressbar;

import java.util.Objects;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

/**
 * Custom widget implementation wrapping an HTML <code>progress</code> tag, providing static factory methods for
 * {@link CustomProgressBar#determinate() determinate} and {@link CustomProgressBar#indeterminate() indeterminate}
 * instances as well as getter and setter methods for the {@code max} and {@code value} attributes.
 */
public class CustomProgressBar extends Widget {

    private static final String TAG = "progress", ATTR_MAX = "max", ATTR_VALUE = "value";

    /**
     * Creates a new {@link CustomProgressBar} instance in indeterminate state.
     * 
     * @return the created {@link CustomProgressBar} instance
     */
    public static CustomProgressBar indeterminate() {
        return new CustomProgressBar();
    }

    /**
     * Creates a new {@link CustomProgressBar} instance in determinate state with default {@code max} attribute.
     * 
     * @return the created {@link CustomProgressBar} instance
     */
    public static CustomProgressBar determinate() {
        final CustomProgressBar progressBar = indeterminate();
        progressBar.setValue(0.0d);
        return progressBar;
    }

    /**
     * Creates a new {@link CustomProgressBar} instance in determinate state with the provided {@code max} attribute.
     * 
     * @param max
     *            numeric value for {@code max} attribute
     * @return the created {@link CustomProgressBar} instance
     * 
     * @see CustomProgressBar#determinate()
     */
    public static CustomProgressBar determinate(double max) {
        final CustomProgressBar progressBar = determinate();
        progressBar.setMax(max);
        return progressBar;
    }

    private CustomProgressBar() {
        setElement(Document.get().createElement(TAG));
        getElement().getStyle().setWidth(100, Unit.PCT);
        getElement().getStyle().setHeight(2, Unit.EM);
    }

    /**
     * @return numeric value of the {@link CustomProgressBar}'s {@code max} attribute
     */
    public final double getMax() {
        return this.getDoubleAttribute(ATTR_MAX);
    }

    /**
     * @param max
     *            numeric value for the {@link CustomProgressBar}'s {@code max} attribute
     */
    public final void setMax(double max) {
        this.setDoubleAtrribute(ATTR_MAX, max);
    }

    /**
     * @return numeric value of the {@link CustomProgressBar}'s {@code value} attribute
     */
    public final double getValue() {
        return this.getDoubleAttribute(ATTR_VALUE);
    }

    /**
     * @param max
     *            numeric value for the {@link CustomProgressBar}'s {@code value} attribute
     */
    public final void setValue(double value) {
        this.setDoubleAtrribute(ATTR_VALUE, value);
    }

    private double getDoubleAttribute(String attrName) {
        final String attrValue = getElement().getAttribute(attrName);
        return (Objects.isNull(attrValue) || attrValue.isEmpty()) ? null : Double.parseDouble(attrValue);
    }

    private void setDoubleAtrribute(String attrName, double attrValue) {
        getElement().setAttribute(attrName, Double.toString(attrValue));
    }

}
