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
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.shared.components.Component;

/**
 * A panel that adds user-positioned splitters between each of its child widgets. These splitters have draggers
 * that are designed that way, that they work on touch enabled devices. Splitters can be hidden or shown.
 * Each dragger has a toggle button associated that shows or hides the splitter it is associated to.
 * 
 * <p>
 * Splitters have a {@link Widget} and a {@link Component} associated. The widget is applied to the split panel,
 * the component is being used to determine the visibility.
 * </p>
 * 
 * <p>
 * Make sure that you insert {@link Widget}s only by using {@link #insert(Widget, Component, Direction, double)}.
 * The {@link Widget} provides the title for the toggle button and the {@link Component} provides the visibility.
 * </p>
 * 
 * <p>
 * For each splitter the visibility is determined by checking its {@link Component}s visibility. If the associated
 * component is not visible then the splitter will hide itself.
 * </p>
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
 * <li>.gwt-SplitLayoutPanel .gwt-SplitLayoutPanel-HDragger-Inverted { horizontal dragger }</li>
 * <li>.gwt-SplitLayoutPanel .gwt-SplitLayoutPanel-VDragger { vertical dragger }</li>
 * </ul>
 * 
 * @author Simon Marcel Pamies, Axel Uhl
 */
public class TouchSplitLayoutPanel extends DockLayoutPanel {

    /**
     * A dragger that is connected to a splitter. It captures
     * mouse events and changes the size of the associated
     * {@link Splitter} once the mouse has been released. It is designed
     * in a way to support touch events.
     * 
     * @author Simon Marcel Pamies, Axel Uhl
     */
    public class Dragger extends Widget {
        private final Splitter associatedSplitter;

        public Dragger(Splitter associatedSplitter) {
            this.associatedSplitter = associatedSplitter;
            this.setElement(Document.get().createDivElement());
            // needed to make sure this custom element gets events
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

    /**
     * <p>
     * Splitter that has a {@link Dragger} associated that
     * controls the size of this widget. Each splitter also has a
     * {@link Widget} associated that is under its direct control.
     * A splitter can be hidden or visible depending on the associated
     * {@link Component}s state.
     * </p>
     * 
     * <p>
     * Splitters can also hold a panel that contains {@link Button}s that
     * toggle all Splitters in the same direction. The idea is that if only
     * one splitter is visible one wants to still display all other splitter
     * toggle buttons. The logic to move that panel to the right splitter
     * is contained in {@link TouchSplitLayoutPanel}.
     * </p>
     * 
     * @author Simon Marcel Pamies, Axel Uhl
     */
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
        private Panel toggleButtonsPanel;

        /**
         * A splitter is an {@link AbsolutePanel} that contains a horizontal line and a dragger.
         * 
         * @param reverse if set to true the size change during {@link Dragger} operations will
         *                      be reversed. That means that when dragging to top for a {@link VSplitter}
         *                      the splitters height will get less.
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
            this.splitterToggleButton = new Button(associatedComponent.getLocalizedShortName());
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
            toggleButtonsPanel = panel;
            hasToggleButtonsAssociated = true;
        }
        
        public Panel removeToggleButtons() {
            toggleButtonsPanel.removeFromParent();
            hasToggleButtonsAssociated = false;
            return toggleButtonsPanel;
        }
        
        public Panel getToggleButtonsPanel() {
            return toggleButtonsPanel;
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

        protected void setSplitterSize(double splitterSize) {
            LayoutData layout = (LayoutData)getLayoutData();
            layout.size = splitterSize;
        }

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

        /**
         * Sets the size of the associated {@link Widget} thus moving it down, up, left or right
         * depending of the position. Informs the {@link DockLayoutPanel} about the change.
         * 
         * @param defer if set to true then the layout change will be deferred until the browser
         *                      event loop returns.
         */
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
                // force layout on DockPanel
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

        protected abstract int getAbsolutePosition();
        protected abstract double getCenterSize();
        protected abstract int getEventPosition(Event event);
        protected abstract int getTargetPosition();
        protected abstract int getTargetSize();
        protected abstract int getSplitterSize();
        
    }

