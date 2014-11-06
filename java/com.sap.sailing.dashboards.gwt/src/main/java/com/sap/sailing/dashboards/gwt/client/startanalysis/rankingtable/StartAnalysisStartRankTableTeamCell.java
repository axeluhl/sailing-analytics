package com.sap.sailing.dashboards.gwt.client.startanalysis.rankingtable;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sse.common.Util.Pair;

public class StartAnalysisStartRankTableTeamCell extends AbstractCell<Pair<String, String>> implements Cell<Pair<String, String>> {

    public StartAnalysisStartRankTableTeamCell() {
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, Pair<String, String> value, SafeHtmlBuilder sb) {
        
        sb.appendHtmlConstant("<div style=\"width: 100%; height: 15pt; line-height: 15pt;\">");
        if (value != null) {
            sb.appendHtmlConstant(value.getA());
            sb.appendHtmlConstant("<div style=\"background-color:"+ value.getB()+"; width: 100%; height: 1.5pt; margin-top: -1.5pt;\"></div>");
         }
        sb.appendHtmlConstant("</div>");
    }
}
