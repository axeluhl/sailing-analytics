package com.sap.sse.gwt.client;

import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.AbstractColor;

/**
 * A text box allowing users to enter a color, either as an HTML hex code, as in {@code #000000}, or as a CSS color name
 * (see <a href=
 * "http://www.w3schools.com/cssref/css_colors_legal.asp">http://www.w3schools.com/cssref/css_colors_legal.asp</a>). If
 * no color is recognized (including the case where the field is left empty), {@code null} is returned from the
 * {@link #getColor()} method. Clients can of course still obtain the {@link #getValue() text value}. Furthermore, the
 * {@link #isValid()} method can be used to check for valid/invalid inputs.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ColorTextBox extends TextBox {
    public ColorTextBox(Color color) {
        super();
        setText(color.getAsHtml());
    }
    
    public ColorTextBox() {
        super();
    }
    
    /**
     * Can be used to distinguish why {@link #getColor()} may be returning {@code null}. If this method returns
     * {@code true} then this means that the text field was empty (trimmed) which is a valid input.
     * 
     * @return {@code true} if the field is empty (ignoring whitespace) or contains a valid color representation
     */
    public boolean isValid() {
        return getColor() != null || getValue() == null || getValue().trim().isEmpty();
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
