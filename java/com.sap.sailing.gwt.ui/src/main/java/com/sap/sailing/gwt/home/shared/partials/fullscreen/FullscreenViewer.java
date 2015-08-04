package com.sap.sailing.gwt.home.shared.partials.fullscreen;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class FullscreenViewer {
    
    private static FullscreenViewerUiBinder uiBinder = GWT.create(FullscreenViewerUiBinder.class);

    interface FullscreenViewerUiBinder extends UiBinder<HeaderPanel, FullscreenViewer> {
    }
    
    interface Style extends CssResource {
        String popup();
        String background();
        String toolbar();
        String toolbarInfoTextLeft();
        String toolbarInfoTextRight();
        String toolbarAction();
        String content();
    }
    
    @UiField Style style;
    @UiField FlowPanel toolbarUi;
    @UiField(provided=true) SimpleLayoutPanel contentUi = new FullscreenViewerContentPanel();
    
    private final FullscreenPopupPanel popup = new FullscreenPopupPanel();
    private final HeaderPanel mainPanel;
    
    public FullscreenViewer() {
        popup.setWidget(mainPanel = uiBinder.createAndBindUi(this));
        popup.addStyleName(style.popup());
        Image closeControl = new Image("images/home/close.svg");
        closeControl.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popup.hide();
            }
        });
        addToolbarAction(closeControl);
    }
    
    public HandlerRegistration addCloseHandler(CloseHandler<PopupPanel> handler) {
        return popup.addCloseHandler(handler);
    }
    
    public void addToolbarAction(Widget widget) {
        widget.addStyleName(style.toolbarAction());
        toolbarUi.add(widget);
    }
    
    public void addToolbarInfoText(Widget widget, boolean left) {
        widget.addStyleName(left ? style.toolbarInfoTextLeft() : style.toolbarInfoTextRight());
        toolbarUi.add(widget);
    }
    
    public void showContent(Widget content) {
        contentUi.setWidget(content);
        contentUi.getWidget().getElement().addClassName(style.content());
        mainPanel.onResize();
        popup.showPopupPanel();
    }
    
    private class FullscreenPopupPanel extends PopupPanel {
        private FullscreenPopupPanel() {
            super(false, true);
            this.addCloseHandler(new CloseHandler<PopupPanel>() {
                @Override
                public void onClose(CloseEvent<PopupPanel> event) {
                    RootPanel.get().getElement().getStyle().setOverflow(Overflow.AUTO);
                    contentUi.getWidget().getElement().removeClassName(style.content());
                }
            });
        }
        
        protected void onPreviewNativeEvent(NativePreviewEvent event) {
            if (event.getTypeInt() == Event.ONKEYDOWN && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                FullscreenPopupPanel.this.hide();
            }
        };
        
        private void showPopupPanel() {
            RootPanel.get().getElement().getStyle().setOverflow(Overflow.HIDDEN);
            FullscreenPopupPanel.this.show();
        }
    }
    
    private class FullscreenViewerContentPanel extends SimpleLayoutPanel {
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
