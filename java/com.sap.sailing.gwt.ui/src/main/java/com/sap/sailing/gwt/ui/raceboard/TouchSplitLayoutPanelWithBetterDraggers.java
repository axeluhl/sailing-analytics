/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sse.gwt.client.player.TimeListener;

/**
 * A panel that adds user-positioned splitters between each of its child widgets.
 * 
 * <p>
 * This panel is used in the same way as {@link DockLayoutPanel}, except that its children's sizes are always specified
 * in {@link Unit#PX} units, and each pair of child widgets has a splitter between them that the user can drag.
 * </p>
 * 
 * <p>
 * This widget will <em>only</em> work in standards mode, which requires that the HTML page in which it is run have an
 * explicit &lt;!DOCTYPE&gt; declaration.
 * </p>
 * 
 * <h3>CSS Style Rules</h3>
 * <ul class='css'>
 * <li>.gwt-SplitLayoutPanel { the panel itself }</li>
 * <li>.gwt-SplitLayoutPanel .gwt-SplitLayoutPanel-HDragger { horizontal dragger }</li>
 * <li>.gwt-SplitLayoutPanel .gwt-SplitLayoutPanel-VDragger { vertical dragger }</li>
 * </ul>
 * 
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.SplitLayoutPanelExample}
 * </p>
 */
public class TouchSplitLayoutPanelWithBetterDraggers extends DockLayoutPanel {

    public class Dragger extends Widget {
        private final Splitter associatedSplitter;

        public Dragger(Splitter associatedSplitter) {
            this.associatedSplitter = associatedSplitter;
            this.setElement(Document.get().createDivElement());
            this.sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE | Event.ONDBLCLICK
                    | Event.ONTOUCHSTART | Event.ONTOUCHEND | Event.ONTOUCHMOVE);
        }

