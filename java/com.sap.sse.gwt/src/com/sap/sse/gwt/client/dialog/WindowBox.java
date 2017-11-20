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
 * Extension of the standard GWT DialogBox to provide a more "window"-like functionality. The WindowBox has
 * two control-buttons in the top right corner of the header, which allow the box to be turned into a dedicated browser window
 * ("popout") or the whole box to be closed. <br>
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

    private int dragMode;
    private PopoutHandler popoutHandler;

    /**
     * Static helper method to change the cursor for a given element when resizing is enabled.
     * 
     * @param dm
     *            The code describing the position of the element in question
     * @param element
     *            The {@link com.google.gwt.dom.client.Element} to set the cursor on
     */
    protected static void updateCursor(int dm, com.google.gwt.dom.client.Element element) {
        Cursor cursor;

        switch (dm) {
        case 0:
            cursor = Cursor.NW_RESIZE;
            break;

        case 1:
            cursor = Cursor.N_RESIZE;
            break;

        case 2:
            cursor = Cursor.NE_RESIZE;
            break;

        case 3:
            cursor = Cursor.W_RESIZE;
            break;

        case 5:
            cursor = Cursor.E_RESIZE;
            break;

        case 6:
            cursor = Cursor.SW_RESIZE;
            break;

        case 7:
            cursor = Cursor.S_RESIZE;
            break;

        case 8:
            cursor = Cursor.SE_RESIZE;
            break;

        default:
            cursor = Cursor.AUTO;
            break;
        }

        element.getStyle().setCursor(cursor);
    }

    public WindowBox(String title, String titleTooltip, Widget content, PopoutHandler popoutHandler) {
        super(false, false);
        WindowBoxResources.INSTANCE.windowBoxCss().ensureInjected();
        this.popoutHandler = popoutHandler;
        this.setStyleName("gwt-extras-WindowBox", true);
        this.container = new FlowPanel();
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
        Grid ctrlGrid = null;
        if (popoutHandler != null) {
            ctrlGrid = new Grid(1, 2);
            ctrlGrid.setWidget(0, 0, this.minimize);
            ctrlGrid.setWidget(0, 1, this.close);
        } else {
            ctrlGrid = new Grid(1, 1);
            ctrlGrid.setWidget(0, 0, this.close);
        }
        this.controlsPanel = new FlowPanel();
        this.controlsPanel.setStyleName("gwt-extras-dialog-controls");
        this.controlsPanel.add(ctrlGrid);
        this.dragMode = -1;
        setTitle(titleTooltip);
        setText(title);
        setWidget(content);
    }

    /**
     * Sets the cursor to indicate resizability for a specified "drag-mode" (i.e. how the box is being resized) on the
     * dialog box. The position is described by an integer, as follows:
     * 
     * <pre>
     *  0-- --1-- --2
     *  |           |
     *  
     *  |           |
     *  3    -1     5
     *  |           |
     *  
     *  |           |
     *  6-- --7-- --8
     * </pre>
     * 
     * passing <code>-1</code> resets the cursor to the default.
     * 
     * @param dragMode
     */
    protected void updateCursor(int dragMode) {
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
            if (this.dragMode >= 0 || calcDragMode(event.getClientX(), event.getClientY()) >= 0) {
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
            if (this.dragMode < 0) {
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
    protected int calcDragMode(int clientX, int clientY) {
        int tolerance = 5;
        com.google.gwt.dom.client.Element resize = this.getCellElement(2, 2).getParentElement();
        int xr = this.getRelX(resize, clientX);
        int yr = this.getRelY(resize, clientY);
        int w = resize.getClientWidth();
        int h = resize.getClientHeight();
        if ((xr >= 0 && xr <= w && yr >= -tolerance && yr <= h) || (yr >= 0 && yr <= h && xr >= -tolerance && xr <= w)) {
            return 8;
        }
        resize = this.getCellElement(2, 0).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);
        w = resize.getClientWidth();
        h = resize.getClientHeight();
        if ((xr >= 0 && xr <= w && yr >= -tolerance && yr <= h)
                || (yr >= 0 && yr <= h && xr >= 0 && xr <= w + tolerance)) {
            return 6;
        }
        /*
         * resize = this.getCellElement(0, 2).getParentElement(); xr = this.getRelX(resize, clientX); yr =
         * this.getRelY(resize, clientY);
         * 
         * w = resize.getClientWidth(); h = resize.getClientHeight();
         * 
         * if ((xr >= 0 && xr <= w && yr >= 0 && yr <= h + tolerance) || (yr >= 0 && yr <= h && xr >= -tolerance && xr
         * <= w)) return 2;
         * 
         * resize = this.getCellElement(0, 0).getParentElement(); xr = this.getRelX(resize, clientX); yr =
         * this.getRelY(resize, clientY);
         * 
         * w = resize.getClientWidth(); h = resize.getClientHeight();
         * 
         * if ((xr >= 0 && xr <= w && yr >= 0 && yr <= h + tolerance) || (yr >= 0 && yr <= h && xr >= 0 && xr <= w +
         * tolerance)) return 0;
         * 
         * resize = this.getCellElement(0, 1).getParentElement(); xr = this.getRelX(resize, clientX); yr =
         * this.getRelY(resize, clientY);
         * 
         * w = resize.getClientWidth(); h = resize.getClientHeight();
         * 
         * if (yr >= 0 && yr <= h) return 1;
         */

        resize = this.getCellElement(1, 0).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        w = resize.getClientWidth();
        h = resize.getClientHeight();

        if (xr >= 0 && xr <= w)
            return 3;

        resize = this.getCellElement(2, 1).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        w = resize.getClientWidth();
        h = resize.getClientHeight();

        if (yr >= 0 && yr <= h)
            return 7;

        resize = this.getCellElement(1, 2).getParentElement();
        xr = this.getRelX(resize, clientX);
        yr = this.getRelY(resize, clientY);

        w = resize.getClientWidth();
        h = resize.getClientHeight();

        if (xr >= 0 && xr <= w) {
            return 5;
        }
        return -1;
    }

    /**
     * Convenience method to set the height, width and position of the given widget
     * 
     * @param panel
     * @param dx
     * @param dy
     */
    protected void dragResizeWidget(PopupPanel panel, int dx, int dy) {
        int x = this.getPopupLeft();
        int y = this.getPopupTop();
        Widget widget = panel.getWidget();
        // left + right
        if ((this.dragMode % 3) != 1) {
            int w = widget.getOffsetWidth();
            // left edge -> move left
            if ((this.dragMode % 3) == 0) {
                x += dx;
                w -= dx;
            } else {
                w += dx;
            }
            w = w < this.minWidth ? this.minWidth : w;
            widget.setWidth(w + "px");
        }
        // up + down
        if ((this.dragMode / 3) != 1) {
            int h = widget.getOffsetHeight();
            // up = dy is negative
            if ((this.dragMode / 3) == 0) {
                y += dy;
                h -= dy;
            } else {
                h += dy;
            }
            h = h < this.minHeight ? this.minHeight : h;
            widget.setHeight(h + "px");
        }
        if (this.dragMode / 3 == 0 || this.dragMode % 3 == 0) {
            panel.setPopupPosition(x, y);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.DialogBox#beginDragging(com.google.gwt.event.dom.client.MouseDownEvent)
     */
    @Override
    protected void beginDragging(MouseDownEvent event) {
        int dm = this.calcDragMode(event.getClientX(), event.getClientY());
        if (dm >= 0) {
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
        if (this.dragMode >= 0) {
            this.updateCursor(this.dragMode);
            int dx = event.getClientX() - this.dragX;
            int dy = event.getClientY() - this.dragY;
            this.dragX = event.getClientX();
            this.dragY = event.getClientY();
            dragResizeWidget(this, dx, dy);
        } else {
            // this updates the cursor when dragging is NOT activated
            int dm = calcDragMode(event.getClientX(), event.getClientY());
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
                && calcDragMode(nativeEvent.getClientX(), nativeEvent.getClientY()) >= 0) {
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
        if (this.dragMode >= 0) {
            DOM.releaseCapture(getElement());
            this.dragX = event.getClientX() - this.dragX;
            this.dragY = event.getClientY() - this.dragY;
            this.dragMode = -1;
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
        if (this.container.getWidgetCount() == 0) {
            // setup
            this.container.add(this.controlsPanel);
            // this.container.add(this.content);
            super.setWidget(this.container);
        } else {
            // remove the old one
            this.container.remove(1);
        }
        this.container.add(widget);
        // add the new widget
        // this.content.add(widget);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.DecoratedPopupPanel#getWidget()
     */
    @Override
    public Widget getWidget() {
        if (this.container.getWidgetCount() > 1) {
            return this.container.getWidget(1);
        } else {
            return null;
        }
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