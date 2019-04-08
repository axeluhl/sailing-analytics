package com.sap.sailing.gwt.home.shared.partials.bubble;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A {@link PopupPanel} implementation to show {@link IsWidget any content} in a bubble in the chosen {@link Direction
 * direction} relative to the given {@link Element}(s).
 */
public class Bubble extends PopupPanel {
    
    /**
     * Default implementation for a presenter to show and hide a {@link Bubble}.
     */
    public static class DefaultPresenter implements EventListener {

        private final Element relativeToX, relativeToY;
        private final Bubble popup;
        private final Direction direction;

        /**
         * Creates a new {@link DefaultPresenter} instance
         * 
         * @param content
         *            the {@link IsWidget content} to show within the {@link Bubble}
         * @param relativeToX
         *            {@link Element} to show the {@link Bubble} relative to (horizontal)
         * @param relativeToY
         *            {@link Element} to show the {@link Bubble} relative to (vertical)
         * @param direction
         *            {@link Direction} in which the {@link Bubble} should be shown
         * 
         * @see Bubble#Bubble(IsWidget)
         * @see Bubble#show(Element, Element, Direction)
         */
        public DefaultPresenter(IsWidget content, Element relativeToX, Element relativeToY, Direction direction) {
            this.relativeToX = relativeToX;
            this.relativeToY = relativeToY;
            this.direction = direction;
            this.popup = new Bubble(content);
        }

        /**
         * Registers the given {@link Element} as {@link DOM#sinkEvents(Element, int) event sink} for click, mouse-over
         * and mouse-out events to trigger show or hide of the managed {@link Bubble} instance.
         * 
         * @param target
         *            the {@link Element} to register
         */
        public void registerTarget(Element target) {
            DOM.sinkEvents(target, Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT);
            Event.setEventListener(target, this);
        }

        @Override
        public void onBrowserEvent(Event event) {
            final int typeInt = event.getTypeInt();
            if ((typeInt == Event.ONCLICK || typeInt == Event.ONMOUSEOVER) && !popup.isAttached()) {
                popup.show(relativeToX, relativeToY, direction);
                event.preventDefault();
                event.stopPropagation();
            }
            if (typeInt == Event.ONMOUSEOUT) {
                popup.hide();
                event.preventDefault();
                event.stopPropagation();
            }
        }
    }

    interface Resources extends ClientBundle {

        @Source("Bubble.gss")
        Style css();
    }

    interface Style extends CssResource {
        String bubble();

        String top();

        String bottom();

        String left();

        String right();
    }

    /**
     * Enumeration which provides the possible directions where a {@link Bubble} can be shown.
     */
    public enum Direction {
        TOP {
            @Override
            protected String getStyleName(Style style) {
                return style.top();
            }
        },
        BOTTOM {
            @Override
            protected String getStyleName(Style style) {
                return style.bottom();
            }
        },
        LEFT {
            @Override
            protected String getStyleName(Style style) {
                return style.left();
            }
        },
        RIGHT {
            @Override
            protected String getStyleName(Style style) {
                return style.right();
            }
        };

        protected abstract String getStyleName(Style style);
    }

    private final Resources resources = GWT.create(Resources.class);
    private HandlerRegistration handlerRegistration;

    /**
     * Creates a new {@link Bubble} instance containing the given {@link IsWidget content}.
     * 
     * @param content
     *            the {@link IsWidget content} to show within the {@link Bubble}
     */
    public Bubble(IsWidget content) {
        super(true);
        resources.css().ensureInjected();
        setWidget(content);
        getWidget().addStyleName(resources.css().bubble());
    }

    /**
     * Shows this {@link Bubble} instance in the given {@link Direction direction} horizontally and vertically relative
     * to the provided {@link Element element}.
     * 
     * @param relativeTo
     *            {@link Element} to show the {@link Bubble} relative to (horizontal and vertical)
     * @param direction
     *            {@link Direction} in which the {@link Bubble} should be shown
     */
    public void show(Element relativeTo, Direction direction) {
        this.show(relativeTo, relativeTo, direction);
    }

    /**
     * Shows this {@link Bubble} instance in the given {@link Direction direction} horizontally and vertically relative
     * to the respectively provided {@link Element elements}.
     * 
     * @param relativeToX
     *            {@link Element} to show the {@link Bubble} relative to (horizontal)
     * @param relativeToY
     *            {@link Element} to show the {@link Bubble} relative to (vertical)
     * @param direction
     *            {@link Direction} in which the {@link Bubble} should be shown
     */
    public void show(Element relativeToX, Element relativeToY, Direction direction) {
        this.getWidget().addStyleName(direction.getStyleName(resources.css()));
        this.setVisible(false);
        this.show();
        if (handlerRegistration == null) {
            handlerRegistration = Window.addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(ResizeEvent event) {
                    Bubble.this.updatePosition(relativeToX, relativeToY, direction);
                }
            });
        }
        this.updatePosition(relativeToX, relativeToY, direction);
    }

    private void updatePosition(Element relativeToX, Element relativeToY, Direction direction) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                final int arrawSize = 16;
                int left = 0, top = 0;
                final int popupWidth = Bubble.this.getOffsetWidth(), popupHeight = Bubble.this.getOffsetHeight();
                switch (direction) {
                case LEFT:
                    left = relativeToY.getAbsoluteLeft() - popupWidth - arrawSize;
                    top = relativeToX.getAbsoluteTop() + relativeToX.getOffsetHeight() / 2 - popupHeight / 2;
                    break;
                case RIGHT:
                    left = relativeToY.getAbsoluteLeft() + relativeToY.getOffsetWidth() + arrawSize;
                    top = relativeToX.getAbsoluteTop() + relativeToX.getOffsetHeight() / 2 - popupHeight / 2;
                    break;
                case TOP:
                    left = relativeToY.getAbsoluteLeft() + relativeToY.getOffsetWidth() / 2 - popupWidth / 2;
                    top = relativeToX.getAbsoluteTop() - popupHeight - arrawSize;
                    break;
                case BOTTOM:
                    left = relativeToY.getAbsoluteLeft() + relativeToY.getOffsetWidth() / 2 - popupWidth / 2;
                    top = relativeToX.getAbsoluteTop() + relativeToX.getOffsetHeight() + arrawSize;
                    break;
                }
                Bubble.this.setPopupPosition(left, top);
                Bubble.this.setVisible(true);
            }
        });
    }

    @Override
    public void hide(boolean autoClosed) {
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
            handlerRegistration = null;
        }
        super.hide(autoClosed);
        this.getWidget().removeStyleName(resources.css().top());
        this.getWidget().removeStyleName(resources.css().bottom());
        this.getWidget().removeStyleName(resources.css().left());
        this.getWidget().removeStyleName(resources.css().right());
    }
}
