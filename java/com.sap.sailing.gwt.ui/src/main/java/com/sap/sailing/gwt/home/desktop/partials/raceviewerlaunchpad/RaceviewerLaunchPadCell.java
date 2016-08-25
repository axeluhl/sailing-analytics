package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.communication.race.RaceMetadataDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.desktop.places.event.EventView;
import com.sap.sailing.gwt.home.desktop.places.event.EventView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardModes;
import com.sap.sse.common.Util;

public class RaceviewerLaunchPadCell<T extends RaceMetadataDTO<?>> extends AbstractCell<T> {
    
    interface CellTemplates extends SafeHtmlTemplates {
        @Template("<div class=\"{0}\">{1}</div>")
        SafeHtml raceNotTracked(String styleNames, String text);
        
        @Template("<div class=\"{0}\"><div>{2}</div><div class=\"{1}\"><img src=\"images/home/launch-loupe.svg\"/></div>")
        SafeHtml raceviewerLaunchPad(String styleNames, String iconStyleNames, String text);
        
        @Template("<a href=\"{4}\" class=\"{0}\"><div>{2}</div> <div class=\"{1}\"><img src=\"{3}\"/></div></a> ")
        SafeHtml standaloneButton(String styleNames, String iconStyleNames, String text, String icon, String link);
    }
    
    private static final CellTemplates TEMPLATE = GWT.create(CellTemplates.class);
    private static final TextMessages I18N_UBI = TextMessages.INSTANCE;
    private static final StringMessages I18N = StringMessages.INSTANCE;
    
    private final RaceviewerLaunchPadResources local_res = RaceviewerLaunchPadResources.INSTANCE;
    private final String notTrackedStyleNames = local_res.css().raceviewerlaunchpad_not_tracked();
    private final String analyzeStyleNames = local_res.css().raceviewerlaunchpad();
    private final String liveStyleNames = Util.join(" ", analyzeStyleNames, local_res.css().raceviewerlaunchpadlive());
    private final String iconStyleNames = local_res.css().raceviewerlaunchpad_icon();

    private final EventView.Presenter presenter;
    private final PopupPanel panel = new PopupPanel(true, false);

    public RaceviewerLaunchPadCell(Presenter presenter) {
        super(BrowserEvents.CLICK);
        local_res.css().ensureInjected();
        this.presenter = presenter;
    }

    @Override
    public void onBrowserEvent(Context context, final Element parent, T data, NativeEvent event,
            ValueUpdater<T> valueUpdater) {
        //If direct button we should not add any handlers
        if (!data.isFinished() && !data.isRunning()) {
            return;
        }
        
        if (data.hasValidTrackingData() && BrowserEvents.CLICK.equals(event.getType())
                && parent.getFirstChildElement().isOrHasChild(Element.as(event.getEventTarget()))) {
            panel.setWidget(new RaceviewerLaunchPad(data, panel) {
                @Override
                protected String getRaceViewerURL(SimpleRaceMetadataDTO data, String mode) {
                    return presenter.getRaceViewerURL(data, mode);
                }
            });
            
            panel.setPopupPositionAndShow(new PositionCallback() {
                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                    //Popup width is max between btn and actual popup size
                    int buttonWidth = 0;
                    if (parent.getChild(0) != null && parent.getChild(0).getNodeType() == Node.ELEMENT_NODE) {
                        Element button = (Element) parent.getChild(0);
                        //Adding 1px as button width is double value like 153.87 floated down to 153
                        buttonWidth = button.getOffsetWidth() + 1;
                    };
                    
                    int width = offsetWidth > buttonWidth ? offsetWidth : buttonWidth;
                    int alignBottom = parent.getAbsoluteTop() + parent.getOffsetHeight() - offsetHeight;
                    int top = (alignBottom - Window.getScrollTop() < 0 ? parent.getAbsoluteTop() - 1 : alignBottom + 1);
                    panel.setPopupPosition(parent.getAbsoluteRight() + 1 - width, top);
                    panel.setWidth(width + "px");
                    panel.getElement().scrollIntoView();
                }
            });
            return;
        }
        super.onBrowserEvent(context, parent, data, event, valueUpdater);
    }
    
    @Override
    public void render(Context context, T data, SafeHtmlBuilder sb) {
        if (data.hasValidTrackingData()) {
            //If race is live then draw direct button instead of popup
            if (!data.isFinished() && !data.isRunning()) {
                sb.append(TEMPLATE.standaloneButton(liveStyleNames, iconStyleNames, I18N.watchLive(), 
                        "images/home/play.png", presenter.getRaceViewerURL(data, RaceBoardModes.PLAYER.name())));
            } else {
                sb.append(TEMPLATE.raceviewerLaunchPad(analyzeStyleNames, iconStyleNames, I18N.raceDetailsToShow()));
            }
        } else {
            sb.append(TEMPLATE.raceNotTracked(notTrackedStyleNames, I18N_UBI.eventRegattaRaceNotTracked()));
        }
    }

}
