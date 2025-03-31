package com.sap.sailing.dashboards.gwt.client.widgets.startanalysis.rankingtable;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.dashboards.gwt.client.theme.Fonts;

public class StartAnalysisStartRankTableTextCell extends AbstractCell<String> implements Cell<String> {

    public StartAnalysisStartRankTableTextCell() {
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
        String fontFamily = Fonts.DASHBOARD_FONT_FAMILY;
        sb.appendHtmlConstant("<div style=\"font-family:"+fontFamily+";\">");
        if (value != null) {  
            sb.appendHtmlConstant(value);
        }
        sb.appendHtmlConstant("</div>");
    }
}
