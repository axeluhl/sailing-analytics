package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.sap.sse.common.Color;

public class ColorColumn<T> extends Column<T, SafeHtml> {
    public interface ColorRetriever<T> {
        Color getColor(T t);
    }
    
    private final ColorRetriever<T> colorRetriever;
    
    public ColorColumn(ColorRetriever<T> colorRetriever) {
        super(new SafeHtmlCell());
        this.colorRetriever = colorRetriever;
    }
    
    @Override
    public SafeHtml getValue(T t) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        if (colorRetriever.getColor(t) != null) {
            String colorString = colorRetriever.getColor(t).getAsHtml();
            sb.appendHtmlConstant("<span style=\"color: " + colorString + ";\">");
            sb.appendHtmlConstant(colorString);
            sb.appendHtmlConstant("</span>");
        } else {
            sb.appendHtmlConstant("&nbsp;");
        }
        return sb.toSafeHtml();
    }
};