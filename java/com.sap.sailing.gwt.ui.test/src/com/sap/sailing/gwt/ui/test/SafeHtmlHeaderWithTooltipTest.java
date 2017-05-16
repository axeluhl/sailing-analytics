package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.gwt.ui.leaderboard.SafeHtmlHeaderWithTooltip;

public class SafeHtmlHeaderWithTooltipTest {

    @Test
    public void textCreation() {
        SafeHtmlBuilder titleBuilder = new SafeHtmlBuilder().appendEscaped("Title Text");
        SafeHtmlHeaderWithTooltip header = new SafeHtmlHeaderWithTooltip(titleBuilder.toSafeHtml(), "Tooltip");
        assertEquals("<span title=\"Tooltip\">Title Text</ span>", header.getValue().asString());
        
        titleBuilder = new SafeHtmlBuilder().appendEscapedLines("Title\nText");
        header =  new SafeHtmlHeaderWithTooltip(titleBuilder.toSafeHtml(), "Tooltip");
        assertEquals("<span title=\"Tooltip\">Title<br>Text</ span>", header.getValue().asString());
        
        titleBuilder = new SafeHtmlBuilder().appendEscaped("Title Text");
        header = new SafeHtmlHeaderWithTooltip(titleBuilder.toSafeHtml(), "Tooltip with\nline break.");
        assertEquals("<span title=\"Tooltip with\nline break.\">Title Text</ span>", header.getValue().asString());
    }

}
