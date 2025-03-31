package com.sap.sailing.gwt.home.desktop.partials.sailorinfo;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.TextTransform;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class SailorInfo extends Composite {
    
    public SailorInfo(String sailorsInfoURL) {
        StringMessages i18n = StringMessages.INSTANCE;
        
        FlowPanel panel = new FlowPanel();
        Style panelStyle = panel.getElement().getStyle();
        panelStyle.setDisplay(Display.INLINE_BLOCK);
        panelStyle.setVerticalAlign(VerticalAlign.BOTTOM);
        
        Label sailorInfoLabel = new Label();
        sailorInfoLabel.getElement().setInnerHTML(i18n.sailorInfoLongText().replace("\n", "<br />"));
        Style labelStyle = sailorInfoLabel.getElement().getStyle();
        labelStyle.setDisplay(Display.INLINE_BLOCK);
        sailorInfoLabel.getElement().getStyle().setMarginRight(0.5, Unit.EM);
        panel.add(sailorInfoLabel);
        
        Anchor sailorInfoAnchor = new Anchor(i18n.sailorInfo());
        sailorInfoAnchor.setHref(sailorsInfoURL);
        sailorInfoAnchor.setTarget("_blank");
        sailorInfoAnchor.setStyleName(SharedResources.INSTANCE.mainCss().button());
        sailorInfoAnchor.addStyleName(SharedResources.INSTANCE.mainCss().buttonprimary());
        Style style = sailorInfoAnchor.getElement().getStyle();
        style.setTextTransform(TextTransform.UPPERCASE);
        style.setVerticalAlign(VerticalAlign.BOTTOM);
        panel.add(sailorInfoAnchor);
        
        initWidget(panel);
    }

}
