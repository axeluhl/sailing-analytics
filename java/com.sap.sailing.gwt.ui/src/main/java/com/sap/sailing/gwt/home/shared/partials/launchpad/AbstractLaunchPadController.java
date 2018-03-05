package com.sap.sailing.gwt.home.shared.partials.launchpad;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract {@link LaunchPadController} implementation providing common functionality for a specific type of data.
 *
 * @param <D>
 *            the actual data type
 */
public abstract class AbstractLaunchPadController<D> implements LaunchPadController<D> {

    private final PopupPanel panel = new PopupPanel(true, false);
    private final BiFunction<D, PopupPanel, Widget> launchPadFactory;

    /**
     * Creates a new {@link AbstractLaunchPadController} using the provided {@link Function factory} to create the
     * {@link Widget launch pad}.
     * 
     * @param launchPadFactory
     *            the {@link Function factory} to create the launch pad
     */
    protected AbstractLaunchPadController(BiFunction<D, PopupPanel, Widget> launchPadFactory) {
        this.launchPadFactory = launchPadFactory;
    }

    @Override
    public final void showLaunchPad(D data, Element relTo) {
        relTo.scrollIntoView();
        panel.setWidget(launchPadFactory.apply(data, panel));
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

}
