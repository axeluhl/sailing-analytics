package com.sap.sailing.dashboards.gwt.client.startanalysis.rankingtable;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class StartAnalysisStartRankTableRankAtFirstMarkCell extends AbstractCell<String> implements Cell<String> {

    public StartAnalysisStartRankTableRankAtFirstMarkCell() {
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {

        String backrgoundColor;
        
        switch(value){ 
        case "1": 
            backrgoundColor = "#F0AB00";
            break; 
        case "2": 
            backrgoundColor = "#1BB6E8";
            break; 
        case "3": 
            backrgoundColor = "#EC5D5A";
            break; 
        default: 
            backrgoundColor = "#d3d3d3";
        } 
        
        sb.appendHtmlConstant("<div style=\"background-color:"+backrgoundColor+"; border-radius: 20pt; width: 20pt; height: 20pt; line-height: 20pt; vertical-align: middle; text-align: center;\">");
        if (value != null) {
            
            sb.appendHtmlConstant(value);
        }
        sb.appendHtmlConstant("</div>");
    }
}