    /**
     * Horizontal {@link Splitter} implementation that will only work when
     * set to {@link Direction#EAST} or {@link Direction#WEST}.
     * 
     * @author Simon Marcel Pamies, Axel Uhl
     */
    class HSplitter extends Splitter {
        public HSplitter(Widget target, Component<?> associatedComponent, boolean reverse) {
            super(target, associatedComponent, reverse);
            addStyleName("SplitLayoutPanel-Divider-Horizontal");
            getDragger().getElement().getStyle().setPropertyPx("width", horizontalSplitterSize);
            if(!reverse) {                
                getDragger().setStyleName("gwt-SplitLayoutPanel-HDragger");
            } else {
                getDragger().setStyleName("gwt-SplitLayoutPanel-HDragger-Inverted");
            }
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

    /**
     * Vertical Splitter that will only work when applied to
     * {@link Direction#NORTH} or {@link Direction#SOUTH}.
     * 
     * @author Simon Marcel Pamies, Axel Uhl
     */
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
     * Construct a new {@link TouchSplitLayoutPanel} with the default splitter size of 8px.
     */
    public TouchSplitLayoutPanel() {
        this(DEFAULT_SPLITTER_SIZE, DEFAULT_SPLITTER_SIZE);
    }

    /**
     * Construct a new {@link TouchSplitLayoutPanel} with the specified splitter sizes in pixels.
     * {@link Splitter}s can be of different sizes. 
     * 
     */
    public TouchSplitLayoutPanel(int horizonatalSplitterSize, int verticalSplitterSize) {
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
    /**
     * This method should never be used directly as the implementation requires
     * a Component to be available.
     */
    public void insert(Widget child, Direction direction, double size, Widget before) {
        this.insert(child, null, direction, size);
    }

    /**
     * Inserts a new child to this panel. When using this method make absolutely sure to
     * call {@link #lastComponentHasBeenAdded(SideBySideComponentViewer, AbsolutePanel, List)} after
     * all components have been added.
     * 
     * @param child the child {@link Widget} that will be added
     * @param associatedComponent the {@link Component} that will be used to determine visibility
     * @param size the size of the widget. This will define the size of the split panel.
     */
    public void insert(Widget child, Component<?> associatedComponent, Direction direction, double size) {
        super.insert(child, direction, size, null);
        if (direction != Direction.CENTER) {
            super.insert(child, direction, size, null);
            insertSplitter(child, associatedComponent);
            LayoutData widgetLayoutData = (LayoutData) child.getLayoutData();
            widgetLayoutData.oldSize = size;
        }
    }

    /**
     * <p>
     * This method makes sure to layout the toggle buttons right. It needs to be the last call
     * after all widgets have been added by using {@link #insert(Widget, Component, Direction, double)}.
     * Be aware of the fact that one needs to provide the panel for the horizontal buttons.
     * </p>
     * 
     * <p>
     * With this call one can provide additional vertical toggle buttons that won't be related
     * to any {@link Splitter} but will be under control of the algorithm that makes sure that
     * all toggle buttons are positioned right.
     * </p>
     *  
     * @param cm the component that will be informed about layout changes by calling forceLayout().
     * @param panelForHorizontalButtons this panel assumes that the horizontal buttons will be positioned
     *          outside of the horizontal splitter panel. In most cases one will create such a panel
     *          on top of the widget that has position {@link Direction#CENTER}.
     */
    public void lastComponentHasBeenAdded(final SideBySideComponentViewer cm, AbsolutePanel panelForHorizontalButtons, List<Pair<Button, String>> additionalVerticalButtonsAndStyles) {
        WidgetCollection splitterChildren = super.getChildren();
        HSplitter lastHorizontalSplitter = null;
        VSplitter lastVerticalSplitter = null;
        List<Splitter> allVerticalSplitters = new ArrayList<TouchSplitLayoutPanel.Splitter>();
        List<Splitter> horizontalEastSplitters = new ArrayList<TouchSplitLayoutPanel.Splitter>();
        List<Splitter> horizontalWestSplitters = new ArrayList<TouchSplitLayoutPanel.Splitter>();
        for (Widget widget : splitterChildren) {
            if (widget instanceof VSplitter) {
                allVerticalSplitters.add((VSplitter) widget);
                ((Splitter)widget).setVisible(true);
                ((Splitter)widget).setDraggerVisible(false);
                lastVerticalSplitter = (VSplitter)widget;
            } else if (widget instanceof HSplitter) {
                lastHorizontalSplitter = (HSplitter) widget;
                LayoutData layoutData = (LayoutData) widget.getLayoutData();
                if(layoutData.direction == Direction.EAST) {
                    horizontalEastSplitters.add((HSplitter) widget);
                } else if(layoutData.direction == Direction.WEST) {
                    horizontalWestSplitters.add((HSplitter) widget);
                }
            }
        }
        if (lastVerticalSplitter != null) {
            Panel panel = createToggleButtonPanel(allVerticalSplitters,
                    "gwt-SplitLayoutPanel-NorthSouthToggleButton-Panel", "gwt-SplitLayoutPanel-NorthSouthToggleButton",
                    cm, additionalVerticalButtonsAndStyles); // TODO additional buttons will only be displayed if at least one vertical splitter is... on purpose?
            lastVerticalSplitter.addToogleButtons(panel);
            lastVerticalSplitter.setVisible(true);
            lastVerticalSplitter.setDraggerVisible(false);
        }
        ensureVerticalToggleButtonPosition();
        if (lastHorizontalSplitter != null) {
            if(horizontalEastSplitters.size() > 0) {
                Panel horizontalButtonsPanel = createToggleButtonPanel(horizontalEastSplitters,
                        "gwt-SplitLayoutPanel-WestToggleButton-Panel", "gwt-SplitLayoutPanel-WestToggleButton", cm, /* additional buttons and styles */ null);                
                panelForHorizontalButtons.add(horizontalButtonsPanel);
            }
            if(horizontalWestSplitters.size() > 0) {
                Panel horizontalButtonsPanel = createToggleButtonPanel(horizontalWestSplitters,
                        "gwt-SplitLayoutPanel-EastToggleButton-Panel", "gwt-SplitLayoutPanel-EastToggleButton", cm, /* additional buttons and styles */ null);                
                panelForHorizontalButtons.add(horizontalButtonsPanel);
            }
            lastHorizontalSplitter.setVisible(false);
            lastHorizontalSplitter.setDraggerVisible(false);
        }
    }

    /**
     * Create toggle buttons and their click handlers. The click handlers will show or hide associated
     * {@link Splitter}s including the {@link Component} and {@link Widget}.
     */
    private Panel createToggleButtonPanel(List<Splitter> splitters, String panelStyleName, String buttonStyleName,
            final SideBySideComponentViewer componentViewer, List<Pair<Button, String>> additionalButtonsAndStyles) {
        FlowPanel buttonFlowPanel = new FlowPanel();
        buttonFlowPanel.setStyleName(panelStyleName);
        if (additionalButtonsAndStyles != null) {
            for (Pair<Button, String> buttonAndComponentPair : additionalButtonsAndStyles) {
                Button button = buttonAndComponentPair.getA();
                button.setStyleName(buttonStyleName);
                button.addStyleDependentName("Closed-"+buttonAndComponentPair.getB());
                if (Document.get().getClientWidth() <= 1024) {
                    button.addStyleDependentName("Small-"+buttonAndComponentPair.getB());
                }
                buttonFlowPanel.add(button);
            }
        }
        for (final Splitter splitter : splitters) {
            final Component<?> associatedComponent = splitter.getAssociatedComponent();
            final Button splitterTogglerButton = splitter.getToggleButton();
            splitterTogglerButton.setStyleName(buttonStyleName);
            splitterTogglerButton.addStyleDependentName("Closed");
            splitterTogglerButton.addStyleDependentName("Closed-"+associatedComponent.getDependentCssClassName());
            splitterTogglerButton.ensureDebugId("SplitLayoutPanelToggleButton-" + associatedComponent.getDependentCssClassName());
            splitterTogglerButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    boolean componentWasVisibleUntilNow = associatedComponent.isVisible();
                    associatedComponent.setVisible(!componentWasVisibleUntilNow);
                    // TODO: Safe to remove style management here? Will also be handled by "componentViewer.forceLayout();" => duplicated style management
                    splitter.setVisible(!componentWasVisibleUntilNow);
                    splitter.setDraggerVisible(!componentWasVisibleUntilNow);
                    if (!componentWasVisibleUntilNow == true) {
                        if (associatedComponent instanceof TimeListener) {
                            ((TimeListener) associatedComponent).timeChanged(new Date(), null);
                        }
                    }
                    if (!componentWasVisibleUntilNow) {
                        splitterTogglerButton.removeStyleDependentName("Closed");
                        splitterTogglerButton.removeStyleDependentName("Closed-"+associatedComponent.getDependentCssClassName());
                        splitterTogglerButton.addStyleDependentName("Open");
                        splitterTogglerButton.addStyleDependentName("Open-"+associatedComponent.getDependentCssClassName());
                    } else {
                        splitterTogglerButton.removeStyleDependentName("Open");
                        splitterTogglerButton.removeStyleDependentName("Open-"+associatedComponent.getDependentCssClassName());
                        splitterTogglerButton.addStyleDependentName("Closed");
                        splitterTogglerButton.addStyleDependentName("Closed-"+associatedComponent.getDependentCssClassName());
                    }
                    ensureVerticalToggleButtonPosition();
                    componentViewer.forceLayout();
                }
            });
            buttonFlowPanel.add(splitterTogglerButton);
        }
        return buttonFlowPanel;
    }
    
