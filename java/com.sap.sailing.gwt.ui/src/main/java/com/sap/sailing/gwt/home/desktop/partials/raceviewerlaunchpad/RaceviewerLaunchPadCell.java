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
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.sap.sailing.gwt.home.communication.race.RaceMetadataDTO;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;

public class RaceviewerLaunchPadCell<T extends RaceMetadataDTO<?>> extends AbstractCell<T> {
    
    interface CellTemplates extends SafeHtmlTemplates {
        @Template("<div class=\"{0}\">{1}</div>")
        SafeHtml raceNotTracked(String styleNames, String text);
        
        @Template("<div class=\"{0}\"><div>{2}</div><div class=\"{1}\"><img src=\"{3}\"/></div>")
        SafeHtml raceviewerLaunchPad(String styleNames, String iconStyleNames, String text, SafeUri iconUrl);
        
        @Template("<a href=\"{4}\" target=\"_blank\" class=\"{0}\"><div>{2}</div><div class=\"{1}\"><img src=\"{3}\"/></div></a> ")
        SafeHtml standaloneButton(String styleNames, String iconStyleNames, String text, SafeUri iconUrl, SafeUri safeUri);
    }
    
    private static final CellTemplates TEMPLATE = GWT.create(CellTemplates.class);
    private static final StringMessages I18N = StringMessages.INSTANCE;
    
    private final RaceviewerLaunchPadResources local_res = RaceviewerLaunchPadResources.INSTANCE;
    private final String notTrackedStyleNames = local_res.css().raceviewerlaunchpad_not_tracked();
    private final String analyzeStyleNames = local_res.css().raceviewerlaunchpad();
    private final String plannedStyleNames = Util.join(" ", analyzeStyleNames, local_res.css().raceviewerlaunchpadplanned());
    private final String liveStyleNames = Util.join(" ", analyzeStyleNames, local_res.css().raceviewerlaunchpadlive());
    private final String iconStyleNames = local_res.css().raceviewerlaunchpad_icon();

    private final RaceviewerLaunchPadController launchPadController;
    private final boolean showNotTracked;

    public RaceviewerLaunchPadCell(RaceviewerLaunchPadController launchPadPresenter, boolean showNotTracked) {
        super(BrowserEvents.CLICK);
        this.showNotTracked = showNotTracked;
        local_res.css().ensureInjected();
        this.launchPadController = launchPadPresenter;
    }

    @Override
    public void onBrowserEvent(Context context, final Element parent, T data, NativeEvent event,
            ValueUpdater<T> valueUpdater) {
        if (!launchPadController.renderAsDirectLink(data)) {
            if (data.hasValidTrackingData() && BrowserEvents.CLICK.equals(event.getType())
                    && parent.getFirstChildElement().isOrHasChild(Element.as(event.getEventTarget()))) {
                launchPadController.showLaunchPad(data, parent);
            }
            return;
        }
        super.onBrowserEvent(context, parent, data, event, valueUpdater);
    }
    
    @Override
    public void render(Context context, T data, SafeHtmlBuilder sb) {
        switch (launchPadController.getRenderingStyle(data)) {
        case WATCH_LIVE_ONLY:
                sb.append(TEMPLATE.standaloneButton(plannedStyleNames, iconStyleNames, I18N.watchLive(), 
                        SharedHomeResources.INSTANCE.launchPlay().getSafeUri(), UriUtils.fromString(launchPadController.getDirectLinkUrl(data))));
                break;
        case WATCH_LIVE_OR_ANALYZE:
            sb.append(TEMPLATE.raceviewerLaunchPad(liveStyleNames, iconStyleNames, I18N.raceDetailsToShow(), SharedHomeResources.INSTANCE.launchLoupe().getSafeUri()));
            break;
        case ANALYZE:
            sb.append(TEMPLATE.raceviewerLaunchPad(analyzeStyleNames, iconStyleNames, I18N.raceDetailsToShow(), SharedHomeResources.INSTANCE.launchLoupe().getSafeUri()));
            break;
        case NOT_TRACKED:
            sb.append(TEMPLATE.raceNotTracked(notTrackedStyleNames, showNotTracked ? I18N.eventRegattaRaceNotTracked() : ""));
            break;
        }
    }

}
