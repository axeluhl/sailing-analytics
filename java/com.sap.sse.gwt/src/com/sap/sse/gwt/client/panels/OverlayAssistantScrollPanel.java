package com.sap.sse.gwt.client.panels;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This scrollpanel overlays a second scrollbar fixed to the bottom of the viewport as soon as the original scrollbar is
 * not visible anymore.
 * <p>
 * 
 * This class observes a set of events to properly synchronize scrollbar states:
 * 
 * <ul>
 * <li>scrollevents on both scrollbars</li>
 * <li>window resize events</li>
 * <li>DOM mutation events on the scrollpanel child widget</li>
 * </ul>
 */
public class OverlayAssistantScrollPanel extends ScrollPanel {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<DivElement, OverlayAssistantScrollPanel> {
    }

    private final DivElement overlayWidget;
    @UiField
    protected DivElement overlayScrollPanelUi;
    @UiField
    protected DivElement overlayDummyContentUi;
    private final OverlayAnimation animation = new OverlayAnimation();
    private final List<HandlerRegistration> registrations = new ArrayList<>();
    private final Element contentToSyncWith;
    private final boolean hasMutationObservationCapability;
    private JavaScriptObject observer;
    /**
     * Flag that indicated that scrolling was initiated by table, the scroll event fired by the overlay scrollbar must
     * be ignored, solving bug 4283
     */
    private boolean ignoreOverlayScrollEvent = false;
    /**
     * Flag that indicated that scrolling was initiated by overlay scrollpanel, the scroll event fired by the table must
     * be ignored, solving bug 4283
     */
    private boolean ignoreTableScrollEvent = false;

    /**
     * Create an overlay scroll panel with the corresponding widget to scroll.
     * 
     * @param contentToScroll
     */
    public OverlayAssistantScrollPanel(Widget contentToScroll) {
        super(contentToScroll);
        // add 15 pixel spacing between bottom scrollpanel and scrolled content
        this.getElement().getStyle().setPaddingBottom(25, Unit.PX);
        this.contentToSyncWith = contentToScroll.getElement();
        this.hasMutationObservationCapability = hasMutationObservationCapability();
        this.overlayWidget = uiBinder.createAndBindUi(this);
    }

