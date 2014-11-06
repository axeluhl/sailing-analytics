package com.sap.sse.gwt.client.controls;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * A password textbox with a watermark property (HTML5 placeholder attribute)
 * @author Frank
 *
 */
public class PasswordTextBoxWithWatermark extends PasswordTextBox implements BlurHandler, FocusHandler {
    private String watermark;
    private String watermarkStyleName;
    private HandlerRegistration blurHandler;
    private HandlerRegistration focusHandler;

    public PasswordTextBoxWithWatermark() {
        super();
    }

    public PasswordTextBoxWithWatermark(String defaultValue) {
        this();
        setText(defaultValue);
    }

    public PasswordTextBoxWithWatermark(String defaultValue, String watermark) {
        this(defaultValue);
        setWatermark(watermark);
    }

    /**
     * Adds a watermark if the parameter is not Null
     *
     * @param watermark
     */
    public void setWatermark(final String watermark) {
        this.watermark = watermark;

        if (watermark != null && !watermark.isEmpty()) {
            blurHandler = addBlurHandler(this);
            focusHandler = addFocusHandler(this);
            enableWatermark();
        } else {
            // Remove handlers
            blurHandler.removeHandler();
            focusHandler.removeHandler();
        }
    }

    public void setWatermarkStyleName(String styleName) {
        this.watermarkStyleName = styleName;
    }
    
    @Override
    public void onBlur(BlurEvent event) {
        enableWatermark();
    }

    private void enableWatermark() {
        String text = getText();
        if ((text.length() == 0) || (text.equalsIgnoreCase(watermark))) {
            // Show watermark
            setText(watermark);
            if(watermarkStyleName != null && !watermarkStyleName.isEmpty()) {
                addStyleName(watermarkStyleName);
            }
        }
    }

    @Override
    public void onFocus(FocusEvent event) {
        if(watermarkStyleName != null && !watermarkStyleName.isEmpty()) {
            removeStyleName(watermarkStyleName);
        }

        if (getText().equalsIgnoreCase(watermark)) {
            // Hide watermark
            setText("");
        }
    }
}