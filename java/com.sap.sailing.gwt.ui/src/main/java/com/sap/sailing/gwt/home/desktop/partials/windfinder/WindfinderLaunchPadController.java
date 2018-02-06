package com.sap.sailing.gwt.home.desktop.partials.windfinder;

import java.util.Collection;
import java.util.function.BiFunction;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

class WindfinderLaunchPadController {

    private final PopupPanel panel = new PopupPanel(true, false);
    private final BiFunction<SpotDTO, TimePoint, String> windfinderUrlFactory;

    /**
     * Creates a new {@link WindfinderLaunchPadController} instance with the given URL factory.
     * 
     * @param raceviewerUrlFactory
     *            {@link BiFunction Factory method} to get the windfinder URL for given {@link SpotDTO spot} and
     *            {@link TimePoint time point} information
     */
    WindfinderLaunchPadController(BiFunction<SpotDTO, TimePoint, String> windfinderUrlFactory) {
        this.windfinderUrlFactory = windfinderUrlFactory;
    }

    /**
     * Provides the direct link URL for the given {@link SpotDTO spot}.
     * 
     * @param data
     *            the {@link SpotDTO spot} to get the link URL for
     * @return the direct link URL (as {@link String})
     */
    String getDirectLinkUrl(Iterable<SpotDTO> data) {
        assert renderAsDirectLink(data);
        final SpotDTO spot = data.iterator().next();
        return windfinderUrlFactory.apply(spot, MillisecondsTimePoint.now());
    }

    /**
     * Shows a {@link WindfinderLaunchPad} for the given {@link SpotDTO spot}s relative to the also provided element.
     * 
     * @param data
     *            {@link Collection} of {@link SpotDTO spot}s to create a launch pad for
     * @param relTo
     *            the {@link Element element} to show the lauOnch pad relative to
     */
    void showWindfinderLaunchPad(Iterable<SpotDTO> data, Element relTo) {
        relTo.scrollIntoView();
        panel.setWidget(new WindfinderLaunchPad(data, windfinderUrlFactory, panel));
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
     * Determine whether a direct link should be/is rendered for the given {@link SpotDTO spot}s instead of a menu
     * popup.
     * 
     * @param data
     *            {@link Collection} of the {@link SpotDTO spot}s which should be/are rendered
     * @return <code>true</code> if a direct link should be/is rendered, <code>false</code> otherwise
     */
    boolean renderAsDirectLink(Iterable<SpotDTO> data) {
        return Util.size(data) == 1;
    }

}