    /**
     * Bind all observers required to manage both scrollbars in sync.
     */
    @Override
    protected void onLoad() {
        RootPanel.get().getElement().appendChild(overlayWidget);
        if (!hasMutationObservationCapability) {
            return;
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                matchScrollpanelAndContentWidths();
                updateOverlayDisplay();
                applyScrollpanelToOverlay();
            }
        });
        registrations.add(Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                matchScrollpanelAndContentWidths();
                updateOverlayDisplay();
                applyScrollpanelToOverlay();
            }
        }));
        registrations.add(Window.addWindowScrollHandler(new Window.ScrollHandler() {
            @Override
            public void onWindowScroll(com.google.gwt.user.client.Window.ScrollEvent event) {
                matchScrollpanelAndContentWidths();
                updateOverlayDisplay();
            }
        }));
        registrations.add(this.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                if (ignoreTableScrollEvent) {
                    ignoreTableScrollEvent = false;
                } else {
                    ignoreOverlayScrollEvent = true;
                    applyScrollpanelToOverlay();
                }
            }
        }));
        createObserverForCurrentChild();
        DOM.sinkEvents(overlayScrollPanelUi, Event.ONSCROLL);
        DOM.setEventListener(overlayScrollPanelUi, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (ignoreOverlayScrollEvent) {
                    ignoreOverlayScrollEvent = false;
                } else {
                    if (event.getTypeInt() == Event.ONSCROLL) {
                        ignoreTableScrollEvent = true;
                        applyOverlayToScrollpanel();
                    }
                }
            }
        });
    }

    private void applyScrollpanelToOverlay() {
        syncScrollers(this.getElement(), overlayScrollPanelUi);
        matchScrollpanelAndContentWidths();
    }

    private void applyOverlayToScrollpanel() {
        syncScrollers(overlayScrollPanelUi, this.getElement());
        matchScrollpanelAndContentWidths();
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (!hasMutationObservationCapability) {
            return;
        }
        animation.detach();
        for (HandlerRegistration handlerRegistration : registrations) {
            handlerRegistration.removeHandler();
        }
        if (observer != null) {
            removeObserverFromCurrentChildWidget();
        }
        overlayWidget.removeFromParent();
    }

    @Override
    public void setWidget(Widget w) {
        if (observer != null) {
            removeObserverFromCurrentChildWidget();
        }
        super.setWidget(w);
        if (isAttached()) {
            createObserverForCurrentChild();
        }
    }

    private void createObserverForCurrentChild() {
        observer = setupObserver(contentToSyncWith, new Command() {
            @Override
            public void execute() {
                matchScrollpanelAndContentWidths();
                updateOverlayDisplay();
                applyScrollpanelToOverlay();
            }
        });
    }

    private void removeObserverFromCurrentChildWidget() {
        disconnectObserver();
        observer = null;
    }

    /**
     * Helper method that synchronizes the scroll position of the target scrollpanel, using the data gathered from the
     * source scrollpanel
     */
    private void syncScrollers(Element source, Element target) {
        target.setScrollLeft(source.getScrollLeft());
    }

    /**
     * Helper method that synchronizes widget sizes of overlay elements with the size of the real scrollpanel and
     * content.
     */
    private void matchScrollpanelAndContentWidths() {
        overlayDummyContentUi.getStyle().setWidth(contentToSyncWith.getClientWidth(), Unit.PX);
        overlayScrollPanelUi.getStyle().setWidth(OverlayAssistantScrollPanel.this.getOffsetWidth(), Unit.PX);
        overlayScrollPanelUi.getStyle().setMarginLeft(this.getAbsoluteLeft(), Unit.PX);
    }

    /**
     * This methods triggers the animation that shows/ hides the overlay scrollbar.
     */
    private void updateOverlayDisplay() {
        // docViewTop and docViewBottom contain the window viewport size
        int docViewTop = Window.getScrollTop();
        int docViewBottom = docViewTop + Window.getClientHeight();
        // elemTop and elemBottom contain the absolute position of the original viewport
        int elemTop = this.getAbsoluteTop();
        int elemBottom = elemTop + this.getOffsetHeight() + 15;
        // are we scrolling?
        boolean scrollbarIsShown = isElementHorizontalScrollShown();
        boolean syncedScrollbarIsVisible = (elemBottom <= docViewBottom);
        boolean scrollPanelEntirelyOutOfView = (elemTop + 40) > overlayWidget.getAbsoluteTop();
        animation.updateOverlayToState(scrollbarIsShown && !syncedScrollbarIsVisible && !scrollPanelEntirelyOutOfView);
    }

    /**
     * Checks if the content in the scrollpanel fits in the scrollpanel without scrolling
     */
    public boolean isElementHorizontalScrollShown() {
        return contentToSyncWith.getClientWidth() > this.getElement().getClientWidth();
    }

    /**
     * Uses JSNI to setup a mutation observer. This way we get notified when the content inside the scrollpanel changes
     * on dom-level. As soon as we have changes, we execute the command provided by this method.
     */
    private native JavaScriptObject setupObserver(final Element elementToObserve, Command onChangeCommand) /*-{
        if (MutationObserver) {
            var observer = new MutationObserver(
                function(mutations) {
                    onChangeCommand.@com.google.gwt.user.client.Command::execute()();
                });
            var observerConfig = {
                attributes : true,
                childList : true,
                characterData : true
            }
            observer.observe(elementToObserve, observerConfig);
            return observer;
        } else {
            return null;
        }
    }-*/;

    /**
     * Disconnect the mutation observer.
     */
    private native void disconnectObserver() /*-{
        if (this.@com.sap.sse.gwt.client.panels.OverlayAssistantScrollPanel::observer) {
            this.@com.sap.sse.gwt.client.panels.OverlayAssistantScrollPanel::observer.disconnect();
        }
    }-*/;

    /**
     * Check if the browser provides the mutation observer API.
     */
    private native boolean hasMutationObservationCapability() /*-{
        if (MutationObserver) {
            return true;
        }
        return false;
    }-*/;

    /**
     * Wrapper class the effectively does show/ hide the overlay scrollbar.
     */
    private final class OverlayAnimation extends Animation {
        private boolean overlayToolbarIsCurrentlyVisible = false;
        private boolean overlayRequestedTargetState;

        /**
         * Convenience method to trigger animation.
         */
        public void updateOverlayToState(boolean doShowOverlay) {
            animate(doShowOverlay, 200);
        }

        /**
         * Convenience method to reset state to "not visible", used when overlay gets detached from view.
         */
        public void detach() {
            overlayToolbarIsCurrentlyVisible = false;
        }

        /**
         * Trigger overlay display animation. The animation only fires if the current internal state and the target
         * state are differ.
         */
        public void animate(boolean requestedVisibleState, int duration) {
            boolean changed = overlayToolbarIsCurrentlyVisible != requestedVisibleState;
            if (!changed)
                return;
            overlayRequestedTargetState = requestedVisibleState;
            run(duration);
        }

        @Override
        protected void onStart() {
            overlayWidget.getStyle().setOpacity(overlayRequestedTargetState ? 0 : 1);
        }

        @Override
        protected void onUpdate(double progress) {
            overlayWidget.getStyle().setOpacity(overlayRequestedTargetState ? progress : 1 - progress);
        }

        protected void onComplete() {
            overlayToolbarIsCurrentlyVisible = overlayRequestedTargetState;
            overlayWidget.getStyle().setOpacity(overlayRequestedTargetState ? 1 : 0);
        };

        protected void onCancel() {
            onComplete();
        }
    };
}
