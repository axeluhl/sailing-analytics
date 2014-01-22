package com.sap.sse.gwt.ui;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;

public abstract class EntryPointUtils {
    /**
     * <p>The attribute which is used for the debug id.</p>
     */
    public static final String DEBUG_ID_ATTRIBUTE = "selenium-id"; //$NON-NLS-1$
    
    /**
     * <p>The prefix which is used for the debug id.</p>
     */
    public static final String DEBUG_ID_PREFIX = ""; //$NON-NLS-1$
    
    public static void linkEnterToButton(final Button button, FocusWidget... widgets) {
        for (FocusWidget widget : widgets) {
            widget.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                        button.click();
                    }
                }
            });
        }
    }

    public static void linkEscapeToButton(final Button button, FocusWidget... widgets) {
        for (FocusWidget widget : widgets) {
            widget.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                        button.click();
                    }
                }
            });
        }
    }

    public static void addFocusUponKeyUpToggler(final FocusWidget focusable) {
        focusable.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                focusable.setFocus(false);
                // this ensures that the value is copied into the TextBox.getValue() result and a ChangeEvent is fired
                focusable.setFocus(true);
            }
        });
    }
}