        @Override
        public void onBrowserEvent(Event event) {
            switch (event.getTypeInt()) {
            case Event.ONMOUSEDOWN:
            case Event.ONTOUCHSTART:
                associatedSplitter.mouseDown = true;

                /*
                 * Resize glassElem to take up the entire scrollable window area, which is the greater of the scroll
                 * size and the client size.
                 */
                int width = Math.max(Window.getClientWidth(), Document.get().getScrollWidth());
                int height = Math.max(Window.getClientHeight(), Document.get().getScrollHeight());
                glassElem.getStyle().setHeight(height, Unit.PX);
                glassElem.getStyle().setWidth(width, Unit.PX);
                Document.get().getBody().appendChild(glassElem);

                associatedSplitter.offset = associatedSplitter.getEventPosition(event)
                        - associatedSplitter.getAbsolutePosition();
                Event.setCapture(getElement());
                event.preventDefault();
                break;

            case Event.ONMOUSEUP:
            case Event.ONTOUCHEND:
                associatedSplitter.mouseDown = false;

                glassElem.removeFromParent();

                // Handle double-clicks.
                // Fake them since the double-click event aren't fired.
                if (associatedSplitter.toggleDisplayAllowed) {
                    double now = Duration.currentTimeMillis();
                    if (now - associatedSplitter.lastClick < DOUBLE_CLICK_TIMEOUT) {
                        now = 0;
                        LayoutData layout = (LayoutData) associatedSplitter.target.getLayoutData();
                        if (layout.size == 0) {
                            // Restore the old size.
                            associatedSplitter.setAssociatedWidgetSize(layout.oldSize, /* defer */true);
                        } else {
                            /*
                             * Collapse to size 0. We change the size instead of hiding the widget because hiding the
                             * widget can cause issues if the widget contains a flash component.
                             */
                            layout.oldSize = layout.size;
                            associatedSplitter.setAssociatedWidgetSize(0, /* defer */true);
                        }
                    }
                    associatedSplitter.lastClick = now;
                }

                Event.releaseCapture(getElement());
                event.preventDefault();
                break;

            case Event.ONMOUSEMOVE:
            case Event.ONTOUCHMOVE:
                if (associatedSplitter.mouseDown) {
                    int size;
                    if (associatedSplitter.reverse) {
                        size = associatedSplitter.getTargetPosition() + associatedSplitter.getTargetSize()
                                - associatedSplitter.getSplitterSize() - associatedSplitter.getEventPosition(event)
                                + associatedSplitter.offset;
                    } else {
                        size = associatedSplitter.getEventPosition(event) - associatedSplitter.getTargetPosition()
                                - associatedSplitter.offset;
                    }
                    ((LayoutData) associatedSplitter.target.getLayoutData()).hidden = false;
                    associatedSplitter.setAssociatedWidgetSize(size, /* defer */true);
                    event.preventDefault();
                }
                break;
            }
        }
    }

    abstract class Splitter extends AbsolutePanel {
        protected final Widget target;

        private int offset;
        private boolean mouseDown;
        private ScheduledCommand layoutCommand;

        private final boolean reverse;
        private int minSize;
        private int snapClosedSize = -1;
        private double centerSize, syncedCenterSize;

        private boolean toggleDisplayAllowed = false;
        private double lastClick = 0;

        private final Component<?> associatedComponent;
        protected Button togglerButton;
        private final Widget dragger;

        private boolean hasToggleButtonsAssociated;
        private final Button splitterToggleButton;

        /**
         * A splitter is an {@link AbsolutePanel} that contains a horizontal line and a dragger
         */
        public Splitter(Widget target, Component<?> associatedComponent, boolean reverse) {
            super();

            getElement().getStyle().setOverflow(Overflow.VISIBLE);

            // add dragger - style will be set by implementing classes
            dragger = new Dragger(this);
            add(dragger);

            this.target = target;
            this.reverse = reverse;
            this.associatedComponent = associatedComponent;
            this.hasToggleButtonsAssociated = false;
            this.splitterToggleButton = new Button(target.getTitle());
        }

        public Component<?> getAssociatedComponent() {
            return associatedComponent;
        }

        public Button getToggleButton() {
            return this.splitterToggleButton;
        }

        public Widget getTarget() {
            return this.target;
        }

        public void setDraggerVisible(boolean visible) {
            this.dragger.setVisible(visible);
        }

        public Widget getDragger() {
            return this.dragger;
        }

        public void addToogleButtons(Panel panel) {
            add(panel);
            hasToggleButtonsAssociated = true;
        }

        public boolean hasToggleButtonsAssociated() {
            return this.hasToggleButtonsAssociated;
        }

        public void setMinSize(int minSize) {
            this.minSize = minSize;
            LayoutData layout = (LayoutData) target.getLayoutData();

            // Try resetting the associated widget's size, which will enforce the new
            // minSize value.
            setAssociatedWidgetSize((int) layout.size, /* defer */true);
        }

        public void setSnapClosedSize(int snapClosedSize) {
            this.snapClosedSize = snapClosedSize;
        }

        public void setToggleDisplayAllowed(boolean allowed) {
            this.toggleDisplayAllowed = allowed;
        }

        protected abstract int getAbsolutePosition();

        protected abstract double getCenterSize();

        protected abstract int getEventPosition(Event event);

        protected abstract int getTargetPosition();

        protected abstract int getTargetSize();

        protected abstract int getSplitterSize();

        private double getMaxSize() {
            // To avoid seeing stale center size values due to deferred layout
            // updates, maintain our own copy up to date and resync when the
            // DockLayoutPanel value changes.
            double newCenterSize = getCenterSize();
            if (syncedCenterSize != newCenterSize) {
                syncedCenterSize = newCenterSize;
                centerSize = newCenterSize;
            }

            return Math.max(((LayoutData) target.getLayoutData()).size + centerSize, 0);
        }

        private void setAssociatedWidgetSize(double size, boolean defer) {
            double maxSize = getMaxSize();
            if (size > maxSize) {
                size = maxSize;
            }

            if (snapClosedSize > 0 && size < snapClosedSize) {
                size = 0;
            } else if (size < minSize) {
                size = minSize;
            }

            LayoutData layout = (LayoutData) target.getLayoutData();
            if (size == layout.size) {
                return;
            }

            // Adjust our view until the deferred layout gets scheduled.
            centerSize += layout.size - size;
            layout.size = size;

            // Defer actually updating the layout, so that if we receive many
            // mouse events before layout/paint occurs, we'll only update once.
            if (layoutCommand == null && defer) {
                layoutCommand = new ScheduledCommand() {
                    @Override
                    public void execute() {
                        layoutCommand = null;
                        forceLayout();
                    }
                };
                Scheduler.get().scheduleDeferred(layoutCommand);
            } else {
                forceLayout();
            }
            if (getAssociatedComponent() instanceof RequiresResize) {
                Scheduler.get().scheduleFinally(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        ((RequiresResize) getAssociatedComponent()).onResize();
                    }
                });
            }
        }
    }

    class HSplitter extends Splitter {
        public HSplitter(Widget target, Component<?> associatedComponent, boolean reverse) {
            super(target, associatedComponent, reverse);
            addStyleName("SplitLayoutPanel-Divider-Horizontal");
            getDragger().getElement().getStyle().setPropertyPx("width", horizontalSplitterSize);
            getDragger().setStyleName("gwt-SplitLayoutPanel-HDragger");
        }

        @Override
        protected int getAbsolutePosition() {
            return getAbsoluteLeft();
        }

        @Override
        protected double getCenterSize() {
            return getCenterWidth();
        }

        @Override
        protected int getEventPosition(Event event) {
            JsArray<Touch> touches = event.getTouches();
            if (touches != null) {
                Touch touch = touches.get(0).cast();
                if (touch != null) {
                    return touch.getClientX();
                }
            }
            return event.getClientX();
        }

        @Override
        protected int getTargetPosition() {
            return target.getAbsoluteLeft();
        }

        @Override
        protected int getTargetSize() {
            return target.getOffsetWidth();
        }

        @Override
        protected int getSplitterSize() {
            return horizontalSplitterSize;
        }
    }

    class VSplitter extends Splitter {
        public VSplitter(final Widget target, final Component<?> associatedComponent, boolean reverse) {
            super(target, associatedComponent, reverse);
            addStyleName("SplitLayoutPanel-Divider-Vertical");
            getDragger().getElement().getStyle().setPropertyPx("height", verticalSplitterSize);
            getDragger().setStyleName("gwt-SplitLayoutPanel-VDragger");
        }

        @Override
        protected int getAbsolutePosition() {
            return getAbsoluteTop();
        }

        @Override
        protected double getCenterSize() {
            return getCenterHeight();
        }

        @Override
        protected int getEventPosition(Event event) {
            JsArray<Touch> touches = event.getTouches();
            if (touches != null) {
                Touch touch = touches.get(0).cast();
                if (touch != null) {
                    return touch.getClientY();
                }
            }
            return event.getClientY();
        }

        @Override
        protected int getTargetPosition() {
            return target.getAbsoluteTop();
        }

        @Override
        protected int getTargetSize() {
            return target.getOffsetHeight();
        }

        @Override
        protected int getSplitterSize() {
            return verticalSplitterSize;
        }
    }

    private static final int DEFAULT_SPLITTER_SIZE = 8;
    private static final int DOUBLE_CLICK_TIMEOUT = 500;

    /**
     * The element that masks the screen so we can catch mouse events over iframes.
     */
    private static Element glassElem = null;

    private final int horizontalSplitterSize;
    private final int verticalSplitterSize;

    /**
     * Construct a new {@link TouchSplitLayoutPanelWithBetterDraggers} with the default splitter size of 8px.
     */
    public TouchSplitLayoutPanelWithBetterDraggers() {
        this(DEFAULT_SPLITTER_SIZE, DEFAULT_SPLITTER_SIZE);
    }

    /**
     * Construct a new {@link TouchSplitLayoutPanelWithBetterDraggers} with the specified splitter size in pixels.
     * 
     * @param splitterSize
     *            the size of the splitter in pixels
     */
    public TouchSplitLayoutPanelWithBetterDraggers(int horizonatalSplitterSize, int verticalSplitterSize) {
        super(Unit.PX);
        this.horizontalSplitterSize = horizonatalSplitterSize;
        this.verticalSplitterSize = verticalSplitterSize;
        setStyleName("gwt-SplitLayoutPanel");

        if (glassElem == null) {
            glassElem = Document.get().createDivElement();
            glassElem.getStyle().setPosition(Position.ABSOLUTE);
            glassElem.getStyle().setTop(0, Unit.PX);
            glassElem.getStyle().setLeft(0, Unit.PX);
            glassElem.getStyle().setMargin(0, Unit.PX);
            glassElem.getStyle().setPadding(0, Unit.PX);
            glassElem.getStyle().setBorderWidth(0, Unit.PX);

            // We need to set the background color or mouse events will go right
            // through the glassElem. If the SplitPanel contains an iframe, the
            // iframe will capture the event and the slider will stop moving.
            glassElem.getStyle().setProperty("background", "white");
            glassElem.getStyle().setOpacity(0.0);
        }
    }

    /**
     * Return the size of the splitter in pixels.
     * 
     * @return the splitter size
     */
    public int getHorizontalSplitterSize() {
        return horizontalSplitterSize;
    }

    public int getVerticalSplitterSize() {
        return verticalSplitterSize;
    }

    @Override
    public void insert(Widget child, Direction direction, double size, Widget before) {
        this.insert(child, null, direction, size, before);
    }

    public void insert(Widget child, Component<?> associatedComponent, Direction direction, double size, Widget before) {
        super.insert(child, direction, size, before);
        if (direction != Direction.CENTER) {
            super.insert(child, direction, size, before);
            insertSplitter(child, before, associatedComponent);
            LayoutData widgetLayoutData = (LayoutData) child.getLayoutData();
            widgetLayoutData.oldSize = size;
        }
    }

    public void lastComponentHasBeenAdded(final SideBySideComponentViewer cm, AbsolutePanel panelForAdditionalButtons) {
        WidgetCollection splitterChildren = super.getChildren();
        VSplitter lastVerticalSplitter = null;
        HSplitter lastHorizontalSplitter = null;
        List<Splitter> allVerticalSplitters = new ArrayList<TouchSplitLayoutPanelWithBetterDraggers.Splitter>();
        List<Splitter> allHorizontalSplitters = new ArrayList<TouchSplitLayoutPanelWithBetterDraggers.Splitter>();
        for (Widget widget : splitterChildren) {
            if (widget instanceof VSplitter) {
                lastVerticalSplitter = (VSplitter) widget;
                allVerticalSplitters.add((VSplitter) widget);
                ((Splitter)widget).setVisible(false);
                ((Splitter)widget).setDraggerVisible(false);
            } else if (widget instanceof HSplitter) {
                lastHorizontalSplitter = (HSplitter) widget;
                allHorizontalSplitters.add((HSplitter) widget);
            }
        }
        if (lastVerticalSplitter != null) {
            Panel panel = addSplitterToggleButtons(allVerticalSplitters,
                    "gwt-SplitLayoutPanel-NorthSouthToggleButton-Panel", "gwt-SplitLayoutPanel-NorthSouthToggleButton",
                    cm);
            lastVerticalSplitter.addToogleButtons(panel);
            lastVerticalSplitter.setVisible(true);
            lastVerticalSplitter.setDraggerVisible(false);
        }
        if (lastHorizontalSplitter != null) {
            Panel horizontalButtonsPanel = addSplitterToggleButtons(allHorizontalSplitters,
                    "gwt-SplitLayoutPanel-EastToggleButton-Panel", "gwt-SplitLayoutPanel-EastToggleButton", cm);
            panelForAdditionalButtons.add(horizontalButtonsPanel);
            lastHorizontalSplitter.setVisible(false);
            lastHorizontalSplitter.setDraggerVisible(false);
        }
    }

    private Panel addSplitterToggleButtons(List<Splitter> splitters, String panelStyleName, String buttonStyleName,
            final SideBySideComponentViewer componentViewer) {
        FlowPanel buttonFlowPanel = new FlowPanel();
        buttonFlowPanel.setStyleName(panelStyleName);
        for (final Splitter splitter : splitters) {
            final Button splitterTogglerButton = splitter.getToggleButton();
            splitterTogglerButton.setStyleName(buttonStyleName);
            splitterTogglerButton.addStyleDependentName("Closed");
            splitterTogglerButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    boolean componentIsVisible = splitter.getAssociatedComponent().isVisible();
                    splitter.getAssociatedComponent().setVisible(!componentIsVisible);
                    if (!splitter.hasToggleButtonsAssociated) {
                        splitter.setVisible(!componentIsVisible);
                    }
                    splitter.setDraggerVisible(!componentIsVisible);
                    if (!componentIsVisible == true) {
                        if (splitter.getAssociatedComponent() instanceof TimeListener) {
                            ((TimeListener) splitter.getAssociatedComponent()).timeChanged(new Date(), null);
                        }
                    }
                    if (!componentIsVisible) {
                        splitterTogglerButton.removeStyleDependentName("Closed");
                        splitterTogglerButton.addStyleDependentName("Open");
                    } else {
                        splitterTogglerButton.removeStyleDependentName("Open");
                        splitterTogglerButton.addStyleDependentName("Closed");
                    }
                    componentViewer.forceLayout();
                }
            });
            buttonFlowPanel.add(splitterTogglerButton);
        }
        return buttonFlowPanel;
    }

    public boolean hidePanelContaining(Widget child) {
        int idx = getWidgetIndex(child);
        if (idx >= 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Widget child) {
        assert !(child instanceof Splitter) : "Splitters may not be directly removed";

        int idx = getWidgetIndex(child);
        if (super.remove(child)) {
            // Remove the associated splitter, if any.
            // Now that the widget is removed, idx is the index of the splitter.
            if (idx < getWidgetCount()) {
                // Call super.remove(), or we'll end up recursing.
                super.remove(getWidget(idx));
            }
            return true;
        }
        return false;
    }

    @Override
    public void setWidgetHidden(Widget widget, boolean hidden) {
        super.setWidgetHidden(widget, hidden);
        Splitter splitter = getAssociatedSplitter(widget);
        if (splitter != null) {
            // The splitter is null for the center element.
            super.setWidgetHidden(splitter, hidden);
        }
    }

    public void setWidgetVisibility(Widget widget, Component<?> associatedComponentToWidget,
            final Widget widgetThatDeterminesSize, final boolean hidden, final int size) {
        super.setWidgetHidden(widget, hidden);
        final Splitter splitter = getAssociatedSplitter(widget);
        if (splitter != null) {
            LayoutData layoutData = (LayoutData) widget.getLayoutData();
            if (hidden) {
                layoutData.oldSize = size;
                widget.setVisible(false);
                if (associatedComponentToWidget != null) {
                    if (associatedComponentToWidget.isVisible()) {
                        associatedComponentToWidget.setVisible(false);
                    }
                }
                splitter.setAssociatedWidgetSize(0, /* defer */false);
            } else {
                widget.setVisible(true);
                if (associatedComponentToWidget != null) {
                    if (!associatedComponentToWidget.isVisible()) {
                        associatedComponentToWidget.setVisible(true);
                    }
                }
                splitter.setAssociatedWidgetSize(size, /* defer */false);
                splitter.setDraggerVisible(!hidden);
                splitter.setVisible(!hidden);
                splitter.getToggleButton().removeStyleDependentName("Closed");
                splitter.getToggleButton().addStyleDependentName("Open");
            }
        }
        if (splitter != null && !hidden && splitter instanceof HSplitter) {
            int widthOfWidget = widgetThatDeterminesSize.getOffsetWidth();
            if (size > widthOfWidget && widthOfWidget > 0) {
                int additionalWidthForScroller = 8;
                splitter.setAssociatedWidgetSize(widthOfWidget+additionalWidthForScroller, /* defer */false);
            }
        }
    }

    private void assertIsChild2(Widget widget) {
        assert (widget == null) || (widget.getParent() == this) : "The specified widget is not a child of this panel";
    }

    /**
     * Sets the minimum allowable size for the given widget.
     * 
     * <p>
     * Its associated splitter cannot be dragged to a position that would make it smaller than this size. This method
     * has no effect for the {@link DockLayoutPanel.Direction#CENTER} widget.
     * </p>
     * 
     * @param child
     *            the child whose minimum size will be set
     * @param minSize
     *            the minimum size for this widget
     */
    public void setWidgetMinSize(Widget child, int minSize) {
        assertIsChild2(child);
        Splitter splitter = getAssociatedSplitter(child);
        // The splitter is null for the center element.
        if (splitter != null) {
            splitter.setMinSize(minSize);
        }
    }
    
    /**
     * Sets a size below which the slider will close completely. This can be used in conjunction with
     * {@link #setWidgetMinSize} to provide a speed-bump effect where the slider will stick to a preferred minimum size
     * before closing completely.
     * 
     * <p>
     * This method has no effect for the {@link DockLayoutPanel.Direction#CENTER} widget.
     * </p>
     * 
     * @param child
     *            the child whose slider should snap closed
     * @param snapClosedSize
     *            the width below which the widget will close or -1 to disable.
     */
    public void setWidgetSnapClosedSize(Widget child, int snapClosedSize) {
        assertIsChild2(child);
        Splitter splitter = getAssociatedSplitter(child);
        // The splitter is null for the center element.
        if (splitter != null) {
            splitter.setSnapClosedSize(snapClosedSize);
        }
    }

    /**
     * Sets whether or not double-clicking on the splitter should toggle the display of the widget.
     * 
     * @param child
     *            the child whose display toggling will be allowed or not.
     * @param allowed
     *            whether or not display toggling is allowed for this widget
     */
    public void setWidgetToggleDisplayAllowed(Widget child, boolean allowed) {
        assertIsChild2(child);
        Splitter splitter = getAssociatedSplitter(child);
        // The splitter is null for the center element.
        if (splitter != null) {
            splitter.setToggleDisplayAllowed(allowed);
        }
    }

    public Splitter getAssociatedSplitter(Widget child) {
        // If a widget has a next sibling, it must be a splitter, because the only
        // widget that *isn't* followed by a splitter must be the CENTER, which has
        // no associated splitter.
        int idx = getWidgetIndex(child);
        if (idx > -1 && idx < getWidgetCount() - 1) {
            Widget splitter = getWidget(idx + 1);
            assert splitter instanceof Splitter : "Expected child widget to be splitter";
            return (Splitter) splitter;
        }
        return null;
    }

    private Splitter insertSplitter(Widget widget, Widget before, Component<?> associatedComponent) {
        assert getChildren().size() > 0 : "Can't add a splitter before any children";

        LayoutData layout = (LayoutData) widget.getLayoutData();
        Splitter splitter = null;
        switch (getResolvedDirection(layout.direction)) {
        case WEST:
            splitter = new HSplitter(widget, associatedComponent, false);
            break;
        case EAST:
            splitter = new HSplitter(widget, associatedComponent, true);
            break;
        case NORTH:
            splitter = new VSplitter(widget, associatedComponent, false);
            break;
        case SOUTH:
            splitter = new VSplitter(widget, associatedComponent, true);
            break;
        default:
            assert false : "Unexpected direction";
        }
        super.insert(splitter, layout.direction, splitter.getSplitterSize(), before);
        ((LayoutData) splitter.getLayoutData()).layer.getContainerElement().getStyle().setOverflow(Overflow.VISIBLE);
        return splitter;
    }
}
