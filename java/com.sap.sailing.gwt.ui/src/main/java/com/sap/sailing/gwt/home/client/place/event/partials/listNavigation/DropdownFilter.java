package com.sap.sailing.gwt.home.client.place.event.partials.listNavigation;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.LinkUtil;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.RegattaNavigationResources.LocalCss;
import com.sap.sailing.gwt.home.client.shared.DropdownHandler;

public class DropdownFilter<T> extends Composite {

    private static final LocalCss CSS = RegattaNavigationResources.INSTANCE.css();
    private static ListDropdownFilterUiBinder uiBinder = GWT.create(ListDropdownFilterUiBinder.class);

    interface ListDropdownFilterUiBinder extends UiBinder<Widget, DropdownFilter<?>> {
    }
    
    @UiField DivElement dropdownContainerUi;
    @UiField DivElement currentValueUi;
    @UiField FlowPanel filterItemContainerUi;
    private final DropdownFilterList<T> filterList;
    private final DropdownHandler dropdownHandler;

    public DropdownFilter(DropdownFilterList<T> filterList) {
        this.filterList = filterList;
        CSS.ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        this.dropdownHandler = new ListFilterDropdownHandler();
        
        this.addFilterValue(filterList.getDefaultValues()).select();
        for (T value : filterList.getSelectableValues()) {
            addFilterValue(value);
        }
    }
    
    private ListDropdownFilterItem addFilterValue(T value) {
        ListDropdownFilterItem filterItem = new ListDropdownFilterItem(value);
        filterItemContainerUi.add(filterItem);
        return filterItem;
    }

    public interface DropdownFilterList<T> {
        T getDefaultValues();
        Collection<T> getSelectableValues();
        void onSelectFilter(T value);
    }
    
    private class ListFilterDropdownHandler extends DropdownHandler {
        private ListFilterDropdownHandler() {
            super(dropdownContainerUi, filterItemContainerUi.getElement());
        }
        @Override
        protected void dropdownStateChanged(boolean dropdownShown) {
            if (dropdownShown) dropdownContainerUi.addClassName(CSS.jsdropdownactive());
            else dropdownContainerUi.removeClassName(CSS.jsdropdownactive());
        }
    }
    
    private class ListDropdownFilterItem extends Widget {
        private final T value;
        private ListDropdownFilterItem(T value) {
            this.value = value;
            DivElement element = DOM.createDiv().<DivElement>cast();
            element.setInnerText(String.valueOf(value));
            this.setElement(element);
            this.addStyleName(CSS.regattanavigation_filter_dropdown_link());
            this.initClickHandler();
        }
        
        private void initClickHandler() {
            this.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if(LinkUtil.handleLinkClick((Event) event.getNativeEvent())) {
                        event.preventDefault();
                        dropdownHandler.setVisible(false);
                        ListDropdownFilterItem.this.select();
                        filterList.onSelectFilter(value);
                    }
                }
            }, ClickEvent.getType());
        }
        
        private void select() {
            currentValueUi.setInnerText(String.valueOf(value));
            for (int i=0; i < filterItemContainerUi.getWidgetCount(); i++) {
                filterItemContainerUi.getWidget(i).removeStyleName(CSS.regattanavigation_filter_dropdown_linkactive());
            }
            this.addStyleName(CSS.regattanavigation_filter_dropdown_linkactive());
        }
    }
}
