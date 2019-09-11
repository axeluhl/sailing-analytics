package com.sap.sse.gwt.client.dialog;

import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.SuggestBox;

public abstract class DialogUtils {
    public static void linkEnterToButton(final Button button, HasAllKeyHandlers... widgets) {
        for (HasAllKeyHandlers widget : widgets) {
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

    public static void linkEscapeToButton(final Button button, HasAllKeyHandlers... widgets) {
        for (HasAllKeyHandlers widget : widgets) {
            widget.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                        button.click();
                    }
                }
            });
            widget.addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                        button.click();
                    }
                }
            });
        }
    }

    public static void addFocusUponKeyUpToggler(final SuggestBox focusable) {
        // this ensures that the value is copied into the TextBox.getValue() result and a ChangeEvent is fired
        focusable.addKeyUpHandler(e -> {
            focusable.setFocus(false);
            focusable.setFocus(true);
        });
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
