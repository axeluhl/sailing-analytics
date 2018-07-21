package com.sap.sse.gwt.client.shared.components;

import java.util.Iterator;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that consists of a header and a content panel that discloses the content when a user clicks on the header.
 * 
 * <h3>CSS Style Rules</h3>
 * <dl class="css">
 * <dt>.collapsablePanel
 * <dd>the panel's primary style
 * <dt>.collapsablePanel-open
 * <dd>dependent style set when panel is open
 * <dt>.collapsablePanel-closed
 * <dd>dependent style set when panel is closed
 * </dl>
 * <p>
 * The header and content sections can be easily selected using css with a child selector:<br/>
 * .collapsablePanel-open .header { ... }
 * </p>
 */
public final class CollapsablePanel extends Composite implements HasWidgets.ForIsWidget, HasAnimation,
        HasOpenHandlers<CollapsablePanel>, HasCloseHandlers<CollapsablePanel> {

    private static ComponentResources resources = GWT.create(ComponentResources.class);
    
    /**
     * An {@link Animation} used to open the content.
     */
    private static class ContentAnimation extends Animation {
        /**
         * Whether the item is being opened or closed.
         */
        private boolean opening;

        /**
         * The {@link CollapsablePanel} being affected.
         */
        private CollapsablePanel curPanel;

        /**
         * Open or close the content.
         * 
         * @param panel
         *            the panel to open or close
         * @param animate
         *            true to animate, false to open instantly
         */
        public void setOpen(CollapsablePanel panel, boolean animate) {
            // Immediately complete previous open
            cancel();

            // Open the new item
            if (animate) {
                curPanel = panel;
                opening = panel.isOpen;
                run(ANIMATION_DURATION);
            } else {
                panel.contentWrapper.setVisible(panel.isOpen);
                if (panel.isOpen) {
                    // Special treatment on the visible case to ensure LazyPanel works
                    panel.getContent().setVisible(true);
                }
            }
        }

        @Override
        protected void onComplete() {
            if (!opening) {
                curPanel.contentWrapper.setVisible(false);
            }
            curPanel.contentWrapper.setHeight("auto");
            curPanel = null;
        }

        @Override
        protected void onStart() {
            super.onStart();
            if (opening) {
                curPanel.contentWrapper.setVisible(true);
                // Special treatment on the visible case to ensure LazyPanel works
                curPanel.getContent().setVisible(true);
            }
        }

        @Override
        protected void onUpdate(double progress) {
            int scrollHeight = curPanel.contentWrapper.getElement().getPropertyInt("scrollHeight");
            int height = (int) (progress * scrollHeight);
            if (!opening) {
                height = scrollHeight - height;
            }
            height = Math.max(height, 1);
            curPanel.contentWrapper.getElement().getStyle().setHeight(height, Unit.PX);
            curPanel.contentWrapper.setWidth("auto");
        }
    }

    public interface Imager {
        Image makeImage();

        void updateImage(boolean open, Image image);
    }

    /**
     * The duration of the animation.
     */
    private static final int ANIMATION_DURATION = 350;

    // style name constants.
    private static final String STYLENAME_DEFAULT = "collapsablePanel";

    private static final String STYLENAME_SUFFIX_OPEN = "open";

    private static final String STYLENAME_SUFFIX_CLOSED = "closed";

    private static final String STYLENAME_HEADER = "collapsablePanel-header";

    private static final String STYLENAME_CONTENT = "collapsablePanel-content";

    /**
     * The {@link Animation} used to open and close the content.
     */
    private static ContentAnimation contentAnimation;

    /**
     * top level widget. The first child will be a reference to {@link #header}. The second child will be a reference to
     * {@link #contentWrapper}.
     */
    private final VerticalPanel mainPanel = new VerticalPanel();

    /**
     * The wrapper around the content widget.
     */
    private final SimplePanel contentWrapper = new SimplePanel();

    /**
     * The wrapper around the header widget.
     */
    private final SimplePanel headerWrapper = new SimplePanel();

    private CollapsablePanelHeader headerPanel;
    
    private boolean isAnimationEnabled = false;

    private boolean isOpen = false;
    
    private boolean isCollapsingEnabled = true;

    /**
     * Creates an empty ThePanel that is initially closed.
     */
    private CollapsablePanel() {
        initWidget(mainPanel);

        mainPanel.add(headerWrapper);
        mainPanel.add(contentWrapper);
        contentWrapper.getElement().getStyle().setPadding(0, Unit.PX);
        contentWrapper.getElement().getStyle().setOverflow(Overflow.HIDDEN);
        setStyleName(STYLENAME_DEFAULT);
        setContentDisplay(false);
    }

    /**
     * Creates a ThePanel with the specified header text, an initial open/close state and a bundle of images to be used
     * in the default header widget.
     * 
     * @param openImage
     *            the open state image resource
     * @param closedImage
     *            the closed state image resource
     * @param headerText
     *            the text to be displayed in the header
     */
    public CollapsablePanel(ImageResource openImage, ImageResource closedImage, String headerText, boolean hasToolbar) {
        this();
        headerPanel = new CollapsablePanelHeader(this, openImage, closedImage, headerText, hasToolbar);
        setHeader(headerPanel);
    }

    /**
     * Creates a ThePanel that will be initially closed using the specified text in the header.
     * 
     * @param headerText
     *            the text to be displayed in the header
     */
    public CollapsablePanel(String headerText, boolean hasToolbar) {
        this(resources.openIcon(), resources.closeIcon(), headerText, hasToolbar);
    }

    public void setHeaderToolbar(Widget toolbar) {
        headerPanel.setToolbar(toolbar);
    }

    public void add(Widget w) {
        if (this.getContent() == null) {
            setContent(w);
        } else {
            throw new IllegalStateException("A CollapsablePanel can only contain two Widgets.");
        }
    }

    /**
     * Overloaded version for IsWidget.
     * 
     * @see #add(Widget)
     */
    public void add(IsWidget w) {
        this.add(asWidgetOrNull(w));
    }

    public HandlerRegistration addCloseHandler(CloseHandler<CollapsablePanel> handler) {
        return addHandler(handler, CloseEvent.getType());
    }

    public HandlerRegistration addOpenHandler(OpenHandler<CollapsablePanel> handler) {
        return addHandler(handler, OpenEvent.getType());
    }

    public void clear() {
        setContent(null);
    }

    /**
     * Gets the widget that was previously set in {@link #setContent(Widget)}.
     * 
     * @return the panel's current content widget
     */
    public Widget getContent() {
        return contentWrapper.getWidget();
    }

    /**
     * Gets the widget that is currently being used as a header.
     * 
     * @return the widget currently being used as a header
     */
    public Widget getHeader() {
        return headerWrapper.getWidget();
    }

    /**
     * Gets a {@link HasText} instance to provide access to the headers's text, if the header widget does provide such
     * access.
     * 
     * @return a reference to the header widget if it implements {@link HasText}, <code>null</code> otherwise
     */
    public HasText getHeaderTextAccessor() {
        Widget widget = headerWrapper.getWidget();
        return (widget instanceof HasText) ? (HasText) widget : null;
    }

    public boolean isAnimationEnabled() {
        return isAnimationEnabled;
    }

    /**
     * Determines whether the panel is open.
     * 
     * @return <code>true</code> if panel is in open state
     */
    public boolean isOpen() {
        return isOpen;
    }

    public boolean isCollapsingEnabled() {
        return isCollapsingEnabled;
    }

    public void setCollapsingEnabled(boolean enable) {
        isCollapsingEnabled = enable;
        
        this.headerPanel.setCollapsingEnabled(enable);
    }

    public Iterator<Widget> iterator() {
        return null;
    }

    public boolean remove(Widget w) {
        if (w == getContent()) {
            setContent(null);
            return true;
        }
        return false;
    }

    public boolean remove(IsWidget w) {
        return this.remove(asWidgetOrNull(w));
    }

    public void setAnimationEnabled(boolean enable) {
        isAnimationEnabled = enable;
    }

    public void setHeader(Widget headerContent) {
        final Widget currentHeaderContent = getHeader();

        // Remove existing header content widget.
        if (currentHeaderContent != null) {
            headerWrapper.setWidget(null);
            currentHeaderContent.removeStyleName(STYLENAME_HEADER);
        }

        // Add new header content widget if != null.
        if (headerContent != null) {
            headerWrapper.setWidget(headerContent);
            
            headerContent.addStyleName(STYLENAME_HEADER);
        }
    }

    /**
     * Sets the content widget which can be opened and closed by this panel. If there is a preexisting content widget,
     * it will be detached.
     * 
     * @param content
     *            the widget to be used as the content panel
     */
    public void setContent(Widget content) {
        final Widget currentContent = getContent();

        // Remove existing content widget.
        if (currentContent != null) {
            contentWrapper.setWidget(null);
            currentContent.removeStyleName(STYLENAME_CONTENT);
        }

        // Add new content widget if != null.
        if (content != null) {
            contentWrapper.setWidget(content);
            
            content.addStyleName(STYLENAME_CONTENT);
            setContentDisplay(false);
        }
    }

    /**
     * Changes the visible state of this <code>ThePanel</code>.
     * 
     * @param isOpen
     *            <code>true</code> to open the panel, <code>false</code> to close
     */
    public void setOpen(boolean isOpen) {
        if (this.isOpen != isOpen) {
            this.isOpen = isOpen;
            setContentDisplay(true);
            fireEvent();
        }
    }

    /**
     * <b>Affected Elements:</b>
     * <ul>
     * <li>-header = the clickable header.</li>
     * </ul>
     * 
     * @see UIObject#onEnsureDebugId(String)
     */
    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);
        headerWrapper.ensureDebugId(baseID + "-header");
    }

    private void fireEvent() {
        if (isOpen) {
            OpenEvent.fire(this, this);
        } else {
            CloseEvent.fire(this, this);
        }
    }

    private void setContentDisplay(boolean animate) {
        if (isOpen) {
            removeStyleDependentName(STYLENAME_SUFFIX_CLOSED);
            addStyleDependentName(STYLENAME_SUFFIX_OPEN);
        } else {
            removeStyleDependentName(STYLENAME_SUFFIX_OPEN);
            addStyleDependentName(STYLENAME_SUFFIX_CLOSED);
        }

        if (getContent() != null) {
            if (contentAnimation == null) {
                contentAnimation = new ContentAnimation();
            }
            contentAnimation.setOpen(this, animate && isAnimationEnabled);
        }
    }
}
