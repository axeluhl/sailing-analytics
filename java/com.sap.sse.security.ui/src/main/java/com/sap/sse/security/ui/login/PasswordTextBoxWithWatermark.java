package com.sap.sse.security.ui.login;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class PasswordTextBoxWithWatermark extends PasswordTextBox implements BlurHandler, FocusHandler {
    private String watermark;
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
     * Adds a watermark if the parameter is not NULL or EMPTY
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

    @Override
    public void onBlur(BlurEvent event) {
        enableWatermark();
    }

    void enableWatermark() {
        String text = getText();
        if ((text.length() == 0) || (text.equalsIgnoreCase(watermark))) {
            // Show watermark
            setText(watermark);
            addStyleName(LoginViewResources.INSTANCE.css().passwordTextInput_watermark());
        }
    }

    @Override
    public void onFocus(FocusEvent event) {
        removeStyleName(LoginViewResources.INSTANCE.css().passwordTextInput_watermark());

        if (getText().equalsIgnoreCase(watermark)) {
            // Hide watermark
            setText("");
        }
    }
}