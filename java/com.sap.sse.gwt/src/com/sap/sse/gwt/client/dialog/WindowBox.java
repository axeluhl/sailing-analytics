package com.sap.sse.gwt.client.dialog;

/*
 * Copyright 2010 Traction Software, Inc.
 * Copyright 2010 clazzes.org Project
 * 
 * Based on TractionDialogBox by Traction Software, Inc. Renamed to WindowBox and 
 * added resize support by clazzes.org 
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
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Extension of the standard GWT DialogBox to provide a more "window"-like functionality. The WindowBox has two
 * control-buttons in the top right corner of the header, which allow the box to be turned into a dedicated browser
 * window ("popout") or the whole box to be closed. <br>
 * <br>
 * The WindowBox relies on the css settings of {@link DialogBox} for styling of the border and header. It also uses the
 * following classes to style the additional elements:
 * 
 * <pre>
 *  .gwt-extras-WindowBox
 *      the box itself
 *  .gwt-extras-WindowBox .gwt-extras-dialog-container
 *      the div holding the contents of the box
 *  .gwt-extras-WindowBox .gwt-extras-dialog-controls
 *      the div holding the window-controls - PLEASE NOTE: on the DOM-tree, this div is located inside the center-center
 *      cell of the windowBox table, not in the top-center (where the header-text is). Therefore the css has a negative
 *      top-value to position the controls on the header 
 *  .gwt-extras-WindowBox .gwt-extras-dialog-controls a.gwt-extras-dialog-close
 *  .gwt-extras-WindowBox .gwt-extras-dialog-controls a.gwt-extras-dialog-close:hover
 *  .gwt-extras-WindowBox .gwt-extras-dialog-controls a.gwt-extras-dialog-minimize
 *  .gwt-extras-WindowBox .gwt-extras-dialog-controls a.gwt-extras-dialog-minimize:hover
 *  .gwt-extras-WindowBox .gwt-extras-dialog-controls a.gwt-extras-dialog-maximize
 *  .gwt-extras-WindowBox .gwt-extras-dialog-controls a.gwt-extras-dialog-maximize:hover
 *      the controls in the header. A background image sprite is used to create the mouseover- and clicking-effects.
 *      When the window is minimized, the style-name of the corresponding control changes to "gwt-extras-dialog-maximize"
 *      and vice-versa
 * </pre>
 */
public class WindowBox extends DialogBox {
    /**
     * A {@link ClientBundle} that provides css resources for {@link WindowBox}.
     */
    public static interface WindowBoxResources extends ClientBundle {
        public static final WindowBoxResources INSTANCE = GWT.create(WindowBoxResources.class);

        @NotStrict
        @Source("WindowBox.css")
        CssResource windowBoxCss();
    }

    public interface PopoutHandler {
        void popout();
    }

    private static final int MIN_WIDTH = 50;
    private static final int MIN_HEIGHT = 50;

    private FlowPanel container;
    private FlowPanel controlsPanel;
    private Anchor close;
    private Anchor minimize;

    private int dragX;
    private int dragY;

    private int minWidth = MIN_WIDTH;
    private int minHeight = MIN_HEIGHT;

    private enum DragMode {
        NORTH_WEST_RESIZE(true, false, true, false), SOUTH_EAST_RESIZE(false, true, false, true), NORTH_RESIZE(true,
                false, false, false), NORTH_EAST_RESIZE(true, false, false, true), WEST_RESIZE(false, false, true,
                        false), EAST_RESIZE(false, false, false, true), SOUTH_WEST_RESIZE(false, true, true,
                                false), SOUTH_RESIZE(false, true, false, false), MOVE(false, false, false, false);
        private boolean north;
        private boolean south;
        private boolean west;
        private boolean east;

        DragMode(boolean north, boolean south, boolean west, boolean east) {
            this.north = north;
            this.south = south;
            this.west = west;
            this.east = east;
        }

        public boolean isNorth() {
            return north;
        }

        public boolean isSouth() {
            return south;
        }

        public boolean isWest() {
            return west;
        }

        public boolean isEast() {
            return east;
        }
    }

