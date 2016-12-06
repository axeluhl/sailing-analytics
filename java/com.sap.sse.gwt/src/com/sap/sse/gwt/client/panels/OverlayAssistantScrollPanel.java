package com.sap.sse.gwt.client.panels;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This scrollpanel overlays a second scrollbar fixed to the bottom of the viewport as soon as the original scrollbar is
 * not visible anymore.
 * 
 * This class observes a set of events to properly synchronize scrollbar states:
 * 
 * <ul>
 * <li>scrollevents on both scrollbars</li>
 * <li>window resize events</li>
 * <li>dom mutation events on the scrollpanel child widget</li>
 * </ul>
 * 
 * 
 */
public class OverlayAssistantScrollPanel extends ScrollPanel {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, OverlayAssistantScrollPanel> {
    }

    @UiField
    protected ScrollPanel overlayScrollPanelUi;
    @UiField
    protected SimplePanel overlayDummyContentUi;
    private final OverlayAnimation animation = new OverlayAnimation();
    private final List<HandlerRegistration> registrations = new ArrayList<>();
    private Widget overlayWidget;
    private final Element contentToSyncWith;
    private final boolean hasMutationObservationCapability;

    /**
     * Create an overlay scroll panel with the corresponding widget to scroll.
     * 
     * @param contentToScroll
     */
    public OverlayAssistantScrollPanel(Widget contentToScroll) {
        super(contentToScroll);
        // add 15 pixel spacing between bottom scrollpanel and scrolled content
        this.getElement().getStyle().setPaddingBottom(15, Unit.PX);
        contentToSyncWith = contentToScroll.getElement();
        this.hasMutationObservationCapability = weCanObserve();
    }

    /**
     * Bind all observers required to manage both scrollbars in sync.
     */
    @Override
    protected void onAttach() {
        super.onAttach();
        GWT.log("onAttach");
        if (!hasMutationObservationCapability) {
            return;
        }
        overlayWidget = uiBinder.createAndBindUi(this);
        RootPanel.get().add(overlayWidget);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                matchScrollpanelAndContentWidths();
                updateOverlayDisplay();
                syncScrollers(OverlayAssistantScrollPanel.this, overlayScrollPanelUi);
            }
        });
        registrations.add(Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                matchScrollpanelAndContentWidths();
                updateOverlayDisplay();
                syncScrollers(OverlayAssistantScrollPanel.this, overlayScrollPanelUi);
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
                syncScrollers(OverlayAssistantScrollPanel.this, overlayScrollPanelUi);
            }
        }));
        registrations.add(overlayScrollPanelUi.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                syncScrollers(overlayScrollPanelUi, OverlayAssistantScrollPanel.this);
            }
        }));
        final JavaScriptObject observer = setupObserver(contentToSyncWith, new Command() {
            @Override
            public void execute() {
                GWT.log("contentToSyncWith");
                matchScrollpanelAndContentWidths();
                updateOverlayDisplay();
                syncScrollers(OverlayAssistantScrollPanel.this, overlayScrollPanelUi);
            }
        });
        registrations.add(new HandlerRegistration() {
            @Override
            public void removeHandler() {
                if (observer != null) {
                    disconnectObserver(observer);
                }
            }
        });
    }

    /**
     * Detach all observers.
     */
    @Override
    protected void onDetach() {
        GWT.log("onDetach");
        super.onDetach();
        if (!hasMutationObservationCapability) {
            return;
        }
        animation.detach();
        for (HandlerRegistration handlerRegistration : registrations) {
            handlerRegistration.removeHandler();
        }
        if (overlayWidget.isAttached()) {
            overlayWidget.removeFromParent();
        }
    }

    /**
     * Helper method that synchronizes the scroll position of the target scrollpanel, using the data gathered from the
     * source scrollpanel
     * 
     * @param source
     * @param target
     */
    private void syncScrollers(ScrollPanel source, ScrollPanel target) {
        target.setHorizontalScrollPosition(source.getHorizontalScrollPosition());
    }

    /**
     * Helper method that synchronizes widget sizes of overlay elements with the size of the real scrollpanel and
     * content.
     */
    private void matchScrollpanelAndContentWidths() {
        overlayDummyContentUi.getElement().getStyle().setWidth(contentToSyncWith.getClientWidth(), Unit.PX);
        overlayScrollPanelUi.getElement().getStyle().setWidth(OverlayAssistantScrollPanel.this.getOffsetWidth() + 16,
                Unit.PX);
        overlayScrollPanelUi.getElement().getStyle().setMarginLeft(this.getAbsoluteLeft(), Unit.PX);
    }

    /**
     * This methods triggers the animation that shows/ hides the overlay scrollbar.
     *
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
     * 
     * @return
     */
    public boolean isElementHorizontalScrollShown() {
        return contentToSyncWith.getClientWidth() > this.getElement().getClientWidth();
    }

    /**
     * Uses JSNI to setup a mutation observer. This way we get notified when the content inside the scrollpanel changes
     * on dom-level. As soon as we have changes, we execute the command provided by this method.
     * 
     * @param elementToObserve
     * @param onChangeCommand
     * @return
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
     * 
     * @param observer
     */
    private native void disconnectObserver(final JavaScriptObject observer) /*-{
	if (observer) {
	    observer.disconnect();
	}
    }-*/;

    /**
     * Check if the browser provides the mutation observer api.
     * 
     * @return
     */
    private native boolean weCanObserve() /*-{
	return (MutationObserver);
    }-*/;

    /**
     * Wrapper class the effectively does show/ hide the overlay scrollbar.
     */
    private final class OverlayAnimation extends Animation {
        private boolean overlayToolbarIsCurrentlyVisible = false;
        private boolean overlayRequestedTargetState;

        /**
         * Convenience method to trigger animation.
         * 
         * @param doShowOverlay
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
         * Trigger overlay display animation The animation only fires if the current internal state and the target state
         * are differ.
         * 
         * @param requestedVisibleState
         * @param duration
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
            overlayWidget.getElement().getStyle().setOpacity(overlayRequestedTargetState ? 0 : 1);
        }

        @Override
        protected void onUpdate(double progress) {
            overlayWidget.getElement().getStyle().setOpacity(overlayRequestedTargetState ? progress : 1 - progress);
        }

        protected void onComplete() {
            overlayToolbarIsCurrentlyVisible = overlayRequestedTargetState;
            overlayWidget.getElement().getStyle().setOpacity(overlayRequestedTargetState ? 1 : 0);
        };

        protected void onCancel() {
            onComplete();
        }
    };
}
