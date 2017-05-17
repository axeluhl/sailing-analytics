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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
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
        
        @Template("<a href=\"{4}\" target=\"_blank\" class=\"{0}\"><div>{2}</div><div class=\"{1}\"><img src=\"images/home/{3}.svg\"/></div></a> ")
        SafeHtml standaloneButton(String styleNames, String iconStyleNames, String text, String icon, SafeUri safeUri);
    }
    
    private static final CellTemplates TEMPLATE = GWT.create(CellTemplates.class);
    private static final StringMessages I18N = StringMessages.INSTANCE;
    
    private final RaceviewerLaunchPadResources local_res = RaceviewerLaunchPadResources.INSTANCE;
    private final String notTrackedStyleNames = local_res.css().raceviewerlaunchpad_not_tracked();
    private final String analyzeStyleNames = local_res.css().raceviewerlaunchpad();
    private final String plannedStyleNames = Util.join(" ", analyzeStyleNames, local_res.css().raceviewerlaunchpadplanned());
    private final String liveStyleNames = Util.join(" ", analyzeStyleNames, local_res.css().raceviewerlaunchpadlive());
    private final String iconStyleNames = local_res.css().raceviewerlaunchpad_icon();

    private final EventView.Presenter presenter;
    private final PopupPanel panel = new PopupPanel(true, false);
    private final boolean showNotTracked;

    public RaceviewerLaunchPadCell(Presenter presenter, boolean showNotTracked) {
        super(BrowserEvents.CLICK);
        this.showNotTracked = showNotTracked;
        local_res.css().ensureInjected();
        this.presenter = presenter;
    }

    @Override
    public void onBrowserEvent(Context context, final Element parent, T data, NativeEvent event,
            ValueUpdater<T> valueUpdater) {
        if (!renderAsDirectLinkButton(data)) {
            if (data.hasValidTrackingData() && BrowserEvents.CLICK.equals(event.getType())
                    && parent.getFirstChildElement().isOrHasChild(Element.as(event.getEventTarget()))) {
                panel.setWidget(createLaunchPad(parent.getFirstChildElement(), data));
                panel.setPopupPositionAndShow(new PositionCallback() {
                    @Override
                    public void setPosition(int offsetWidth, int offsetHeight) {
                        int alignBottom = parent.getAbsoluteTop() + parent.getOffsetHeight() - offsetHeight;
                        int top = (alignBottom - Window.getScrollTop() < 0 ? parent.getAbsoluteTop() - 1 : alignBottom + 1);
                        panel.setPopupPosition(parent.getAbsoluteRight() + 1 - offsetWidth, top);
                        panel.getElement().scrollIntoView();
                    }
                });
            }
            return;
        }
        super.onBrowserEvent(context, parent, data, event, valueUpdater);
    }
    
    @Override
    public void render(Context context, T data, SafeHtmlBuilder sb) {
        switch (getRenderingStyle(data)) {
        case WATCH_LIVE_ONLY:
                sb.append(TEMPLATE.standaloneButton(plannedStyleNames, iconStyleNames, I18N.watchLive(), 
                        "launch-play",
                        UriUtils.fromString(presenter.getRaceViewerURL(data, RaceBoardModes.PLAYER.name()))));
                break;
        case WATCH_LIVE_OR_ANALYZE:
            sb.append(TEMPLATE.raceviewerLaunchPad(liveStyleNames, iconStyleNames, I18N.raceDetailsToShow()));
            break;
        case ANALYZE:
            sb.append(TEMPLATE.raceviewerLaunchPad(analyzeStyleNames, iconStyleNames, I18N.raceDetailsToShow()));
            break;
        case NOT_TRACKED:
            sb.append(TEMPLATE.raceNotTracked(notTrackedStyleNames, showNotTracked ? I18N.eventRegattaRaceNotTracked() : ""));
            break;
        }
    }
    
    public static enum RenderingStyle {
        WATCH_LIVE_ONLY, WATCH_LIVE_OR_ANALYZE, ANALYZE, NOT_TRACKED
    }
    
    public RenderingStyle getRenderingStyle(T data) {
        final RenderingStyle result;
        if (data.hasValidTrackingData()) {
            if (renderAsDirectLinkButton(data)) {
                result = RenderingStyle.WATCH_LIVE_ONLY;
            } else {
                result = data.isFinished() ? RenderingStyle.ANALYZE : RenderingStyle.WATCH_LIVE_OR_ANALYZE;
            }
        } else {
            result = RenderingStyle.NOT_TRACKED;
        }
        return result;
    }
    
    /** 
     * Creates a {@link RaceviewerLaunchPad} for the given data object.
     */
    private RaceviewerLaunchPad createLaunchPad(Element button, T data) {
        RaceviewerLaunchPad launchPad = new RaceviewerLaunchPad(data, panel) {
            @Override
            protected String getRaceViewerURL(SimpleRaceMetadataDTO data, String raceBoardMode) {
                return presenter.getRaceViewerURL(data, raceBoardMode);
            }
        };
        return launchPad;
    }
    
    /** Determine whether a direct link button should be/is rendered instead of a menu popup. */
    private boolean renderAsDirectLinkButton(T data) {
        return !data.isFinished() && !data.isRunning();
    }

}
