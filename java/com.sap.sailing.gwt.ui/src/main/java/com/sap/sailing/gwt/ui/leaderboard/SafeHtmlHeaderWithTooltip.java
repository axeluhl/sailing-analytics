package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;

public class SafeHtmlHeaderWithTooltip extends SafeHtmlHeader {

    private static SafeHtml buildHeader(SafeHtml title, String tooltip) {
        //Escaping the tooltip to be able, to add it in the div safely with standard string concatenation.
        //If you try to append it with the SafeHtmlBuilder methods, an exception will be thrown (because of the unclosed div).
        String safeTooltip = new SafeHtmlBuilder().appendEscaped(tooltip).toSafeHtml().asString();
        return new SafeHtmlBuilder().appendHtmlConstant("<span title=\"" + safeTooltip + "\">")
                   .append(title).appendHtmlConstant("</ span>").toSafeHtml();
    }
    
    /**
     * Creates a new html header with the given title and tooltip.<br>
     * Use '\n', if you want a line break in the tooltip.
     */
    public SafeHtmlHeaderWithTooltip(SafeHtml title, String tooltip) {
        super(buildHeader(title, tooltip));
    }

}
