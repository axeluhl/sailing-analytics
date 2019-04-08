package com.sap.sse.gwt.client.shared.controls;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A {@link TabLayoutPanel} that shows scroll buttons if necessary.
 */
public class ScrolledTabLayoutPanel extends TabLayoutPanel {
    
    public static final String scrollLeftWidgetStyle = "gwt-ScrolledTabLayoutPanel-scrollLeft";
    public static final String scrollRightWidgetStyle = "gwt-ScrolledTabLayoutPanel-scrollRight";

    private static final int SCROLL_RIGHT_TAB_MARGIN = 10;
    private static final int SCROLL_ANIMATION_SPEED = 150;
    
    private LayoutPanel panel;
    private FlowPanel tabBar;
    private Widget scrollLeftWidget;
    private Widget scrollRightWidget;
    private HandlerRegistration windowResizeHandler;
    private boolean isScrolling = false;
    
    private final ImageResource leftArrowImage;
    private final ImageResource rightArrowImage;
    private final int scrollSpeed;
    private final double barHeight;
    private final Unit barUnit;
    
    
    
    public ScrolledTabLayoutPanel(double barHeight, Unit barUnit, ImageResource leftArrowImage,
            ImageResource rightArrowImage) {
        this(barHeight, barUnit, leftArrowImage, rightArrowImage, 100);
    }

    public ScrolledTabLayoutPanel(double barHeight, Unit barUnit, ImageResource leftArrowImage,
            ImageResource rightArrowImage, int scrollSpeed) {
        super(barHeight, barUnit);

        this.leftArrowImage = leftArrowImage;
        this.rightArrowImage = rightArrowImage;
        this.scrollSpeed = scrollSpeed;
        this.barHeight = barHeight;
        this.barUnit = barUnit;

        // The main widget wrapped by this composite, which is a LayoutPanel with the tab bar & the tab content
        panel = (LayoutPanel) getWidget();

        // Find the tab bar, which is the first flow panel in the LayoutPanel
        for (int i = 0; i < panel.getWidgetCount(); ++i) {
            Widget widget = panel.getWidget(i);
            if (widget instanceof FlowPanel) {
                tabBar = (FlowPanel) widget;
                break; // tab bar found
            }
        }

        initScrollButtons();
    }

    @Override
    public void add(Widget child, Widget tab) {
        super.add(child, tab);
        checkIfScrollButtonsNecessary();
    }