    /**
     * This method ensures that the toggle buttons always are positioned on top of all split panels. This requires
     * a bit of trickery as there are some edge cases (e.g. when no splitter is visible at all). 
     */
    private void ensureVerticalToggleButtonPosition() {
        List<VSplitter> splitterVisibleAndComponentVisible = new ArrayList<TouchSplitLayoutPanel.VSplitter>();
        List<VSplitter> splitterVisibleButComponentnNotVisible = new ArrayList<TouchSplitLayoutPanel.VSplitter>();
        List<VSplitter> splitterInvisible = new ArrayList<TouchSplitLayoutPanel.VSplitter>();
        VSplitter splitterWithToggleButtons = null;
        for (Widget widget : super.getChildren()) {
            if (widget instanceof VSplitter) {
                VSplitter vSplitter = (VSplitter)widget;
                if (vSplitter.hasToggleButtonsAssociated()) {
                    splitterWithToggleButtons = vSplitter;
                    vSplitter.setVisible(true);
                }
                if (vSplitter.isVisible()) {
                    if (vSplitter.getTarget().isVisible()) {
                        splitterVisibleAndComponentVisible.add(vSplitter);
                    } else {
                        splitterVisibleButComponentnNotVisible.add(vSplitter);
                    }
                } else {
                    splitterInvisible.add(vSplitter);
                }
            }
        }
        if (!splitterVisibleAndComponentVisible.isEmpty()) {
            VSplitter lastSplitterVisibleAndComponentVisible = splitterVisibleAndComponentVisible.get(splitterVisibleAndComponentVisible.size()-1);
            if (!lastSplitterVisibleAndComponentVisible.hasToggleButtonsAssociated()) {
                Panel panel = splitterWithToggleButtons.removeToggleButtons();
                lastSplitterVisibleAndComponentVisible.addToogleButtons(panel);
                lastSplitterVisibleAndComponentVisible.setSplitterSize(verticalSplitterSize);
            }
        } else {
            if (!splitterVisibleButComponentnNotVisible.isEmpty()) {
                // there is no splitter that has a component visible
                // take the last one and add buttons there
                VSplitter lastSplitterVisibleButComponentNotVisible = splitterVisibleButComponentnNotVisible.get(splitterVisibleButComponentnNotVisible.size()-1);
                if (!lastSplitterVisibleButComponentNotVisible.hasToggleButtonsAssociated()) {
                    Panel panel = splitterWithToggleButtons.removeToggleButtons();
                    lastSplitterVisibleButComponentNotVisible.addToogleButtons(panel);
                    lastSplitterVisibleButComponentNotVisible.setSplitterSize(verticalSplitterSize);
                }
            }
        }
        for (VSplitter vsplitterThatHasNoComponentVisible : splitterVisibleButComponentnNotVisible) {
            if (!vsplitterThatHasNoComponentVisible.hasToggleButtonsAssociated()) {
                vsplitterThatHasNoComponentVisible.setSplitterSize(0.0);
            } else {
                vsplitterThatHasNoComponentVisible.setSplitterSize((double)verticalSplitterSize);
            }
        }
        for (VSplitter vSplitterWithComponentVisible : splitterVisibleAndComponentVisible) {
            vSplitterWithComponentVisible.setSplitterSize((double)verticalSplitterSize);
        }
        if (!splitterVisibleAndComponentVisible.isEmpty()) {
            for (VSplitter vSplitterInvisible : splitterInvisible) {
                vSplitterInvisible.setSplitterSize(0.0);
            }
        } else {
            // special case related to css issues: when all components are invisible
            // then we need to at least show the splitter containers to get the right height
            for (VSplitter vSplitterInvisible : splitterVisibleButComponentnNotVisible) {
                if (vSplitterInvisible.hasToggleButtonsAssociated()) {
                    vSplitterInvisible.setSplitterSize(verticalSplitterSize*2);
                } else {
                    vSplitterInvisible.setSplitterSize(0);
                }
            }
            
        }
        forceLayout();
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

    /**
     * <p>
     * Sets the widget visibility and makes sure to also hide or show associated
     * {@link Splitter}s and their {@link Dragger}s. Also control the associated
     * toggle button by setting the correct style name.
     * </p>
     * 
     * <p>
     * As this method has no knowledge about the connection between a {@link Widget} and
     * a {@link Component} the component needs to be provided.
     * </p>
     * 
     * @param hidden set this to true if the panel should be hidden
     * @param initialSize the size of the panel. only required to be valid if not hidden.
     */
    public void setWidgetVisibility(Widget widget, Component<?> associatedComponentToWidget,
            final boolean hidden, final int initialSize) {
        super.setWidgetHidden(widget, hidden);
        final Splitter splitter = getAssociatedSplitter(widget);
        if (splitter != null) {
            LayoutData layoutData = (LayoutData) widget.getLayoutData();
            if (hidden) {
                if(layoutData.size > 0) {
                    layoutData.oldSize = layoutData.size;
                }
                widget.setVisible(false);
                if (associatedComponentToWidget != null) {
                    if (associatedComponentToWidget.isVisible()) {
                        associatedComponentToWidget.setVisible(false);
                    }
                }
                splitter.setAssociatedWidgetSize(0, /* defer */false);

                // need to ensure visibility of dragger, do NOT use setVisible() as south/north splitter should be shown always
                splitter.setDraggerVisible(!hidden);
            } else {
                widget.setVisible(true);
                if (associatedComponentToWidget != null) {
                    if (!associatedComponentToWidget.isVisible()) {
                        associatedComponentToWidget.setVisible(true);
                    }
                }
                if (layoutData.size <= 0) {
                    //it is not yet sized
                    if(layoutData.oldSize > 0) {
                        //and we have a stored old size it had before minimizing it
                        splitter.setAssociatedWidgetSize(layoutData.oldSize, /* defer */false);
                    } else {
                        //and we do not have any size yet, so use the default size
                        splitter.setAssociatedWidgetSize(initialSize, /* defer */false);
                    }
                }
                splitter.setDraggerVisible(!hidden);
                splitter.setVisible(!hidden);
                splitter.getToggleButton().removeStyleDependentName("Closed");
                splitter.getToggleButton().removeStyleDependentName("Closed-"+associatedComponentToWidget.getDependentCssClassName());
                splitter.getToggleButton().addStyleDependentName("Open");
                splitter.getToggleButton().addStyleDependentName("Open-"+associatedComponentToWidget.getDependentCssClassName());
            }
        }
    }
    
    /**
     * Sets the size of the given {@link Widget} if it is {@link Widget#isVisible() visible}. Otherwise, the given size
     * will be persisted as the widgets {@link LayoutData#oldSize old size}.
     * 
     * @param widget the {@link Widget} to resize
     * @param size the new size
     */
    public void setWidgetSize(Widget widget, int size) {
        final Splitter splitter = getAssociatedSplitter(widget);
        if (splitter != null) {
            if (widget.isVisible()) {
                splitter.setAssociatedWidgetSize(size, /* defer */false);
            } else {
                ((LayoutData) widget.getLayoutData()).oldSize = size;
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

    private Splitter insertSplitter(Widget widget, Component<?> associatedComponent) {
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
        super.insert(splitter, layout.direction, splitter.getSplitterSize(), null);
        // this is needed to make the Draggers visible
        ((LayoutData) splitter.getLayoutData()).layer.getContainerElement().getStyle().setOverflow(Overflow.VISIBLE);
        return splitter;
    }
}
