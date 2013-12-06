package com.sap.sailing.domain.common.impl;


/**
 * A color defined in the RGB color schema but defined by a HTML color code (e.g. #FF0000)
 * @author Frank
 */
public class HtmlColor extends RGBColor {
    private static final long serialVersionUID = 8630909239259098473L;

    HtmlColor() {
    } // for GWT serializability

    public HtmlColor(String htmlColor) {
        if (htmlColor.startsWith("#")) {
            htmlColor = htmlColor.substring(1);
        }
        if (htmlColor.endsWith(";")) {
            htmlColor = htmlColor.substring(0, htmlColor.length() - 1);
        }

        int r, g, b;
        switch (htmlColor.length()) {
        case 6:
            r = Integer.parseInt(htmlColor.substring(0, 2), 16);
            g = Integer.parseInt(htmlColor.substring(2, 4), 16);
            b = Integer.parseInt(htmlColor.substring(4, 6), 16);
            break;
        case 3:
            r = Integer.parseInt(htmlColor.substring(0, 1), 16);
            g = Integer.parseInt(htmlColor.substring(1, 2), 16);
            b = Integer.parseInt(htmlColor.substring(2, 3), 16);
            break;
        case 1:
            r = g = b = Integer.parseInt(htmlColor.substring(0, 1), 16);
            break;
        default:
            throw new IllegalArgumentException("Invalid color: " + htmlColor);
        }
        
        this.red = ensureValidRange(r);
        this.green = ensureValidRange(g);
        this.blue = ensureValidRange(b);
    }
}