    @Override
    public boolean remove(Widget w) {
        boolean b = super.remove(w);
        checkIfScrollButtonsNecessary();
        return b;
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        if (windowResizeHandler == null) {
            windowResizeHandler = Window.addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(ResizeEvent event) {
                    checkIfScrollButtonsNecessary();
                }
            });
        }
        checkIfScrollButtonsNecessary();
    }

    @Override
    protected void onUnload() {
        super.onUnload();

        if (windowResizeHandler != null) {
            windowResizeHandler.removeHandler();
            windowResizeHandler = null;
        }
    }

    private ClickHandler createScrollClickHandler(final int diff) {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (isScrolling) {
                    return;
                }
                
                int difference = diff;
                Widget lastTab = getLastTab();
                if (lastTab == null)
                    return;

                int oldLeft = parsePosition(tabBar.getElement().getStyle().getLeft());
                if (getAbsoluteRightOfTabBar() < getAbsoluteRightOfWidget(lastTab)) {
                    // Prevent over-scrolling right-most tab
                    difference = Math.max(difference, 
                            (getAbsoluteRightOfTabBar() - getAbsoluteRightOfWidget(lastTab) - SCROLL_RIGHT_TAB_MARGIN));
                }
                
                // Prevent over-scrolling left border
                int newLeft = Math.min(oldLeft + difference, 0);
                scrollTo(oldLeft, newLeft);
            }
        };
    }

    /** Create and attach the scroll button images with a click handler */
    private void initScrollButtons() {
        Image scrollLeftButton = new Image(leftArrowImage);
        scrollLeftWidget = centerVertical(scrollLeftButton, scrollLeftWidgetStyle);
        panel.insert(scrollLeftWidget, 0);
        panel.setWidgetLeftWidth(scrollLeftWidget, 0, Unit.PX, scrollLeftButton.getWidth(), Unit.PX);
        panel.setWidgetTopHeight(scrollLeftWidget, 0, Unit.PX, barHeight, barUnit);
        
        scrollLeftButton.addClickHandler(createScrollClickHandler(+scrollSpeed));
        scrollLeftWidget.setVisible(false);

        Image scrollRightButton = new Image(rightArrowImage);
        scrollRightWidget = centerVertical(scrollRightButton, scrollRightWidgetStyle);
        panel.insert(scrollRightWidget, 0);
        panel.setWidgetRightWidth(scrollRightWidget, 0, Unit.PX, scrollRightButton.getWidth(), Unit.PX);
        panel.setWidgetTopHeight(scrollRightWidget, 0, Unit.PX, barHeight, barUnit);

        scrollRightButton.addClickHandler(createScrollClickHandler(-scrollSpeed));
        scrollRightWidget.setVisible(false);
    }
    
    private Widget centerVertical(Widget widget, String styleName) {
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setSize("100%", "100%");
        vPanel.setStylePrimaryName(styleName);
        vPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        vPanel.getElement().getStyle().setBackgroundColor("#ccc");
        vPanel.getElement().getStyle().setMarginTop(6, Unit.PX);
        vPanel.add(widget);
        return vPanel;
    }

    public void checkIfScrollButtonsNecessary() {
        // Defer size calculations until sizes are available, when calculating immediately after
        // add(), all size methods return zero
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {

            @Override
            public void execute() {
                boolean needsScrollingToRight = isScrollingToRightNecessary();
                boolean needsScrollingToLeft = isScrollingToLeftNecessary();
                // When the scroll buttons are being hidden, reset the scroll position to zero to
                // make sure no tabs are still out of sight
                if (scrollRightWidget.isVisible() && !needsScrollingToLeft && !needsScrollingToRight) {
                    resetScrollPosition();
                }
                scrollLeftWidget.setVisible(needsScrollingToLeft);
                scrollRightWidget.setVisible(needsScrollingToRight);
            }

        });

    }
    
    public void scrollToTab(int tabNumber) {
        Widget tab = getTab(tabNumber);
        if (tab == null || !isScrollingNecessary())
            return;
        
        int oldLeft = parsePosition(tabBar.getElement().getStyle().getLeft());
        int difference = getAbsoluteRightOfTabBar() - getAbsoluteRightOfWidget(tab) - SCROLL_RIGHT_TAB_MARGIN;
        
        // Prevent over-scrolling left border
        int newLeft = Math.min(oldLeft + difference, 0);
        scrollTo(oldLeft, newLeft);
    }

    private void resetScrollPosition() {
        scrollTo(0, 0);
    }

    private void scrollTo(final int oldPos, final int newPos) {
        if (isScrolling) {
            return;
        }
        isScrolling = true;
        
        final int difference = newPos - oldPos;
        new Animation() {
            
            @Override
            protected void onUpdate(double progress) {
                tabBar.getElement().getStyle().setLeft(oldPos + (interpolate(progress) * difference), Unit.PX);
            }
            
            @Override
            protected void onComplete() {
                super.onComplete();
                checkIfScrollButtonsNecessary();
                isScrolling = false;
            }
        }.run(SCROLL_ANIMATION_SPEED);
    }
    
    private boolean isScrollingNecessary() {
        return isScrollingToLeftNecessary() || isScrollingToRightNecessary();
    }

    private boolean isScrollingToRightNecessary() {
        Widget lastTab = getLastTab();
        if (lastTab == null)
            return false;
        
        return getAbsoluteRightOfWidget(lastTab) > getAbsoluteRightOfTabBar();
    }

    private boolean isScrollingToLeftNecessary() {
        Widget firstTab = getFirstTab();
        if (firstTab == null)
            return false;

        return getAbsoluteLeftOfWidget(firstTab) < getAbsoluteLeftOfTabBar();
    }

    private int getAbsoluteLeftOfTabBar() {
        return tabBar.getElement().getParentElement().getAbsoluteLeft();
    }
    
    private int getAbsoluteRightOfTabBar() {
        return tabBar.getElement().getParentElement().getAbsoluteRight();
    }

    private int getAbsoluteLeftOfWidget(Widget widget) {
        return widget.getElement().getAbsoluteLeft();
    }
    
    private int getAbsoluteRightOfWidget(Widget widget) {
        return widget.getElement().getAbsoluteRight();
    }

    private Widget getLastTab() {
        if (tabBar.getWidgetCount() == 0)
            return null;

        return tabBar.getWidget(tabBar.getWidgetCount() - 1);
    }
    
    private Widget getTab(int tabNumber) {
        if (tabNumber < 0 || tabNumber >= tabBar.getWidgetCount()) {
            return null;
        }
        
        return tabBar.getWidget(tabNumber);
    }
    
    private Widget getFirstTab() {
        if (tabBar.getWidgetCount() == 0)
            return null;

        return tabBar.getWidget(0);
    }

    private static int parsePosition(String positionString) {
        int position;
        try {
            for (int i = 0; i < positionString.length(); i++) {
                char c = positionString.charAt(i);
                if (c != '-' && !(c >= '0' && c <= '9')) {
                    positionString = positionString.substring(0, i);
                }
            }

            position = Integer.parseInt(positionString);
        } catch (NumberFormatException ex) {
            position = 0;
        }
        return position;
    }
}