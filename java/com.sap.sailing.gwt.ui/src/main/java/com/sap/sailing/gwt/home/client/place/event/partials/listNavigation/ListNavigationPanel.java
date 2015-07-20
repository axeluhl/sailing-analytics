package com.sap.sailing.gwt.home.client.place.event.partials.listNavigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.ListNavigationPanel.ListNavigationAction;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.RegattaNavigationResources.LocalCss;

public class ListNavigationPanel<T extends ListNavigationAction> extends Composite {
    
    private static final LocalCss CSS = RegattaNavigationResources.INSTANCE.css();

    private static ListNavigationPanelUiBinder uiBinder = GWT.create(ListNavigationPanelUiBinder.class);

    interface ListNavigationPanelUiBinder extends UiBinder<Widget, ListNavigationPanel<?>> {
    }

    @UiField FlowPanel actionContainerUi;
    @UiField SimplePanel additionalWidgetContainerUi;

    private SelectionCallback<T> selectionCallback;

    public ListNavigationPanel(SelectionCallback<T> selectionCallback) {
        this.selectionCallback = selectionCallback;
        CSS.ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void addAction(final T action, boolean select) {
        ActionWidget actionWidget = new ActionWidget(action);
        actionContainerUi.add(actionWidget);
        if (select) {
            actionWidget.select();
        }
    }
    
    public void setAdditionalWidget(Widget additionalWidget) {
        this.additionalWidgetContainerUi.setWidget(additionalWidget);
    }
    
    public interface SelectionCallback<T> {
        void onSelectAction(T action);
    }
    
    public interface ListNavigationAction {
        String getDisplayName();
        boolean isShowAdditionalWidget();
    }
    
    private class ActionWidget extends Label {
        private final T action;
        private final Display displayStyle;

        private ActionWidget(T action) {
            super(action.getDisplayName());
            this.action = action;
            this.displayStyle = action.isShowAdditionalWidget() ? Display.BLOCK : Display.NONE;
            this.addStyleName(CSS.regattanavigation_button());
            this.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    ActionWidget.this.select();
                }
            });
        }
        
        private void select() {
            additionalWidgetContainerUi.getElement().getStyle().setDisplay(displayStyle);
            for (int i=0; i < actionContainerUi.getWidgetCount(); i++) {
                actionContainerUi.getWidget(i).removeStyleName(CSS.regattanavigation_buttonactive());
            }
            this.addStyleName(CSS.regattanavigation_buttonactive());
            selectionCallback.onSelectAction(action);
        }
    }
    
}
