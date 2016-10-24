package com.sap.sailing.gwt.home.shared.partials.fullscreen;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for fullscreen viewer UIs. This viewer has an extensible header area with a close button and a content
 * area that takes the rest of the available space.
 *
 * @param <T> The type of the content widget.
 */
public class FullscreenContainer<T extends Widget> {
    
    private static FullscreenContainerUiBinder uiBinder = GWT.create(FullscreenContainerUiBinder.class);

    interface FullscreenContainerUiBinder extends UiBinder<HeaderPanel, FullscreenContainer<?>> {
    }
    
    interface Style extends CssResource {
        String popup();
        String toolbarInfo();
        String toolbarAction();
        String toolbarBusyIndicator();
        String content();
        String contentBorder();
    }
    
    @UiField Style style;
    @UiField AnchorElement logoUi;
    @UiField SimplePanel headerContentUi;
    @UiField FlowPanel toolbarUi;
    @UiField(provided=true) SimpleLayoutPanel contentUi = new FullscreenContentPanel();
    
    private final FullscreenPopupPanel popup = new FullscreenPopupPanel();
    private final HeaderPanel mainPanel;
    
    public FullscreenContainer() {
        popup.setWidget(mainPanel = uiBinder.createAndBindUi(this));
        popup.addStyleName(style.popup());
    }
    
    /**
     * To be overwritten by subclasses to hook into the fullscreen viewer's lifecycle.
     */
    protected void onShow() {
    }
    
    /**
     * To be overwritten by subclasses to hook into the fullscreen viewer's lifecycle.
     */
    protected void onClose() {
    }
    
    @SuppressWarnings("unchecked")
    protected T getContentWidget() {
        return (T) contentUi.getWidget();
    }
    
    protected void showLogo() {
        logoUi.getStyle().clearDisplay();
        headerContentUi.getElement().getStyle().setMarginLeft(5, Unit.EM);
    }
    
    /**
     * Should be called by subclasses if the content needs a border as optical guidance for the user.
     */
    protected void showBorder() {
        contentUi.addStyleName(style.contentBorder());
    }
    
    @UiHandler("closeActionUi")
    void onCloseActionClicked(ClickEvent event) {
        popup.hide();
    }
    
    public HandlerRegistration addCloseHandler(CloseHandler<PopupPanel> handler) {
        return popup.addCloseHandler(handler);
    }
    
    /**
     * To be called by subclasses to add more actions (in addition to the close button) to the upper right corner of the fullscreen veiwer.
     * 
     * @param widget the action widget to add to the toolbar in the upper right corner.
     */
    public void addToolbarAction(Widget widget) {
        widget.addStyleName(style.toolbarAction());
        toolbarUi.add(widget);
    }

    public void addToolbarBusyIndicator(Widget widget) {
        widget.addStyleName(style.toolbarBusyIndicator());
        toolbarUi.add(widget);
    }

    public void addToolbarInfo(Widget widget) {
        widget.addStyleName(style.toolbarInfo());
        toolbarUi.add(widget);
    }
    
    public void setHeaderWidget(Widget widget) {
        headerContentUi.setWidget(widget);
    }
    
    public void showContent(T content) {
        contentUi.setWidget(content);
        getContentWidget().getElement().addClassName(style.content());
        mainPanel.onResize();
        popup.showPopupPanel();
        this.onShow();
    }
    
    private class FullscreenPopupPanel extends PopupPanel {
        private FullscreenPopupPanel() {
            super(false, true);
            this.addCloseHandler(new CloseHandler<PopupPanel>() {
                @Override
                public void onClose(CloseEvent<PopupPanel> event) {
                    RootPanel.get().getElement().getStyle().setOverflow(Overflow.AUTO);
                    getContentWidget().getElement().removeClassName(style.content());
                    FullscreenContainer.this.onClose();
                }
            });
        }
        
        protected void onPreviewNativeEvent(NativePreviewEvent event) {
            if (event.getTypeInt() == Event.ONKEYDOWN && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                FullscreenPopupPanel.this.hide();
                event.cancel();
                return;
            }
            event.consume();
        };
        
        private void showPopupPanel() {
            RootPanel.get().getElement().getStyle().setOverflow(Overflow.HIDDEN);
            FullscreenPopupPanel.this.show();
        }
    }
    
    private class FullscreenContentPanel extends SimpleLayoutPanel {
        private HandlerRegistration windowResizeHandler;
        
        @Override
        public void onResize() {
            popup.getWidget().getElement().getStyle().setWidth(Window.getClientWidth(), Unit.PX);
            popup.getWidget().getElement().getStyle().setHeight(Window.getClientHeight(), Unit.PX);
            super.onResize();
        }
        
        @Override
        protected void onLoad() {
            windowResizeHandler = Window.addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(ResizeEvent event) {
                    mainPanel.onResize();
                }
            });
        }
        
        @Override
        protected void onUnload() {
            if (windowResizeHandler != null) {
                windowResizeHandler.removeHandler();
            }
        }
    }
}
