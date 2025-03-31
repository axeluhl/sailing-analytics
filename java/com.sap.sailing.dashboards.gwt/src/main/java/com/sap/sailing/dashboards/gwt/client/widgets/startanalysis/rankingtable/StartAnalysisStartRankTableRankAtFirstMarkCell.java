package com.sap.sailing.dashboards.gwt.client.widgets.startanalysis.rankingtable;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.dashboards.gwt.client.theme.Fonts;
import com.sap.sse.common.Util.Pair;

public class StartAnalysisStartRankTableRankAtFirstMarkCell extends AbstractCell<Pair<String, String>> implements Cell<Pair<String, String>> {

    public StartAnalysisStartRankTableRankAtFirstMarkCell() {
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, Pair<String, String> value, SafeHtmlBuilder sb) {
        String borderColor = "white";
        String textColor = "white";
        String width = "22px";
        String height = "22px";
        String fontFamily = Fonts.DASHBOARD_FONT_FAMILY;
        if(value.getB().equals("#FFFFFF")){
            borderColor = "black";
            textColor = "black";
            width = "22px";
            height = "22px";
        }
        sb.appendHtmlConstant("<div style=\"background-color:"+value.getB()+"; border-radius: "+width+"; width: "+width+"; height: "+height+"; line-height: 22px; vertical-align: middle; text-align: center; color:"+textColor+";  border-color:"+borderColor+"; border-style: solid; border-width: 1px; font-family:"+fontFamily+";\">");
        if (value != null) {  
            sb.appendHtmlConstant(value.getA());
        }
        sb.appendHtmlConstant("</div>");
    }
}