    private DragMode dragMode;
    private PopoutHandler popoutHandler;
    private FlowPanel blocker;
    private Grid ctrlGrid;

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    /**
     * Static helper method to change the cursor for a given element when resizing is enabled.
     * 
     * @param dragMode
     *            The code describing the position of the element in question
     * @param element
     *            The {@link com.google.gwt.dom.client.Element} to set the cursor on
     */
    protected void updateCursor(DragMode dragMode, com.google.gwt.dom.client.Element element) {
        Cursor cursor;

        if (dragMode != null)
            switch (dragMode) {
            case MOVE:
                cursor = Cursor.MOVE;
                break;

            case NORTH_WEST_RESIZE:
                cursor = Cursor.NW_RESIZE;
                break;

            case NORTH_RESIZE:
                cursor = Cursor.N_RESIZE;
                break;

            case NORTH_EAST_RESIZE:
                cursor = Cursor.NE_RESIZE;
                break;

            case WEST_RESIZE:
                cursor = Cursor.W_RESIZE;
                break;

            case EAST_RESIZE:
                cursor = Cursor.E_RESIZE;
                break;

            case SOUTH_WEST_RESIZE:
                cursor = Cursor.SW_RESIZE;
                break;

            case SOUTH_RESIZE:
                cursor = Cursor.S_RESIZE;
                break;

            case SOUTH_EAST_RESIZE:
                cursor = Cursor.SE_RESIZE;
                break;

            default:
                cursor = Cursor.AUTO;
                break;
            }
        else {
            cursor = Cursor.AUTO;
        }
        element.getStyle().setCursor(cursor);

    }

