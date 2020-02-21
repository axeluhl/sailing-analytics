package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Color;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * Encodes an invalid color; can be used to transport an error message through to
 * a dialog's {@link DataEntryDialog#getResult()} method. The output of
 * {@link #getAsHtml()} contains a translated error message based on the
 * string passed to the constructor.
 * 
 * @author Axel Uhl (D043530)
 *
 */
class InvalidColor implements Color {
    private static final long serialVersionUID = 4012986110898149543L;
    private final String invalidColorString;
    private final StringMessages stringMessages;

    protected InvalidColor(StringMessages stringMessages, String invalidColorString) {
        this.stringMessages = stringMessages;
        this.invalidColorString = invalidColorString;
    }

    @Override
    public com.sap.sse.common.Util.Triple<Integer, Integer, Integer> getAsRGB() {
        return null;
    }

    @Override
    public com.sap.sse.common.Util.Triple<Float, Float, Float> getAsHSV() {
        return null;
    }

    @Override
    public String getAsHtml() {
        return stringMessages.invalidColor(invalidColorString);
    }

    @Override
    public Color invert() {
        return null;
    }
}