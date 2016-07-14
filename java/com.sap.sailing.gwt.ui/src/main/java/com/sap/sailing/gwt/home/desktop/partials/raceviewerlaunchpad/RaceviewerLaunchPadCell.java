package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.communication.race.RaceMetadataDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.desktop.places.event.EventView;
import com.sap.sailing.gwt.home.desktop.places.event.EventView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;

public class RaceviewerLaunchPadCell<T extends RaceMetadataDTO<?>> extends AbstractCell<T> {
    
    interface CellTemplates extends SafeHtmlTemplates {
        @Template("<div class=\"{0}\">{1}</div>")
        SafeHtml raceNotTracked(String styleNames, String text);
        
        @Template("<div class=\"{0}\"><div>{2}</div><div class=\"{1}\"><img src=\"images/home/launch-loupe.svg\"/></div>")
        SafeHtml raceviewerLaunchPad(String styleNames, String iconStyleNames, String text);
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
    private final PopupPanel panel = new PopupPanel(true, true);

    public RaceviewerLaunchPadCell(Presenter presenter) {
        super(BrowserEvents.CLICK);
        local_res.css().ensureInjected();
        this.presenter = presenter;
    }

    @Override
    public void onBrowserEvent(Context context, final Element parent, T data, NativeEvent event,
            ValueUpdater<T> valueUpdater) {
        if (data.hasValidTrackingData() && BrowserEvents.CLICK.equals(event.getType())) {
            panel.setWidget(new RaceviewerLaunchPad(data) {
                @Override
                protected String getRaceViewerURL(SimpleRaceMetadataDTO data, String mode) {
                    return presenter.getRaceViewerURL(data, mode);
                }
            });
            panel.setPopupPositionAndShow(new PositionCallback() {
                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                    panel.setPopupPosition(parent.getAbsoluteRight() + 1 - offsetWidth, 
                            parent.getAbsoluteBottom() - offsetHeight);
                }
            });
            return;
        }
        super.onBrowserEvent(context, parent, data, event, valueUpdater);
    }
    
    @Override
    public void render(Context context, T data, SafeHtmlBuilder sb) {
        if (data.hasValidTrackingData()) {
            String styleNames = data.isFinished() ? analyzeStyleNames : liveStyleNames;
            sb.append(TEMPLATE.raceviewerLaunchPad(styleNames, iconStyleNames, I18N.raceDetailsToShow()));
        } else {
            sb.append(TEMPLATE.raceNotTracked(notTrackedStyleNames, I18N_UBI.eventRegattaRaceNotTracked()));
        }
    }

}
