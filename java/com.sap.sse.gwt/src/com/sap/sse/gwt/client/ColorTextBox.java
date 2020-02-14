package com.sap.sse.gwt.client;

import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.AbstractColor;

public class ColorTextBox extends TextBox {
    public ColorTextBox(Color color) {
        super();
        setText(color.getAsHtml());
    }
    
    public ColorTextBox() {
        super();
    }
    
    public Color getColor() {
        final Color color;
        if (getValue() == null || getValue().isEmpty()) {
            color = null;
        } else {
            color = AbstractColor.getCssColor(getText());
        }
        return color;
    }
}