    public WindowBox(String title, String titleTooltip, Widget content, PopoutHandler popoutHandler) {
        super(false, false);
        WindowBoxResources.INSTANCE.windowBoxCss().ensureInjected();
        this.popoutHandler = popoutHandler;
        this.setStyleName("gwt-extras-WindowBox", true);
        this.container = new FlowPanel();
        super.setWidget(container);
        this.close = new Anchor();
        this.close.setStyleName("gwt-extras-dialog-close");
        this.close.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                onCloseClick(event);
            }
        });
        this.minimize = new Anchor();
        this.minimize.setStyleName("gwt-extras-dialog-minimize");
        this.minimize.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                onPopoutClick(event);
            }
        });

        ctrlGrid = null;
        if (popoutHandler != null) {
            ctrlGrid = new Grid(1, 3);
            ctrlGrid.setWidget(0, 1, this.minimize);
            ctrlGrid.setWidget(0, 2, this.close);
        } else {
            ctrlGrid = new Grid(1, 2);
            ctrlGrid.setWidget(0, 1, this.close);
        }
        this.controlsPanel = new FlowPanel();
        this.controlsPanel.setStyleName("gwt-extras-dialog-controls");
        this.controlsPanel.add(ctrlGrid);
        this.container.add(this.controlsPanel);
        setTitle(titleTooltip);
        setText(title);
        setWidget(content);
        blocker = new FlowPanel();
        blocker.getElement().getStyle().setZIndex(1000);
        blocker.getElement().getStyle().setTop(0, Unit.PX);
        blocker.getElement().getStyle().setLeft(0, Unit.PX);
        blocker.getElement().getStyle().setOpacity(0.1f);
        blocker.getElement().getStyle().setBackgroundColor("grey");
        blocker.getElement().getStyle().setPosition(Position.ABSOLUTE);
        dragResizeWidget(this, 0, 0);
    }

    public void addBeforeBarButtons(Widget widget) {
        ctrlGrid.setWidget(0, 0, widget);
    }

    /**
     * Sets the cursor to indicate resizability for a specified "drag-mode" (i.e. how the box is being resized) on the
     * dialog box. The position is described by an enum
     * 
     * @param dragMode
     */
    protected void updateCursor(DragMode dragMode) {
        updateCursor(dragMode, this.getElement());
        com.google.gwt.dom.client.Element top = this.getCellElement(0, 1);
        updateCursor(dragMode, top);
        top = Element.as(top.getFirstChild());
        if (top != null) {
            updateCursor(dragMode, top);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.DialogBox#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    @Override
    public void onBrowserEvent(Event event) {
        // If we're not yet dragging, only trigger mouse events if the event occurs
        // in the caption wrapper
        switch (event.getTypeInt()) {
        case Event.ONMOUSEDOWN:
        case Event.ONMOUSEUP:
        case Event.ONMOUSEMOVE:
        case Event.ONMOUSEOVER:
        case Event.ONMOUSEOUT:
            if (this.dragMode != null || calcDragMode(event.getClientX(), event.getClientY()) != null) {
                // paste'n'copy from Widget.onBrowserEvent
                switch (DOM.eventGetType(event)) {
                case Event.ONMOUSEOVER:
                    // Only fire the mouse over event if it's coming from outside this
                    // widget.
                case Event.ONMOUSEOUT:
                    // Only fire the mouse out event if it's leaving this
                    // widget.
                    Element related = event.getRelatedEventTarget().cast();
                    if (related != null && getElement().isOrHasChild(related)) {
                        return;
                    }
                    break;
                }
                DomEvent.fireNativeEvent(event, this, this.getElement());
                return;
            }
            if (this.dragMode == null) {
                this.updateCursor(this.dragMode);
            }
        }
        super.onBrowserEvent(event);
    }

    /**
     * 
     * @param resize
     * @param clientX
     * @return
     */
    private int getRelX(com.google.gwt.dom.client.Element resize, int clientX) {
        return clientX - resize.getAbsoluteLeft() + resize.getScrollLeft() + resize.getOwnerDocument().getScrollLeft();
    }

    /**
     * 
     * @param resize
     * @param clientY
     * @return
     */
    private int getRelY(com.google.gwt.dom.client.Element resize, int clientY) {
        return clientY - resize.getAbsoluteTop() + resize.getScrollTop() + resize.getOwnerDocument().getScrollTop();
    }

    /**
     * Calculates the position of the mouse relative to the dialog box, and returns the corresponding "drag-mode"
     * integer, which describes which area of the box is being resized.
     * 
     * @param clientX
     *            The x-coordinate of the mouse in screen pixels
     * @param clientY
     *            The y-coordinate of the mouse in screen pixels
     * @return A value in range [-1..8] describing the position of the mouse (see {@link #updateCursor(int)} for more
     *         information)
     */
    protected DragMode calcDragMode(int clientX, int clientY) {
        int tolerance = 5;
        com.google.gwt.dom.client.Element resize = this.getCellElement(2, 2).getParentElement();
        int xr = this.getRelX(resize, clientX);
        int yr = this.getRelY(resize, clientY);
        int w = resize.getClientWidth();
        int h = resize.getClientHeight();
        if ((xr >= 0 && xr <= w && yr >= -tolerance && yr <= h)
                || (yr >= 0 && yr <= h && xr >= -tolerance && xr <= w)) {
            return DragMode.SOUTH_EAST_RESIZE;
        }
        resize = this.getCellElement(2, 0).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);
        w = resize.getClientWidth();
        h = resize.getClientHeight();
        if ((xr >= 0 && xr <= w && yr >= -tolerance && yr <= h)
                || (yr >= 0 && yr <= h && xr >= 0 && xr <= w + tolerance)) {
            return DragMode.SOUTH_WEST_RESIZE;
        }
        resize = this.getCellElement(1, 0).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        w = resize.getClientWidth();
        h = resize.getClientHeight();

        if (xr >= 0 && xr <= w)
            return DragMode.WEST_RESIZE;

        resize = this.getCellElement(2, 1).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        w = resize.getClientWidth();
        h = resize.getClientHeight();

        if (yr >= 0 && yr <= h)
            return DragMode.SOUTH_RESIZE;

        resize = this.getCellElement(1, 2).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        w = resize.getClientWidth();
        h = resize.getClientHeight();

        if (xr >= 0 && xr <= w) {
            return DragMode.EAST_RESIZE;
        }

        resize = this.getCellElement(0, 1).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);
        w = resize.getClientWidth();
        h = resize.getClientHeight();
        if (yr >= 0 && yr <= h && xr < w - 100)
            return DragMode.MOVE;

        return null;
    }

    /**
     * Convenience method to set the height, width and position of the given widget
     * 
     * @param panel
     * @param dx
     * @param dy
     */
    protected void dragResizeWidget(PopupPanel panel, int dx, int dy) {
        Widget widget = panel.getWidget();

        int x = this.getPopupLeft();
        int y = this.getPopupTop();
        int w = widget.getOffsetWidth();
        int h = widget.getOffsetHeight();

        if (dragMode != null) {
            // left + right
            if (this.dragMode.isEast() || dragMode.isWest()) {
                // left edge -> move left
                if (dragMode.isWest()) {
                    x += dx;
                    w -= dx;
                    if (w < minWidth) {
                        int toAdd = minWidth - w;
                        x -= toAdd;
                        w += toAdd;
                    }
                } else {
                    w += dx;
                    if (w < minWidth) {
                        w = minWidth;
                    }
                }
            }
            // up + down
            if (dragMode.isNorth() || dragMode.isSouth()) {
                // up = dy is negative
                if (dragMode.isNorth()) {
                    y += dy;
                    h -= dy;
                    if (h < minHeight) {
                        int toAdd = minHeight - h;
                        h += toAdd;
                        y -= toAdd;
                    }
                } else {
                    h += dy;
                    if (h < minHeight) {
                        h = minHeight;
                    }
                }
                h = h < this.minHeight ? this.minHeight : h;
            }
        }

        widget.setHeight(h + "px");
        widget.setWidth(w + "px");
        panel.setPopupPosition(x, y);

        blocker.setWidth(getOffsetWidth() + "px");
        blocker.setHeight(getOffsetHeight() + "px");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.DialogBox#beginDragging(com.google.gwt.event.dom.client.MouseDownEvent)
     */
    @Override
    protected void beginDragging(MouseDownEvent event) {
        container.add(blocker);
        blocker.setWidth(getOffsetWidth() + "px");
        blocker.setHeight(getOffsetHeight() + "px");
        DragMode dm = this.calcDragMode(event.getClientX(), event.getClientY());
        if (dm != null && dm != DragMode.MOVE) {
            this.dragMode = dm;
            DOM.setCapture(getElement());
            this.dragX = event.getClientX();
            this.dragY = event.getClientY();
            updateCursor(dm, RootPanel.get().getElement());
        } else {
            super.beginDragging(event);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.DialogBox#continueDragging(com.google.gwt.event.dom.client.MouseMoveEvent)
     */
    @Override
    protected void continueDragging(MouseMoveEvent event) {
        if (this.dragMode != null && dragMode != DragMode.MOVE) {
            this.updateCursor(this.dragMode);
            int dx = event.getClientX() - this.dragX;
            int dy = event.getClientY() - this.dragY;
            this.dragX = event.getClientX();
            this.dragY = event.getClientY();
            dragResizeWidget(this, dx, dy);
        } else {
            // this updates the cursor when dragging is NOT activated
            DragMode dm = calcDragMode(event.getClientX(), event.getClientY());
            this.updateCursor(dm);
            super.continueDragging(event);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.gwt.user.client.ui.DialogBox#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
     */
    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {
        // We need to preventDefault() on mouseDown events (outside of the
        // DialogBox content) to keep text from being selected when it
        // is dragged.
        NativeEvent nativeEvent = event.getNativeEvent();
        if (!event.isCanceled() && (event.getTypeInt() == Event.ONMOUSEDOWN)
                && calcDragMode(nativeEvent.getClientX(), nativeEvent.getClientY()) != null) {
            nativeEvent.preventDefault();
        }
        super.onPreviewNativeEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.DialogBox#endDragging(com.google.gwt.event.dom.client.MouseUpEvent)
     */
    @Override
    protected void endDragging(MouseUpEvent event) {
        container.remove(blocker);

        if (this.dragMode != null && this.dragMode != DragMode.MOVE) {
            DOM.releaseCapture(getElement());
            this.dragX = event.getClientX() - this.dragX;
            this.dragY = event.getClientY() - this.dragY;
            this.dragMode = null;
            this.updateCursor(this.dragMode);
            RootPanel.get().getElement().getStyle().setCursor(Cursor.AUTO);
        } else {
            super.endDragging(event);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.DecoratedPopupPanel#setWidget(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void setWidget(Widget widget) {
        Widget old = getWidget();
        if (old != null) {
            this.container.remove(old);
        }
        this.container.add(widget);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.DecoratedPopupPanel#getWidget()
     */
    @Override
    public Widget getWidget() {
        for (int i = 0; i < container.getWidgetCount(); i++) {
            Widget w = container.getWidget(i);
            if (w == blocker) {
                continue;
            }
            if (w == controlsPanel) {
                continue;
            }
            return w;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.DecoratedPopupPanel#remove(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public boolean remove(Widget w) {
        return this.container.remove(w);
    }

    /**
     * Called when the close icon is clicked. The default implementation hides the dialog box.
     * 
     * @param event
     *            The {@link ClickEvent} to handle
     */
    protected void onCloseClick(ClickEvent event) {
        hide();
    }

    /**
     * Called when the minimize icon is clicked. The default implementation hides the container of the dialog box.
     * 
     * @param event
     *            The {@link ClickEvent} to handle
     */
    protected void onPopoutClick(ClickEvent event) {
        if (popoutHandler != null) {
            popoutHandler.popout();
        }
    }

}