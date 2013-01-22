package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.gwt.ui.leaderboard.SafeHtmlHeaderWithTooltip;

public class SafeHtmlHeaderWithTooltipTest {

    @Test
    public void textCreation() {
        SafeHtmlBuilder titleBuilder = new SafeHtmlBuilder().appendEscaped("Title Text");
        SafeHtmlHeaderWithTooltip header = new SafeHtmlHeaderWithTooltip(titleBuilder.toSafeHtml(), "Tooltip");
        assertEquals("<div title=\"Tooltip\">Title Text</ div>", header.getValue().asString());
        
        titleBuilder = new SafeHtmlBuilder().appendEscapedLines("Title\nText");
        header =  new SafeHtmlHeaderWithTooltip(titleBuilder.toSafeHtml(), "Tooltip");
        assertEquals("<div title=\"Tooltip\">Title<br>Text</ div>", header.getValue().asString());
        
        titleBuilder = new SafeHtmlBuilder().appendEscaped("Title Text");
        header = new SafeHtmlHeaderWithTooltip(titleBuilder.toSafeHtml(), "Tooltip with\nline break.");
        assertEquals("<div title=\"Tooltip with<br>line break.\">Title Text</ div>", header.getValue().asString());
    }

}
