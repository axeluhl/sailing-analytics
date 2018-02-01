package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import java.util.function.BiFunction;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardModes;

public class RaceviewerLaunchPadController {

    public static enum RenderingStyle {
        WATCH_LIVE_ONLY, WATCH_LIVE_OR_ANALYZE, ANALYZE, NOT_TRACKED
    }

    private final PopupPanel panel = new PopupPanel(true, false);
    private final BiFunction<SimpleRaceMetadataDTO, String, String> raceboardUrlFactory;

    /**
     * Creates a new {@link RaceviewerLaunchPadController} instance with the given URL factory.
     * 
     * @param raceviewerUrlFactory
     *            {@link BiFunction Factory method} to get the raceboard URL for given {@link SimpleRaceMetadataDTO
     *            race} and {@link RaceBoardModes raceboard mode} information
     */
    public RaceviewerLaunchPadController(BiFunction<SimpleRaceMetadataDTO, String, String> raceboardUrlFactory) {
        this.raceboardUrlFactory = raceboardUrlFactory;
    }

    /**
     * Provides the direct link URL for the given {@link SimpleRaceMetadataDTO race}.
     * 
     * @param data
     *            the {@link SimpleRaceMetadataDTO race} to get the link URL for
     * @return the direct link URL (as {@link String})
     */
    public <T extends SimpleRaceMetadataDTO> String getDirectLinkUrl(T data) {
        return raceboardUrlFactory.apply(data, RaceBoardModes.PLAYER.name());
    }

    /**
     * Creates a {@link RaceviewerLaunchPad} for the given {@link SimpleRaceMetadataDTO race} and {@link PopupPanel}
     * instance.
     * 
     * @param data
     *            the {@link SimpleRaceMetadataDTO race} to create a launch pad for
     * @param parent
     *            the {@link PopupPanel} which will contain the launch pad
     * @return the newly created {@link RaceviewerLaunchPad} instance
     */
    public <T extends SimpleRaceMetadataDTO> RaceviewerLaunchPad getRaceviewerLaunchPad(T data, PopupPanel parent) {
        return new RaceviewerLaunchPad(data, parent) {
            @Override
            protected String getRaceViewerURL(SimpleRaceMetadataDTO data, String raceBoardMode) {
                return raceboardUrlFactory.apply(data, raceBoardMode);
            }
        };
    }

    /**
     * Shows a {@link RaceviewerLaunchPad} for the given {@link SimpleRaceMetadataDTO race} relative to the also
     * provided element.
     * 
     * @param data
     *            the {@link SimpleRaceMetadataDTO race} to create a launch pad for
     * @param relTo
     *            the {@link Element element} to show the launch pad relative to
     */
    public <T extends SimpleRaceMetadataDTO> void showRaceviewerLaunchPad(T data, Element relTo) {
        relTo.scrollIntoView();
        panel.setWidget(new RaceviewerLaunchPad(data, panel) {
            @Override
            protected String getRaceViewerURL(SimpleRaceMetadataDTO data, String raceBoardMode) {
                return raceboardUrlFactory.apply(data, raceBoardMode);
            }
        });
        panel.setVisible(false);
        panel.show();
        Scheduler.get().scheduleDeferred(() -> {
            Widget panelContent = panel.getWidget();
            int alignRight = relTo.getAbsoluteLeft() + relTo.getOffsetWidth() - panelContent.getOffsetWidth();
            int left = (alignRight - Window.getScrollLeft() < 0 ? relTo.getAbsoluteLeft() - 1 : alignRight + 1);
            int alignBottom = relTo.getAbsoluteTop() + relTo.getOffsetHeight() - panelContent.getOffsetHeight();
            int top = (alignBottom - Window.getScrollTop() < 0 ? relTo.getAbsoluteTop() - 1 : alignBottom + 1);
            panel.setPopupPosition(left, top);
            panel.setVisible(true);
        });
    }

    /**
     * Determine whether a direct link should be/is rendered for the given {@link SimpleRaceMetadataDTO race} instead of
     * a menu popup.
     * 
     * @param data
     *            the {@link SimpleRaceMetadataDTO race} which should be/is rendered
     * @return <code>true</code> if a direct link should be/is rendered, <code>false</code> otherwise
     */
    public <T extends SimpleRaceMetadataDTO> boolean renderAsDirectLink(T data) {
        return !data.isRunning() && !data.isFinished();
    }

    /**
     * Calculates the {@link RenderingStyle} to use for the given {@link SimpleRaceMetadataDTO race}.
     * 
     * @param data
     *            the {@link SimpleRaceMetadataDTO race} to get the {@link RenderingStyle} for
     * @return the calculated {@link RenderingStyle}
     */
    public <T extends SimpleRaceMetadataDTO> RenderingStyle getRenderingStyle(T data) {
        final RenderingStyle result;
        if (data.hasValidTrackingData()) {
            if (renderAsDirectLink(data)) {
                result = RenderingStyle.WATCH_LIVE_ONLY;
            } else {
                result = data.isFinished() ? RenderingStyle.ANALYZE : RenderingStyle.WATCH_LIVE_OR_ANALYZE;
            }
        } else {
            result = RenderingStyle.NOT_TRACKED;
        }
        return result;
    }

}
